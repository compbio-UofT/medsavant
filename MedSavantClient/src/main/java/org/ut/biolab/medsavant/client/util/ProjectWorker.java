/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

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
            DialogUtils.displayMessage(getLongSuccessMessage());
            MedSavantFrame.getInstance().forceRestart();
        } else {
            MedSavantFrame.getInstance().showNotficationMessage(getLongSuccessMessage());
        }
    }
}
