/*
 *    Copyright 2011 University of Toronto
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
public class ConnectionController {
    private static final Logger LOG = Logger.getLogger(ConnectionController.class.getName());
    private static Connection lastConnection;

    public static void disconnectAll() {
        if (lastConnection != null) {
            try {
                lastConnection.close();
            } catch (SQLException ex) {
            }
            lastConnection = null;
        }

    }
    
    private static String dbhost;
    private static int port;
    private static String dbname;
    
    public static Connection connectPooled() throws SQLException {
        
        if (!hostSet) {
            throw new SQLException("DB host not set");
        }
        
        if (!dbnameset) {
            throw new SQLException("DB name not set");
        }
        
        if (!portset) {
            throw new SQLException("DB port not set");
        }
        
        return connectInternal(dbhost,port,dbname);
    }
    
    public static Connection connectUnpooled(String dbhost, int port, String dbname) throws SQLException {
        return connectOnce(dbhost, port, dbname);
    }
    
    private static Connection connectInternal(String dbhost, int port, String dbname) throws SQLException {

        if (lastConnection == null || lastConnection.isClosed()) {
            lastConnection = connectOnce(dbhost, port, dbname);
        }
        
        return lastConnection;
    }

    public static void setDbhost(String dbhost) {
        hostSet = true;
        ConnectionController.dbhost = dbhost;
    }

    public static void setDbname(String dbname) {
        dbnameset = true;
        ConnectionController.dbname = dbname;
    }

    public static void setPort(int port) {
        portset = true;
        ConnectionController.port = port;
    }

    public static String getDbname() {
        return dbname;
    }

    
    
    
    private static String dbdriver = "com.mysql.jdbc.Driver";
    private static String dburl = "jdbc:mysql://";
    private static String user = "root";
    private static String pw = "";
    private static String props = "enableQueryTimeouts=false";//"useCompression=true"; //"useCompression=true&enableQueryTimeouts=false";

    private static Connection connectOnce(String dbhost, int port, String dbname) throws SQLException {
        Connection c;
        try {
            Class.forName(dbdriver).newInstance();
        } catch (Exception ex) {
        }
        c = DriverManager.getConnection(dburl + dbhost + ":" + port + "/" + dbname + "?" + props, user, pw);
        
        return c;
    }
    private static boolean hostSet;
    private static boolean portset;
    private static boolean dbnameset;
    
    
    /**
     * Utility method to make it easier to execute SELECT-style queries.
     * 
     * @param format format string
     * @param args arguments for format string
     * @return a ResultSet containing the results of the query
     * @throws SQLException 
     */
    public static ResultSet executeQuery(String format, Object... args) throws SQLException {
        Statement st = connectPooled().createStatement();
        String query = String.format(format, args);
        LOG.log(Level.FINE, query);
        return st.executeQuery(query);
    }

    /**
     * Utility method to make it easier to execute data-manipulation calls which don't
     * return a result.
     *
     * @param format format string
     * @param args arguments for the format string
     * @throws SQLException 
     */
    public static void executeUpdate(String format, Object... args) throws SQLException {
        Statement st = connectPooled().createStatement();
        String query = String.format(format, args);
        LOG.log(Level.FINE, query);
        st.executeUpdate(query);
    }
}
