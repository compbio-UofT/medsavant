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
package org.ut.biolab.medsavant.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.db.shard.result.ShardedResultSetFactory;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

    private static final Log LOG = LogFactory.getLog(ConnectionController.class);
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String PROPS = "enableQueryTimeouts=false";//"useCompression=true"; //"useCompression=true&enableQueryTimeouts=false";
    private static final Map<String, ConnectionPool> sessionPoolMap = new HashMap<String, ConnectionPool>();
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

    public static PooledConnection connectPooled(String sessID) throws SQLException {
        synchronized (sessionPoolMap) {
            ConnectionPool pool = sessionPoolMap.get(sessID);
            if (pool != null) {
                return pool.getConnection();
            }
        }
        return null;
    }

    public static ResultSet executeQuery(String sessID, String query) throws SQLException {
        PooledConnection conn = connectPooled(sessID);
        try {
            return conn.executeQuery(query);
        } finally {
            conn.close();
        }
    }

    private static class QueryExecutionRunnable implements Runnable {

        private final String query;
        private final String sessID;
        private ResultSet resultSet;
        private SQLException ex;

        public QueryExecutionRunnable(String s, String q) {
            this.sessID = s;
            this.query = q;
        }

        @Override
        public void run() {
            try {
                resultSet = executeQuery(sessID, query);
            } catch (SQLException ex) {
                this.ex = ex;
                LOG.error(ex);
            }
        }

        public boolean didCompleteSuccessfully() { return ex == null; }
        public SQLException getException() { return ex; }
        public ResultSet getResultSet() { return resultSet; }
    }

    public static ResultSet executeQuerySharded(String sessID, String query) throws SQLException {

        boolean isCount = query.contains("COUNT(*)");
        boolean isSelect = !isCount;

        List<String> shardedQueries = shardQuery(sessID, query);
        int numShards = shardedQueries.size();
        List<ResultSet> resultSets = new ArrayList<ResultSet>(numShards);
        List<QueryExecutionRunnable> runnables = new ArrayList<QueryExecutionRunnable>(numShards);
        List<Thread> threads = new ArrayList<Thread>();
        for (String q : shardedQueries) {
            QueryExecutionRunnable r = new QueryExecutionRunnable(sessID,q);
            runnables.add(r);
            Thread t = new Thread(r);
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                throw new SQLException(ex.getMessage());
            }
        }

        for (QueryExecutionRunnable r : runnables) {
            if (r.didCompleteSuccessfully()) {
                resultSets.add(r.getResultSet());
            } else {
                throw r.getException();
            }
        }

        if (isCount) {
            return ShardedResultSetFactory.ShardedCountQueryResultSet(resultSets, 1); // TODO: actually determine column

        // assuming it must be a select query at this point
        } else {
            return ShardedResultSetFactory.ShardedSelectQueryResultSet(resultSets);
        }
    }

    /**
     * Take a query and shard it (in a scale-up fashion, i.e. all shards exist in the
     * same database) so that the initial query is divided and comprehensive
     * @param sessID The session requesting the query
     * @param query The query to shard
     * @return A list of queries to run, which altogether are comprehensive of query
     */
    private static List<String> shardQuery(String sessID, String query) {
        // TODO: complete
        throw new UnsupportedOperationException("Sharding up queries not implemented yet");
    }

    public static ResultSet executePreparedQuery(String sessID, String query, Object... args) throws SQLException {
        PooledConnection conn = connectPooled(sessID);
        try {
            return conn.executePreparedQuery(query, args);
        } finally {
            conn.close();
        }
    }

    public static void executeUpdate(String sessID, String query) throws SQLException {
        PooledConnection conn = connectPooled(sessID);
        try {
            conn.executeUpdate(query);
        } finally {
            conn.close();
        }
    }

    public static void executePreparedUpdate(String sessID, String query, Object... args) throws SQLException {
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

    public static void removeSession(String sessID) throws SQLException {
        synchronized (sessionPoolMap) {
            ConnectionPool pool = sessionPoolMap.remove(sessID);
            pool.close();
        }
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
}
