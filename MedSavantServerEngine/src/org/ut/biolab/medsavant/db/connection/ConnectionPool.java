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
package org.ut.biolab.medsavant.db.connection;

import java.sql.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Based on JDC Connection Pool example.
 *
 * @author Andrew
 */
public class ConnectionPool {
    
    private static final Log LOG = LogFactory.getLog(ConnectionPool.class);
    private static final long TIMEOUT = 60000;

    private final List<PooledConnection> connections;
    private final String user, password;
    private String dbName;
    private ConnectionReaper reaper;

    public ConnectionPool(String db, String user, String password) {
        this.dbName = db;
        this.user = user;
        this.password = password;
        connections = new ArrayList<PooledConnection>();
        reaper = new ConnectionReaper();
        reaper.start();
    }

    public synchronized void reapConnections() {

        long stale = System.currentTimeMillis() - TIMEOUT;

        for (PooledConnection conn: connections) {
            if (conn.inUse() && stale > conn.getLastUse() && !conn.validate()) {
                removeConnection(conn);
            }
        }
    }

    /**
     * Closes all connections, but also stops the reaper thread.
     */
    public synchronized void close() {
        while (connections.size() > 0) {
            removeConnection(connections.get(0));
        }
        reaper = null;
    }

    private synchronized void removeConnection(PooledConnection conn) {
        connections.remove(conn);
    }


    public synchronized PooledConnection getConnection() throws SQLException {

        for (PooledConnection conn: connections) {
            if (conn.lease()) {
                return conn;
            }
        }

        // Create a new connection
        LOG.info("Creating new connection to " + dbName + " for " + user + ":" + password);
        Connection conn = DriverManager.getConnection(ConnectionController.getConnectionString(dbName), user, password);
        PooledConnection pooledConn = new PooledConnection(conn, this);
        pooledConn.lease();
        connections.add(pooledConn);
        return pooledConn;
    } 

    public synchronized void returnConnection(PooledConnection conn) {
        conn.expireLease();
    }

    public String getDBName() {
        return dbName;
    }

    /**
     * Change the database name.  As a side-effect, this updates the URL and closes the connections.
     * @param db the new database name
     */
    public synchronized void setDBName(String db) {
        if (!db.equals(dbName)) {
            for (PooledConnection c: connections) {
                returnConnection(c);
            }
            LOG.info("Returned connections for " + dbName + ", new database will be " + db);
            dbName = db;
        }
    }

    public String getUser() {
        return user;
    }

    private class ConnectionReaper extends Thread {

        private static final long DELAY = 30000;

        @Override
        public void run() {
            while (reaper != null) {
                try {
                    sleep(DELAY);
                } catch (InterruptedException e) {
                }
                reapConnections();
            }
        }
    }
}

