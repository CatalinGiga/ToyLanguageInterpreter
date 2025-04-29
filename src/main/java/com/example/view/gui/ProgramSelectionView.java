package com.example.view.gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.util.List;
import java.util.function.Consumer;

public class ProgramSelectionView {
    private Stage stage;
    private ListView<String> programList;
    private Button selectButton;

    public ProgramSelectionView() {
        stage = new Stage();
        initializeComponents();
    }

    private void initializeComponents() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Select a program to execute:");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        programList = new ListView<>();
        programList.setPrefHeight(300);

        selectButton = new Button("Select Program");
        selectButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white;");

        VBox centerBox = new VBox(15, titleLabel, programList);
        centerBox.setPadding(new Insets(10));
        root.setCenter(centerBox);

        HBox bottomBox = new HBox(selectButton);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-alignment: center;");
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 800, 400);
        stage.setTitle("Program Selection");
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }

    public void setPrograms(List<String> programs) {
        programList.getItems().addAll(programs);
    }

    public void setOnProgramSelect(Consumer<String> handler) {
        selectButton.setOnAction(e -> {
            String selected = programList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                stage.close();
                handler.accept(selected);
            }
        });
    }
}