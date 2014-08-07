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
package org.medsavant.api.database;

import java.sql.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Based on JDC Connection Pool example.
 *
 * @author Andrew
 */
public class MedSavantJDBCConnectionPool {

    private static final Log LOG = LogFactory.getLog(MedSavantJDBCConnectionPool.class);
    private static final long TIMEOUT = 60000;

    private MedSavantJDBCDatabase databaseProvider;
    private final List<MedSavantJDBCPooledConnection> connections;
    private final String user, password;
    private String dbName;
    private ConnectionReaper reaper;

    public MedSavantJDBCConnectionPool(MedSavantJDBCDatabase mjd, String db, String user, String password) {
        this.dbName = db;
        this.user = user;
        this.password = password;
        this.databaseProvider = mjd;
        connections = new ArrayList<MedSavantJDBCPooledConnection>();
        reaper = new ConnectionReaper();
        reaper.start();
    }

    public synchronized void reapConnections() {

        long stale = System.currentTimeMillis() - TIMEOUT;

        for (MedSavantJDBCPooledConnection conn: connections) {
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

    private synchronized void removeConnection(MedSavantJDBCPooledConnection conn) {
        connections.remove(conn);
    }


    public synchronized MedSavantJDBCPooledConnection getConnection() throws SQLException {

        for (MedSavantJDBCPooledConnection conn: connections) {
            if (conn.lease()) {
                return conn;
            }
        }

        // Create a new connection
        LOG.info(connections.size() + " connections opened, all in use. Creating new connection to " + dbName + " for " + user);// + ":" + password);
        Connection conn = DriverManager.getConnection(databaseProvider.getConnectionString(dbName), user, password);
        MedSavantJDBCPooledConnection pooledConn = new MedSavantJDBCPooledConnection(conn, this);
        pooledConn.lease();
        connections.add(pooledConn);
        return pooledConn;
    }

    public synchronized void returnConnection(MedSavantJDBCPooledConnection conn) {
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
            for (MedSavantJDBCPooledConnection c: connections) {
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

