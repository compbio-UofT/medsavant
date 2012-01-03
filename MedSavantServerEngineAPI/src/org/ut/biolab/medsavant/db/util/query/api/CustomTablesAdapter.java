package org.ut.biolab.medsavant.db.util.query.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;


/**
 *
 * @author mfiume
 */
public interface CustomTablesAdapter extends Remote {

    public TableSchema getCustomTableSchema(String sessionId, String tablename) throws SQLException, RemoteException;
    public TableSchema getCustomTableSchema(String sessionId, String tablename, boolean update) throws SQLException, RemoteException;
    public void clearMap(String sessionId) throws RemoteException ;


}
