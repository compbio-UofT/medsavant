package org.ut.biolab.medsavant.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Date;

/**
 *
 * @author mfiume
 */
public interface ServerLogQueryUtilAdapter extends Remote {

    public static enum LogType { INFO, ERROR, LOGIN, LOGOUT };

    public void addServerLog(String sid, LogType t, String description) throws RemoteException;
    public void addLog(String sid, String uname, LogType t, String description) throws RemoteException;
    public Date getDateOfLastServerLog(String sid) throws SQLException, RemoteException;
}
