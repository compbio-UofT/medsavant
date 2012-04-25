/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.connection;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.server.SessionController;

/**
 * 
 * Wrapper to allow universal catching of exceptions. 
 *
 * @author Andrew
 */
public class MSStatement implements Statement {
    
    private Statement s;
    
    public MSStatement(Statement statement){
        this.s = statement;
    }
    
    private Object handleException(Exception e) throws SQLException {
        
        if(e instanceof SQLException){
            throw (SQLException)e;
        }
      
        System.out.println("Communications Exception - Terminating active sessions - type: " + e.getClass());
        try {
            SessionController.getInstance().terminateAllSessions("MedSavant server cannot connect to the database. Please contact your administrator. ");
        } catch (RemoteException ex) {
            Logger.getLogger(MSStatement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            return s.executeQuery(sql);
        } catch (Exception ex){      
            return (ResultSet)handleException(ex);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        try {
            return s.executeUpdate(sql);
        }  catch (Exception ex){      
            return (Integer)handleException(ex);
        }
    }

    @Override
    public void close() throws SQLException {
        s.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return s.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        s.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return s.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        s.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        s.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return s.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        s.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        s.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return s.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        s.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        s.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        try {
            return s.execute(sql);
        }  catch (Exception ex){      
            return (Boolean)handleException(ex);
        }        
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return s.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return s.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return s.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        s.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return s.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        s.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return s.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return s.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return s.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        s.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        s.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return s.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return s.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return s.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return s.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            return s.executeUpdate(sql, autoGeneratedKeys);
        }  catch (Exception ex){      
            return (Integer)handleException(ex);
        }           
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try {
            return s.executeUpdate(sql, columnIndexes);
        }  catch (Exception ex){      
            return (Integer)handleException(ex);
        }   
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try {
            return s.executeUpdate(sql, columnNames);
        }  catch (Exception ex){      
            return (Integer)handleException(ex);
        }          
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            return s.execute(sql, autoGeneratedKeys);
        }  catch (Exception ex){      
            return (Boolean)handleException(ex);
        }          
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        try {
            return s.execute(sql, columnIndexes);
        }  catch (Exception ex){      
            return (Boolean)handleException(ex);
        }         
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        try {
            return s.execute(sql, columnNames);
        }  catch (Exception ex){      
            return (Boolean)handleException(ex);
        }           
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return s.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return s.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        s.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return s.isPoolable();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return s.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return s.isWrapperFor(iface);
    }
    
}
