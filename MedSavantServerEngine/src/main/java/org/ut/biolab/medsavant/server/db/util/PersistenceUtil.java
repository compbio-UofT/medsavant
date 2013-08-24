package org.ut.biolab.medsavant.server.db.util;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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
}
