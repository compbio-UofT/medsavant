package org.ut.biolab.medsavant.shared.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public interface LogManagerAdapter extends Remote {

    public static enum LogType { INFO, ERROR, LOGIN, LOGOUT };

    public List<GeneralLog> getClientLog(String sid,int start, int limit) throws SQLException, RemoteException, SessionExpiredException;
    public List<GeneralLog> getServerLog(String sid,int start, int limit) throws SQLException, RemoteException, SessionExpiredException;
    public List<AnnotationLog> getAnnotationLog(String sid,int start, int limit) throws SQLException, RemoteException, SessionExpiredException;

    public int getAnnotationLogSize(String sid) throws SQLException, RemoteException, SessionExpiredException;
    public int getServerLogSize(String sid) throws SQLException, RemoteException, SessionExpiredException;
    public int getClientLogSize(String sid) throws SQLException, RemoteException, SessionExpiredException;

    public void addLog(String sid, String uname, LogType t, String description) throws RemoteException, SessionExpiredException;

    public void addServerLog(String sid, LogType t, String description) throws RemoteException, SessionExpiredException;
    public Date getDateOfLastServerLog(String sid) throws SQLException, RemoteException, SessionExpiredException;
}
