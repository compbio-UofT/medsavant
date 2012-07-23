package org.ut.biolab.medsavant.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.model.AnnotationLog;
import org.ut.biolab.medsavant.model.GeneralLog;

/**
 *
 * @author mfiume
 */
public interface LogManagerAdapter extends Remote {

    public static enum LogType { INFO, ERROR, LOGIN, LOGOUT };

    public List<GeneralLog> getClientLog(String sid,int start, int limit) throws SQLException, RemoteException;
    public List<GeneralLog> getServerLog(String sid,int start, int limit) throws SQLException, RemoteException;
    public List<AnnotationLog> getAnnotationLog(String sid,int start, int limit) throws SQLException, RemoteException;

    public int getAnnotationLogSize(String sid) throws SQLException, RemoteException;
    public int getServerLogSize(String sid) throws SQLException, RemoteException;
    public int getClientLogSize(String sid) throws SQLException, RemoteException;

    public void addLog(String sid, String uname, LogType t, String description) throws RemoteException;

    public void addServerLog(String sid, LogType t, String description) throws RemoteException;
    public Date getDateOfLastServerLog(String sid) throws SQLException, RemoteException;
}
