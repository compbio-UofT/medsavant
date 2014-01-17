/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;

/**
 *
 * @author mfiume
 */
public abstract class BackgroundTaskWorker extends SwingWorker<Void,Void> implements TaskWorker {
    
    private static final int MAX_MEDSAVANT_WORKER_THREADS = 20;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(MAX_MEDSAVANT_WORKER_THREADS);
    
    List<String> taskLog;
    boolean preventsQuit;
    private Date startDate;
    private Date stopDate;
    private TaskStatus status;
    private final LaunchableApp owner;
    private String completionMessage;

    private final List<Listener<TaskWorker>> listeners;

    String getStatus() {
        return status.toString();
    }

    enum TaskStatus {
        UNSTARTED {

            @Override
            public String toString() {
                return "Not started";
            }
            
        },
        INPROGRESS {

            @Override
            public String toString() {
                return "Running";
            }
            
        },
        FINISHED {

            @Override
            public String toString() {
                return "Done";
            }
            
        },
        CANCELLED {

            @Override
            public String toString() {
                return "Cancelled";
            }
            
        },
        ERROR {

            @Override
            public String toString() {
                return "Error";
            }
            
        },
    }

    /**
     * Add a string log to this task. Use to indicate which step a task is on.
     *
     * @param s A string to log
     */
    public void addLog(String s) {
        taskLog.add((new Date().toString() + " - " + s));
        taskUpdated();
    }

    /**
     * Notify listeners of changes to this task
     */
    private void taskUpdated() {
        for (Listener<TaskWorker> listener : listeners) {
            listener.handleEvent(this);
        }
    }
    
    /**
     * Set the status for this task
     * @param taskStatus 
     */
    final void setStatus(TaskStatus taskStatus) {
        this.status = taskStatus;
    }
    
    public BackgroundTaskWorker(LaunchableApp owner) {
        taskLog = new ArrayList<String>();
        this.owner = owner;
        setStatus(TaskStatus.UNSTARTED);
        listeners = new ArrayList<Listener<TaskWorker>>();
        AppDirectory.getTaskManager().submitTask(this);
    }
    
    /**
     * Start the task. Do not use execute() or the status 
     * won't appear to have started in the task manager.
     */
    public void start() {
        this.startDate = new Date();
        setStatus(TaskStatus.INPROGRESS);
        super.execute();
    }
    
    /**
     * Cancel the thread
     */
    @Override
    public void cancel() {
        this.stopDate = new Date();
        setStatus(TaskStatus.CANCELLED);
        super.cancel(true);
    }
    
    /**
     * Add a listener for this worker
     * @param l 
     */
    @Override
    public void addListener(Listener<TaskWorker> l) {
        listeners.add(l);
    }
    
    protected abstract Void doInBackground() throws Exception;

    @Override
    public String getTaskName() {
        return this.owner.getName();
    }
    
    @Override
    public String getCurrentStatus() {
        if (taskLog.isEmpty()) { return ""; }
        return this.taskLog.get(taskLog.size()-1);
    }
    
    @Override
    public double getTaskProgress() {
        return 0.0;
    }
    
    @Override
    public LaunchableApp getOwner() {
        return this.owner;
    }
    
}
