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
public class VariantAnnotationGatkTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "variant_annotation_gatk_ib";
    
    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_POSITION = "position";
    public static final String DBFIELDNAME_REF = "ref";
    public static final String DBFIELDNAME_ALT = "alt";
    public static final String DBFIELDNAME_NAME_GATK = "name_gatk";
    public static final String DBFIELDNAME_NAME2_GATK = "name2_gatk";
    public static final String DBFIELDNAME_TRANSCRIPTSTRAND = "transcriptStrand";
    public static final String DBFIELDNAME_POSITIONTYPE = "positionType";
    public static final String DBFIELDNAME_FRAME = "frame";
    public static final String DBFIELDNAME_MRNACOORD = "mrnaCoord";
    public static final String DBFIELDNAME_CODONCOORD = "codonCoord";
    public static final String DBFIELDNAME_SPLICEDIST = "spliceDist";
    public static final String DBFIELDNAME_REFERENCECODON = "referenceCodon";
    public static final String DBFIELDNAME_REFERENCEAA = "referenceAA";
    public static final String DBFIELDNAME_VARIANTCODON = "variantCodon";
    public static final String DBFIELDNAME_VARIANTAA = "variantAA";
    public static final String DBFIELDNAME_CHANGESAA = "changesAA";
    public static final String DBFIELDNAME_FUNCTIONALCLASS = "functionalClass";
    public static final String DBFIELDNAME_CODINGCOORDSTR = "codingCoordStr";
    public static final String DBFIELDNAME_PROTEINCOORDSTR = "proteinCoordStr";
    public static final String DBFIELDNAME_INCODINGREGION = "inCodingRegion";
    public static final String DBFIELDNAME_SPLICEINFO = "spliceInfo";
    public static final String DBFIELDNAME_UORFCHANGE = "uorfChange";
    public static final String DBFIELDNAME_SPLICEINFOCOPY = "spliceInfoCopy";
    
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_POSITION = "Position";
    public static final String ALIAS_REF = "Reference";
    public static final String ALIAS_ALT = "Alternate";
    public static final String ALIAS_NAME_GATK = "name gatk";
    public static final String ALIAS_NAME2_GATK = "name2 gatk";
    public static final String ALIAS_TRANSCRIPTSTRAND = "transcriptStrand";
    public static final String ALIAS_POSITIONTYPE = "positionType";
    public static final String ALIAS_FRAME = "frame";
    public static final String ALIAS_MRNACOORD = "mrnaCoord";
    public static final String ALIAS_CODONCOORD = "codonCoord";
    public static final String ALIAS_SPLICEDIST = "spliceDist";
    public static final String ALIAS_REFERENCECODON = "referenceCodon";
    public static final String ALIAS_REFERENCEAA = "referenceAA";
    public static final String ALIAS_VARIANTCODON = "variantCodon";
    public static final String ALIAS_VARIANTAA = "variantAA";
    public static final String ALIAS_CHANGESAA = "changesAA";
    public static final String ALIAS_FUNCTIONALCLASS = "functionalClass";
    public static final String ALIAS_CODINGCOORDSTR = "codingCoordStr";
    public static final String ALIAS_PROTEINCOORDSTR = "proteinCoordStr";
    public static final String ALIAS_INCODINGREGION = "inCodingRegion";
    public static final String ALIAS_SPLICEINFO = "spliceInfo";
    public static final String ALIAS_UORFCHANGE = "uorfChange";
    public static final String ALIAS_SPLICEINFOCOPY = "spliceInfoCopy";
   
    public static int INDEX_GENOMEID;
    public static int INDEX_CHROM;
    public static int INDEX_POSITION;
    public static int INDEX_REF;
    public static int INDEX_ALT;
    public static int INDEX_NAME_GATK;
    public static int INDEX_NAME2_GATK;
    public static int INDEX_TRANSCRIPTSTRAND;
    public static int INDEX_POSITIONTYPE;
    public static int INDEX_FRAME;
    public static int INDEX_MRNACOORD;
    public static int INDEX_CODONCOORD;
    public static int INDEX_SPLICEDIST;
    public static int INDEX_REFERENCECODON;
    public static int INDEX_REFERENCEAA;
    public static int INDEX_VARIANTCODON;
    public static int INDEX_VARIANTAA;
    public static int INDEX_CHANGESAA;
    public static int INDEX_FUNCTIONALCLASS;
    public static int INDEX_CODINGCOORDSTR;
    public static int INDEX_PROTEINCOORDSTR;
    public static int INDEX_INCODINGREGION;
    public static int INDEX_SPLICEINFO;
    public static int INDEX_UORFCHANGE;
    public static int INDEX_SPLICEINFOCOPY;
   
    public VariantAnnotationGatkTableSchema(DbSchema s) {
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
        INDEX_NAME_GATK = this.getFieldIndexInDB(DBFIELDNAME_NAME_GATK);
        INDEX_NAME2_GATK = this.getFieldIndexInDB(DBFIELDNAME_NAME2_GATK);
        INDEX_TRANSCRIPTSTRAND = this.getFieldIndexInDB(DBFIELDNAME_TRANSCRIPTSTRAND);
        INDEX_POSITIONTYPE = this.getFieldIndexInDB(DBFIELDNAME_POSITIONTYPE);
        INDEX_FRAME = this.getFieldIndexInDB(DBFIELDNAME_FRAME);
        INDEX_MRNACOORD = this.getFieldIndexInDB(DBFIELDNAME_MRNACOORD);
        INDEX_CODONCOORD = this.getFieldIndexInDB(DBFIELDNAME_CODONCOORD);
        INDEX_SPLICEDIST = this.getFieldIndexInDB(DBFIELDNAME_SPLICEDIST);
        INDEX_REFERENCECODON = this.getFieldIndexInDB(DBFIELDNAME_REFERENCECODON);
        INDEX_REFERENCEAA = this.getFieldIndexInDB(DBFIELDNAME_REFERENCEAA);
        INDEX_VARIANTCODON = this.getFieldIndexInDB(DBFIELDNAME_VARIANTCODON);
        INDEX_VARIANTAA = this.getFieldIndexInDB(DBFIELDNAME_VARIANTAA);
        INDEX_CHANGESAA = this.getFieldIndexInDB(DBFIELDNAME_CHANGESAA);
        INDEX_FUNCTIONALCLASS = this.getFieldIndexInDB(DBFIELDNAME_FUNCTIONALCLASS);
        INDEX_CODINGCOORDSTR = this.getFieldIndexInDB(DBFIELDNAME_CODINGCOORDSTR);
        INDEX_PROTEINCOORDSTR = this.getFieldIndexInDB(DBFIELDNAME_PROTEINCOORDSTR);
        INDEX_INCODINGREGION = this.getFieldIndexInDB(DBFIELDNAME_INCODINGREGION);
        INDEX_SPLICEINFO = this.getFieldIndexInDB(DBFIELDNAME_SPLICEINFO);
        INDEX_UORFCHANGE = this.getFieldIndexInDB(DBFIELDNAME_UORFCHANGE);
        INDEX_SPLICEINFOCOPY = this.getFieldIndexInDB(DBFIELDNAME_SPLICEINFOCOPY);  
    }
    
    private void addColumns() {
        addColumn(DBFIELDNAME_GENOMEID,ALIAS_GENOMEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_CHROM,ALIAS_CHROM,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_POSITION,ALIAS_POSITION,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_REF,ALIAS_REF,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_ALT,ALIAS_ALT,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_NAME_GATK,ALIAS_NAME_GATK,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_NAME2_GATK,ALIAS_NAME2_GATK,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_TRANSCRIPTSTRAND,ALIAS_TRANSCRIPTSTRAND,TableSchema.ColumnType.VARCHAR,1);
        addColumn(DBFIELDNAME_POSITIONTYPE,ALIAS_POSITIONTYPE,TableSchema.ColumnType.VARCHAR,50);
        addColumn(DBFIELDNAME_FRAME,ALIAS_FRAME,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_MRNACOORD,ALIAS_MRNACOORD,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_CODONCOORD,ALIAS_CODONCOORD,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_SPLICEDIST,ALIAS_SPLICEDIST,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_REFERENCECODON,ALIAS_REFERENCECODON,TableSchema.ColumnType.VARCHAR,5);
        addColumn(DBFIELDNAME_REFERENCEAA,ALIAS_REFERENCEAA,TableSchema.ColumnType.VARCHAR,50);
        addColumn(DBFIELDNAME_VARIANTCODON,ALIAS_VARIANTCODON,TableSchema.ColumnType.VARCHAR,5);
        addColumn(DBFIELDNAME_VARIANTAA,ALIAS_VARIANTAA,TableSchema.ColumnType.VARCHAR,50);
        addColumn(DBFIELDNAME_CHANGESAA,ALIAS_CHANGESAA,TableSchema.ColumnType.VARCHAR,255);
        addColumn(DBFIELDNAME_FUNCTIONALCLASS,ALIAS_FUNCTIONALCLASS,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_CODINGCOORDSTR,ALIAS_CODINGCOORDSTR,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_PROTEINCOORDSTR,ALIAS_PROTEINCOORDSTR,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_INCODINGREGION,ALIAS_INCODINGREGION,TableSchema.ColumnType.VARCHAR,255);
        addColumn(DBFIELDNAME_SPLICEINFO,ALIAS_SPLICEINFO,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_UORFCHANGE,ALIAS_UORFCHANGE,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_SPLICEINFOCOPY,ALIAS_SPLICEINFOCOPY,TableSchema.ColumnType.VARCHAR,30);
    }
    
    public Object[][] getJoinedColumnGrid() {
        
        String[] usedColumns = {ALIAS_NAME_GATK, ALIAS_NAME2_GATK, ALIAS_TRANSCRIPTSTRAND, 
            ALIAS_POSITIONTYPE, ALIAS_FRAME, ALIAS_MRNACOORD, ALIAS_CODONCOORD, 
            ALIAS_SPLICEDIST, ALIAS_REFERENCECODON, ALIAS_REFERENCEAA, 
            ALIAS_VARIANTCODON, ALIAS_VARIANTAA, ALIAS_CHANGESAA, 
            ALIAS_FUNCTIONALCLASS, ALIAS_CODINGCOORDSTR, ALIAS_PROTEINCOORDSTR, 
            ALIAS_INCODINGREGION, ALIAS_SPLICEINFO, ALIAS_UORFCHANGE, 
            ALIAS_SPLICEINFOCOPY };
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
