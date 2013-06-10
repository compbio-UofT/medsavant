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

package org.ut.biolab.medsavant.shared.db;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;


/**
 *
 * @author mfiume
 */
public class TableSchema implements Serializable {
    private static final Log LOG = LogFactory.getLog(TableSchema.class);

    private final LinkedHashMap<String, DbColumn> nameToColumn;

    private final DbTable table;
    protected SelectQuery selectQuery;
    private List<DbColumn> autoIncrements;

    public TableSchema(DbTable t) {
        this.table = t;

        nameToColumn = new LinkedHashMap<String, DbColumn>();
    }

    /**
     * Define a table-scheme using an interface class which provides all the column defs as members.
     * @param s the database schema
     * @param name name for the new table
     * @param columnsClass an interface which defines the column defs as static members
     */
    public TableSchema(DbSchema s, String name, Class columnsClass) {
        this(s.addTable(name));
        autoIncrements = new ArrayList<DbColumn>();
        Field[] fields = columnsClass.getDeclaredFields();
        for (Field f: fields) {
            try {
                Object fieldValue = f.get(null);
                if (fieldValue instanceof ColumnDef) {
                    addColumn((ColumnDef)fieldValue);
                }
            } catch (Exception ex) {
                LOG.error("Unable to get column definition for " + f, ex);
            }
        }
    }

    /**
     * Define a table-schema using a list of column-defs.
     * @param s the database schema
     * @param name name for the new table
     * @param cols a list of column-defs.
     */
    public TableSchema(DbSchema s, String name, ColumnDef[] cols) {
        this(s.addTable(name));
        autoIncrements = new ArrayList<DbColumn>();
        for (ColumnDef c: cols) {
            addColumn(c);
        }
    }

    public final DbColumn addColumn(String dbName, ColumnType t, int length) {
        DbColumn c = table.addColumn(dbName, t.toString(), length);
        nameToColumn.put(dbName, c);
        return c;
    }

    public final DbColumn addColumn(ColumnDef col) {
        DbColumn dbc = addColumn(col.name, col.type, col.length);
        if (col.defaultValue != null) {
            dbc.setDefaultValue(col.defaultValue);
        }
        if (col.autoIncrement) {
            autoIncrements.add(dbc);
        }
        if (col.nonNull) {
            dbc.notNull();
        }
        if (col.primaryKey) {
            dbc.primaryKey();
        }
        return dbc;
    }

    public int getNumFields() {
        return nameToColumn.size();
    }

    public DbColumn getDBColumn(String name) {
        return nameToColumn.get(name);
    }

    public DbColumn getDBColumn(ColumnDef def) {
        return getDBColumn(def.name);
    }

    public List<DbColumn> getColumns() {
        // IMPORTANT: this assumes the values returned are in order of insert (LinkedHashMap)
        return new ArrayList<DbColumn>(nameToColumn.values());
    }

    public DbTable getTable() {
        return table;
    }

    public String getTableName() {
        return table.getName();
    }

    public CreateTableQuery getCreateQuery() {
        CreateTableQuery query = new CreateTableQuery(table, true);
        for (DbColumn col: autoIncrements) {
            query.addColumnConstraint(col, "AUTO_INCREMENT");
        }
        return query;
    }

    /**
     * Create a query object for retrieving the given columns.  The query can be modified using the where() and distinct() modifiers.
     *
     * @param cols list of columns whose values are to be fetched
     * @return a selection query
     */
    public synchronized SelectQuery select(Object... cols) {
        if (selectQuery == null) {
            selectQuery = new SelectQuery(false);
            selectQuery.addFromTable(table);
        }
        for (Object o: cols) {
            if (o instanceof ColumnDef) {
                selectQuery.addColumns(table.findColumn(((ColumnDef)o).name));
            } else {
                selectQuery.addCustomColumns(new CustomSql(o));
            }
        }
        SelectQuery result = selectQuery;
        selectQuery = null;
        return result;
    }

