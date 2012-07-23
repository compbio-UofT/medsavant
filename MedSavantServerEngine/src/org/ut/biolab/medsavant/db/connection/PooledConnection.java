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

import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.sql.*;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author Andrew
 */
public class PooledConnection implements Connection {
    private static Log LOG = LogFactory.getLog(PooledConnection.class);

    private ConnectionPool pool;
    private Connection conn;
    private boolean inUse;
    private long timestamp;

    public PooledConnection(Connection conn, ConnectionPool pool){
        this.conn = conn;
        this.pool = pool;
        this.inUse = false;
        this.timestamp = 0;
    }

    public synchronized boolean lease() {
       if (inUse) {
           return false;
       } else {
          inUse = true;
          timestamp = System.currentTimeMillis();
          return true;
       }
    }

    public boolean validate() {
	try {
            conn.getMetaData();
        } catch (Exception e) {
	    return false;
	}
	return true;
    }

    public boolean inUse() {
        return inUse;
    }

    public long getLastUse() {
        return timestamp;
    }

    protected void expireLease() {
        inUse = false;
    }

    @Override
    public void close() throws SQLException {
        pool.returnConnection(this);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }

    public int getNetworkTimeout() { return 0; }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return conn.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return conn.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        conn.commit();
    }

    @Override
    public void rollback() throws SQLException {
        conn.rollback();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return conn.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return conn.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        conn.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        conn.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return conn.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        conn.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return conn.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        conn.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return conn.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        conn.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        conn.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return conn.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return conn.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return conn.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return conn.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return conn.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return conn.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return conn.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return conn.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return conn.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return conn.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        conn.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        conn.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return conn.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return conn.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return conn.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return conn.createStruct(typeName, attributes);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return conn.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return conn.isWrapperFor(iface);
    }

    public ResultSet executeQuery(String query) throws SQLException {
        LOG.debug(query);
        return createStatement().executeQuery(query);
    }

    public void executeUpdate(String query) throws SQLException {
        LOG.debug(query);
        createStatement().executeUpdate(query);
    }

    public void executeUpdate(String query, Object... args) throws SQLException {
        executeUpdate(String.format(query, args));
    }

    /**
     * Utility method to make it easier to execute SELECT-style queries.
     *
     * @param query a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @return a ResultSet containing the results of the query
     * @throws SQLException
     */
    public ResultSet executePreparedQuery(PreparedStatement stmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            stmt.setObject(i + 1, args[i]);
        }
        return stmt.executeQuery();
    }

    /**
     * Utility method to make it easier to execute SELECT-style queries.
     *
     * @param query a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @return a ResultSet containing the results of the query
     * @throws SQLException
     */
    public ResultSet executePreparedQuery(String query, Object... args) throws SQLException {
        LOG.debug(query);
        return executePreparedQuery(conn.prepareStatement(query), args);
    }

    /**
     * Utility method to make it easier to execute SELECT-style queries.
     *
     * @param query a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @return a ResultSet containing the results of the query
     * @throws SQLException
     */
    public ResultSet executePreparedQuery(SelectQuery query, Object... args) throws SQLException {
        return executePreparedQuery(query.toString(), args);
    }

    /**
     * Utility method to make it easier to execute data-manipulation calls which don't
     * return a result.
     *
     * @param query a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @throws SQLException
     */
    public void executePreparedUpdate(PreparedStatement stmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            stmt.setObject(i + 1, args[i]);
        }
        stmt.executeUpdate();
    }
    /**
     * Utility method to make it easier to execute data-manipulation calls which don't
     * return a result.
     *
     * @param query a query, possibly containing '?' placeholder elements
     * @param args arguments for the placeholders
     * @throws SQLException
     */
    public void executePreparedUpdate(String query, Object... args) throws SQLException {
        LOG.debug(query);
        executePreparedUpdate(conn.prepareStatement(query), args);
    }

    public boolean tableExists(String tableName) throws SQLException {
        return executeQuery(String.format("SHOW TABLES LIKE '%s'", tableName)).next();
    }
}
