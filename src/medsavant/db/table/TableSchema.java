/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.db.table;

import medsavant.exception.FatalDatabaseException;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mfiume
 */
public class TableSchema {

    private final LinkedHashMap<String,String> dbNameToAlias;
    private final LinkedHashMap<Integer,String> indexToDBName;
    private final LinkedHashMap<Integer,ColumnType> indexToColumnType;
    private final LinkedHashMap<DbColumn,Integer> columnToIndex;
    private final LinkedHashMap<String,DbColumn> aliasToColumn;
    private final LinkedHashMap<Integer,DbColumn> indexToColumn;
    private final LinkedHashMap<String,Integer> dbNameToIndex;
    private final LinkedHashMap<String, String> aliasToDBName;



    private static String getColumnTypeString(ColumnType t) {
        switch (t) {
            case INTEGER:
                return COLUMN_INTEGER;
            case FLOAT:
                return COLUMN_FLOAT;
            case BOOLEAN:
                return COLUMN_BOOLEAN;
            case VARCHAR:
                return COLUMN_VARCHAR;
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

    public DbColumn getDBColumn(String alias) {
        assert (aliasToColumn.containsKey(alias));
        return aliasToColumn.get(alias);
    }

    public int getFieldIndexInDB(String dbName) {
        assert (dbNameToIndex.containsKey(dbName));
        return dbNameToIndex.get(dbName);
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
        return ColumnType.FLOAT == columnType || ColumnType.INTEGER == columnType;
    }

    public enum ColumnType {
        VARCHAR,
        BOOLEAN,
        INTEGER,
        FLOAT
    };

    public static final String COLUMN_VARCHAR = "varchar";
    public static final String COLUMN_BOOLEAN = "bool";
    public static final String COLUMN_INTEGER = "int";
    public static final String COLUMN_FLOAT = "float";
    
    /*
    public static List<ColumnType> mapColumnsToEnums(List<DbColumn> columns) throws MedSavantDBException {
        List<ColumnType> enums = new ArrayList<ColumnType>();
        for (DbColumn c : columns) {
            enums.add(mapColumnTypeStringToEnum(c.getTypeNameSQL()));
        }
        return enums;
    }
     */

    /*
    public static List<ColumnType> mapColumnsToEnums(List<DbColumn> columns) throws FatalDatabaseException {
        List<ColumnType> enums = new ArrayList<ColumnType>();
        for (DbColumn c : columns) {
            enums.add(mapColumnTypeStringToEnum(c.getTypeNameSQL()));
        }
        return enums;
    }

    public static ColumnType mapColumnTypeStringToEnum(String c) throws FatalDatabaseException {
        if (c.equals(COLUMN_VARCHAR)) { return ColumnType.VARCHAR; }
        if (c.equals(COLUMN_BOOLEAN)) { return ColumnType.BOOLEAN; }
        if (c.equals(COLUMN_INTEGER)) { return ColumnType.INTEGER; }
        if (c.equals(COLUMN_FLOAT)) { return ColumnType.FLOAT; }
        throw new FatalDatabaseException("Unrecognized column type " + c);
    }
     *
     */

    private DbTable table;
    //private Map<String,DbColumn> columnMap;

    public TableSchema(DbTable t) {
        this.table = t;
        //columnMap = new LinkedHashMap<String,DbColumn>(); // preserves order
        
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

    /*
    public DbColumn getColumn(String colname) {
        if (!columnMap.containsKey(colname)) {
            throw new FatalDatabaseException("Table" + this.getTable().getTableNameSQL() + " does not contain field " + colname);
        }
        return columnMap.get(colname);
    }
     * 
     */

    public List<DbColumn> getColumns() {
        // IMPORTANT: this assumes the values returned are in order of insert (LinkedHashMap)
        return new ArrayList<DbColumn>(indexToColumn.values());
    }

    /*
    public void addColumn(String name, String typeName, int length) {
        DbColumn c = table.addColumn(name, typeName, length);
        columnMap.put(name, c);
    }
     *
     */
}
