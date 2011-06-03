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

    public static String mapColumnTypeEnumToString(ColumnType c) throws FatalDatabaseException {
        switch (c) {
            case VARCHAR:
               return COLUMN_VARCHAR;
            case BOOLEAN:
                return COLUMN_BOOLEAN;
            case INTEGER:
                return COLUMN_INTEGER;
            case FLOAT:
                return COLUMN_FLOAT;
            default:
                throw new FatalDatabaseException("Unrecognized column type " + c);
        }
    }

    /*
    public static List<ColumnType> mapColumnsToEnums(List<DbColumn> columns) throws MedSavantDBException {
        List<ColumnType> enums = new ArrayList<ColumnType>();
        for (DbColumn c : columns) {
            enums.add(mapColumnTypeStringToEnum(c.getTypeNameSQL()));
        }
        return enums;
    }
     */

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

    private DbTable table;
    private Map<String,DbColumn> columnMap;

    public TableSchema(DbTable t) {
        this.table = t;
        columnMap = new LinkedHashMap<String,DbColumn>(); // preserves order
    }

    public DbTable getTable() {
        return table;
    }

    public DbColumn getColumn(String colname) {
        return columnMap.get(colname);
    }

    public List<DbColumn> getColumns() {
        return new ArrayList<DbColumn>(columnMap.values());
    }

    public void addColumn(String name, String typeName, int length) {
        DbColumn c = table.addColumn(name, typeName, length);
        columnMap.put(name, c);
    }
}
