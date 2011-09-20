/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

/**
 *
 * @author AndrewBrook
 */
public class VariantAnnotationSiftTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "variant_annotation_sift_ib"; //TODO: back to _ib
    
    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_POSITION = "position";
    public static final String DBFIELDNAME_REF = "ref";
    public static final String DBFIELDNAME_ALT = "alt";
    public static final String DBFIELDNAME_NAME_SIFT = "name";
    public static final String DBFIELDNAME_NAME2_SIFT = "name2";
    public static final String DBFIELDNAME_DAMAGEPROBABILITY = "damage_probability";
    //public static final String DBFIELDNAME_ANNOTATIONID = "variant_annotation_sift_id";
    
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_POSITION = "Position";
    public static final String ALIAS_REF = "Reference";
    public static final String ALIAS_ALT = "Alternate";
    public static final String ALIAS_NAME_SIFT = "Name Sift";
    public static final String ALIAS_NAME2_SIFT = "Name2 Sift";
    public static final String ALIAS_DAMAGEPROBABILITY = "Damage Probability";
    //public static final String ALIAS_ANNOTATIONID = "Variant Annotation Sift ID";
    
    public static int INDEX_GENOMEID;
    public static int INDEX_CHROM;
    public static int INDEX_POSITION;
    public static int INDEX_REF;
    public static int INDEX_ALT;
    public static int INDEX_NAME_SIFT;
    public static int INDEX_NAME2_SIFT;
    public static int INDEX_DAMAGEPROBABILITY;
    //public static int INDEX_ANNOTATIONID;
    
    public VariantAnnotationSiftTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
    
    private void setIndexes() {
        INDEX_GENOMEID = this.getFieldIndexInDB(DBFIELDNAME_GENOMEID);
        INDEX_CHROM = this.getFieldIndexInDB(DBFIELDNAME_CHROM);
        INDEX_POSITION = this.getFieldIndexInDB(DBFIELDNAME_POSITION);
        INDEX_REF = this.getFieldIndexInDB(DBFIELDNAME_REF);
        INDEX_ALT = this.getFieldIndexInDB(DBFIELDNAME_ALT);
        INDEX_NAME_SIFT = this.getFieldIndexInDB(DBFIELDNAME_NAME_SIFT);
        INDEX_NAME2_SIFT = this.getFieldIndexInDB(DBFIELDNAME_NAME2_SIFT);
        INDEX_DAMAGEPROBABILITY = this.getFieldIndexInDB(DBFIELDNAME_DAMAGEPROBABILITY);
        //INDEX_ANNOTATIONID = this.getFieldIndexInDB(DBFIELDNAME_ANNOTATIONID);
    }
    
    private void addColumns() {
        addColumn(DBFIELDNAME_GENOMEID,ALIAS_GENOMEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_CHROM,ALIAS_CHROM,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_POSITION,ALIAS_POSITION,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_REF,ALIAS_REF,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_ALT,ALIAS_ALT,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_NAME_SIFT,ALIAS_NAME_SIFT,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_NAME2_SIFT,ALIAS_NAME2_SIFT,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_DAMAGEPROBABILITY,ALIAS_DAMAGEPROBABILITY,TableSchema.ColumnType.DECIMAL,3);
        //addColumn(DBFIELDNAME_ANNOTATIONID,ALIAS_ANNOTATIONID,TableSchema.ColumnType.INTEGER,11);
    }
    
    public Object[][] getJoinedColumnGrid() {
                
        String[] usedColumns = {ALIAS_NAME_SIFT, ALIAS_NAME2_SIFT, ALIAS_DAMAGEPROBABILITY};
        Object[][] result = new Object[usedColumns.length][3];
        
        for(int i = 0; i < usedColumns.length; i++){
            String alias = usedColumns[i];
            result[i][0] = i+1;
            result[i][1] = this.getDBColumn(alias);
            result[i][2] = this.getColumnType(this.getDBColumn(alias));
        }
        
        return result;

    }
    
}
