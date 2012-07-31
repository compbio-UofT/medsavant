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
package org.ut.biolab.medsavant.db.util;

import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.serverapi.CustomTablesAdapter;

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

    @Override
    public TableSchema getCustomTableSchema(String sid, String tablename) throws SQLException, RemoteException {
        return getCustomTableSchema(sid, tablename, false);
    }

    @Override
    public synchronized TableSchema getCustomTableSchema(String sid, String tablename, boolean update) throws SQLException, RemoteException {

        String dbName = ConnectionController.getDBName(sid);
        if (!dbnameToTableMap.containsKey(dbName)) {
            dbnameToTableMap.put(dbName, new HashMap<String, TableSchema>());
        }
        if(!dbnameToTableMap.get(dbName).containsKey(tablename) || update) {
            if(!dbnameToTableMap.get(dbName).containsKey(tablename) && isOverLimit()){
                clearMap();
            }
            dbnameToTableMap.get(dbName).put(tablename, DBUtils.getInstance().importTableSchema(sid, tablename));
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
