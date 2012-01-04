/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query.api;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 *
 * @author Andrew
 */
public interface DBUtilAdapter extends Remote {
    
    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, RemoteException;
    //public DbTable importTable(String sessionId, String tablename) throws SQLException, RemoteException;
    
}
