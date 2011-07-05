/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author AndrewBrook
 */
public class GenomeTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "genome";
   
    public static int INDEX_GENOMEID;
    public static int INDEX_SPECIES;
    public static int INDEX_VERSION;
    public static int INDEX_BAMPATH;
    
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_SPECIES = "Species";
    public static final String ALIAS_VERSION = "Version";
    public static final String ALIAS_BAMPATH = "BAM Path";

    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_SPECIES = "species";
    public static final String DBFIELDNAME_VERSION = "version";
    public static final String DBFIELDNAME_BAMPATH = "bam_path";

    public GenomeTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
    
    private void setIndexes() {
        INDEX_GENOMEID = this.getFieldIndexInDB(DBFIELDNAME_GENOMEID);
        INDEX_SPECIES = this.getFieldIndexInDB(DBFIELDNAME_SPECIES);
        INDEX_VERSION = this.getFieldIndexInDB(DBFIELDNAME_VERSION);
        INDEX_BAMPATH = this.getFieldIndexInDB(DBFIELDNAME_BAMPATH);
    }
 
    private void addColumns() {
        addColumn(DBFIELDNAME_GENOMEID,ALIAS_GENOMEID,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_SPECIES,ALIAS_SPECIES,TableSchema.ColumnType.INTEGER,50);
        addColumn(DBFIELDNAME_VERSION,ALIAS_VERSION,TableSchema.ColumnType.VARCHAR,50);
        addColumn(DBFIELDNAME_BAMPATH,ALIAS_BAMPATH,TableSchema.ColumnType.VARCHAR,200);
    }
    
}
