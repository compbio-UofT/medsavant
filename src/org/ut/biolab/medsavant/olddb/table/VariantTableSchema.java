/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.table;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

/**
 *
 * @author AndrewBrook
 */
public class VariantTableSchema extends TableSchema {

    public static final String TABLE_NAME = "variant_combined_ib";

    public static final String DBFIELDNAME_VARIANTID = "variant_id";
    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_PIPELINEID = "pipeline_id";
    public static final String DBFIELDNAME_DNAID = "dna_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_POSITION = "position";
    public static final String DBFIELDNAME_DBSNPID = "dbsnp_id";
    public static final String DBFIELDNAME_REFERENCE = "ref";
    public static final String DBFIELDNAME_ALTERNATE = "alt";
    public static final String DBFIELDNAME_QUALITY = "qual";
    public static final String DBFIELDNAME_FILTER = "filter";
    public static final String DBFIELDNAME_AA = "aa";
    public static final String DBFIELDNAME_AC = "ac";
    public static final String DBFIELDNAME_AF = "af";
    public static final String DBFIELDNAME_AN = "an";
    public static final String DBFIELDNAME_BQ = "bq";
    public static final String DBFIELDNAME_CIGAR = "cigar";
    public static final String DBFIELDNAME_DB = "db";
    public static final String DBFIELDNAME_DP = "dp";
    public static final String DBFIELDNAME_END = "end";
    public static final String DBFIELDNAME_H2 = "h2";
    public static final String DBFIELDNAME_MQ = "mq";
    public static final String DBFIELDNAME_MQ0 = "mq0";
    public static final String DBFIELDNAME_NS = "ns";
    public static final String DBFIELDNAME_SB = "sb";
    public static final String DBFIELDNAME_SOMATIC = "somatic";
    public static final String DBFIELDNAME_VALIDATED = "validated";
    public static final String DBFIELDNAME_CUSTOMINFO = "custom_info";    
    //public static final String DBFIELDNAME_ANNOTATIONSIFT = "variant_annotation_sift_id";
    
    public enum FIELD_NAMES {
        VARIANT_ID, GENOME_ID, PIPELINE_ID, DNA_ID, CHROM, POSITION, DBSNP_ID, 
        REF, ALT, QUAL, FILTER, AA, AC, AF, AN, BQ, CIGAR, DB, DP, END, H2, MQ, 
        MQ0, NS, SB, SOMATIC, VALIDATED, CUSTOM_INFO//, VARIANT_ANNOTATION_SIFT_ID
    };

    public static final String ALIAS_VARIANTID = "Variant ID";
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_PIPELINEID = "Pipeline ID";
    public static final String ALIAS_DNAID = "DNA ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_POSITION = "Position";
    public static final String ALIAS_DBSNPID = "dbSNP ID";
    public static final String ALIAS_REFERENCE = "Reference Nucleotide";
    public static final String ALIAS_ALTERNATE = "Alternate Nucleotide";
    public static final String ALIAS_QUALITY = "Quality";
    public static final String ALIAS_FILTER = "Filter";
    public static final String ALIAS_AA = "Ancestral Allele";
    public static final String ALIAS_AC = "Allele Count";
    public static final String ALIAS_AF = "Allele Frequency";
    public static final String ALIAS_AN = "Number of Alleles";
    public static final String ALIAS_BQ = "RMS Base Quality";
    public static final String ALIAS_CIGAR = "Cigar";
    public static final String ALIAS_DB = "dbSNP Membership";
    public static final String ALIAS_DP = "Coverage";
    public static final String ALIAS_END = "End Position";
    public static final String ALIAS_H2 = "HapMap2 Membership";
    public static final String ALIAS_MQ = "RMS Mapping Quality";
    public static final String ALIAS_MQ0 = "Number of MAPQ";
    public static final String ALIAS_NS = "Number of Samples";
    public static final String ALIAS_SB = "Strand Bias";
    public static final String ALIAS_SOMATIC = "Somatic Mutation";
    public static final String ALIAS_VALIDATED = "Validated";
    public static final String ALIAS_CUSTOMINFO = "Information";
    //public static final String ALIAS_ANNOTATIONSIFT = "Variant Annotation Sift ID";
    
    public static int INDEX_VARIANTID;
    public static int INDEX_GENOMEID;
    public static int INDEX_PIPELINEID;
    public static int INDEX_DNAID;
    public static int INDEX_CHROM;
    public static int INDEX_POSITION;
    public static int INDEX_DBSNPID;
    public static int INDEX_REFERENCE;
    public static int INDEX_ALTERNATE;
    public static int INDEX_QUALITY;
    public static int INDEX_FILTER;
    public static int INDEX_AA;
    public static int INDEX_AC;
    public static int INDEX_AF;
    public static int INDEX_AN;
    public static int INDEX_BQ;
    public static int INDEX_CIGAR;
    public static int INDEX_DB;
    public static int INDEX_DP;
    public static int INDEX_END;
    public static int INDEX_H2;
    public static int INDEX_MQ;
    public static int INDEX_MQ0;
    public static int INDEX_NS;
    public static int INDEX_SB;
    public static int INDEX_SOMATIC;
    public static int INDEX_VALIDATED;
    public static int INDEX_CUSTOMINFO;
    //public static int INDEX_ANNOTATIONSIFT;
    


