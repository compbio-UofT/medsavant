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

/**
 *
 * @author Andrew
 */
public class CustomTables {

    private static Map<String, TableSchema> map = new HashMap<String, TableSchema>();

    public static TableSchema getCustomTableSchema(String sessionId, String tablename) throws SQLException, RemoteException {
        return getCustomTableSchema(sessionId, tablename, false);
    }
    
    public static TableSchema getCustomTableSchema(String sessionId, String tablename, boolean update) throws SQLException, RemoteException {

        TableSchema table;
        if(!update && (table = map.get(tablename)) != null){
            return table;
        }

        table = DBUtil.getInstance().importTableSchema(sessionId, tablename);
        map.put(tablename, table);
        return table;
    }

    public static void clearMap(){
        map.clear();
    }

}
