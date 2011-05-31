/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mfiume
 */
public class TableSchema {

    private DbTable table;
    private Map<String,DbColumn> columnMap;

    public TableSchema(DbTable t) {
        this.table = t;
        columnMap = new HashMap<String,DbColumn>();
    }

    public DbTable getTable() {
        return table;
    }

    public DbColumn getColumn(String colname) {
        return columnMap.get(colname);
    }

    public void addColumn(String name, String typeName, int length) {
        DbColumn c = table.addColumn(name, typeName, length);
        columnMap.put(name, c);
    }

}