    public VariantTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }    
    
    private void setIndexes() {
        INDEX_VARIANTID = this.getFieldIndexInDB(DBFIELDNAME_VARIANTID);
        INDEX_GENOMEID = this.getFieldIndexInDB(DBFIELDNAME_GENOMEID);
        INDEX_PIPELINEID = this.getFieldIndexInDB(DBFIELDNAME_PIPELINEID);
        INDEX_DNAID = this.getFieldIndexInDB(DBFIELDNAME_DNAID);
        INDEX_CHROM = this.getFieldIndexInDB(DBFIELDNAME_CHROM);
        INDEX_POSITION = this.getFieldIndexInDB(DBFIELDNAME_POSITION);
        INDEX_DBSNPID = this.getFieldIndexInDB(DBFIELDNAME_DBSNPID);
        INDEX_REFERENCE = this.getFieldIndexInDB(DBFIELDNAME_REFERENCE);
        INDEX_ALTERNATE = this.getFieldIndexInDB(DBFIELDNAME_ALTERNATE);
        INDEX_QUALITY = this.getFieldIndexInDB(DBFIELDNAME_QUALITY);
        INDEX_FILTER = this.getFieldIndexInDB(DBFIELDNAME_FILTER);
        INDEX_AA = this.getFieldIndexInDB(DBFIELDNAME_AA);
        INDEX_AC = this.getFieldIndexInDB(DBFIELDNAME_AC);
        INDEX_AF = this.getFieldIndexInDB(DBFIELDNAME_AF);
        INDEX_AN = this.getFieldIndexInDB(DBFIELDNAME_AN);
        INDEX_BQ = this.getFieldIndexInDB(DBFIELDNAME_BQ);
        INDEX_CIGAR = this.getFieldIndexInDB(DBFIELDNAME_CIGAR);
        INDEX_DB = this.getFieldIndexInDB(DBFIELDNAME_DB);
        INDEX_DP = this.getFieldIndexInDB(DBFIELDNAME_DP);
        INDEX_END = this.getFieldIndexInDB(DBFIELDNAME_END);
        INDEX_H2 = this.getFieldIndexInDB(DBFIELDNAME_H2);
        INDEX_MQ = this.getFieldIndexInDB(DBFIELDNAME_MQ);
        INDEX_MQ0 = this.getFieldIndexInDB(DBFIELDNAME_MQ0);
        INDEX_NS = this.getFieldIndexInDB(DBFIELDNAME_NS);
        INDEX_SB = this.getFieldIndexInDB(DBFIELDNAME_SB);
        INDEX_SOMATIC = this.getFieldIndexInDB(DBFIELDNAME_SOMATIC);
        INDEX_VALIDATED = this.getFieldIndexInDB(DBFIELDNAME_VALIDATED);
        INDEX_CUSTOMINFO = this.getFieldIndexInDB(DBFIELDNAME_CUSTOMINFO);
        //INDEX_ANNOTATIONSIFT = this.getFieldIndexInDB(DBFIELDNAME_ANNOTATIONSIFT);
    }
    
    private void addColumns() {
        addColumn(DBFIELDNAME_VARIANTID,ALIAS_VARIANTID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_GENOMEID,ALIAS_GENOMEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_PIPELINEID,ALIAS_PIPELINEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_DNAID,ALIAS_DNAID,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_CHROM,ALIAS_CHROM,TableSchema.ColumnType.VARCHAR,5);
        addColumn(DBFIELDNAME_POSITION,ALIAS_POSITION,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_DBSNPID,ALIAS_DBSNPID,TableSchema.ColumnType.VARCHAR,45);
        addColumn(DBFIELDNAME_REFERENCE,ALIAS_REFERENCE,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_ALTERNATE,ALIAS_ALTERNATE,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_QUALITY,ALIAS_QUALITY,TableSchema.ColumnType.FLOAT,11);
        addColumn(DBFIELDNAME_FILTER,ALIAS_FILTER,TableSchema.ColumnType.VARCHAR,500);
        addColumn(DBFIELDNAME_AA,ALIAS_AA,TableSchema.ColumnType.VARCHAR,500);
        addColumn(DBFIELDNAME_AC,ALIAS_AC,TableSchema.ColumnType.VARCHAR,500);
        addColumn(DBFIELDNAME_AF,ALIAS_AF,TableSchema.ColumnType.VARCHAR,500);
        addColumn(DBFIELDNAME_AN,ALIAS_AN,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_BQ,ALIAS_BQ,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_CIGAR,ALIAS_CIGAR,TableSchema.ColumnType.VARCHAR,500);
        addColumn(DBFIELDNAME_DB,ALIAS_DB,TableSchema.ColumnType.BOOLEAN,1);
        addColumn(DBFIELDNAME_DP,ALIAS_DP,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_END,ALIAS_END,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_H2,ALIAS_H2,TableSchema.ColumnType.BOOLEAN,1);
        addColumn(DBFIELDNAME_MQ,ALIAS_MQ,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_MQ0,ALIAS_MQ0,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_NS,ALIAS_NS,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_SB,ALIAS_SB,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_SOMATIC,ALIAS_SOMATIC,TableSchema.ColumnType.BOOLEAN,1);
        addColumn(DBFIELDNAME_VALIDATED,ALIAS_VALIDATED,TableSchema.ColumnType.BOOLEAN,1);
        addColumn(DBFIELDNAME_CUSTOMINFO,ALIAS_CUSTOMINFO,TableSchema.ColumnType.VARCHAR,500);
        //addColumn(DBFIELDNAME_ANNOTATIONSIFT,ALIAS_ANNOTATIONSIFT,TableSchema.ColumnType.INTEGER,11);
    }
    
    /*
     * Create a column from another table that pretends it is from this table. 
     */
    public DbColumn createTempColumn(DbColumn other){
        return new DbColumn(this.getTable(), other.getName(), other.getTypeNameSQL(), other.getTypeLength());
    }

}
