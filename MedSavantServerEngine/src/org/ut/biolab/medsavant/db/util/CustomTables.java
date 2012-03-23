/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.api.CustomTablesAdapter;
import org.ut.biolab.medsavant.db.util.shared.MedSavantServerUnicastRemoteObject;

/**
 *
 * @author Andrew
 */
public class CustomTables extends MedSavantServerUnicastRemoteObject implements CustomTablesAdapter {

    private static CustomTables instance;
    private static final int MAX_TABLES = 30;

    private Map<String, Map<String, TableSchema>> dbnameToTableMap = new HashMap<String, Map<String, TableSchema>>();

    public static synchronized CustomTables getInstance() throws RemoteException {
        if (instance == null) {
            instance = new CustomTables();
        }
        return instance;
    }

    private CustomTables() throws RemoteException {super();}

    public TableSchema getCustomTableSchema(String sid, String tablename) throws SQLException, RemoteException {
        return getCustomTableSchema(sid, tablename, false);
    }

    public synchronized TableSchema getCustomTableSchema(String sid, String tablename, boolean update) throws SQLException, RemoteException {

        String dbName = ConnectionController.getDBName(sid);
        if (!dbnameToTableMap.containsKey(dbName)) {
            dbnameToTableMap.put(dbName, new HashMap<String, TableSchema>());
        }
        if(!dbnameToTableMap.get(dbName).containsKey(tablename) || update) {
            if(!dbnameToTableMap.get(dbName).containsKey(tablename) && isOverLimit()){
                clearMap();
            }
            dbnameToTableMap.get(dbName).put(tablename, DBUtil.getInstance().importTableSchema(sid, tablename));
        }

        return dbnameToTableMap.get(dbName).get(tablename);
    }

    private boolean isOverLimit() {

        //number of dbs
        if(dbnameToTableMap.size() >= MAX_TABLES) return true;

        //number of tables
        int size = 0;
        for(String dbName : dbnameToTableMap.keySet()){
            size += dbnameToTableMap.get(dbName).size();
        }
        if(size >= MAX_TABLES) return true;

        return false;
    }

    private void clearMap() {
        dbnameToTableMap.clear();
    }

}
