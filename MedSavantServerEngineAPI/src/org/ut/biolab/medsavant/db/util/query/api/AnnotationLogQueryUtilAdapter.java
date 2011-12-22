package org.ut.biolab.medsavant.db.util.query.api;

import java.io.IOException;
import java.rmi.Remote;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 *
 * @author mfiume
 */
public interface AnnotationLogQueryUtilAdapter extends Remote {

    public static enum Action {ADD_VARIANTS, UPDATE_TABLE};
    public static enum Status {PREPROCESS, PENDING, INPROGRESS, ERROR, COMPLETE};

    
    public Action intToAction(int action);
    public Status intToStatus(int status);
    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action, String user) throws SQLException;
    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action, Status status, String user) throws SQLException;
    public void removeAnnotationLogEntry(String sid,int updateId) throws SQLException;
    public ResultSet getPendingUpdates(String sid) throws SQLException, IOException;
    public void setAnnotationLogStatus(String sid,int updateId, Status status) throws SQLException;
    public void setAnnotationLogStatus(String sid,int updateId, Status status, Timestamp sqlDate) throws SQLException;
    public Status getUserPriorityStatus(String sid,String user) throws SQLException;

}