    /**
     * Create a query object which will match the given values of the given columns.
     *
     * @param wheres pairs consisting of column def followed by value
     * @return <code>this</code>
     */
    public synchronized TableSchema where(Object... wheres) {
        if (selectQuery == null) {
            selectQuery = new SelectQuery(false);
            selectQuery.addFromTable(table);
        }
        for (int i = 0; i < wheres.length; i += 2) {
            selectQuery.addCondition(BinaryConditionMS.equalTo(table.findColumn(((ColumnDef)wheres[i]).name), wheres[i + 1]));
        }
        return this;
    }

    /**
     * Create a query object which will use the <code>IN</code> keyword to group all elements.
     *
     * @param ins collection of values for the <code>IN</code> clause
     * @return <code>this</code>
     */
    public synchronized TableSchema whereIn(ColumnDef col, Collection ins) {
        if (selectQuery == null) {
            selectQuery = new SelectQuery(false);
            selectQuery.addFromTable(table);
        }
        selectQuery.addCondition(new InCondition(table.findColumn(col.name), ins));
        return this;
    }

    /**
     * Make the query only fetch unique values.
     */
    public synchronized TableSchema distinct() {
        if (selectQuery != null) {
            selectQuery.setIsDistinct(true);
        } else {
            selectQuery = new SelectQuery(true);
            selectQuery.addFromTable(table);
        }
        return this;
    }

    /**
     * Add an ORDER BY clause to the query.
     */
    public synchronized TableSchema orderBy(ColumnDef... groupCols) {
        if (selectQuery == null) {
            selectQuery = new SelectQuery(false);
            selectQuery.addFromTable(table);
        }
        for (ColumnDef col: groupCols) {
            selectQuery.addOrdering(table.findColumn(col.name), Dir.ASCENDING);
        }
        return this;
    }

    /**
     * Add a GROUP BY clause to the query
     */
    public synchronized TableSchema groupBy(ColumnDef... groupCols) {
        if (selectQuery == null) {
            selectQuery = new SelectQuery(false);
            selectQuery.addFromTable(table);
        }
        for (ColumnDef col: groupCols) {
            selectQuery.addGroupings(table.findColumn(col.name));
        }
        return this;
    }

    /**
     * Add a LEFT JOIN clause to the query with a USING subclause.
     */
    public synchronized TableSchema leftJoin(TableSchema t, String colName) {
        if (selectQuery == null) {
            selectQuery = new SelectQuery(false);
            selectQuery.addFromTable(table);
        }
        selectQuery.addCustomJoin(String.format(" LEFT JOIN %s USING (%s)", t.getTableName(), colName));
        return this;
    }



    /**
     * Create a query object for inserting the given data.
     *
     * @param insertions pairs consisting of column def followed by value
     * @return an insert query
     */
    public synchronized InsertQuery insert(Object... insertions) {
        InsertQuery query = new InsertQuery(table);
        for (int i = 0; i < insertions.length; i += 2) {
            query.addColumn(table.findColumn(((ColumnDef)insertions[i]).name), insertions[i + 1]);
        }
        return query;
    }

    /**
     * Create a query object for inserting the given data.
     *
     * @param insertions pairs consisting of column def followed by value
     * @return an insert query
     */
    public synchronized InsertQuery preparedInsert(ColumnDef... cols) {
        InsertQuery query = new InsertQuery(table);
        for (int i = 0; i < cols.length; i++) {
            query.addPreparedColumns(table.findColumn(cols[i].name));
        }
        return query;
    }

    public synchronized DeleteQuery delete(Object... wheres) {
        DeleteQuery query = new DeleteQuery(table);
        for (int i = 0; i < wheres.length; i += 2) {
            query.addCondition(BinaryConditionMS.equalTo(table.findColumn(((ColumnDef)wheres[i]).name), wheres[i + 1]));
        }
        return query;
    }
}
