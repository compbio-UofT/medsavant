package org.ut.biolab.medsavant.server.db.util;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.ut.biolab.medsavant.server.db.ConnectionPool;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 * Abstracts operations not supported by JPQL
 *
 */
public interface PersistenceEngine {

    public boolean fieldExists(String sid, String tableName, String fieldName) throws SQLException, SessionExpiredException;

    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException;

    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, SessionExpiredException;

    public void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException;

    public void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException;

    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException;

    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException;

    public void registerCredentials(String sessionId, String user, String password, String dbName) throws SQLException;

    public void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) throws RemoteException, SQLException, SessionExpiredException;

    public String createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword, String versionString) throws RemoteException, SQLException, SessionExpiredException;

    public void createTables(String sessID) throws SQLException, RemoteException, SessionExpiredException;

    public void testConnection(String sessID) throws SQLException, SessionExpiredException;

    public void initializePooledConnection(ConnectionPool pool) throws SQLException;

    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging, boolean sub) throws RemoteException, SessionExpiredException, SQLException;

    public String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException, SessionExpiredException;

    public Object[] getVariantTableInfo(String sid, int projectid, int refid, boolean published) throws SQLException, SessionExpiredException;
}
