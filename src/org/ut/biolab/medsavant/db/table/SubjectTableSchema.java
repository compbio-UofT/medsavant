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
public class SubjectTableSchema extends TableSchema {
    
    public static final String TABLE_NAME = "subject";

    
    public static int INDEX_HOSPITALID;
    public static int INDEX_DOB;
    public static int INDEX_GENDER;
    public static int INDEX_POOLID;
    public static int INDEX_FAMILY;
    public static int INDEX_RELATIONSHIP;
    public static int INDEX_ETHNICITY;
    public static int INDEX_RATIONALE;
    public static int INDEX_DNAIDS;
    public static int INDEX_CURRENTDNAID;
    public static int INDEX_DNASOURCE;
    public static int INDEX_PHENOTYPEIDS;
    public static int INDEX_CURRENTPHENOTYPEID;

    private void setIndexes() {
        INDEX_HOSPITALID = this.getFieldIndexInDB(DBFIELDNAME_HOSPITALID);
        INDEX_DOB = this.getFieldIndexInDB(DBFIELDNAME_DOB);
        INDEX_GENDER = this.getFieldIndexInDB(DBFIELDNAME_GENDER);
        INDEX_POOLID = this.getFieldIndexInDB(DBFIELDNAME_POOLID);
        INDEX_FAMILY = this.getFieldIndexInDB(DBFIELDNAME_FAMILY);
        INDEX_RELATIONSHIP = this.getFieldIndexInDB(DBFIELDNAME_RELATIONSHIP);
        INDEX_ETHNICITY = this.getFieldIndexInDB(DBFIELDNAME_ETHNICITY);
        INDEX_RATIONALE = this.getFieldIndexInDB(DBFIELDNAME_RATIONALE);
        INDEX_DNAIDS = this.getFieldIndexInDB(DBFIELDNAME_DNAIDS);
        INDEX_CURRENTDNAID = this.getFieldIndexInDB(DBFIELDNAME_CURRENTDNAID);
        INDEX_DNASOURCE = this.getFieldIndexInDB(DBFIELDNAME_DNASOURCE);
        INDEX_PHENOTYPEIDS = this.getFieldIndexInDB(DBFIELDNAME_PHENOTYPEIDS);
        INDEX_CURRENTPHENOTYPEID = this.getFieldIndexInDB(DBFIELDNAME_CURRENTPHENOTYPEID);
    }

    public SubjectTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }
    
    public static final String ALIAS_HOSPITALID = "Hospital ID";
    public static final String ALIAS_DOB = "Date of Birth";
    public static final String ALIAS_GENDER = "Gender";
    public static final String ALIAS_POOLID = "Pool";
    public static final String ALIAS_FAMILY = "Family";
    public static final String ALIAS_RELATIONSHIP = "Relationship";
    public static final String ALIAS_ETHNICITY = "Ethnicity";
    public static final String ALIAS_RATIONALE = "Rationale";
    public static final String ALIAS_DNAIDS = "DNA IDs";
    public static final String ALIAS_CURRENTDNAID = "Current DNA";
    public static final String ALIAS_DNASOURCE = "DNA Source";
    public static final String ALIAS_PHENOTYPEIDS = "Phenotype IDS";
    public static final String ALIAS_CURRENTPHENOTYPEID = "Current Phenotype";
    
    public static final String DBFIELDNAME_HOSPITALID = "hospital_id";
    public static final String DBFIELDNAME_DOB = "dob";
    public static final String DBFIELDNAME_GENDER= "gender";
    public static final String DBFIELDNAME_POOLID = "pool_id";
    public static final String DBFIELDNAME_FAMILY= "family";
    public static final String DBFIELDNAME_RELATIONSHIP = "relationship";
    public static final String DBFIELDNAME_ETHNICITY = "ethnicity";
    public static final String DBFIELDNAME_RATIONALE = "rationale";
    public static final String DBFIELDNAME_DNAIDS = "dna_ids";
    public static final String DBFIELDNAME_CURRENTDNAID = "current_dna_id";
    public static final String DBFIELDNAME_DNASOURCE = "dna_source";
    public static final String DBFIELDNAME_PHENOTYPEIDS = "phenotype_ids";
    public static final String DBFIELDNAME_CURRENTPHENOTYPEID = "current_phenotype_id";
    
    private void addColumns() {
        addColumn(DBFIELDNAME_HOSPITALID,ALIAS_HOSPITALID,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_DOB,ALIAS_DOB,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_GENDER,ALIAS_GENDER,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_POOLID,ALIAS_POOLID,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_FAMILY,ALIAS_FAMILY,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_RELATIONSHIP,ALIAS_RELATIONSHIP,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_ETHNICITY,ALIAS_ETHNICITY,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_RATIONALE,ALIAS_RATIONALE,TableSchema.ColumnType.VARCHAR,100);
        addColumn(DBFIELDNAME_DNAIDS,ALIAS_DNAIDS,TableSchema.ColumnType.VARCHAR,100);
        addColumn(DBFIELDNAME_CURRENTDNAID,ALIAS_CURRENTDNAID,TableSchema.ColumnType.INTEGER,1);
        addColumn(DBFIELDNAME_DNASOURCE,ALIAS_DNASOURCE,TableSchema.ColumnType.VARCHAR,20);
        addColumn(DBFIELDNAME_PHENOTYPEIDS,ALIAS_PHENOTYPEIDS,TableSchema.ColumnType.VARCHAR,100);
        addColumn(DBFIELDNAME_CURRENTPHENOTYPEID,ALIAS_CURRENTPHENOTYPEID,TableSchema.ColumnType.INTEGER,1);
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
