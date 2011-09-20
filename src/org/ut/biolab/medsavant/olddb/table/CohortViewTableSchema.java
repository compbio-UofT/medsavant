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
public class CohortViewTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "cohort_membership";

    
    public static int INDEX_COHORTID;
    //public static int INDEX_COHORTNAME;
    public static int INDEX_HOSPITALID;


    private void setIndexes() {
        INDEX_COHORTID = this.getFieldIndexInDB(DBFIELDNAME_COHORTID);
        //INDEX_COHORTNAME = this.getFieldIndexInDB(DBFIELDNAME_COHORTNAME);
        INDEX_HOSPITALID = this.getFieldIndexInDB(DBFIELDNAME_HOSPITALID);
    }

    public CohortViewTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
   
    
    public static final String ALIAS_COHORTID = "Cohort ID";
   // public static final String ALIAS_COHORTNAME = "Cohort Name";
    public static final String ALIAS_HOSPITALID = "Hospital ID";

    
    public static final String DBFIELDNAME_COHORTID = "cohort_id";
   // public static final String DBFIELDNAME_COHORTNAME = "cohort_name";
    public static final String DBFIELDNAME_HOSPITALID = "hospital_id";
 
    private void addColumns() {
        addColumn(DBFIELDNAME_COHORTID,ALIAS_COHORTID,TableSchema.ColumnType.INTEGER,11);
      //  addColumn(DBFIELDNAME_COHORTNAME,ALIAS_COHORTNAME,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_HOSPITALID,ALIAS_HOSPITALID,TableSchema.ColumnType.VARCHAR,20);
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
