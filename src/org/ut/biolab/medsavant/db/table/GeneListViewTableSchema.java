/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author mfiume
 */
public class GeneListViewTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "region_set_view";
    
    public static int INDEX_REGIONSETID;
    public static int INDEX_REGIONSETNAME;
    public static int INDEX_GENOMEID;
    public static int INDEX_CHROM;
    public static int INDEX_START;
    public static int INDEX_END;
    public static int INDEX_DESCRIPTION;


    private void setIndexes() {
        INDEX_REGIONSETID = this.getFieldIndexInDB(DBFIELDNAME_REGIONSETID);
        INDEX_REGIONSETNAME = this.getFieldIndexInDB(DBFIELDNAME_REGIONSETNAME);
        INDEX_GENOMEID = this.getFieldIndexInDB(DBFIELDNAME_GENOMEID);
        INDEX_CHROM = this.getFieldIndexInDB(DBFIELDNAME_REGIONSETID);
        INDEX_START = this.getFieldIndexInDB(DBFIELDNAME_REGIONSETNAME);
        INDEX_END = this.getFieldIndexInDB(DBFIELDNAME_GENOMEID);
        INDEX_DESCRIPTION = this.getFieldIndexInDB(DBFIELDNAME_REGIONSETID);
    }

    public GeneListViewTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
   
    
    public static final String ALIAS_REGIONSETID = "Region Set ID";
    public static final String ALIAS_REGIONSETNAME = "Region Set Name";
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_START = "Start";
    public static final String ALIAS_END = "End";
    public static final String ALIAS_DESCRIPTION = "Description";

    
    public static final String DBFIELDNAME_REGIONSETID = "id";
    public static final String DBFIELDNAME_REGIONSETNAME = "name";
    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_START = "start";
    public static final String DBFIELDNAME_END = "end";
    public static final String DBFIELDNAME_DESCRIPTION = "description";

 
    private void addColumns() {
        addColumn(DBFIELDNAME_REGIONSETID,ALIAS_REGIONSETID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_REGIONSETNAME,ALIAS_REGIONSETNAME,TableSchema.ColumnType.VARCHAR,255);
        addColumn(DBFIELDNAME_GENOMEID,ALIAS_GENOMEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_CHROM,ALIAS_CHROM,TableSchema.ColumnType.VARCHAR,255);
        addColumn(DBFIELDNAME_START,ALIAS_START,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_END,ALIAS_END,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_DESCRIPTION,ALIAS_DESCRIPTION,TableSchema.ColumnType.VARCHAR,255);
    }

    /*
    public static VariantRecord convertToVariantRecord(Vector dbResult) {
        return new VariantRecord(
                (String) dbResult.get(INDEX_GENOMEID-1),
                (String) dbResult.get(INDEX_PIPELINEID-1),
                (String) dbResult.get(INDEX_DNAID-1),
                (String) dbResult.get(INDEX_CHROM-1),
                new Long((Integer) dbResult.get(INDEX_POSITION-1)),
                (String) dbResult.get(INDEX_ID-1),
                (String) dbResult.get(INDEX_REFERENCE-1),
                (String) dbResult.get(INDEX_ALTERNATE-1),
                (Float)  dbResult.get(INDEX_QUALITY-1),
                (String) dbResult.get(INDEX_FILTER-1),
                //(String) dbResult.get(INDEX_INFORMATION-1)
                (String) dbResult.get(INDEX_AA-1),
                (String) dbResult.get(INDEX_AC-1),
                (String) dbResult.get(INDEX_AF-1),
                (Integer) dbResult.get(INDEX_AN-1),
                (Float) dbResult.get(INDEX_BQ-1),
                (String) dbResult.get(INDEX_CIGAR-1),
                (Boolean) dbResult.get(INDEX_DB-1),
                (Integer) dbResult.get(INDEX_DP-1),
                new Long((Integer) dbResult.get(INDEX_END-1)),
                (Boolean) dbResult.get(INDEX_H2-1),
                (Float) dbResult.get(INDEX_MQ-1),
                (Integer) dbResult.get(INDEX_MQ0-1),
                (Integer) dbResult.get(INDEX_NS-1),
                (Float) dbResult.get(INDEX_SB-1),
                (Boolean) dbResult.get(INDEX_SOMATIC-1),
                (Boolean) dbResult.get(INDEX_VALIDATED-1),
                (String) dbResult.get(INDEX_CUSTOMINFO-1)
                );
    }
     * 
     */

}
