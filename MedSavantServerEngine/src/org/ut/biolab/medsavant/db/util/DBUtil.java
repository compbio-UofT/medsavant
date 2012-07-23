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
package org.ut.biolab.medsavant.db.util;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import com.mysql.jdbc.CommunicationsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.db.ColumnType;
import org.ut.biolab.medsavant.db.FatalDatabaseException;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.connection.PooledConnection;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.serverapi.DBUtilAdapter;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class DBUtil extends MedSavantServerUnicastRemoteObject implements DBUtilAdapter {
    private static final Log LOG = LogFactory.getLog(DBUtil.class);

    private static DBUtil instance;

    public static synchronized DBUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }

    private DBUtil() throws RemoteException {
    }

    public static boolean fieldExists(String sid, String tableName, String fieldName) throws SQLException {
        ResultSet rs = ConnectionController.executeQuery(sid, "SHOW COLUMNS IN " + tableName);

        while(rs.next()) {
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
        if(cpos != -1 && cpos < rpos){
            rpos = cpos;
        }

        if (fpos == -1) {
            return -1;
        } else {
            return Integer.parseInt(s.substring(fpos+1,rpos));
        }
    }

    public DbTable importTable(String sessionId, String tablename) throws SQLException {

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
    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, RemoteException {

        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);
        TableSchema ts = new TableSchema(table);

        LOG.info(String.format("Executing %s on %s...", "DESCRIBE " + tablename, sessionId));
        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        while (rs.next()) {
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), getColumnLength(rs.getString(2)));
            ts.addColumn(rs.getString(1), rs.getString(1), ColumnType.fromString(getColumnTypeString(rs.getString(2))), getColumnLength(rs.getString(2)));
        }

        return ts;
    }

    //Returns 0 based position
    public static int getIndexOfField(TableSchema t, String columnname) throws SQLException {
        return t.getFieldIndexInDB(columnname) - 1;
    }

    public static int getIndexOfField(String sessID, String tableName, String colName) throws SQLException, RemoteException {
        return getIndexOfField(instance.importTableSchema(sessID, tableName), colName);
    }

    public static void dropTable(String sessID, String tableName) throws SQLException {
        ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName + ";");
    }

    public static boolean tableExists(String sessID, String tableName) throws SQLException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            return conn.tableExists(tableName);
        } finally {
            conn.close();
        }
    }

    @Override
    public int getNumRecordsInTable(String sessID, String tablename) throws SQLException {
        ResultSet rs =  ConnectionController.executeQuery(sessID, "SELECT COUNT(*) FROM `" + tablename + "`");
        rs.next();
        return rs.getInt(1);
    }

    /**
     * The message for a MySQL CommunicationsException contains a lot of junk (including
     * a full stack-trace), but hidden inside is a useful message.  Extract it.
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
     * Sometimes Throwable.getMessage() returns a useless string (e.g. "null" for a NullPointerException).
     * Return a string which is more meaningful to the end-user.
     */
    public static String getMessage(Throwable t) {
        if (t instanceof CommunicationsException) {
            String result = t.getMessage();
            int retPos = result.indexOf('\n');
            if (retPos > 0) {
                result = result.substring(0, retPos);
                result += extractMySQLMessage((CommunicationsException)t);
            }
            return result;
        } else {
            return MiscUtils.getMessage(t);
        }
    }

    @Override
    public List<String> getDistinctValuesForColumn(String sid,TableSchema t, DbColumn col) throws SQLException {
        return getDistinctValuesForColumn(sid,t, col, -1);
    }

    @Override
    public List<String> getDistinctValuesForColumn(String sid,TableSchema t, DbColumn col, int limit) throws SQLException {

        if (t.getColumnType(t.getColumnIndex(col)).isNumeric()) {
            throw new FatalDatabaseException("Can't get distinct values for numeric field : " + col.getAbsoluteName());
        }

        SelectQuery q = new SelectQuery();
        q.setIsDistinct(true);
        q.addColumns(col);
        q.addFromTable(t.getTable());

        String queryString = q.toString();
        if(limit > 0) queryString = queryString + " LIMIT " + limit;
        ResultSet rs = ConnectionController.executeQuery(sid, queryString);

        List<String> distinctValues = new ArrayList<String>();

        while(rs.next()) {
            String val = rs.getString(1);
            if(val == null){
                distinctValues.add("");
            } else {
                distinctValues.add(val);
            }
        }

        Collections.sort(distinctValues);

        return distinctValues;
    }


    @Override
    public Range getExtremeValuesForColumn(String sid,TableSchema t, DbColumn col) throws SQLException {

        if (!t.getColumnType(t.getColumnIndex(col)).isNumeric()) {
            throw new FatalDatabaseException("Can't get extreme values for non-numeric field : " + col.getAbsoluteName());
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addCustomColumns(FunctionCall.min().addColumnParams(col));
        q.addCustomColumns(FunctionCall.max().addColumnParams(col));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        double min = 0;
        double max = 0;

        ColumnType type = t.getColumnType(col);

        while(rs.next()) {
            switch(type) {
                case INTEGER:
                    min = rs.getInt(1);
                    max = rs.getInt(2);
                    break;
                case FLOAT:
                    min = rs.getFloat(1);
                    max = rs.getFloat(2);
                    break;
                case DECIMAL:
                    min = rs.getDouble(1);
                    max = rs.getDouble(2);
                    break;
                default:
                    throw new FatalDatabaseException("Unhandled column type: " + type);
            }
        }

        return new Range(min,max);

    }


    @Override
    public Condition getRangeCondition(DbColumn col, Range r) {
        Condition[] results = new Condition[2];
        results[0] = BinaryCondition.greaterThan(col, r.getMin(), true);
        results[1] = BinaryCondition.lessThan(col, r.getMax(), false);

        return ComboCondition.and(results);
    }
}
