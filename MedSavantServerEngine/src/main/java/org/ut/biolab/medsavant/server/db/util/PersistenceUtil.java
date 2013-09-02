package org.ut.biolab.medsavant.server.db.util;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.ut.biolab.medsavant.server.db.ConnectionPool;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 * Provides abstraction support for misc database operations.
 */
public class PersistenceUtil {

    private static PersistenceEngine engine = PersistenceEngineFactory.getPersistenceEngine();

    public static boolean fieldExists(String sid, String tableName, String fieldName) throws SQLException, SessionExpiredException {
        return engine.fieldExists(sid, tableName, fieldName);
    }

    public static DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        return engine.importTable(sessionId, tablename);
    }


    public static TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        return engine.importTableSchema(sessionId, tablename);
    }

    public static void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException {
        engine.dropTable(sessID, tableName);
    }

    public static void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException {
        engine.addUser(sessID, user, pass, level);
    }

    public static void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException {
        engine.grantPrivileges(sessID, name, level);
    }

    public static void removeUser(String sid, String name) throws SQLException, SessionExpiredException {
        engine.removeUser(sid, name);
    }

    public static void registerCredentials(String sessionId, String user, String password, String dbName) throws SQLException {
        engine.registerCredentials(sessionId, user, password, dbName);
    }

    public static String createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword, String versionString) throws IOException, SQLException, SessionExpiredException {
        return engine.createDatabase(dbHost, port, dbName, adminName, rootPassword,versionString);
    }

    public static void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) throws SQLException, SessionExpiredException, RemoteException {
        engine.removeDatabase(dbHost, port, dbName, adminName, rootPassword);
    }

    public static void createTables(String sessID) throws SQLException, RemoteException, SessionExpiredException {
        engine.createTables(sessID);
    }

    public static void testConnection(String sessID) throws SQLException, SessionExpiredException {
        engine.testConnection(sessID);
    }

    public static void initializePooledConnectio(ConnectionPool pool) throws SQLException {
        engine.initializePooledConnection(pool);
    }

    public static String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging, boolean sub) throws SQLException, RemoteException, SessionExpiredException {
        return engine.createVariantTable(sessID, projID, refID, updID, annIDs, staging, sub);
    }

    public static String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException, SessionExpiredException {
        return engine.getVariantTableName(sid, projectid, refid, published, sub);
    }

    public static Object[] getVariantTableInfo(String sid, int projectid, int refid, boolean published) throws SQLException, SessionExpiredException {
        return engine.getVariantTableInfo(sid, projectid, refid, published);
    }
}
