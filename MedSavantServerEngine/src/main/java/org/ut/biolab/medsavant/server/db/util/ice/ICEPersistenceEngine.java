package org.ut.biolab.medsavant.server.db.util.ice;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.db.util.PersistenceEngine;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Implements PersistenceEngine operations for the ICE backend.
 */
public class ICEPersistenceEngine implements PersistenceEngine {

    private static final Log LOG = LogFactory.getLog(ICEPersistenceEngine.class);

    @Override
    public boolean fieldExists(String sid, String tableName, String fieldName) throws SQLException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sid, "SHOW COLUMNS IN " + tableName);

        while (rs.next()) {
            if (rs.getString(1).equals(fieldName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);

        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberOfColumns = rsMetaData.getColumnCount();

        while (rs.next()) {
            table.addColumn(rs.getString(1), DBUtils.getColumnTypeString(rs.getString(2)), DBUtils.getColumnLength(rs.getString(2)));
        }

        return table;
    }

    @Override
    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);
        TableSchema ts = new TableSchema(table);

        LOG.info(String.format("Executing %s on %s...", "DESCRIBE " + tablename, sessionId));
        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        while (rs.next()) {
            table.addColumn(rs.getString(1), DBUtils.getColumnTypeString(rs.getString(2)), DBUtils.getColumnLength(rs.getString(2)));
            ts.addColumn(rs.getString(1), ColumnType.fromString(DBUtils.getColumnTypeString(rs.getString(2))), DBUtils.getColumnLength(rs.getString(2)));
        }

        return ts;
    }

    @Override
    public void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName + ";");
    }
}
