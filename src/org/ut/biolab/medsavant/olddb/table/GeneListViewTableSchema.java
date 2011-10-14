/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 *
 * @author mfiume
 */
public class GeneListViewTableSchema extends TableSchema {

    public static final String TABLE_NAME = "region_set_membership";
    
    public static int INDEX_REGIONSETID;
    public static int INDEX_GENOMEID;
    public static int INDEX_CHROM;
    public static int INDEX_START;
    public static int INDEX_END;
    public static int INDEX_DESCRIPTION;
    
    public static final String ALIAS_REGIONSETID = "Region Set ID";
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_START = "Start";
    public static final String ALIAS_END = "End";
    public static final String ALIAS_DESCRIPTION = "Description";
    
    public static final String DBFIELDNAME_REGIONSETID = "region_set_id";
    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_START = "start";
    public static final String DBFIELDNAME_END = "end";
    public static final String DBFIELDNAME_DESCRIPTION = "description";

    public GeneListViewTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }

    private void setIndexes() {
        INDEX_REGIONSETID = this.getFieldIndexInDB(DBFIELDNAME_REGIONSETID);
        INDEX_GENOMEID = this.getFieldIndexInDB(DBFIELDNAME_GENOMEID);
        INDEX_CHROM = this.getFieldIndexInDB(DBFIELDNAME_CHROM);
        INDEX_START = this.getFieldIndexInDB(DBFIELDNAME_START);
        INDEX_END = this.getFieldIndexInDB(DBFIELDNAME_END);
        INDEX_DESCRIPTION = this.getFieldIndexInDB(DBFIELDNAME_DESCRIPTION);
    }

    private void addColumns() {
        addColumn(DBFIELDNAME_REGIONSETID, ALIAS_REGIONSETID, TableSchema.ColumnType.INTEGER, 11);
        addColumn(DBFIELDNAME_GENOMEID, ALIAS_GENOMEID, TableSchema.ColumnType.INTEGER, 11);
        addColumn(DBFIELDNAME_CHROM, ALIAS_CHROM, TableSchema.ColumnType.VARCHAR, 255);
        addColumn(DBFIELDNAME_START, ALIAS_START, TableSchema.ColumnType.INTEGER, 11);
        addColumn(DBFIELDNAME_END, ALIAS_END, TableSchema.ColumnType.INTEGER, 11);
        addColumn(DBFIELDNAME_DESCRIPTION, ALIAS_DESCRIPTION, TableSchema.ColumnType.VARCHAR, 255);
    }
}
