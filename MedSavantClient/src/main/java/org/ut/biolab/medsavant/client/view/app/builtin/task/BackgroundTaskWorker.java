/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.app.builtin.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.notify.NotificationsPanel;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.notify.Notification;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.GeneralLog;

/**
 *
 * @author mfiume
 */
public abstract class BackgroundTaskWorker<T> implements TaskWorker {

    private static final int MAX_MEDSAVANT_WORKER_THREADS = 20;

    List<GeneralLog> taskLog;
    boolean preventsQuit;
    private Date startDate;
    private Date stopDate;
    private double taskProgress = -1; // indefinite when -1
    private TaskStatus status;
    private final LaunchableApp owner;
    private String completionMessage;

    private final List<Listener<TaskWorker>> listeners;
    private SwingWorker<T, Object> worker;
    private String taskName;

    public String getStatus() {
        return status.toString();
    }

    /**
     * Add a string log to this task. Use to indicate which step a task is on.
     *
     * @param s A string to log
     */
    public void addLog(String s) {
        taskLog.add(new GeneralLog(s));
        taskUpdated();
    }
    

    /**
     * Get the log
     *
     * @return A list of string logs
     */
    @Override
    public List<GeneralLog> getLog() {
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
        taskLog = new ArrayList<GeneralLog>();
        this.owner = owner;
        this.taskName = taskName;
        listeners = new ArrayList<Listener<TaskWorker>>();

        setStatus(TaskStatus.UNSTARTED);
        AppDirectory.getTaskManager().submitTask(this);
    }

    public BackgroundTaskWorker(String taskName) {
        this(null, taskName);
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
     * Display exceptions that occur while running the task. Should be
     * overridden.
     *
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
        return taskName; //+ ((owner == null) ? "" : " - " + owner.getName());
    }

    @Override
    public TaskStatus getCurrentStatus() {
        return status;
    }

    @Override
    public double getTaskProgress() {
        return taskProgress;
    }

    /**
     * The progress for this task. -1 if indefinite, between 0 and 1 for percent
     * completion.
     *
     * @param taskProgress
     */
    public void setTaskProgress(double taskProgress) {
        this.taskProgress = taskProgress;
        taskUpdated();
    }

    @Override
    public LaunchableApp getOwner() {
        return this.owner;
    }

    public GeneralLog getLastLog() {
        if (this.taskLog.isEmpty()) {
            return null;
        }
        return this.getLog().get(this.getLog().size() - 1);
    }

    public Notification getNotificationForWorker() {
        
        final BackgroundTaskWorker instance = this;
        final Notification n = new Notification();
        n.setName(instance.getTaskName());
        n.setIcon(instance.getOwner().getIcon());
        instance.addListener(new Listener<TaskWorker>() {

            @Override
            public void handleEvent(TaskWorker event) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        n.setDescription(instance.getLastLog().getDescription());
                        n.setProgress(instance.getTaskProgress());
                    }

                });
            }

        });
        return n;
    }

}
