package com.example.view.gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import com.example.collections.dictionary.MyDictionary;
import com.example.collections.heap.MyIHeap;
import com.example.controller.Controller;
import com.example.repository.Repository;
import com.example.model.PrgState;
import com.example.controller.Programs;
import com.example.model.statements.IStmt;
import com.example.model.values.Value;
import com.example.model.exceptions.MyException;
import com.example.collections.list.MyIList;

import java.util.stream.Collectors;

public class MainProgramView {
    private Controller controller;
    private PrgState prgState;
    private MyIList<PrgState> prgList;
    private Programs programs = new Programs();

    private TableView<HeapEntry> heapTable;
    private ListView<String> outputList;
    private ListView<String> fileTableList;
    private ListView<String> prgStateList;
    private TableView<SymTableEntry> symTable;
    private ListView<String> exeStackList;
    private TextField prgStatesCount;
    private TableView<SemaphoreEntry> semaphoreTable; // Add Semaphore Table

    public MainProgramView() {
        createComponents();
    }

    public void showMainProgramWindow(String selectedProgram) {
        Stage mainStage = new Stage();
        BorderPane mainRoot = new BorderPane();

        IStmt prg = programs.getProgramFromString(selectedProgram);
        try {
            prg.typeCheck(new MyDictionary<>());
            prgState = new PrgState(prg);
            Repository repo = new Repository(programs.getProgramLogFile(selectedProgram));
            repo.addProgram(prgState);
            controller = new Controller(repo);
            controller.executor = java.util.concurrent.Executors.newFixedThreadPool(2);

            VBox leftPanel = new VBox(10);
            leftPanel.setPadding(new Insets(10));
            leftPanel.getChildren().addAll(new Label("Execution Stack"), exeStackList,
                    new Label("Symbol Table"), symTable);

            VBox centerPanel = new VBox(10);
            centerPanel.setPadding(new Insets(10));
            centerPanel.getChildren().addAll(new Label("Heap Table"), heapTable,
                    new Label("Output"), outputList,
                    new Label("Program States"), prgStateList);

            VBox rightPanel = new VBox(10);
            rightPanel.setPadding(new Insets(10));
            rightPanel.getChildren().addAll(new Label("File Table"), fileTableList,
                    new Label("Semaphore Table"), semaphoreTable);

            HBox bottomPanel = new HBox();
            bottomPanel.setPadding(new Insets(10));
            bottomPanel.getChildren().addAll(new Label("Program States: "), prgStatesCount,
                    new Button("Run One Step"), new Button("Run All Steps"));

            ((Button) bottomPanel.getChildren().get(2)).setOnAction(e -> runOneStep());
            ((Button) bottomPanel.getChildren().get(3)).setOnAction(e -> {
                while (controller.getRepo().getProgramList().size() > 0) {
                    runOneStep();
                }
            });

            mainRoot.setLeft(leftPanel);
            mainRoot.setCenter(centerPanel);
            mainRoot.setRight(rightPanel);
            mainRoot.setBottom(bottomPanel);

            Scene mainScene = new Scene(mainRoot, 1200, 800);
            mainStage.setTitle("Program Execution");
            mainStage.setScene(mainScene);
            mainStage.show();

            mainStage.setOnCloseRequest(e -> {
                if (controller != null && controller.executor != null) {
                    controller.executor.shutdownNow();
                }
                System.exit(0);
            });

            // Only run first step if initialization was successful
            updateUIComponents(); // Show initial state
        } catch (MyException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Type Check Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            mainStage.close();
            // Return to program selection and close current window
            ProgramSelectionView selectionView = new ProgramSelectionView();
            selectionView.setPrograms(programs.getProgramStrings());
            selectionView.setOnProgramSelect(this::showMainProgramWindow);
            selectionView.show();
            return;
        }

        mainStage.show();
    }

