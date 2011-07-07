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

    public static final String TABLE_NAME = "variant1";

    public static final String DBFIELDNAME_GATKID = "annotation_gatk_id";
    public static final String DBFIELDNAME_POLYPHENSIFTID = "annotation_polyphensift_id";
    public static final String DBFIELDNAME_GENOMEID = "genome_id";
    public static final String DBFIELDNAME_PIPELINEID = "pipeline_id";
    public static final String DBFIELDNAME_DNAID = "dna_id";
    public static final String DBFIELDNAME_CHROM = "chrom";
    public static final String DBFIELDNAME_POSITION = "position";
    public static final String DBFIELDNAME_ID = "id";
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
    public static final String DBFIELDNAME_GT = "gt";
    public static final String DBFIELDNAME_GPHASED = "gphased";
    public static final String DBFIELDNAME_GDP = "gdp";
    public static final String DBFIELDNAME_GFT = "gft";
    public static final String DBFIELDNAME_GLHOMOREF = "gl_homoref";
    public static final String DBFIELDNAME_GLHET = "gl_het";
    public static final String DBFIELDNAME_GLHOMOALT = "gl_homoalt";
    public static final String DBFIELDNAME_GQ = "gq";
    public static final String DBFIELDNAME_HQA = "hqa";
    public static final String DBFIELDNAME_HQB = "hqb";

    public static final String ALIAS_GATKID = "Gatk ID";
    public static final String ALIAS_POLYPHENSIFTID = "Polyphensift ID";
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_PIPELINEID = "Pipeline ID";
    public static final String ALIAS_DNAID = "DNA ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_POSITION = "Position";
    public static final String ALIAS_ID = "ID";
    public static final String ALIAS_REFERENCE = "Reference";
    public static final String ALIAS_ALTERNATE = "Alternate";
    public static final String ALIAS_QUALITY = "Quality";
    public static final String ALIAS_FILTER = "Filter";
    public static final String ALIAS_AA = "Ancestral Allele";
    public static final String ALIAS_AC = "Allele Count";
    public static final String ALIAS_AF = "Allele Frequency";
    public static final String ALIAS_AN = "Number of Alleles";
    public static final String ALIAS_BQ = "RMS Base Quality";
    public static final String ALIAS_CIGAR = "Cigar";
    public static final String ALIAS_DB = "dbSNP Membership";
    public static final String ALIAS_DP = "Depth";
    public static final String ALIAS_END = "End Position";
    public static final String ALIAS_H2 = "HapMap2 Membership";
    public static final String ALIAS_MQ = "RMS Mapping Quality";
    public static final String ALIAS_MQ0 = "Number of MAPQ";
    public static final String ALIAS_NS = "Number of Samples";
    public static final String ALIAS_SB = "Strand Bias";
    public static final String ALIAS_SOMATIC = "Somatic Mutation";
    public static final String ALIAS_VALIDATED = "Validated";
    public static final String ALIAS_CUSTOMINFO = "Information";
    public static final String ALIAS_GT = "Genotype";
    public static final String ALIAS_GPHASED = "Genotype Phased";
    public static final String ALIAS_GDP = "Genotype Read Depth";
    public static final String ALIAS_GFT = "Genotype Filter";
    public static final String ALIAS_GLHOMOREF = "Genotype Likelihood HomoRef";
    public static final String ALIAS_GLHET = "Genotype Likelihood Hetero";
    public static final String ALIAS_GLHOMOALT = "Genotype Likelihood HomoAlt";
    public static final String ALIAS_GQ = "Genotype Quality";
    public static final String ALIAS_HQA = "Haplotype Quality A";
    public static final String ALIAS_HQB = "Haplotype Quality B";

    public static int INDEX_GATKID;
    public static int INDEX_POLYPHENSIFTID;
    public static int INDEX_GENOMEID;
    public static int INDEX_PIPELINEID;
    public static int INDEX_DNAID;
    public static int INDEX_CHROM;
    public static int INDEX_POSITION;
    public static int INDEX_ID;
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
    public static int INDEX_GT;
    public static int INDEX_GPHASED;
    public static int INDEX_GDP;
    public static int INDEX_GFT;
    public static int INDEX_GLHOMOREF;
    public static int INDEX_GLHET;
    public static int INDEX_GLHOMOALT;
    public static int INDEX_GQ;
    public static int INDEX_HQA;
    public static int INDEX_HQB;


    private void setIndexes() {
        INDEX_GATKID = this.getFieldIndexInDB(DBFIELDNAME_GATKID);
        INDEX_POLYPHENSIFTID = this.getFieldIndexInDB(DBFIELDNAME_POLYPHENSIFTID);
        INDEX_GENOMEID = this.getFieldIndexInDB(DBFIELDNAME_GENOMEID);
        INDEX_PIPELINEID = this.getFieldIndexInDB(DBFIELDNAME_PIPELINEID);
        INDEX_DNAID = this.getFieldIndexInDB(DBFIELDNAME_DNAID);
        INDEX_CHROM = this.getFieldIndexInDB(DBFIELDNAME_CHROM);
        INDEX_POSITION = this.getFieldIndexInDB(DBFIELDNAME_POSITION);
        INDEX_ID = this.getFieldIndexInDB(DBFIELDNAME_ID);
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
        INDEX_GT = this.getFieldIndexInDB(DBFIELDNAME_GT);
        INDEX_GPHASED = this.getFieldIndexInDB(DBFIELDNAME_GPHASED);
        INDEX_GDP = this.getFieldIndexInDB(DBFIELDNAME_GDP);
        INDEX_GFT = this.getFieldIndexInDB(DBFIELDNAME_GFT);
        INDEX_GLHOMOREF = this.getFieldIndexInDB(DBFIELDNAME_GLHOMOREF);
        INDEX_GLHET = this.getFieldIndexInDB(DBFIELDNAME_GLHET);
        INDEX_GLHOMOALT = this.getFieldIndexInDB(DBFIELDNAME_GLHOMOALT);
        INDEX_GQ = this.getFieldIndexInDB(DBFIELDNAME_GQ);
        INDEX_HQA = this.getFieldIndexInDB(DBFIELDNAME_HQA);
        INDEX_HQB = this.getFieldIndexInDB(DBFIELDNAME_HQB);
    }

    public VariantTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }

    private void addColumns() {
        addColumn(DBFIELDNAME_GATKID, ALIAS_GATKID, TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_POLYPHENSIFTID, ALIAS_POLYPHENSIFTID, TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_GENOMEID,ALIAS_GENOMEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_PIPELINEID,ALIAS_PIPELINEID,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_DNAID,ALIAS_DNAID,TableSchema.ColumnType.VARCHAR,10);
        addColumn(DBFIELDNAME_CHROM,ALIAS_CHROM,TableSchema.ColumnType.VARCHAR,5);
        addColumn(DBFIELDNAME_POSITION,ALIAS_POSITION,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_ID,ALIAS_ID,TableSchema.ColumnType.VARCHAR,45);
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
        addColumn(DBFIELDNAME_GT,ALIAS_GT,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_GPHASED,ALIAS_GPHASED,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_GDP,ALIAS_GDP,TableSchema.ColumnType.INTEGER,11);
        addColumn(DBFIELDNAME_GFT,ALIAS_GFT,TableSchema.ColumnType.VARCHAR,500);
        addColumn(DBFIELDNAME_GLHOMOREF,ALIAS_GLHOMOREF,TableSchema.ColumnType.FLOAT,1);
        addColumn(DBFIELDNAME_GLHET,ALIAS_GLHET,TableSchema.ColumnType.FLOAT,11);
        addColumn(DBFIELDNAME_GLHOMOALT,ALIAS_GLHOMOALT,TableSchema.ColumnType.FLOAT,11);
        addColumn(DBFIELDNAME_GQ,ALIAS_GQ,TableSchema.ColumnType.FLOAT,11);
        addColumn(DBFIELDNAME_HQA,ALIAS_HQA,TableSchema.ColumnType.FLOAT,11);
        addColumn(DBFIELDNAME_HQB,ALIAS_HQB,TableSchema.ColumnType.FLOAT,11);
    }

    public static VariantRecord convertToVariantRecord(Vector dbResult) {
        return new VariantRecord(
                (Integer) dbResult.get(INDEX_GENOMEID-1),
                (Integer) dbResult.get(INDEX_PIPELINEID-1),
                (String) dbResult.get(INDEX_DNAID-1),
                (String) dbResult.get(INDEX_CHROM-1),
                new Long((Integer) dbResult.get(INDEX_POSITION-1)),
                (String) dbResult.get(INDEX_ID-1),
                (String) dbResult.get(INDEX_REFERENCE-1),
                (String) dbResult.get(INDEX_ALTERNATE-1),
                (Float)  dbResult.get(INDEX_QUALITY-1),
                (String) dbResult.get(INDEX_FILTER-1),
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
                (String) dbResult.get(INDEX_CUSTOMINFO-1),
                (Integer) dbResult.get(INDEX_GT-1),
                (Integer) dbResult.get(INDEX_GPHASED-1),
                (Integer) dbResult.get(INDEX_GDP-1),
                (String) dbResult.get(INDEX_GFT-1),
                (Float) dbResult.get(INDEX_GLHOMOREF-1),
                (Float) dbResult.get(INDEX_GLHET-1),
                (Float) dbResult.get(INDEX_GLHOMOALT-1),
                (Float) dbResult.get(INDEX_GQ-1),
                (Float) dbResult.get(INDEX_HQA-1),
                (Float) dbResult.get(INDEX_HQB-1)              
                );
    }

}
