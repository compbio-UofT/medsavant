/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model.structure;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.ut.biolab.medsavant.db.util.DBUtil;

/**
 *
 * @author Andrew
 */
public class CustomTables {
    
    private static Map<String, TableSchema> map = new HashMap<String, TableSchema>();

    public static TableSchema getCustomTableSchema(String tablename) throws SQLException {
        return getCustomTableSchema(tablename, false);
    }
    
    public static TableSchema getCustomTableSchema(String tablename, boolean update) throws SQLException {
        
        TableSchema table;
        if(!update && (table = map.get(tablename)) != null){
            return table;
        }
        
        table = DBUtil.importTableSchema(tablename);
        map.put(tablename, table);
        return table;
    }
    
    public static void clearMap(){
        map.clear();
    }
    
}
