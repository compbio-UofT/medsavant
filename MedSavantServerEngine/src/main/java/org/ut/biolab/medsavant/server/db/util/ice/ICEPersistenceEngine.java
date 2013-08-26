package org.ut.biolab.medsavant.server.db.util.ice;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.db.util.PersistenceEngine;
import org.ut.biolab.medsavant.server.serverapi.UserManager;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

import java.rmi.RemoteException;
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

    /**
     * Add a new user to MedSavant.
     *
     * @param sessID the session we're logged in as
     * @param user the user to add
     * @param pass the password
     * @param level the user's level
     * @throws SQLException
     */
    @Override
    public synchronized void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            // TODO: Transactions aren't supported for MyISAM, so this has no effect.
            conn.setAutoCommit(false);

            conn.executePreparedUpdate("CREATE USER ?@'localhost' IDENTIFIED BY ?", user, new String(pass));
            grantPrivileges(sessID, user, level);
            conn.commit();
        } catch (SQLException sqlx) {
            conn.rollback();
            throw sqlx;
        } finally {
            for (int i = 0; i < pass.length; i++) {
                pass[i] = 0;
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    /**
     * Grant the user the privileges appropriate to their level
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException
     */
    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            String dbName = ConnectionController.getDBName(sessID);
            LOG.info("Granting " + level + " privileges to " + name + " on " + dbName + "...");
            switch (level) {
                case ADMIN:
                    conn.executePreparedUpdate("GRANT ALTER, CREATE, CREATE TEMPORARY TABLES, CREATE USER, DELETE, DROP, FILE, GRANT OPTION, INSERT, SELECT, UPDATE ON *.* TO ?@'localhost'", name);
                    conn.executePreparedUpdate(String.format("GRANT GRANT OPTION ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT ALTER, CREATE, CREATE TEMPORARY TABLES, DELETE, DROP, INSERT, SELECT, UPDATE ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.db TO ?@'localhost'", name);
                    break;
                case USER:
                    conn.executePreparedUpdate(String.format("GRANT CREATE TEMPORARY TABLES, SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.region_set TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.region_set_membership TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);
                    break;
                case GUEST:
                    conn.executePreparedUpdate(String.format("GRANT SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);
                    break;
            }
            LOG.info("... granted.");
        } finally {
            conn.close();
        }
    }

    @Override
    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException {
        ConnectionController.executePreparedUpdate(sid, "DROP USER ?@'localhost'", name);
    }

    @Override
    public void registerCredentials(String sessionId, String user, String password, String dbName) throws SQLException {
        ConnectionController.registerCredentials(sessionId, user, password, dbName);
    }
}
