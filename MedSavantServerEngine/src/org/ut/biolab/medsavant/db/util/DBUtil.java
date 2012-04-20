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
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import com.mysql.jdbc.CommunicationsException;

import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.api.DBUtilAdapter;
import org.ut.biolab.medsavant.db.util.shared.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.db.util.shared.MiscUtils;

/**
 *
 * @author mfiume
 */
public class DBUtil extends MedSavantServerUnicastRemoteObject implements DBUtilAdapter {
    private static final Logger LOG = Logger.getLogger(DBUtil.class.getName());
    private static DBUtil instance;

    public static synchronized DBUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new DBUtil();
        }
        return instance;
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

    public DBUtil() throws RemoteException {super(); }

    public static String getColumnTypeString(String s) {
        int pos = s.indexOf("(");
        if (pos == -1) { return s; }
        else { return s.substring(0,pos); }
    }

    public static int getColumnLength(String s) {

        int fpos = s.indexOf("(");
        int rpos = s.indexOf(")");
        int cpos = s.indexOf(",");
        if(cpos != -1 && cpos < rpos){
            rpos = cpos;
        }

        if (fpos == -1) { return -1; }
        else {
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

        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        while (rs.next()) {
            table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), getColumnLength(rs.getString(2)));
            ts.addColumn(rs.getString(1), rs.getString(1), TableSchema.convertStringToColumnType(getColumnTypeString(rs.getString(2))), getColumnLength(rs.getString(2)));
        }

        return ts;
    }

    //Returns 0 based position
    public static int getIndexOfField(TableSchema t, String columnname) throws SQLException {
        return t.getFieldIndexInDB(columnname) - 1;
    }

    public static int getIndexOfField(String sessionId, String tablename, String columnname) throws SQLException, RemoteException {
        return getIndexOfField(instance.importTableSchema(sessionId,tablename), columnname);
    }

    public static void dropTable(String sessionId,String tablename) throws SQLException {
        ConnectionController.execute(sessionId, "DROP TABLE IF EXISTS " + tablename + ";");
    }

    public static boolean tableExists(String sessionId, String tablename) throws SQLException {
        ResultSet rs = ConnectionController.executeQuery(sessionId, "SHOW TABLES");

        while(rs.next()) {
            if (rs.getString(1).equals(tablename)) {
                return true;
            }
        }

        return false;
    }

    public static int getNumRecordsInTable(String sessionId, String tablename) throws SQLException {
        ResultSet rs =  ConnectionController.executeQuery(sessionId, "SELECT COUNT(*) FROM `" + tablename + "`");
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
}
