/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.Notification;
import org.ut.biolab.medsavant.client.view.NotificationsPanel;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 * A worker for making changes to a project, which includes project
 * modification, removing variants, and importing variants.
 *
 * @author jim
 */
public abstract class ProjectWorker<T> extends MedSavantWorker<T> {

    private static final Log LOG = LogFactory.getLog(ProjectWorker.class);
    protected int notificationId;
    protected Notification notification;
    private boolean autoPublish;
    private String shortSuccessMessage = "Done.";
    private String shortCancelMessage = "CANCELLED";
    private String shortFailureMessage = "FAILED";
    private String longSuccessMessage;
    private String sessionID;
    private int projectID;

    public ProjectWorker<T> setLongSuccessMessage(String msg) {
        this.longSuccessMessage = msg;
        return this;
    }

    public ProjectWorker<T> setShortSuccessMessage(String msg) {
        this.shortSuccessMessage = msg;
        return this;
    }

    public ProjectWorker<T> setShortCancelMessage(String msg) {
        this.shortCancelMessage = msg;
        return this;
    }

    public ProjectWorker<T> setShortFailureMessage(String msg) {
        this.shortFailureMessage = msg;
        return this;
    }

    public ProjectWorker<T> setMessages(String successMsg, String cancelMsg, String failureMsg, String longSuccessMessage) {
        setShortSuccessMessage(successMsg);
        setShortCancelMessage(cancelMsg);
        setShortFailureMessage(failureMsg);
        setLongSuccessMessage(longSuccessMessage);
        return this;
    }

    public ProjectWorker<T> setIndeterminate(boolean b) {
        if (progressTimer != null) {
            progressTimer.stop();
            progressTimer = null;
        }
        if (this.getState() == StateValue.STARTED) {
            startProgressTimer();
        }
        notification.setIndeterminate(b);
        return this;
    }

    public ProjectWorker(String pageName, String notificationTitle, boolean autoPublish, String sessionID, int projectID) {
        super(pageName);
        this.autoPublish = autoPublish;
        this.projectID = projectID;
        this.sessionID = sessionID;
        notification = new Notification(notificationTitle) {
            @Override
            public void cancelJob() {
                cancel(true);
                showFailure(new InterruptedException());
            }

            @Override
            protected void showResults() {
            }

            @Override
            protected void closeJob() {
                NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).removeNotification(notificationId);
            }
        };

        notification.setIndeterminate(true);
        notification.showResultsOnFinish(false);
        notificationId = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).addNotification(notification.getView());
        notification.setStatus(Notification.JobStatus.NOT_STARTED);
    }

    public ProjectWorker(String name, boolean autoPublish, String sessionID, int projectID) {
        this(name, name, autoPublish, sessionID, projectID);
    }

    public boolean isAutoPublish() {
        return autoPublish;
    }

    private String getLongSuccessMessage() {
        if (longSuccessMessage == null) {
            if (autoPublish) {
                longSuccessMessage = notification.getTitle() + " was successful.  MedSavant must restart to publish changes.  Press OK to restart";
            } else {
                longSuccessMessage = notification.getTitle() + " was successful. Changes have not yet been published.";
            }
        }
        return longSuccessMessage;
    }

    private void publish() {
        try {
            //The switch to loginView prevents certain polling threads from trying to 
            //query the server. 
            MedSavantFrame.getInstance().switchToLoginView();
            DialogUtils.displayMessage(getLongSuccessMessage());
            MedSavantClient.VariantManager.publishVariants(sessionID, projectID);
            MedSavantClient.restart(null);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    @Override
    protected void showSuccess(T result) {
        notification.setStatus(Notification.JobStatus.FINISHED);
        notification.setStatusMessage(shortSuccessMessage);
        if (autoPublish) {
            publish();
        } else {
            MedSavantFrame.getInstance().notificationMessage(getLongSuccessMessage());
            ViewController.getInstance().getMenu().checkForUpdateNotifications();
        }
    }

    @Override
    protected void showFailure(Throwable ex) {
        notification.setStatus(Notification.JobStatus.CANCELLED);
        if (ex instanceof InterruptedException) {
            notification.setStatusMessage(shortCancelMessage);
        } else {
            LOG.error(shortFailureMessage, ex);
            notification.setStatusMessage(shortFailureMessage);
        }
    }

    @Override
    protected final T doInBackground() throws Exception {
        notification.setStatus(Notification.JobStatus.RUNNING);
        if (!notification.isIndeterminate()) {
            startProgressTimer();
        }
        return backgroundTask();
    }

    protected abstract T backgroundTask() throws Exception;
}
