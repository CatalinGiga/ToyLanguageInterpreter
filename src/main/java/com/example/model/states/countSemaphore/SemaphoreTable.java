package com.example.model.states.countSemaphore;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.example.model.exceptions.MyException;

public class SemaphoreTable implements ISemaphoreTable {
private HashMap<Integer, Pair<Integer, List<Integer>>> semaphoreTable;
    // lock for synchronization purposes
    ReentrantLock lock;
    // freeLocation is used to keep track of the next free location in the semaphoreTable
    private int freeLocation = 0;

    public SemaphoreTable() { // constructor for SemaphoreTable
        // initialize semaphoreTable as a new HashMap
        this.semaphoreTable = new HashMap<>();
        // initialize lock as a new ReentrantLock
        this.lock = new ReentrantLock();
    }

    @Override
    public void put(int key, Pair<Integer, List<Integer>> value) throws MyException {
        // synchronize on the current object (SemaphoreTable)
        synchronized (this) {
            // if semaphoreTable doesn't contain the key
            if (!semaphoreTable.containsKey(key)) {
                // put the key and value in the semaphoreTable
                semaphoreTable.put(key, value);
            } else {
                throw new MyException("Semaphore table already contains the key!");
            }
        }
    }

    @Override
    public Pair<Integer, List<Integer>> get(int key) throws MyException {
        // synchronize on the current object (SemaphoreTable)
        synchronized (this) {
            // if semaphoreTable contains the key
            if (semaphoreTable.containsKey(key)) {
                // return the value associated with the key
                return semaphoreTable.get(key);
            } else {
                throw new MyException(String.format("Semaphore table doesn't contain the key %d!", key));
            }
        }
    }

    @Override
    public boolean containsKey(int key) {
        synchronized (this) {
            // return whether semaphoreTable contains the key
            return semaphoreTable.containsKey(key);
        }
    }

    @Override
    public int getFreeAddress() {
        synchronized (this) {
            freeLocation++;
            return freeLocation;
        }
    }

    @Override
    public void setFreeAddress(int freeAddress) {
        synchronized (this) {
            this.freeLocation = freeAddress;
        }
    }

    @Override
    public void update(int key, Pair<Integer, List<Integer>> value) throws MyException {
        synchronized (this) {
            if (semaphoreTable.containsKey(key)) {
                semaphoreTable.replace(key, value);
            } else {
                throw new MyException("Semaphore table doesn't contain key");
            }
        }
    }

    @Override
    public HashMap<Integer, Pair<Integer, List<Integer>>> getSemaphoreTable() {
        synchronized (this) {
            return semaphoreTable;
        }
    }

    @Override
    public void setSemaphoreTable(HashMap<Integer, Pair<Integer, List<Integer>>> newSemaphoreTable) {
        synchronized (this) {
            this.semaphoreTable = newSemaphoreTable;
        }
    }

    @Override
    public List<Pair<Pair<Integer, Integer>, List<Integer>>> getSemaphoreDictionaryAsList() {
        this.lock.lock();
        List<Pair<Pair<Integer, Integer>, List<Integer> > > answer = new ArrayList<>();
        this.semaphoreTable.forEach((x, y) -> {
            answer.add(new Pair<>(new Pair<>(x, y.getKey()), y.getValue()));
        });
        this.lock.unlock();
        return answer;
    }

    @Override
    public String toString() {
        return semaphoreTable.toString();
    }
}
