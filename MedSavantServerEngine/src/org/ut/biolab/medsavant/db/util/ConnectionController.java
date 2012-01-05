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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

    private static Map<String, PreparedStatement> statementCache = new HashMap<String, PreparedStatement>();
    private static final Logger LOG = Logger.getLogger(ConnectionController.class.getName());
    private static String dbHost;
    private static int dbPort = -1;
    private static String dbDriver = "com.mysql.jdbc.Driver";
    private static String props = "enableQueryTimeouts=false";//"useCompression=true"; //"useCompression=true&enableQueryTimeouts=false";
    private static final Map<String, SessionConnection> sessionConnectionMap = new HashMap<String, SessionConnection>();

    public static void setHost(String value) {
        ConnectionController.dbHost = value;
    }

    public static void setPort(int value) {
        ConnectionController.dbPort = value;
    }

    static String getHost() {
        return dbHost;
    }

    static int getPort() {
        return dbPort;
    }

    static Connection connectOnce(String dbhost, int port, String dbname, String username, String password) throws SQLException {
        try {
            Class.forName(dbDriver).newInstance();
        } catch (Exception ex) {
            if (ex instanceof ClassNotFoundException || ex instanceof InstantiationException) {
                throw new SQLException("Unable to load MySQL driver.");
            }
        }

        return DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?%s", dbhost, port, dbname, props), username, password);
    }

    /**
     * Utility statement to retrieve a prepared statement if one has already been prepared,
     * or to prepare a new statement if the given one is not found in the cache.
     */
    private static PreparedStatement getPreparedStatement(String sessionId, String query) throws SQLException {
        if (statementCache.containsKey(query)) {
            return statementCache.get(query);
        }
        PreparedStatement result = connectPooled(sessionId).prepareStatement(query);
        statementCache.put(query, result);
        return result;
    }

    public static Connection connectPooled(String sessionId) {
        if (sessionConnectionMap.containsKey(sessionId)) {
            try {
                return sessionConnectionMap.get(sessionId).connectPooled();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Utility method to make it easier to execute SELECT-style queries.
     *
     * @param stmt a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @return a ResultSet containing the results of the query
     * @throws SQLException
     */
    public static ResultSet executeQuery(String sessionId, String query, Object... args) throws SQLException {
        PreparedStatement st = getPreparedStatement(sessionId, query);
        for (int i = 0; i < args.length; i++) {
            st.setObject(i + 1, args[i]);
        }
        return st.executeQuery();
    }

    /**
     * Utility method to make it easier to execute data-manipulation calls which don't
     * return a result.
     *
     * @param stmt a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @throws SQLException
     */
    public static void executeUpdate(String sessionId, String query, Object... args) throws SQLException {
        PreparedStatement st = getPreparedStatement(sessionId, query);
        for (int i = 0; i < args.length; i++) {
            st.setObject(i + 1, args[i]);
        }
        LOG.log(Level.INFO, query);
        st.executeUpdate();
    }

    public static boolean registerCredentials(String sessionId, String uname, String pw, String dbname) {
        SessionConnection sc = new SessionConnection(uname, pw, dbname);
        sessionConnectionMap.put(sessionId, sc);
        try {
            Connection c = sc.connectPooled();
            if (c != null && !c.isClosed()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

    public static void switchDatabases(String sessionId, String dbname) {
        SessionConnection sc = sessionConnectionMap.get(sessionId);
        sc.setDBName(dbname);
    }

    public static String getDBName(String sid) {
        SessionConnection sc = sessionConnectionMap.get(sid);
        return sc.getDBName();
    }

    public static String getUserForSession(String sid) {
        SessionConnection sc = sessionConnectionMap.get(sid);
        return sc.getUser();
    }
    
    public static void removeSession(String sid) {
        sessionConnectionMap.remove(sid);
    }

}
