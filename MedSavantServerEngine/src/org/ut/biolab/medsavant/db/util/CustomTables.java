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

/**
 *
 * @author Andrew
 */
public class CustomTables extends java.rmi.server.UnicastRemoteObject implements CustomTablesAdapter {

    private static CustomTables instance;

    private Map<String, Map> sessionIdToMapMap = new HashMap<String,Map>();

    public static CustomTables getInstance() throws RemoteException {
        if (instance == null) {
            instance = new CustomTables();
        }
        return instance;
    }

    private CustomTables() throws RemoteException {}

    public TableSchema getCustomTableSchema(String sessionId, String tablename) throws SQLException, RemoteException {
        return getCustomTableSchema(sessionId, tablename, false);
    }

    public TableSchema getCustomTableSchema(String sessionId, String tablename, boolean update) throws SQLException, RemoteException {

        if (!sessionIdToMapMap.containsKey(sessionId)) {
            Map<String, TableSchema> tableNameToSchemaMap = new HashMap<String, TableSchema>();
            sessionIdToMapMap.put(sessionId, tableNameToSchemaMap);
        }

        Map<String, TableSchema> tableNameToSchemaMap = sessionIdToMapMap.get(sessionId);

        TableSchema table;
        if(!update && (table = tableNameToSchemaMap.get(tablename)) != null){
            return table;
        }

        table = DBUtil.getInstance().importTableSchema(sessionId, tablename);
        tableNameToSchemaMap.put(tablename, table);

        sessionIdToMapMap.put(sessionId, tableNameToSchemaMap);

        return table;
    }

    public void clearMap(String sessionId) {

        Map m = sessionIdToMapMap.get(sessionId);
        if (m != null) {
            m.clear();
        }
    }

}
