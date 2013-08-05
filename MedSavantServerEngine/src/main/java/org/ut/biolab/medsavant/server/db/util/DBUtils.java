/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.server.db.util;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import com.mysql.jdbc.CommunicationsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.query.*;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.solr.SolrQueryManager;
import org.ut.biolab.medsavant.shared.serverapi.DBUtilsAdapter;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class DBUtils extends MedSavantServerUnicastRemoteObject implements DBUtilsAdapter {

    private static final Log LOG = LogFactory.getLog(DBUtils.class);
    private static DBUtils instance;
    private static QueryManager queryManager;

    static {
        queryManager = QueryManagerFactory.getQueryManager();
    }

    public static synchronized DBUtils getInstance() throws RemoteException {
        if (instance == null) {
            instance = new DBUtils();
        }
        return instance;
    }

    private DBUtils() throws RemoteException {

    }

    public static boolean fieldExists(String sid, String tableName, String fieldName) throws SQLException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sid, "SHOW COLUMNS IN " + tableName);

        while (rs.next()) {
            if (rs.getString(1).equals(fieldName)) {
                return true;
            }
        }

        return false;
    }

    public static String getColumnTypeString(String s) {
        int pos = s.indexOf("(");
        if (pos == -1) {
            return s;
        } else {
            return s.substring(0, pos);
        }
    }

    public static int getColumnLength(String s) {

        int fpos = s.indexOf("(");
        int rpos = s.indexOf(")");
        int cpos = s.indexOf(",");
        if (cpos != -1 && cpos < rpos) {
            rpos = cpos;
        }

        if (fpos == -1) {
            return -1;
        } else {
            return Integer.parseInt(s.substring(fpos + 1, rpos));
        }
    }

    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {

        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);

        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberOfColumns = rsMetaData.getColumnCount();

        while (rs.next()) {
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), getColumnLength(rs.getString(2)));
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
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), getColumnLength(rs.getString(2)));
            ts.addColumn(rs.getString(1), ColumnType.fromString(getColumnTypeString(rs.getString(2))), getColumnLength(rs.getString(2)));
        }

        return ts;
    }

    public static void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName + ";");
    }

    public static boolean tableExists(String sessID, String tableName) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            return conn.tableExists(tableName);
        } finally {
            conn.close();
        }
    }

    @Override
    public int getNumRecordsInTable(String sessID, String tablename) throws SQLException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sessID, "SELECT COUNT(*) FROM `" + tablename + "`");
        rs.next();
        return rs.getInt(1);
    }

    /**
     * The message for a MySQL CommunicationsException contains a lot of junk
     * (including a full stack-trace), but hidden inside is a useful message.
     * Extract it.
     *
     * @param x the exception to be parsed.
     * @return text found on line starting with "MESSAGE: "
     */
    public static String extractMySQLMessage(CommunicationsException x) {
        // MySQL stuffs the whole stack-trace into this exception.
        String key = "\nMESSAGE: ";
        String msg = x.getMessage();
        int startPos = msg.indexOf(key);
        if (startPos >= 0) {
            startPos += key.length();
            int endPos = msg.indexOf('\n', startPos);
            return msg.substring(startPos, endPos);
        }
        // Couldn' find our magic string.  Return the whole thing.
        return msg;
    }

    /**
     * Sometimes Throwable.getMessage() returns a useless string (e.g. "null"
     * for a NullPointerException). Return a string which is more meaningful to
     * the end-user.
     */
    public static String getMessage(Throwable t) {
        if (t instanceof CommunicationsException) {
            String result = t.getMessage();
            int retPos = result.indexOf('\n');
            if (retPos > 0) {
                result = result.substring(0, retPos);
                result += extractMySQLMessage((CommunicationsException) t);
            }
            return result;
        } else {
            return MiscUtils.getMessage(t);
        }
    }

    /**
     * A return value of null indicates too many values.
     */
    @Override
    public List<String> getDistinctValuesForColumn(String sessID, String tableName, String colName, boolean cacheing) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {
        return getDistinctValuesForColumn(sessID, tableName, colName, false, cacheing);
    }

    /**
     * A return value of null indicates too many values.
     */
    @Override
    public List<String> getDistinctValuesForColumn(String sessID, String tableName, String colName, boolean explodeCommaSeparatedValues, boolean cacheing) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {
        LOG.info("Getting distinct values for " + tableName + "." + colName);

        QueryManager queryManager = new SolrQueryManager();

        Query query = queryManager.createQuery("Select e." + colName + " from " + tableName + " e group by e." + colName);

        makeProgress(sessID, String.format("Retrieving distinct values for %s...", colName), 0.0);

        List<ResultRow> resultRowList = query.executeForRows();
        List<String> result = new ArrayList<String>();

        //Todo add cache
        for (ResultRow resultRow : resultRowList) {
            result.add(String.valueOf(resultRow.getObject(colName)));
        }
        return result;
    }

    @Override
    public Range getExtremeValuesForColumn(String sessID, String tabName, String colName) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {
        LOG.info("Getting extreme values for " + tabName + "." + colName);
        makeProgress(sessID, String.format("Retrieving extreme values for %s...", colName), 0.0);
        String dbName = SessionController.getInstance().getDatabaseForSession(sessID);
        if (DistinctValuesCache.isCached(dbName, tabName, colName)) {
            try {
                Range result = DistinctValuesCache.getCachedRange(dbName, tabName, colName);
                if (result != null) {
                    return result;
                }
            } catch (Exception ex) {
                LOG.warn("Unable to get cached extreme values for " + dbName + "/" + tabName + "/" + colName, ex);
            }
        }
        makeProgress(sessID, "Querying database...", 0.2);

        String statement = String.format("select e.%s, min(e.%s), max(e.%s) from %s e", colName, colName, colName,tabName);

        Query query = queryManager.createQuery(statement);

        List<ResultRow> results = query.executeForRows();

        double min = (Double) results.get(0).getObject("min");
        double max = (Double) results.get(0).getObject("max");

        Range result = new Range(min, max);
        makeProgress(sessID, "Saving cached values...", 0.9);
        DistinctValuesCache.cacheResults(dbName, tabName, colName, Arrays.asList(min, max));
        return result;
    }

    @Override
    public Condition getRangeCondition(DbColumn col, Range r) {
        Condition[] results = new Condition[2];
        results[0] = BinaryCondition.greaterThan(col, r.getMin(), true);
        results[1] = BinaryCondition.lessThan(col, r.getMax(), false);

        return ComboCondition.and(results);
    }
}
