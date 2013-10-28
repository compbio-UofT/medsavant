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
package org.ut.biolab.medsavant.server.db.util;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.CustomTablesAdapter;

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

    private CustomTables() throws RemoteException {
    }

    @Override
    public TableSchema getCustomTableSchema(String sid, String tablename) throws SQLException, RemoteException, SessionExpiredException {
        return getCustomTableSchema(sid, tablename, false);
    }

    @Override
    public synchronized TableSchema getCustomTableSchema(String sid, String tablename, boolean update) throws SQLException, RemoteException, SessionExpiredException {

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
