package org.ut.biolab.medsavant.db.table;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import fiume.vcf.VariantRecord;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class VariantTableSchema extends TableSchema {

    public static final String TABLE_NAME = "variant";
    
    public static final String DBFIELDNAME_SAMPLEID = "sample_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_POSITION = "position";
    public static final String DBFIELDNAME_ID = "id";
    public static final String DBFIELDNAME_REFERENCE = "ref";
    public static final String DBFIELDNAME_ALTERNATE = "alt";
    public static final String DBFIELDNAME_QUALITY = "qual";
    public static final String DBFIELDNAME_FILTER = "filter";
    public static final String DBFIELDNAME_INFORMATION = "info";

    public static final String ALIAS_SAMPLEID = "Sample ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_POSITION = "Position";
    public static final String ALIAS_ID = "ID";
    public static final String ALIAS_REFERENCE = "Reference";
    public static final String ALIAS_ALTERNATE = "Alternate";
    public static final String ALIAS_QUALITY = "Quality";
    public static final String ALIAS_FILTER = "Filter";
    public static final String ALIAS_INFORMATION = "Information";

    public static int INDEX_SAMPLEID;
    public static int INDEX_CHROM;
    public static int INDEX_POSITION;
    public static int INDEX_ID;
    public static int INDEX_REFERENCE;
    public static int INDEX_ALTERNATE;
    public static int INDEX_QUALITY;
    public static int INDEX_FILTER;
    public static int INDEX_INFORMATION;

        
    private void setIndexes() {
        INDEX_SAMPLEID = this.getFieldIndexInDB(DBFIELDNAME_SAMPLEID);
        INDEX_CHROM = this.getFieldIndexInDB(DBFIELDNAME_CHROM);
        INDEX_POSITION = this.getFieldIndexInDB(DBFIELDNAME_POSITION);
        INDEX_ID = this.getFieldIndexInDB(DBFIELDNAME_ID);
        INDEX_REFERENCE = this.getFieldIndexInDB(DBFIELDNAME_REFERENCE);
        INDEX_ALTERNATE = this.getFieldIndexInDB(DBFIELDNAME_ALTERNATE);
        INDEX_QUALITY = this.getFieldIndexInDB(DBFIELDNAME_QUALITY);
        INDEX_FILTER = this.getFieldIndexInDB(DBFIELDNAME_FILTER);
        INDEX_INFORMATION = this.getFieldIndexInDB(DBFIELDNAME_INFORMATION);
    }

    public VariantTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }

    private void addColumns() {
        addColumn(DBFIELDNAME_SAMPLEID,ALIAS_SAMPLEID,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_CHROM,ALIAS_CHROM,TableSchema.ColumnType.VARCHAR,5);
        addColumn(DBFIELDNAME_POSITION,ALIAS_POSITION,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_ID,ALIAS_ID,TableSchema.ColumnType.VARCHAR,45);
        addColumn(DBFIELDNAME_REFERENCE,ALIAS_REFERENCE,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_ALTERNATE,ALIAS_ALTERNATE,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_QUALITY,ALIAS_QUALITY,TableSchema.ColumnType.FLOAT,11);
        addColumn(DBFIELDNAME_FILTER,ALIAS_FILTER,TableSchema.ColumnType.VARCHAR,500);
        addColumn(DBFIELDNAME_INFORMATION,ALIAS_INFORMATION,TableSchema.ColumnType.VARCHAR,500);
    }

    public static VariantRecord convertToVariantRecord(Vector dbResult) {
        return new VariantRecord(
                (String) dbResult.get(INDEX_SAMPLEID-1),
                (String) dbResult.get(INDEX_CHROM-1),
                new Long((Integer) dbResult.get(INDEX_POSITION-1)),
                (String) dbResult.get(INDEX_ID-1),
                (String) dbResult.get(INDEX_REFERENCE-1),
                (String) dbResult.get(INDEX_ALTERNATE-1),
                (Float)  dbResult.get(INDEX_QUALITY-1),
                (String) dbResult.get(INDEX_FILTER-1),
                (String) dbResult.get(INDEX_INFORMATION-1)
                );
    }



}
