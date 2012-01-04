package org.ut.biolab.medsavant.db.util.query.api;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.ut.biolab.medsavant.db.model.AnnotationLog.Action;
import org.ut.biolab.medsavant.db.model.AnnotationLog.Status;

/**
 *
 * @author mfiume
 */
public interface AnnotationLogQueryUtilAdapter extends Remote {

    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action, String user) throws SQLException, RemoteException;
    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action, Status status, String user) throws SQLException, RemoteException;
    public void removeAnnotationLogEntry(String sid,int updateId) throws SQLException, RemoteException;
    public ResultSet getPendingUpdates(String sid) throws SQLException, IOException, RemoteException;
    public void setAnnotationLogStatus(String sid,int updateId, Status status) throws SQLException, RemoteException;
    public void setAnnotationLogStatus(String sid,int updateId, Status status, Timestamp sqlDate) throws SQLException, RemoteException;
    public Status getUserPriorityStatus(String sid,String user) throws SQLException, RemoteException;

}
