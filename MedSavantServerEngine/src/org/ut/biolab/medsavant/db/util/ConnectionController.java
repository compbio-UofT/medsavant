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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.client.api.ClientCallbackAdapter;
import org.ut.biolab.medsavant.db.connection.MSConnection;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

    private static final Map<String, PreparedStatement> statementCache = new HashMap<String, PreparedStatement>();
    private static final Logger LOG = Logger.getLogger(ConnectionController.class.getName());
    private static String dbHost;
    private static int dbPort = -1;
    private static String dbDriver = "com.mysql.jdbc.Driver";
    private static String props = "enableQueryTimeouts=false";//"useCompression=true"; //"useCompression=true&enableQueryTimeouts=false";
    private static final Map<String, SessionConnection> sessionConnectionMap = new HashMap<String, SessionConnection>();
    private static final Map<String, ClientCallbackAdapter> sessionCallbackMap = new HashMap<String, ClientCallbackAdapter>();

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
    
    public static Connection connectOnce(String dbhost, int port, String dbname, String username, String password) throws SQLException {
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
    private static PreparedStatement getPreparedStatement(Connection c, String sessionId, String query) throws SQLException {
        synchronized(statementCache) {
            if (statementCache.containsKey(query)) {
                return statementCache.get(query);
            }
            PreparedStatement result = c.prepareStatement(query);
            statementCache.put(query, result);
            return result;
        }
    }

    public static Connection connectPooled(String sessionId) {
        synchronized(sessionConnectionMap) {
            if (sessionConnectionMap.containsKey(sessionId)) {
                try {
                    return sessionConnectionMap.get(sessionId).connectPooled();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
    
    public static ResultSet executeQuery(String sid, String query) throws SQLException {
        Connection conn = connectPooled(sid);
        ResultSet rs = conn.createStatement().executeQuery(query);
        conn.close();
        return rs;
    }
    
    public static void executeUpdate(String sid, String query) throws SQLException {
        Connection conn = connectPooled(sid);
        conn.createStatement().executeUpdate(query);
        conn.close();
    }
    
    public static void execute(String sid, String query) throws SQLException {
        Connection conn = connectPooled(sid);
        conn.createStatement().execute(query);
        conn.close();
    }

    /**
     * Utility method to make it easier to execute SELECT-style queries.
     *
     * @param stmt a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @return a ResultSet containing the results of the query
     * @throws SQLException
     */
    public static ResultSet executePreparedQuery(String sessionId, String query, Object... args) throws SQLException {
        Connection c = connectPooled(sessionId);
        PreparedStatement st = getPreparedStatement(c, sessionId, query);
        for (int i = 0; i < args.length; i++) {
            st.setObject(i + 1, args[i]);
        }
        ResultSet rs = st.executeQuery();
        c.close();
        return rs;
    }

    /**
     * Utility method to make it easier to execute data-manipulation calls which don't
     * return a result.
     *
     * @param stmt a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @throws SQLException
     */
    public static void executePreparedUpdate(String sessionId, String query, Object... args) throws SQLException {
        Connection c = connectPooled(sessionId);
        PreparedStatement st = getPreparedStatement(c, sessionId, query);
        for (int i = 0; i < args.length; i++) {
            st.setObject(i + 1, args[i]);
        }
        LOG.log(Level.INFO, query);
        st.executeUpdate();
        c.close();
    }

    public static boolean registerCredentials(String sessionId, String uname, String pw, String dbname) {
        SessionConnection sc = new SessionConnection(uname, pw, dbname);
        synchronized(sessionConnectionMap) {
            sessionConnectionMap.put(sessionId, sc);
        }
        try {
            Connection c = sc.connectPooled();
            if (c != null && !c.isClosed()) {
                c.close();
                return true;
            }
            c.close();
        } catch (Exception e) {}
        return false;
    }

    /*
     * Make sure you get a new connection after this!
     */
    public static void switchDatabases(String sessionId, String dbname) {
        synchronized(sessionConnectionMap) {
            SessionConnection sc = sessionConnectionMap.get(sessionId);
            sc.setDBName(dbname);
        }
    }

    public static String getDBName(String sid) {
        synchronized(sessionConnectionMap) {
            SessionConnection sc = sessionConnectionMap.get(sid);
            return sc.getDBName();
        }
    }

    public static String getUserForSession(String sid) {
        synchronized(sessionConnectionMap) {
            SessionConnection sc = sessionConnectionMap.get(sid);
            return sc.getUser();
        }
    }

    public static void removeSession(String sid) throws SQLException {
        sessionConnectionMap.get(sid).close();
        synchronized(sessionConnectionMap) {
            sessionConnectionMap.remove(sid);
        }
    }

    public static void addCallback(String sid, ClientCallbackAdapter c){
        sessionCallbackMap.put(sid, c);
    }

    public static void removeCallback(String sid){
        sessionCallbackMap.remove(sid);
    }

    public static ClientCallbackAdapter getCallback(String sid) {
        return sessionCallbackMap.get(sid);
    }

    public static String getDatabaseForSession(String sid) {
        synchronized(sessionConnectionMap) {
            SessionConnection sc = sessionConnectionMap.get(sid);
            return sc.getDBName();
        }
    }

    public static Collection<String> getSessionIDs() {
        synchronized(sessionConnectionMap) {
            return sessionConnectionMap.keySet();
        }
    }
    
    public static List<String> getDbNames() {
        List<String> result = new ArrayList<String>();
        for(SessionConnection sc : sessionConnectionMap.values()){
            if(!result.contains(sc.getDBName())){
                result.add(sc.getDBName());
            }
        }
        return result;
    }
}