    @SuppressWarnings("unchecked")
    private void createComponents() {
        // Initialize all UI components
        prgStatesCount = new TextField();
        prgStatesCount.setEditable(false);

        heapTable = new TableView<>();
        TableColumn<HeapEntry, Integer> addressCol = new TableColumn<>("Address");
        TableColumn<HeapEntry, String> valueCol = new TableColumn<>("Value");
        addressCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAddress()));
        valueCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
        heapTable.getColumns().addAll(addressCol, valueCol);

        outputList = new ListView<>();
        fileTableList = new ListView<>();
        prgStateList = new ListView<>();

        symTable = new TableView<>();
        TableColumn<SymTableEntry, String> varNameCol = new TableColumn<>("Variable");
        TableColumn<SymTableEntry, String> varValueCol = new TableColumn<>("Value");
        varNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVarName()));
        varValueCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
        symTable.getColumns().addAll(varNameCol, varValueCol);

        exeStackList = new ListView<>();

        // Create Semaphore Table
        semaphoreTable = new TableView<>();
        TableColumn<SemaphoreEntry, Integer> semaphoreAddressCol = new TableColumn<>("Location");
        TableColumn<SemaphoreEntry, Integer> semaphoreValueCol = new TableColumn<>("Value");
        TableColumn<SemaphoreEntry, String> semaphoreListCol = new TableColumn<>("List");
        semaphoreAddressCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getLocation()));
        semaphoreValueCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getValue()));
        semaphoreListCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getList()));
        semaphoreTable.getColumns().addAll(semaphoreAddressCol, semaphoreValueCol, semaphoreListCol);

        // Add listeners for PrgState selection
        prgStateList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                MyIList<PrgState> programStates = controller.getRepo().getProgramList();
                programStates.stream()
                    .filter(p -> String.valueOf(p.getId()).equals(newVal))
                    .findFirst()
                    .ifPresent(prg -> {
                        // Update SymTable for selected program
                        symTable.getItems().setAll(prg.getSymTable().entrySet().stream()
                            .map(entry -> new SymTableEntry(entry.getKey(), entry.getValue().toString()))
                            .collect(Collectors.toList()));

                        // Update ExeStack for selected program
                        exeStackList.getItems().setAll(
                            prg.getExeStack().stream()
                                .map(IStmt::toString)
                                .collect(Collectors.toList())
                        );
                    });
            }
        });
    }

    private void runOneStep() {
        if (controller.getRepo().getProgramList().size() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Program completed");
            alert.setContentText("Program execution finished");
            alert.showAndWait();
            controller.executor.shutdownNow();
            // Return to program selection and close current window
            ProgramSelectionView selectionView = new ProgramSelectionView();
            selectionView.setPrograms(programs.getProgramStrings());
            selectionView.setOnProgramSelect(this::showMainProgramWindow);
            selectionView.show();
            return;
        }

        try {
            prgList = controller.removeCompletedPrg(controller.getRepo().getProgramList());
            synchronized (controller.getRepo()) {
                // Garbage collector
                MyIHeap<Integer, Value> heap = controller.getRepo().getProgramList().get(0).getHeap();
                heap = controller.conservativeGarbageCollector(controller.getRepo().getProgramList(), heap);
                for (PrgState prg: controller.getRepo().getProgramList()) { prg.setHeap(heap); }
            }
            controller.oneStepForAllPrg(prgList);
            prgList = controller.removeCompletedPrg(controller.getRepo().getProgramList());

            updateUIComponents();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void updateUIComponents() {
        // Update number of program states
        prgStatesCount.setText(String.valueOf(controller.getRepo().getProgramList().size()));

        // Get current program state
        MyIList<PrgState> programStates = controller.getRepo().getProgramList();
        if (programStates.size() == 0) return;
        PrgState currentPrg = programStates.get(0);

        // Update HeapTable
        heapTable.getItems().setAll(currentPrg.getHeap().entrySet().stream()
            .map(entry -> new HeapEntry(entry.getKey(), entry.getValue().toString()))
            .collect(Collectors.toList()));

        // Update Output
        outputList.getItems().setAll(currentPrg.getOut().stream()
            .map(Value::toString)
            .collect(Collectors.toList()));

        // Update FileTable
        fileTableList.getItems().setAll(currentPrg.getFileTable().entrySet().stream()
            .map(entry -> entry.getKey().toString())
            .collect(Collectors.toList()));

        // Store current selection
        String selectedPrgState = prgStateList.getSelectionModel().getSelectedItem();

        // Update PrgState IDs list
        prgStateList.getItems().setAll(programStates.stream()
            .map(prg -> String.valueOf(prg.getId()))
            .collect(Collectors.toList()));

        // Restore selection if it still exists
        if (selectedPrgState != null && prgStateList.getItems().contains(selectedPrgState)) {
            prgStateList.getSelectionModel().select(selectedPrgState);
        } else {
            // If selected program no longer exists, select the id from the top of the prtList
            PrgState topPrg = programStates.get(0);
            prgStateList.getSelectionModel().select(String.valueOf(topPrg.getId()));
        }

        // Update SymTable
        symTable.getItems().setAll(currentPrg.getSymTable().entrySet().stream()
            .map(entry -> new SymTableEntry(entry.getKey(), entry.getValue().toString()))
            .collect(Collectors.toList()));

        // Update ExeStack
        exeStackList.getItems().setAll(
            currentPrg.getExeStack().stream()
                .map(IStmt::toString)
                .collect(Collectors.toList())
        );

        // Update SemaphoreTable
        semaphoreTable.getItems().setAll(currentPrg.getSemaphoreTable().getSemaphoreDictionaryAsList().stream()
            .map(entry -> new SemaphoreEntry(
                entry.getKey().getKey(),
                entry.getKey().getValue(),
                entry.getValue().toString()
            ))
            .collect(Collectors.toList()));
    }
}

// Helper classes for TableViews
class HeapEntry {
    private Integer address;
    private String value;

    public HeapEntry(Integer address, String value) {
        this.address = address;
        this.value = value;
    }

    public Integer getAddress() { return address; }
    public String getValue() { return value; }
}

class SymTableEntry {
    private String varName;
    private String value;

    public SymTableEntry(String varName, String value) {
        this.varName = varName;
        this.value = value;
    }

    public String getVarName() { return varName; }
    public String getValue() { return value; }
}

class SemaphoreEntry {
    private Integer location;
    private Integer value;
    private String list;

    public SemaphoreEntry(Integer location, Integer value, String list) {
        this.location = location;
        this.value = value;
        this.list = list;
    }

    public Integer getLocation() { return location; }
    public Integer getValue() { return value; }
    public String getList() { return list; }
}
