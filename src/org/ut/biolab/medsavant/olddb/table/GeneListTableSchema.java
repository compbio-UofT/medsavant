/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author mfiume
 */
public class GeneListTableSchema extends TableSchema {

    public static final String TABLE_NAME = "region_set";
   
    public static int INDEX_ID;
    public static int INDEX_NAME;

    private void setIndexes() {
        INDEX_ID = this.getFieldIndexInDB(DBFIELDNAME_ID);
        INDEX_NAME = this.getFieldIndexInDB(DBFIELDNAME_NAME);
    }

    public GeneListTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
   
    
    public static final String ALIAS_ID = "Region Set ID";
    public static final String ALIAS_NAME = "Region Set Name";
    
    public static final String DBFIELDNAME_ID = "regionset_id";
    public static final String DBFIELDNAME_NAME = "name";
 
    private void addColumns() {
        addColumn(DBFIELDNAME_ID,ALIAS_ID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_NAME,ALIAS_NAME,TableSchema.ColumnType.VARCHAR,10);
    }
    
    
}
