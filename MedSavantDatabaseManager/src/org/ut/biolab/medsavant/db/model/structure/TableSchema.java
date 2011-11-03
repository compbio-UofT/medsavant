/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.db.model.structure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import org.ut.biolab.medsavant.db.api.TableSchemaAdapter;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;

/**
 *
 * @author mfiume
 */
public class TableSchema implements TableSchemaAdapter {

    public static ColumnType convertStringToColumnType(String typeNameSQL) {
        typeNameSQL = typeNameSQL.toLowerCase();
        if (typeNameSQL.contains("float")) {
            return ColumnType.FLOAT;
        } else if (typeNameSQL.contains("int")) {
            return ColumnType.INTEGER;
        } else if (typeNameSQL.contains("varchar")) {
            return ColumnType.VARCHAR;
        } else if (typeNameSQL.contains("tinyint")) {
            return ColumnType.INTEGER;
        } else if (typeNameSQL.contains("blob")) {
            return ColumnType.VARCHAR;
        } else if (typeNameSQL.contains("datetime")) {
            return ColumnType.DATE;
        } else if(typeNameSQL.contains("decimal")){
            return ColumnType.DECIMAL;
        } else if (typeNameSQL.contains("date")){
            return ColumnType.DATE;
        }
        
        throw new UnsupportedOperationException("Type not supported: " + typeNameSQL);
    }

    private final LinkedHashMap<String,String> dbNameToAlias;
    private final LinkedHashMap<Integer,String> indexToDBName;
    private final LinkedHashMap<Integer,ColumnType> indexToColumnType;
    private final LinkedHashMap<DbColumn,Integer> columnToIndex;
    private final LinkedHashMap<String,DbColumn> aliasToColumn;
    private final LinkedHashMap<Integer,DbColumn> indexToColumn;
    private final LinkedHashMap<String,Integer> dbNameToIndex;
    private final LinkedHashMap<String, String> aliasToDBName;

    public static String getColumnTypeString(ColumnType t) {
        switch (t) {
            case INTEGER:
                return COLUMN_INTEGER;
            case FLOAT:
                return COLUMN_FLOAT;
            case BOOLEAN:
                return COLUMN_BOOLEAN;
            case VARCHAR:
                return COLUMN_VARCHAR;
            case DECIMAL:
                return COLUMN_DECIMAL;
            case DATE:
                return COLUMN_DATE;
            default:
                throw new FatalDatabaseException("Unrecognized column type " + t);
        }
    }

    private void addColumn(int index, String dbName, String alias, ColumnType t, int length) {
        assert (!indexToDBName.containsKey(index));
        DbColumn c = table.addColumn(dbName, getColumnTypeString(t), length);
        dbNameToAlias.put(dbName,alias);
        indexToDBName.put(index, dbName);
        aliasToColumn.put(alias, c);
        columnToIndex.put(c,index);
        indexToColumnType.put(index, t);
        indexToColumn.put(index, c);
        dbNameToIndex.put(dbName, index);
        aliasToDBName.put(alias,dbName);
    }

    public void addColumn(String dbName, String alias, ColumnType t, int length) {
        addColumn(getNumFields()+1,dbName,alias,t,length);
    }

    public List<String> getFieldAliases() {
        return new ArrayList<String>(this.aliasToDBName.keySet());
    }

    public int getNumFields() {
        return indexToDBName.keySet().size();
    }

    public ColumnType getColumnType(int index) {
        assert (indexToColumnType.containsKey(index));
        return indexToColumnType.get(index);
    }

    public int getColumnIndex(DbColumn c) {
        assert (columnToIndex.containsKey(c));
        return columnToIndex.get(c);
    }

    public ColumnType getColumnType(DbColumn c) {
        return this.getColumnType(this.getColumnIndex(c));
    }

    public DbColumn getDBColumnByAlias(String alias) {
        assert (aliasToColumn.containsKey(alias));
        return aliasToColumn.get(alias);
    }
    
    public DbColumn getDBColumn(String columnname) {
        assert (dbNameToAlias.containsKey(columnname));
        return getDBColumnByAlias(dbNameToAlias.get(columnname));
    }
    
    public int getFieldIndexInDB(String dbName) {
        assert (dbNameToIndex.containsKey(dbName));
        return dbNameToIndex.get(dbName);
    }
    
    public int getFieldIndexByAlias(String alias) {
        return getFieldIndexInDB(getDBName(alias));
    }

    public String getFieldAlias(String dbName) {
        assert (dbNameToAlias.containsKey(dbName));
        return dbNameToAlias.get(dbName);
    }

    public String getDBName(String alias) {
        assert (aliasToDBName.containsKey(alias));
        return aliasToDBName.get(alias);
    }

    public String getDBName(int index) {
        assert (indexToDBName.containsKey(index));
        return indexToDBName.get(index);
    }

    public Object[][] getColumnGrid() {

        Object[][] result = new Object[this.indexToColumn.keySet().size()][3];

        for (int index : this.indexToColumn.keySet()) {
            result[index-1][0] = index;
            result[index-1][1] = this.indexToColumn.get(index);
            result[index-1][2] = this.indexToColumnType.get(index);
        }

        return result;
    }
    
    public static boolean isNumeric(ColumnType columnType) {
        return ColumnType.INTEGER == columnType || ColumnType.FLOAT == columnType || ColumnType.DECIMAL == columnType;
    }

    public static boolean isInt(ColumnType columnType) {
        return ColumnType.INTEGER == columnType;
    }
    
    public static boolean isFloat(ColumnType columnType) {
        return ColumnType.FLOAT == columnType || ColumnType.DECIMAL == columnType;        
    }

    public static boolean isBoolean(ColumnType columnType) {
        return ColumnType.BOOLEAN == columnType;
    }

    public enum ColumnType {
        VARCHAR,
        BOOLEAN,
        INTEGER,
        FLOAT,
        DECIMAL,
        DATE
    };

    public static final String COLUMN_VARCHAR = "varchar";
    public static final String COLUMN_BOOLEAN = "bool";
    public static final String COLUMN_INTEGER = "int";
    public static final String COLUMN_FLOAT = "float";
    public static final String COLUMN_DECIMAL = "decimal";
    public static final String COLUMN_DATE = "date";
    
    private DbTable table;

    public TableSchema(DbTable t) {
        this.table = t;
        
        dbNameToAlias = new LinkedHashMap<String,String>();
        indexToDBName = new LinkedHashMap<Integer,String>();
        indexToColumnType = new LinkedHashMap<Integer,ColumnType>();
        columnToIndex = new LinkedHashMap<DbColumn,Integer>();
        aliasToColumn = new LinkedHashMap<String,DbColumn>();
        indexToColumn = new LinkedHashMap<Integer,DbColumn>();
        dbNameToIndex = new LinkedHashMap<String,Integer>();
        aliasToDBName = new LinkedHashMap<String,String>();
    }

    public DbTable getTable() {
        return table;
    }

    public List<DbColumn> getColumns() {
        // IMPORTANT: this assumes the values returned are in order of insert (LinkedHashMap)
        return new ArrayList<DbColumn>(indexToColumn.values());
    }
    
    public String getTablename() {
        return table.getName();
    }
}
