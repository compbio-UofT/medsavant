package org.ut.biolab.medsavant.server.db.util;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

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
}
