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
package org.ut.biolab.medsavant.server.serverapi;

import java.rmi.RemoteException;
import java.sql.SQLException;

import org.ut.biolab.medsavant.shared.model.Notification;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.NotificationManagerAdapter;

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
    public Notification[] getNotifications(String sessID, String user) throws SQLException, RemoteException, SessionExpiredException {

        ProjectDetails[] unpublished = ProjectManager.getInstance().getUnpublishedChanges(sessID);
        Notification[] result = new Notification[unpublished.length];
        for (int i = 0; i < unpublished.length; i++) {
            ProjectDetails pd = unpublished[i];
            result[i] = new Notification(Notification.Type.PUBLISH, "The variant table for project " + pd.getProjectName() + " and reference " + pd.getReferenceName() + " has not yet been published. ", pd);
        }

        return result;
    }
}
