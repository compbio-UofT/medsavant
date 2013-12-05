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
package org.ut.biolab.medsavant.server.db;

import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

    private static final Log LOG = LogFactory.getLog(ConnectionController.class);
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String PROPS = "enableQueryTimeouts=false&autoReconnect=true";//"useCompression=true"; //"useCompression=true&enableQueryTimeouts=false";
    private static final Map<String, ConnectionPool> sessionPoolMap = new ConcurrentHashMap<String, ConnectionPool>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static String dbHost;
    private static int dbPort = -1;

    public static void setHost(String value) {
        dbHost = value;
    }

    public static void setPort(int value) {
        dbPort = value;
    }

    private static String getHost() {
        return dbHost;
    }

    private static int getPort() {
        return dbPort;
    }

    static String getConnectionString(String host, int port, String db) {
        return String.format("jdbc:mysql://%s:%d/%s?%s", host, port, db, PROPS);
    }

    static String getConnectionString(String db) {
        return getConnectionString(dbHost, dbPort, db);
    }

    public static void revalidate(String user, String pass, String sessionID) throws SQLException {
        connectOnce(dbHost, dbPort, getDBName(sessionID), user, pass);
    }

    public static Connection connectOnce(String host, int port, String db, String user, String pass) throws SQLException {
        try {
            Class.forName(DRIVER).newInstance();
        } catch (Exception ex) {
            if (ex instanceof ClassNotFoundException || ex instanceof InstantiationException) {
                throw new SQLException("Unable to load MySQL driver.");
            }
        }

        return DriverManager.getConnection(getConnectionString(host, port, db), user, pass);
    }

    public static PooledConnection connectPooled(String sessID) throws SQLException, SessionExpiredException {
        synchronized (sessionPoolMap) {

            if (!sessionPoolMap.containsKey(sessID)) {
                throw new SessionExpiredException();
            }

            ConnectionPool pool = sessionPoolMap.get(sessID);
            if (pool != null) {
                return pool.getConnection();
            }
        }
        return null;
    }

    public static ResultSet executeQuery(String sessID, String query) throws SQLException, SessionExpiredException {
        PooledConnection conn = connectPooled(sessID);
        try {
            return conn.executeQuery(query);
        } finally {
            conn.close();
        }
    }

    public static ResultSet executePreparedQuery(String sessID, String query, Object... args) throws SQLException, SessionExpiredException {
        PooledConnection conn = connectPooled(sessID);
        try {
            return conn.executePreparedQuery(query, args);
        } finally {
            conn.close();
        }
    }

    public static void executeUpdate(String sessID, String query) throws SQLException, SessionExpiredException {
        PooledConnection conn = connectPooled(sessID);
        try {
            conn.executeUpdate(query);
        } finally {
            conn.close();
        }
    }

    public static void executePreparedUpdate(String sessID, String query, Object... args) throws SQLException, SessionExpiredException {
        PooledConnection conn = connectPooled(sessID);
        try {
            conn.executePreparedUpdate(query, args);
        } finally {
            conn.close();
        }
    }

    /**
     * Register credentials for the given session.
     */
    public static void registerCredentials(String sessID, String user, String pass, String db) throws SQLException {
        LOG.debug(String.format("ConnectionController.registerCredentials(%s, %s, %s, %s)", sessID, user, pass, db));
        ConnectionPool pool = new ConnectionPool(db, user, pass);
        LOG.debug(String.format("sc=%s", pool));
        synchronized (sessionPoolMap) {
            sessionPoolMap.put(sessID, pool);

            Connection c = null;
            try {
                c = pool.getConnection();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    /*
     * Make sure you get a new connection after this!
     */
    public static void switchDatabases(String sessID, String db) {
        synchronized (sessionPoolMap) {
            sessionPoolMap.get(sessID).setDBName(db);
        }
    }

    public static String getDBName(String sessID) {
        synchronized (sessionPoolMap) {
            return sessionPoolMap.get(sessID).getDBName();
        }
    }

    public static String getUserForSession(String sessID) {
        synchronized (sessionPoolMap) {
            return sessionPoolMap.get(sessID).getUser();
        }
    }

    public static synchronized void removeSession(String sessID) throws SQLException {
        executor.submit(new CloseConnectionWhenDone(sessID));
    }

    public static Collection<String> getSessionIDs() {
        synchronized (sessionPoolMap) {
            return sessionPoolMap.keySet();
        }
    }

    public static Collection<String> getDBNames() {
        List<String> result = new ArrayList<String>();
        for (ConnectionPool pool : sessionPoolMap.values()) {
            if (!result.contains(pool.getDBName())) {
                result.add(pool.getDBName());
            }
        }
        return result;
    }

    public static void registerAdditionalSessionForSession(String fromSessionID, String toSessionID) {
        ConnectionPool pool = sessionPoolMap.get(fromSessionID);
        sessionPoolMap.put(toSessionID, pool);
    }

    /**
     * Closes the connection pool for a session once no background users of the
     * session are left (immediately if nobody is using it right now).
     */
    private static class CloseConnectionWhenDone implements Callable<Boolean> {

        /**
         * The session to close.
         */
        private final String sessionId;

        /**
         * Simple constructor.
         *
         * @param sessionId the session to close
         */
        CloseConnectionWhenDone(String sessionId) {
            this.sessionId = sessionId;
        }

        @Override
        public Boolean call() throws Exception {
            synchronized (sessionPoolMap) {

                if (sessionPoolMap.containsKey(sessionId)) {

                    // dissassociate this session with its connection pool
                    ConnectionPool pool = sessionPoolMap.remove(sessionId);

                    LOG.info("Unregistered session " + sessionId);

                    // some other session may be using this pool, so check first
                    if (!sessionPoolMap.containsValue(pool)) {
                        pool.close();
                        LOG.info("Reaped connections for " + sessionId);
                    } else {
                        LOG.info("Connection pool still in use for " + sessionId + ", not reaping connections");
                    }
                }
            }
            return Boolean.TRUE;
        }
    }
}
