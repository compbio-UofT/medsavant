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
package org.ut.biolab.medsavant.shard.nonshard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.hibernate.shards.cfg.ShardConfiguration;
import org.ut.biolab.medsavant.shard.db.NonShardDBUtils;

/**
 * Thread executing a query on a shard.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardQueryExecutor implements Callable<Object> {

    private ShardConfiguration config;
    private String query = "";
    private QueryType qType;
    private static Connection conn = null;

    public ShardQueryExecutor(ShardConfiguration config, String query, QueryType qType) {
        this.config = config;
        this.query = query;
        this.qType = qType;
    }

    /**
     * Obtains a connection to the database and executes a query.
     * 
     * @return
     * 
     */
    public Object call() {
        Object res = null;

        switch (qType) {
        case SELECT:
            connect(true);
            res = executeSelect();
            break;
        case UPDATE:
            connect(true);
            executeUpdate();
            res = config.getShardId();
            break;
        case SELECT_WITHOUT_RESULT:
            connect(true);
            executeSelectWithoutResult();
            res = config.getShardId();
            break;
        case DB:
            // database does not exist yet, we don't want to connect to it
            connect(false);
            executeUpdate();
            res = config.getShardId();
            break;
        }

        disconnect();

        return res;
    }

    /**
     * Executes a query with return value.
     * 
     * @return result
     */
    private Object executeSelect() {
        Object res = null;

        PreparedStatement s = null;
        try {
            s = conn.prepareStatement(query);
        } catch (SQLException e) {
            System.err.println("Failed to create query.");
        }

        ResultSet r = null;
        try {
            r = s.executeQuery();

            // return first result
            r.next();
            res = r.getObject(0);
        } catch (SQLException e) {
            System.err.println("Failed to execute query.");
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close the statement.");
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (SQLException e) {
                    System.err.println("Resultset could not be closed.");
                }
            }
        }

        return res;
    }

    /**
     * Executes a query without waiting for the return value.
     * 
     */
    private void executeSelectWithoutResult() {
        PreparedStatement s = null;
        try {
            s = conn.prepareStatement(query);
        } catch (SQLException e) {
            System.err.println("Failed to create query.");
        }

        try {
            s.executeQuery();
        } catch (SQLException e) {
            System.err.println("Failed to execute query.");
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close the statement.");
                }
            }
        }
    }

    /**
     * Executes a query without a return value.
     * 
     */
    private void executeUpdate() {
        try {
            conn.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Failed to execute update.");
        }
    }

    /**
     * Obtains connection.
     */
    private void connect(boolean connectToDB) {
        try {
            Class.forName(NonShardDBUtils.JDBC_DRIVER);
            conn = DriverManager.getConnection(connectToDB ? config.getShardUrl() : ShardConfigurationUtil.getServerForShard(config.getShardId()), config.getShardUser(),
                    config.getShardPassword());
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not loaded: " + NonShardDBUtils.JDBC_DRIVER);
        } catch (SQLException e) {
            System.err.println("Error connecting to shard: " + config.getShardUrl());
        }
    }

    /**
     * Closes the connection.
     */
    private void disconnect() {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
