/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import org.ut.biolab.medsavant.server.serverapi.SessionManager;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.DBUtilsAdapter;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class DBUtils extends MedSavantServerUnicastRemoteObject implements DBUtilsAdapter {

    private static final Log LOG = LogFactory.getLog(DBUtils.class);
    private static DBUtils instance;

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
    
    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {

        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);

        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberOfColumns = rsMetaData.getColumnCount();

        while (rs.next()) {
            int[] ls = CustomField.extractColumnLengthAndScale(rs.getString(2));               
            //scale argument should be set to null if it is unspecified (i.e. 0)
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), ls[0], ls[1]>0 ? ls[1] : null);
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
            int[] ls = CustomField.extractColumnLengthAndScale(rs.getString(2));        
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), ls[0], (ls[1] == 0 ? null : ls[1]));
            ts.addColumn(rs.getString(1), ColumnType.fromString(getColumnTypeString(rs.getString(2))), ls[0], ls[1]);    
            
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
    public List<String> getDistinctValuesForColumn(String sessID, String tableName, String colName, boolean explodeColonSeparatedValues, boolean cacheing) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {
        LOG.info("Getting distinct values for " + tableName + "." + colName);

        makeProgress(sessID, String.format("Retrieving distinct values for %s...", colName), 0.0);

        String dbName = SessionManager.getInstance().getDatabaseForSession(sessID);
        if (cacheing && DistinctValuesCache.isCached(dbName, tableName, colName)) {
            try {
                makeProgress(sessID, "Using cached values...", 1.0);
                return DistinctValuesCache.getCachedStringList(dbName, tableName, colName);
            } catch (Exception ex) {
                LOG.warn("Unable to get cached distinct values for " + dbName + "/" + tableName + "/" + colName, ex);
            }
        }

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tableName);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(colName));

        makeProgress(sessID, "Querying database...", 0.2);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString() + (cacheing ? " LIMIT " + DistinctValuesCache.CACHE_LIMIT : ""));

        Set<String> set = new HashSet<String>();
        while (rs.next()) {
            makeProgress(sessID, String.format("Retrieving distinct values for %s...", colName), 0.75);
            String val = rs.getString(1);
            if (val == null) {
                // We treat nulls and empty strings as being interchangeable.
                set.add("");
            } else {
                if (explodeColonSeparatedValues) {
                    String[] vals = val.split(";");
                    for (int i = 0; i < vals.length; i++) {
                        vals[i] = vals[i].trim();
                    }
                    set.addAll(Arrays.asList(vals));
                } else {
                    set.add(val);
                }
            }
        }

        List<String> result = new ArrayList<String>(set);
        Collections.sort(result);

        if (cacheing) {
            makeProgress(sessID, "Saving cached values...", 0.9);
            if (result.size() == DistinctValuesCache.CACHE_LIMIT) {
                DistinctValuesCache.cacheResults(dbName, tableName, colName, null);
                result = null;
            } else {
                DistinctValuesCache.cacheResults(dbName, tableName, colName, result);
            }
        }

        return result;
    }

    @Override
    public Range getExtremeValuesForColumn(String sessID, String tabName, String colName) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {
        LOG.info("Getting extreme values for " + tabName + "." + colName);
        makeProgress(sessID, String.format("Retrieving extreme values for %s...", colName), 0.0);
        String dbName = SessionManager.getInstance().getDatabaseForSession(sessID);
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

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tabName);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.min().addColumnParams(table.getDBColumn(colName)));
        query.addCustomColumns(FunctionCall.max().addColumnParams(table.getDBColumn(colName)));

        makeProgress(sessID, "Querying database...", 0.2);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());
        rs.next();

        double min = rs.getDouble(1);
        double max = rs.getDouble(2);
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
