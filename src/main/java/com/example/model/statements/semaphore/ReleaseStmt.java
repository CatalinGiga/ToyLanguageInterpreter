package com.example.model.statements.semaphore;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.List;

import javafx.util.Pair;

import com.example.collections.dictionary.MyIDictionary;
import com.example.model.exceptions.MyException;
import com.example.model.statements.IStmt;
import com.example.model.PrgState;
import com.example.model.types.IntType;
import com.example.model.types.Type;
import com.example.model.values.IntValue;


public class ReleaseStmt implements IStmt {
    // ReleaseStmt(var) -> var is the name of the variable
    private String var;
    // lock used to synchronize the access to the semaphore table
    private static final Lock lock = new ReentrantLock();

    public ReleaseStmt(String var) {
        // Constructor for the ReleaseStmt class
        this.var = var;
    }

    @Override
    public MyIDictionary<String, Type> typeCheck(MyIDictionary<String, Type> typeEnv) throws MyException {
        // check if the variable is defined and is an integer
        if (!typeEnv.containsKey(var)) { throw new MyException("Variable is not defined"); }

        var typ = typeEnv.get(var);
        if (!typ.equals(new IntType())) { throw new MyException("Variable is not an integer"); }

        return typeEnv;
    }

    @Override
    public PrgState execute(PrgState prg) throws MyException {
        // Lock the semaphore table
        lock.lock();
        try {
            // Get the symbol table and semaphore table
            var symTbl = prg.getSymTable();
            var semTbl = prg.getSemaphoreTable();

            // Check if the variable is defined and is an integer
            if (!symTbl.containsKey(var)) { throw new MyException("Variable is not defined"); }
            if (!symTbl.get(var).getType().equals(new IntType())) { throw new MyException("Variable is not an integer"); }

            // Get the integer value of the variable
            IntValue idx = (IntValue) symTbl.get(var);
            int index = idx.getValue();

            // Check if the semaphore is defined
            if (!semTbl.containsKey(index)) { throw new MyException("Semaphore is not defined"); }

            // Get the semaphore value
            Pair<Integer, List<Integer>> sem = semTbl.get(index);

            // Remove the program id from the semaphore value
            if (sem.getValue().contains(prg.getId())) {
                sem.getValue().remove((Integer) prg.getId());
            }

            // Update the semaphore table
            semTbl.update(index, new Pair<>(sem.getKey(), sem.getValue()));
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public IStmt deepCopy() {
        return new ReleaseStmt(var);
    }

    @Override
    public String toString() {
        return "release(" + var + ")";
    }

}
