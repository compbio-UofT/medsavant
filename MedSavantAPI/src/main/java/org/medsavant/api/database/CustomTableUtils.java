/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.ut.biolab.medsavant.shared.db.TableSchema;

/**
 *
 * @author jim
 */
public class CustomTableUtils {    
    private static final int MAX_TABLES = 30;
    private static CustomTableUtils instance = null;
    private Map<String, Map<String, TableSchema>> dbnameToTableMap = new HashMap<String, Map<String, TableSchema>>();
                  
    private CustomTableUtils(){
        
    }
    
    public static CustomTableUtils getInstance(){
        if(instance == null){
            instance = new CustomTableUtils();
        }
        return instance;
    }
       
    public TableSchema getCustomTableSchema(Connection conn, String dbName, String tablename) throws SQLException{
        return getCustomTableSchema(conn, dbName, tablename, true);
    }
            
    public synchronized TableSchema getCustomTableSchema(Connection conn, String dbName, String tablename, boolean update) throws SQLException{
        
        if (!dbnameToTableMap.containsKey(dbName)) {
            dbnameToTableMap.put(dbName, new HashMap<String, TableSchema>());
        }
        if(!dbnameToTableMap.get(dbName).containsKey(tablename) || update) {
            if(!dbnameToTableMap.get(dbName).containsKey(tablename) && isOverLimit()){
                clearMap();
            }            
            dbnameToTableMap.get(dbName).put(tablename, MedSavantDBUtils.importTableSchema(conn, tablename));
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
