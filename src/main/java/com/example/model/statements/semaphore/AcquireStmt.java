package com.example.model.statements.semaphore;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.List;


import com.example.model.statements.IStmt;
import com.example.model.PrgState;
import com.example.model.exceptions.MyException;
import com.example.model.exceptions.StmtException;
import com.example.collections.dictionary.MyIDictionary;
import com.example.model.types.Type;
import com.example.model.values.IntValue;
import com.example.model.types.IntType;

import javafx.util.Pair;


public class AcquireStmt implements IStmt {
    // AcquireStmt(var) -> var is the name of the variable
    private String var;
    // lock used to synchronize the access to the semaphore table
    private static final Lock lock = new ReentrantLock();

    public AcquireStmt(String var) {
        // Constructor for the AcquireStmt class
        this.var = var;
    }

    @Override
    public MyIDictionary<String, Type> typeCheck(MyIDictionary<String, Type> typeEnv) throws MyException {
        // check if the variable is defined
        if (!typeEnv.containsKey(var)) { throw new MyException("Variable " + var + " is not defined"); }

        // check if the variable is an integer
        Type varType = typeEnv.get(var);
        if (!varType.equals(new IntType())) { throw new MyException("Variable " + var + " is not an integer"); }

        // return the type environment
        return typeEnv;
    }

    @Override
    public PrgState execute(PrgState prg) throws MyException, StmtException {
        // Lock the semaphore table
        lock.lock();

        try {
            // Get the symbol table and semaphore table
            var symTbl = prg.getSymTable();
            var semTbl = prg.getSemaphoreTable();

            // Check if the variable is defined and is an integer
            if (!symTbl.containsKey(var)) { throw new StmtException("Variable " + var + " is not defined"); }
            if (!symTbl.get(var).getType().equals(new IntType())) { throw new StmtException("Variable " + var + " is not an integer"); }

            // Get the integer value of the variable
            IntValue idx = (IntValue) symTbl.get(var);
            int index = idx.getValue();

            // Check if the semaphore is defined
            if (!semTbl.containsKey(index)) { throw new StmtException("Semaphore " + index + " is not defined"); }

            // Get the semaphore value
            Pair<Integer, List<Integer>> sem = semTbl.get(index);
            // Get the number of available permits and the list of processes that are waiting
            int nl = sem.getValue().size();
            int n1 = sem.getKey();

            // If there are available permits, decrement the number of available permits
            // and add the current process to the list of processes that are waiting else push the current statement on the execution stack
            if (n1 > nl) {
                if (!sem.getValue().contains(prg.getId())) {
                    sem.getValue().add(prg.getId());
                    semTbl.update(index, new Pair<>(n1, sem.getValue()));
                }
            } else {
                prg.getExeStack().push(this);
            }

            return null;
        } finally {
            // Unlock the semaphore table
            lock.unlock();
        }
    }

    @Override
    public IStmt deepCopy() {
        return new AcquireStmt(var);
    }

    @Override
    public String toString() {
        return "acquire(" + var + ")";
    }

}