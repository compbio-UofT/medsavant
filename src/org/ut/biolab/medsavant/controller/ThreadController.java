/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.controller;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class ThreadController {
    
    private List<Thread> runningThreads;
    
    private static ThreadController instance;
    
    public static ThreadController getInstance() {
        if (instance == null) { instance = new ThreadController(); }
        return instance;
    }
    
    public ThreadController() {
        runningThreads = new ArrayList<Thread>();
    }

    public List<Thread> getRunningThreads() {
        List<Thread> deadThreads = new ArrayList<Thread>();
        for (Thread t : runningThreads) {
            if (!t.isAlive() || t.isInterrupted()) {
                deadThreads.add(t);
            }
        }
        runningThreads.removeAll(deadThreads);
        return runningThreads;
    }

    public void runInThread(Runnable r) {
        Thread t = new Thread(r);
        runningThreads.add(t);
        t.start();
    }
}
