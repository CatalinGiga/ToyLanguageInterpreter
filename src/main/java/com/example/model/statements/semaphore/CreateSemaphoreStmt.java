package com.example.model.statements.semaphore;

import com.example.collections.dictionary.MyIDictionary;
import com.example.model.exceptions.MyException;
import com.example.model.types.IntType;
import com.example.model.types.Type;
import com.example.model.statements.IStmt;
import com.example.model.PrgState;
import com.example.model.exceptions.StmtException;
import com.example.model.expressions.Exp;
import com.example.model.values.IntValue;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class CreateSemaphoreStmt implements IStmt {
    // CreateSemaphoreStmt(var, exp) -> var is the name of the variable and exp is the expression that will be evaluated
    private final String var;
    private final Exp exp;
    // lock used to synchronize the access to the semaphore table
    private static final Lock lock = new ReentrantLock();

    public CreateSemaphoreStmt(String var, Exp exp) {
        // Constructor for the CreateSemaphoreStmt class
        this.var = var;
        this.exp = exp;
    }
    
    @Override
    public MyIDictionary<String, Type> typeCheck(MyIDictionary<String, Type> typeEnv) throws MyException {
        if (!typeEnv.containsKey(var)) { throw new MyException("Variable " + var + " is not defined"); }

        // check if the variable is an integer
        Type varType = typeEnv.get(var);
        Type expType = exp.typeCheck(typeEnv);

        if (!varType.equals(new IntType())) { throw new MyException("Variable " + var + " is not an integer"); }
        if (!expType.equals(new IntType())) { throw new MyException("Expression is not an integer"); }

        return typeEnv;
    }

    @Override
    public PrgState execute(PrgState prg) throws MyException, StmtException {
        // Lock the semaphore table
        lock.lock();

        // Get the symbol table, heap and semaphore table
        var symTbl = prg.getSymTable();
        var heap = prg.getHeap();
        var semTbl = prg.getSemaphoreTable();

        // Evaluate the expression
        IntValue val = (IntValue) exp.eval(symTbl, heap);
        // Get the integer value of the expression
        int number = val.getValue();

        // Get the next free address in the semaphore table
        int freeAddress = semTbl.getFreeAddress();

        // Put the free address in the semaphore table
        semTbl.put(freeAddress, new Pair<>(number, new ArrayList<>()));

        if (!symTbl.containsKey(var)) { throw new StmtException("Variable " + var + " is not defined"); }
        if (!symTbl.get(var).getType().equals(new IntType())) { throw new StmtException("Variable " + var + " is not an integer"); }

        // Put the free address in the symbol table
        symTbl.put(var, new IntValue(freeAddress));

        // Unlock the semaphore table
        lock.unlock();
        return null;
    }

    @Override
    public IStmt deepCopy() {
        return new CreateSemaphoreStmt(this.var, this.exp); 
    }

    @Override
    public String toString() {
        return "CreateSemaphoreStmt()";
    }

}

