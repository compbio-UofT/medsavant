package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author mfiume
 */
public interface LogQueryUtilAdapter extends Remote {

    public ResultSet getClientLog(String sid,int start, int limit) throws SQLException;
    public ResultSet getServerLog(String sid,int start, int limit) throws SQLException;
    public ResultSet getAnnotationLog(String sid,int start, int limit) throws SQLException;
    public int getAnnotationLogSize(String sid) throws SQLException;
    public int getServerLogSize(String sid) throws SQLException;
    public int getClientLogSize(String sid) throws SQLException;

}
