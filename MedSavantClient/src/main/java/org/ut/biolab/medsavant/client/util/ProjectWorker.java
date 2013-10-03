/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.notification.VisibleMedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.ViewController;

/**
 * A worker for making changes to a project, which includes project
 * modification, removing variants, and importing variants.
 *
 * @author jim
 */
public abstract class ProjectWorker<T> extends VisibleMedSavantWorker<T> {

    private static final Log LOG = LogFactory.getLog(ProjectWorker.class);
    protected int notificationId;
    private boolean autoPublish;
    private String shortSuccessMessage = "Done.";
    private String longSuccessMessage;
    private String sessionID;
    private int projectID;


    public ProjectWorker(String pageName, String notificationTitle, boolean autoPublish, String sessionID, int projectID) {
        super(pageName, notificationTitle);      
        this.autoPublish = autoPublish;
        this.projectID = projectID;
        this.sessionID = sessionID;        
      
        setIndeterminate(true);
        showResultsOnFinish(false);
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
                longSuccessMessage = getTitle() + " was successful. MedSavant will now close.";
            } else {
                longSuccessMessage = getTitle() + " was successful. Changes have not yet been published.";
            }
        }
        return longSuccessMessage;
    }

    @Override
    protected void jobDone() {              
        setStatus(JobStatus.FINISHED);
        setStatusMessage(shortSuccessMessage);
        if (autoPublish) {
            ProjectController.getInstance().publishVariants(sessionID, projectID, getLongSuccessMessage());
        } else {
            MedSavantFrame.getInstance().notificationMessage(getLongSuccessMessage());
            ViewController.getInstance().getMenu().checkForUpdateNotifications();
        }
    }              
}
