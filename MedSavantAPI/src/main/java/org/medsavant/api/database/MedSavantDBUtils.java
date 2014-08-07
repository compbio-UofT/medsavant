/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.medsavant.api.database;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.common.storage.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 * A collection of utilities for manipulating SQL databases. Note that some of
 * these methods may be MySQL specific.
 */
public class MedSavantDBUtils {

    private static final Log LOG = LogFactory.getLog(MedSavantDBUtils.class);

    public static Date getCurrentDatabaseTime(MedSavantJDBCPooledConnection conn) throws SQLException, SessionExpiredException {
        //The modification time is the time when a change was made to the last comment.  Since there are 
        //no comments yet, take the modification time as the current database server time.
        ResultSet rs = null;
        try {
            rs = conn.executeQuery("SELECT NOW()+0");
            Date currentDate = null;
            if (rs.next()) {
                currentDate = new Date(rs.getTimestamp(1).getTime());
            }
            return currentDate;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public static boolean fieldExists(MedSavantJDBCPooledConnection conn, String tableName, String fieldName) throws SQLException, SessionExpiredException {
        ResultSet rs = conn.executeQuery("SHOW COLUMNS IN " + tableName);

        while (rs.next()) {
            if (rs.getString(1).equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    //Moved to InfobrightUtils
    @Deprecated
    public static void dumpTable(MedSavantJDBCPooledConnection conn, String tableName, File dst) throws SQLException, SessionExpiredException {
        throw new UnsupportedOperationException("Moved to InfobrightUtils");

    }

    //Moved to InfobrightUtils
    @Deprecated
    public static void loadTable(MedSavantJDBCPooledConnection conn, File src, String dst) throws SQLException, SessionExpiredException {
       throw new UnsupportedOperationException("Moved to InfobrightUtils");
    }
    
    //Moved to InfobrightUtils
    @Deprecated
    public static void copyTable(MedSavantJDBCPooledConnection conn, String srcTableName, String dstTableName) throws IOException, SQLException {
       throw new UnsupportedOperationException("Moved to InfobrightUtils");
    }

    //moved to InfobrightUtils.
    @Deprecated
    public static void dropTable(MedSavantJDBCPooledConnection conn, String tableName) throws SQLException, SessionExpiredException {
        throw new UnsupportedOperationException("Moved to infobrightUtils");
        conn.executeUpdate("DROP TABLE IF EXISTS " + tableName + ";");
    }

    public static boolean tableExists(MedSavantJDBCPooledConnection conn, String tableName) throws SQLException, SessionExpiredException {
        try {
            return conn.tableExists(tableName);
        } finally {
            conn.close();
        }
    }

    private static String getColumnTypeString(String s) {
        int pos = s.indexOf("(");
        if (pos == -1) {
            return s;
        } else {
            return s.substring(0, pos);
        }
    }

    public static TableSchema importTableSchema(Connection conn, String tablename) throws SQLException {
       
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);
        TableSchema ts = new TableSchema(table);

        ResultSet rs = null;
        try {
            LOG.info(String.format("Executing %s ", "DESCRIBE " + tablename));
            rs = conn.createStatement().executeQuery("DESCRIBE " + tablename);
            boolean tableExists = false;
            while (rs.next()) {
                int[] ls = CustomField.extractColumnLengthAndScale(rs.getString(2));
                table.addColumn(rs.getString(1), getColumnTypeString(rs.getString(2)), ls[0], (ls[1] == 0 ? null : ls[1]));
                ts.addColumn(rs.getString(1), ColumnType.fromString(getColumnTypeString(rs.getString(2))), ls[0], ls[1]);
                tableExists = true;
            }
            if(!tableExists){
                throw new SQLException("Can't import table schema with name "+tablename+" -- does not exist");
            }
            return ts;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    /**
     * Sometimes Throwable.getMessage() returns a useless string (e.g. "null"
     * for a NullPointerException). Return a string which is more meaningful to
     * the end-user.
     */
    /*public static String getMessage(Throwable t) {
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
     */
    /*
     public static void dropViewIfExists(String sessID, String tableName) throws SQLException, SessionExpiredException {
     ConnectionController.executeUpdate(sessID, "DROP VIEW IF EXISTS " + tableName);
     }

     public static void dropTableIfExists(String sessID, String tableName) throws SQLException, SessionExpiredException {
     ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName);
     }*/
}
