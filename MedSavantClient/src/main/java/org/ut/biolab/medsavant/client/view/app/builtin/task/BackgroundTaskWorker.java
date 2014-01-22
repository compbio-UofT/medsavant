/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app.builtin.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public abstract class BackgroundTaskWorker<T> implements TaskWorker {

    private static final int MAX_MEDSAVANT_WORKER_THREADS = 20;

    List<String> taskLog;
    boolean preventsQuit;
    private Date startDate;
    private Date stopDate;
    private TaskStatus status;
    private final LaunchableApp owner;
    private String completionMessage;

    private final List<Listener<TaskWorker>> listeners;
    private SwingWorker<T, Object> worker;
    private String taskName;

    String getStatus() {
        return status.toString();
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
     * Update the last log. Usefule when presenting progress.
     *
     * @param s A string to log, overwriting the previous
     *
    public void addLogInline(String s) {
        if (taskLog.isEmpty()) {
            addLog(s);
            return;
        }
        taskLog.remove(taskLog.size() - 1);
        taskLog.add((new Date().toString() + " - " + s));
        taskUpdated();
    }*/

    /**
     * Get the log
     *
     * @return A list of string logs
     */
    @Override
    public List<String> getLog() {
        return taskLog;
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
     *
     * @param taskStatus
     */
    public final void setStatus(TaskStatus taskStatus) {
        this.status = taskStatus;
        taskUpdated();
    }
    
    public BackgroundTaskWorker(LaunchableApp owner, String taskName) {
        taskLog = new ArrayList<String>();
        this.owner = owner;
        this.taskName = taskName;
        listeners = new ArrayList<Listener<TaskWorker>>();

        setStatus(TaskStatus.UNSTARTED);
        AppDirectory.getTaskManager().submitTask(this);
    }
    

    public BackgroundTaskWorker(String taskName) {
        this(null,taskName);
    }

    /**
     * Start the task. Do not use execute() or the status won't appear to have
     * started in the task manager.
     */
    public void start() {
        this.startDate = new Date();
        setStatus(TaskStatus.INPROGRESS); // calls taskUpdated, so no need to call it separately
        final BackgroundTaskWorker instance = this;
        worker = new SwingWorker<T, Object>() {
            @Override
            public void done() {
                if (status == TaskStatus.INPROGRESS) {
                    setStatus(TaskStatus.FINISHED);
                }
                instance.done();
            }

            @Override
            protected T doInBackground() throws Exception {
                return (T) instance.doInBackground();
            }
        };
        worker.execute();
    }

    /**
     * Cancel the thread
     */
    @Override
    public void cancel() {
        this.stopDate = new Date();
        setStatus(TaskStatus.CANCELLED); // calls taskUpdated, so no need to call it separately
        worker.cancel(true);
    }

    /**
     * The worker either finished or was cancelled
     */
    public void done() {
        try {
            if (!worker.isCancelled()) {
                showSuccess(worker.get());
            } else {
                throw new InterruptedException("Task interrupted");
            }
        } catch (Exception e) {
            showFailure(e);
        }
    }

    /**
     * Add a listener for this worker
     *
     * @param l
     */
    @Override
    public void addListener(Listener<TaskWorker> l) {
        listeners.add(l);
    }

    protected abstract T doInBackground() throws Exception;

    protected abstract void showSuccess(T result);

    /**
     * Display exceptions that occur while running the task. Should
     * be overridden.
     * @param e The exception thrown while running the task.
     */
    protected void showFailure(final Exception e) {
        final TaskWorker t = this;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                DialogUtils.displayException("Task Manager", "Error running task " + t.getTaskName(), e);
            }

        });
    }

    @Override
    public String getTaskName() {
        return taskName + ((owner == null) ? "" : "(" + owner.getName() + ")");
    }

    @Override
    public TaskStatus getCurrentStatus() {
        return status;
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
