/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.db.model.Notification;

/**
 *
 * @author Andrew
 */
public interface NotificationQueryUtilAdapter extends Remote {
    
    public List<Notification> getNotifications(String sid, String user) throws SQLException, RemoteException;
    
}
