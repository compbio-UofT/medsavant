/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingWorker;

/**
 *
 * @author Andrew
 */
public class ThreadController {
 
    private static ThreadController instance;
    private Map<String, List<SwingWorker>> workers = new HashMap<String, List<SwingWorker>>();
       
    public static ThreadController getInstance(){
        if(instance == null){
            instance = new ThreadController();
        }
        return instance;
    }
    
    public synchronized void addWorker(String page, SwingWorker worker){
        List<SwingWorker> list = workers.get(page);
        if(list == null){
            workers.put(page, list = new ArrayList<SwingWorker>());
        }
        list.add(worker);
    }
    
    public synchronized void removeWorker(String page, SwingWorker worker){
        if(workers.get(page) == null) return;
        workers.get(page).remove(worker);
    }
    
    public synchronized void cancelWorkers(String page){
        System.out.println("cancel: " + page);
        if(workers.get(page) == null) return;
        for(SwingWorker worker : workers.get(page)){
            worker.cancel(true);
        }
        workers.put(page, null);
    }
}
