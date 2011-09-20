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
public class VariantAnnotationPolyphenTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "variant_annotation_polyphen_ib";
    
    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_POSITION = "position";
    public static final String DBFIELDNAME_REF = "ref";
    public static final String DBFIELDNAME_ALT = "alt";
    public static final String DBFIELDNAME_CDNACOORD = "cdna_coord";
    public static final String DBFIELDNAME_OPOS = "o_pos";
    public static final String DBFIELDNAME_OAA1 = "o_aa1";
    public static final String DBFIELDNAME_OAA2 = "o_aa2";
    public static final String DBFIELDNAME_SNPID = "snp_id";
    public static final String DBFIELDNAME_ACC = "acc";
    public static final String DBFIELDNAME_POS = "pos";
    public static final String DBFIELDNAME_PREDICTION = "prediction";
    public static final String DBFIELDNAME_PPH2CLASS = "pph2_class";
    public static final String DBFIELDNAME_PPH2PROB = "pph2_prob";
    public static final String DBFIELDNAME_PPH2FPR = "pph2_FPR";
    public static final String DBFIELDNAME_PPH2TPR = "pph2_TPR";
    public static final String DBFIELDNAME_PPH2FDR = "pph2_FDR";
    public static final String DBFIELDNAME_TRANSV = "transv";
    public static final String DBFIELDNAME_CODPOS = "CodPos";
    public static final String DBFIELDNAME_CPG = "CpG";
    public static final String DBFIELDNAME_MINDJNC = "MinDjnc";
    public static final String DBFIELDNAME_IDPMAX = "IdPmax";
    public static final String DBFIELDNAME_IDPSNP = "IdPSNP";
    public static final String DBFIELDNAME_IDQMIN = "IdQmin";    
    
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_POSITION = "Position";
    public static final String ALIAS_REF = "Reference";
    public static final String ALIAS_ALT = "Alternate";
    public static final String ALIAS_CDNACOORD = "cdna_coord";
    public static final String ALIAS_OPOS = "o_pos";
    public static final String ALIAS_OAA1 = "o_aa1";
    public static final String ALIAS_OAA2 = "o_aa2";
    public static final String ALIAS_SNPID = "snp_id";
    public static final String ALIAS_ACC = "acc";
    public static final String ALIAS_POS = "pos";
    public static final String ALIAS_PREDICTION = "prediction";
    public static final String ALIAS_PPH2CLASS = "pph2_class";
    public static final String ALIAS_PPH2PROB = "pph2_prob";
    public static final String ALIAS_PPH2FPR = "pph2_FPR";
    public static final String ALIAS_PPH2TPR = "pph2_TPR";
    public static final String ALIAS_PPH2FDR = "pph2_FDR";
    public static final String ALIAS_TRANSV = "transv";
    public static final String ALIAS_CODPOS = "CodPos";
    public static final String ALIAS_CPG = "CpG";
    public static final String ALIAS_MINDJNC = "MinDjnc";
    public static final String ALIAS_IDPMAX = "IdPmax";
    public static final String ALIAS_IDPSNP = "IdPSNP";
    public static final String ALIAS_IDQMIN = "IdQmin";    
    
    public static int INDEX_GENOMEID;
    public static int INDEX_CHROM;
    public static int INDEX_POSITION;
    public static int INDEX_REF;
    public static int INDEX_ALT;
    public static int INDEX_CDNACOORD;
    public static int INDEX_OPOS;
    public static int INDEX_OAA1;
    public static int INDEX_OAA2;
    public static int INDEX_SNPID;
    public static int INDEX_ACC;
    public static int INDEX_POS;
    public static int INDEX_PREDICTION;
    public static int INDEX_PPH2CLASS;
    public static int INDEX_PPH2PROB;
    public static int INDEX_PPH2FPR;
    public static int INDEX_PPH2TPR;
    public static int INDEX_PPH2FDR;
    public static int INDEX_TRANSV;
    public static int INDEX_CODPOS;
    public static int INDEX_CPG;
    public static int INDEX_MINDJNC;
    public static int INDEX_IDPMAX;
    public static int INDEX_IDPSNP;
    public static int INDEX_IDQMIN;    

    public VariantAnnotationPolyphenTableSchema(DbSchema s) {
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
        INDEX_CDNACOORD = this.getFieldIndexInDB(DBFIELDNAME_CDNACOORD);
        INDEX_OPOS = this.getFieldIndexInDB(DBFIELDNAME_OPOS);
        INDEX_OAA1 = this.getFieldIndexInDB(DBFIELDNAME_OAA1);
        INDEX_OAA2 = this.getFieldIndexInDB(DBFIELDNAME_OAA2);
        INDEX_SNPID = this.getFieldIndexInDB(DBFIELDNAME_SNPID);
        INDEX_ACC = this.getFieldIndexInDB(DBFIELDNAME_ACC);
        INDEX_POS = this.getFieldIndexInDB(DBFIELDNAME_POS);
        INDEX_PREDICTION = this.getFieldIndexInDB(DBFIELDNAME_PREDICTION);
        INDEX_PPH2CLASS = this.getFieldIndexInDB(DBFIELDNAME_PPH2CLASS);
        INDEX_PPH2PROB = this.getFieldIndexInDB(DBFIELDNAME_PPH2PROB);
        INDEX_PPH2FPR = this.getFieldIndexInDB(DBFIELDNAME_PPH2FPR);
        INDEX_PPH2TPR = this.getFieldIndexInDB(DBFIELDNAME_PPH2TPR);
        INDEX_PPH2FDR = this.getFieldIndexInDB(DBFIELDNAME_PPH2FDR);
        INDEX_TRANSV = this.getFieldIndexInDB(DBFIELDNAME_TRANSV);
        INDEX_CODPOS = this.getFieldIndexInDB(DBFIELDNAME_CODPOS);
        INDEX_CPG = this.getFieldIndexInDB(DBFIELDNAME_CPG);
        INDEX_MINDJNC = this.getFieldIndexInDB(DBFIELDNAME_MINDJNC);
        INDEX_IDPMAX = this.getFieldIndexInDB(DBFIELDNAME_IDPMAX);
        INDEX_IDPSNP = this.getFieldIndexInDB(DBFIELDNAME_IDPSNP);
        INDEX_IDQMIN = this.getFieldIndexInDB(DBFIELDNAME_IDQMIN);    
    }
    
    private void addColumns() {
        addColumn(DBFIELDNAME_GENOMEID,ALIAS_GENOMEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_CHROM,ALIAS_CHROM,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_POSITION,ALIAS_POSITION,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_REF,ALIAS_REF,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_ALT,ALIAS_ALT,TableSchema.ColumnType.VARCHAR,30);
        addColumn(DBFIELDNAME_CDNACOORD,ALIAS_CDNACOORD,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_OPOS,ALIAS_OPOS,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_OAA1,ALIAS_OAA1,TableSchema.ColumnType.VARCHAR,5);
        addColumn(DBFIELDNAME_OAA2,ALIAS_OAA2,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_SNPID,ALIAS_SNPID,TableSchema.ColumnType.VARCHAR,45);
        addColumn(DBFIELDNAME_ACC,ALIAS_ACC,TableSchema.ColumnType.VARCHAR,45);
        addColumn(DBFIELDNAME_POS,ALIAS_POS,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_PREDICTION,ALIAS_PREDICTION,TableSchema.ColumnType.VARCHAR,45);
        addColumn(DBFIELDNAME_PPH2CLASS,ALIAS_PPH2CLASS,TableSchema.ColumnType.VARCHAR,45);
        addColumn(DBFIELDNAME_PPH2PROB,ALIAS_PPH2PROB,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_PPH2FPR,ALIAS_PPH2FPR,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_PPH2TPR,ALIAS_PPH2TPR,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_PPH2FDR,ALIAS_PPH2FDR,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_TRANSV,ALIAS_TRANSV,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_CODPOS,ALIAS_CODPOS,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_CPG,ALIAS_CPG,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_MINDJNC,ALIAS_MINDJNC,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_IDPMAX,ALIAS_IDPMAX,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_IDPSNP,ALIAS_IDPSNP,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_IDQMIN,ALIAS_IDQMIN,TableSchema.ColumnType.FLOAT,1);
    }
    
    public Object[][] getJoinedColumnGrid() {
        
        String[] usedColumns = {ALIAS_CDNACOORD, ALIAS_OPOS, ALIAS_OAA1, 
            ALIAS_OAA2, ALIAS_SNPID, ALIAS_ACC, ALIAS_POS, ALIAS_PREDICTION, 
            ALIAS_PPH2CLASS, ALIAS_PPH2PROB, ALIAS_PPH2FPR, ALIAS_PPH2TPR, 
            ALIAS_PPH2FDR, ALIAS_TRANSV, ALIAS_CODPOS, ALIAS_CPG, ALIAS_MINDJNC, 
            ALIAS_IDPMAX, ALIAS_IDPSNP, ALIAS_IDQMIN};
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
