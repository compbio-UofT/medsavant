package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author mfiume
 */
public interface ServerLogQueryUtilAdapter extends Remote {

    public static enum LogType { INFO, ERROR, LOGIN, LOGOUT };

    public void addServerLog(String sid, LogType t, String description);
    public void addLog(String sid, String uname, LogType t, String description);
    public Date getDateOfLastServerLog(String sid) throws SQLException;
}
