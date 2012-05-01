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
import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.model.Notification;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author Andrew
 */
public class NotificationQueryUtil extends MedSavantServerUnicastRemoteObject implements NotificationQueryUtilAdapter {

    private static NotificationQueryUtil instance;

    public static synchronized NotificationQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new NotificationQueryUtil();
        }
        return instance;
    }

    public NotificationQueryUtil() throws RemoteException {super();}

    @Override
    public List<Notification> getNotifications(String sid, String user) throws SQLException, RemoteException {
        List<Notification> result = new ArrayList<Notification>();

        List<ProjectDetails> unpublishedTables = ProjectQueryUtil.getInstance().getUnpublishedTables(sid);
        for(ProjectDetails table : unpublishedTables){
            result.add(new Notification(Notification.Type.PUBLISH, "The variant table for project " + table.getProjectName() + " and reference " + table.getReferenceName() + " has not yet been published. ", table));
        }

        return result;
    }
}
