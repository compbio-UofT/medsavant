/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.serverapi;

import java.rmi.RemoteException;
import java.sql.SQLException;

import org.ut.biolab.medsavant.model.Notification;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author Andrew
 */
public class NotificationManager extends MedSavantServerUnicastRemoteObject implements NotificationManagerAdapter {

    private static NotificationManager instance;

    private NotificationManager() throws RemoteException {
    }

    public static synchronized NotificationManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Get all available notifications for this user.  Currently the only one we have is the unpublished-variants notification, which
     * applies across all users.
     */
    @Override
    public Notification[] getNotifications(String sessID, String user) throws SQLException, RemoteException {

        ProjectDetails[] unpublished = ProjectManager.getInstance().getUnpublishedChanges(sessID);
        Notification[] result = new Notification[unpublished.length];
        for (int i = 0; i < unpublished.length; i++) {
            ProjectDetails pd = unpublished[i];
            result[i] = new Notification(Notification.Type.PUBLISH, "The variant table for project " + pd.getProjectName() + " and reference " + pd.getReferenceName() + " has not yet been published. ", pd);
        }

        return result;
    }
}
