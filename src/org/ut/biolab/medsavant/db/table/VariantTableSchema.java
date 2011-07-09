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

    public static final String TABLE_NAME = "variant_sift_gatk";

    //public static final String DBFIELDNAME_GATKID = "annotation_gatk_id";
    //public static final String DBFIELDNAME_POLYPHENSIFTID = "annotation_polyphensift_id";
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
    public static final String DBFIELDNAME_position_a = "position_a";
    public static final String DBFIELDNAME_position_b = "position_b";
    public static final String DBFIELDNAME_o_acc = "o_acc";
    public static final String DBFIELDNAME_o_pos = "o_pos";
    public static final String DBFIELDNAME_o_aa1 = "o_aa1";
    public static final String DBFIELDNAME_o_aa2 = "o_aa2";
    public static final String DBFIELDNAME_snp_id = "snp_id";
    public static final String DBFIELDNAME_acc = "acc";
    public static final String DBFIELDNAME_pos = "pos";
    public static final String DBFIELDNAME_aa1 = "aa1";
    public static final String DBFIELDNAME_aa2 = "aa2";
    public static final String DBFIELDNAME_nt1 = "nt1";
    public static final String DBFIELDNAME_nt2 = "nt2";
    public static final String DBFIELDNAME_prediction = "prediction";
    public static final String DBFIELDNAME_pph2_class = "pph2_class";
    public static final String DBFIELDNAME_pph2_prob = "pph2_prob";
    public static final String DBFIELDNAME_pph2_FPR = "pph2_FPR";
    public static final String DBFIELDNAME_pph2_TPR = "pph2_TPR";
    public static final String DBFIELDNAME_pph2_FDR = "pph2_FDR";
    public static final String DBFIELDNAME_Transv = "Transv";
    public static final String DBFIELDNAME_CodPos = "CodPos";
    public static final String DBFIELDNAME_CpG = "CpG";
    public static final String DBFIELDNAME_MinDJnc = "MinDJnc";
    public static final String DBFIELDNAME_PfamHit = "PfamHit";
    public static final String DBFIELDNAME_IdPmax = "IdPmax";
    public static final String DBFIELDNAME_IdPSNP = "IdPSNP";
    public static final String DBFIELDNAME_IdQmin = "IdQmin";
    public static final String DBFIELDNAME_sift_prediction = "sift_prediction";
    public static final String DBFIELDNAME_name = "name";
    public static final String DBFIELDNAME_name2 = "name2";
    public static final String DBFIELDNAME_transcriptStrand = "transcriptStrand";
    public static final String DBFIELDNAME_positionType = "positionType";
    public static final String DBFIELDNAME_frame = "frame";
    public static final String DBFIELDNAME_mrnaCoord = "mrnaCoord";
    public static final String DBFIELDNAME_codonCoord = "codonCoord";
    public static final String DBFIELDNAME_spliceDist = "spliceDist";
    public static final String DBFIELDNAME_referenceCodon = "referenceCodon";
    public static final String DBFIELDNAME_referenceAA = "referenceAA";
    public static final String DBFIELDNAME_variantCodon = "variantCodon";
    public static final String DBFIELDNAME_variantAA = "variantAA";
    public static final String DBFIELDNAME_changesAA = "changesAA";
    public static final String DBFIELDNAME_functionalClass = "functionalClass";
    public static final String DBFIELDNAME_codingCoordStr = "codingCoordStr";
    public static final String DBFIELDNAME_proteinCoordStr = "proteinCoordStr";
    public static final String DBFIELDNAME_inCodingRegion = "inCodingRegion";
    public static final String DBFIELDNAME_spliceInfo = "spliceInfo";
    public static final String DBFIELDNAME_uorfChange = "uorfChange";

    public static final String ALIAS_GATKID = "Gatk ID";
    public static final String ALIAS_POLYPHENSIFTID = "Polyphensift ID";
    public static final String ALIAS_GENOMEID = "Genome ID";
    public static final String ALIAS_PIPELINEID = "Pipeline ID";
    public static final String ALIAS_DNAID = "DNA ID";
    public static final String ALIAS_CHROM = "Chromosome";
    public static final String ALIAS_POSITION = "Position";
    public static final String ALIAS_ID = "ID";
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
    public static final String ALIAS_position_a = "position_a";
    public static final String ALIAS_position_b = "position_b";
    public static final String ALIAS_o_acc = "Protein Identifier";
    public static final String ALIAS_o_pos = "Position in Protein";
    public static final String ALIAS_o_aa1 = "Reference AA";
    public static final String ALIAS_o_aa2 = "Variant AA";
    public static final String ALIAS_snp_id = "SNP ID";
    public static final String ALIAS_acc = "UniProtKB Accession";
    public static final String ALIAS_pos = "Substitution Positon";
    public static final String ALIAS_aa1 = "Reference AA";
    public static final String ALIAS_aa2 = "Variant AA";
    public static final String ALIAS_nt1 = "Reference Nucleotide";
    public static final String ALIAS_nt2 = "Variant Nucleotide";
    public static final String ALIAS_prediction = "Polyphen-2 Prediction";
    public static final String ALIAS_pph2_class = "Polyphen-2 Classifier";
    public static final String ALIAS_pph2_prob = "Polyphen-2 Damage Probability";
    public static final String ALIAS_pph2_FPR = "Polyphen-2 FRP";
    public static final String ALIAS_pph2_TPR = "Polyphen-2 TRP";
    public static final String ALIAS_pph2_FDR = "Polyphen-2 FDR";
    public static final String ALIAS_Transv = "Transversion";
    public static final String ALIAS_CodPos = "Codon Position";
    public static final String ALIAS_CpG = "Changes CpG";
    public static final String ALIAS_MinDJnc = "Distance to Intron-Exon Junction";
    public static final String ALIAS_PfamHit = "Pfam of Protein";
    public static final String ALIAS_IdPmax = "Max Congruency of Mutant AA (all)";
    public static final String ALIAS_IdPSNP = "Max Congruency of Mutant AA (like)";
    public static final String ALIAS_IdQmin = "Closest Homologue";
    public static final String ALIAS_sift_prediction = "Sift Prediction";
    public static final String ALIAS_name = "Gene Name";
    public static final String ALIAS_name2 = "Secondary Gene Name";
    public static final String ALIAS_transcriptStrand = "Transcript Strand";
    public static final String ALIAS_positionType = "Position in Transcript";
    public static final String ALIAS_frame = "Frame";
    public static final String ALIAS_mrnaCoord = "mRNA Coordinate";
    public static final String ALIAS_codonCoord = "Codon Coordinate";
    public static final String ALIAS_spliceDist = "Distance to Splice Junction";
    public static final String ALIAS_referenceCodon = "Reference Codon";
    public static final String ALIAS_referenceAA = "Reference AA";
    public static final String ALIAS_variantCodon = "Variant Codon";
    public static final String ALIAS_variantAA = "Variant AA";
    public static final String ALIAS_changesAA = "Changes AA";
    public static final String ALIAS_functionalClass = "Functional Class";
    public static final String ALIAS_codingCoordStr = "Coding Coordinate";
    public static final String ALIAS_proteinCoordStr = "Protein Coordinate";
    public static final String ALIAS_inCodingRegion = "Coding / Non-Coding";
    public static final String ALIAS_spliceInfo = "Splice Information";
    public static final String ALIAS_uorfChange = "5' UTR ORF Change";

    //public static int INDEX_GATKID;
    //public static int INDEX_POLYPHENSIFTID;
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
    public static int INDEX_position_a;
    public static int INDEX_position_b;
    public static int INDEX_o_acc;
    public static int INDEX_o_pos;
    public static int INDEX_o_aa1;
    public static int INDEX_o_aa2;
    public static int INDEX_snp_id;
    public static int INDEX_acc;
    public static int INDEX_pos;
    public static int INDEX_aa1;
    public static int INDEX_aa2;
    public static int INDEX_nt1;
    public static int INDEX_nt2;
    public static int INDEX_prediction;
    public static int INDEX_pph2_class;
    public static int INDEX_pph2_prob;
    public static int INDEX_pph2_FPR;
    public static int INDEX_pph2_TPR;
    public static int INDEX_pph2_FDR;
    public static int INDEX_Transv;
    public static int INDEX_CodPos;
    public static int INDEX_CpG;
    public static int INDEX_MinDJnc;
    public static int INDEX_PfamHit;
    public static int INDEX_IdPmax;
    public static int INDEX_IdPSNP;
    public static int INDEX_IdQmin;
    public static int INDEX_sift_prediction;
    public static int INDEX_name;
    public static int INDEX_name2;
    public static int INDEX_transcriptStrand;
    public static int INDEX_positionType;
    public static int INDEX_frame;
    public static int INDEX_mrnaCoord;
    public static int INDEX_codonCoord;
    public static int INDEX_spliceDist;
    public static int INDEX_referenceCodon;
    public static int INDEX_referenceAA;
    public static int INDEX_variantCodon;
    public static int INDEX_variantAA;
    public static int INDEX_changesAA;
    public static int INDEX_functionalClass;
    public static int INDEX_codingCoordStr;
    public static int INDEX_proteinCoordStr;
    public static int INDEX_inCodingRegion;
    public static int INDEX_spliceInfo;
    public static int INDEX_uorfChange;

    private void setIndexes() {
        //INDEX_GATKID = this.getFieldIndexInDB(DBFIELDNAME_GATKID);
        //INDEX_POLYPHENSIFTID = this.getFieldIndexInDB(DBFIELDNAME_POLYPHENSIFTID);
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
        INDEX_position_a = this.getFieldIndexInDB(DBFIELDNAME_position_a);
INDEX_position_b = this.getFieldIndexInDB(DBFIELDNAME_position_b);
INDEX_o_acc = this.getFieldIndexInDB(DBFIELDNAME_o_acc);
INDEX_o_pos = this.getFieldIndexInDB(DBFIELDNAME_o_pos);
INDEX_o_aa1 = this.getFieldIndexInDB(DBFIELDNAME_o_aa1);
INDEX_o_aa2 = this.getFieldIndexInDB(DBFIELDNAME_o_aa2);
INDEX_snp_id = this.getFieldIndexInDB(DBFIELDNAME_snp_id);
INDEX_acc = this.getFieldIndexInDB(DBFIELDNAME_acc);
INDEX_pos = this.getFieldIndexInDB(DBFIELDNAME_pos);
INDEX_aa1 = this.getFieldIndexInDB(DBFIELDNAME_aa1);
INDEX_aa2 = this.getFieldIndexInDB(DBFIELDNAME_aa2);
INDEX_nt1 = this.getFieldIndexInDB(DBFIELDNAME_nt1);
INDEX_nt2 = this.getFieldIndexInDB(DBFIELDNAME_nt2);
INDEX_prediction = this.getFieldIndexInDB(DBFIELDNAME_prediction);
INDEX_pph2_class = this.getFieldIndexInDB(DBFIELDNAME_pph2_class);
INDEX_pph2_prob = this.getFieldIndexInDB(DBFIELDNAME_pph2_prob);
INDEX_pph2_FPR = this.getFieldIndexInDB(DBFIELDNAME_pph2_FPR);
INDEX_pph2_TPR = this.getFieldIndexInDB(DBFIELDNAME_pph2_TPR);
INDEX_pph2_FDR = this.getFieldIndexInDB(DBFIELDNAME_pph2_FDR);
INDEX_Transv = this.getFieldIndexInDB(DBFIELDNAME_Transv);
INDEX_CodPos = this.getFieldIndexInDB(DBFIELDNAME_CodPos);
INDEX_CpG = this.getFieldIndexInDB(DBFIELDNAME_CpG);
INDEX_MinDJnc = this.getFieldIndexInDB(DBFIELDNAME_MinDJnc);
INDEX_PfamHit = this.getFieldIndexInDB(DBFIELDNAME_PfamHit);
INDEX_IdPmax = this.getFieldIndexInDB(DBFIELDNAME_IdPmax);
INDEX_IdPSNP = this.getFieldIndexInDB(DBFIELDNAME_IdPSNP);
INDEX_IdQmin = this.getFieldIndexInDB(DBFIELDNAME_IdQmin);
INDEX_sift_prediction = this.getFieldIndexInDB(DBFIELDNAME_sift_prediction);
INDEX_name = this.getFieldIndexInDB(DBFIELDNAME_name);
INDEX_name2 = this.getFieldIndexInDB(DBFIELDNAME_name2);
INDEX_transcriptStrand = this.getFieldIndexInDB(DBFIELDNAME_transcriptStrand);
INDEX_positionType = this.getFieldIndexInDB(DBFIELDNAME_positionType);
INDEX_frame = this.getFieldIndexInDB(DBFIELDNAME_frame);
INDEX_mrnaCoord = this.getFieldIndexInDB(DBFIELDNAME_mrnaCoord);
INDEX_codonCoord = this.getFieldIndexInDB(DBFIELDNAME_codonCoord);
INDEX_spliceDist = this.getFieldIndexInDB(DBFIELDNAME_spliceDist);
INDEX_referenceCodon = this.getFieldIndexInDB(DBFIELDNAME_referenceCodon);
INDEX_referenceAA = this.getFieldIndexInDB(DBFIELDNAME_referenceAA);
INDEX_variantCodon = this.getFieldIndexInDB(DBFIELDNAME_variantCodon);
INDEX_variantAA = this.getFieldIndexInDB(DBFIELDNAME_variantAA);
INDEX_changesAA = this.getFieldIndexInDB(DBFIELDNAME_changesAA);
INDEX_functionalClass = this.getFieldIndexInDB(DBFIELDNAME_functionalClass);
INDEX_codingCoordStr = this.getFieldIndexInDB(DBFIELDNAME_codingCoordStr);
INDEX_proteinCoordStr = this.getFieldIndexInDB(DBFIELDNAME_proteinCoordStr);
INDEX_inCodingRegion = this.getFieldIndexInDB(DBFIELDNAME_inCodingRegion);
INDEX_spliceInfo = this.getFieldIndexInDB(DBFIELDNAME_spliceInfo);
INDEX_uorfChange = this.getFieldIndexInDB(DBFIELDNAME_uorfChange);
        
    }

    public VariantTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
        setIndexes();
    }

    private void addColumns() {
        //addColumn(DBFIELDNAME_GATKID, ALIAS_GATKID, TableSchema.ColumnType.INTEGER,11);
        //addColumn(DBFIELDNAME_POLYPHENSIFTID, ALIAS_POLYPHENSIFTID, TableSchema.ColumnType.INTEGER,11);
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
        
        addColumn(DBFIELDNAME_position_a,ALIAS_position_a,TableSchema.ColumnType.VARCHAR,75);
addColumn(DBFIELDNAME_position_b,ALIAS_position_b,TableSchema.ColumnType.VARCHAR,75);
addColumn(DBFIELDNAME_o_acc,ALIAS_o_acc,TableSchema.ColumnType.VARCHAR,75);
addColumn(DBFIELDNAME_o_pos,ALIAS_o_pos,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_o_aa1,ALIAS_o_aa1,TableSchema.ColumnType.VARCHAR,5);
addColumn(DBFIELDNAME_o_aa2,ALIAS_o_aa2,TableSchema.ColumnType.VARCHAR,10);
addColumn(DBFIELDNAME_snp_id,ALIAS_snp_id,TableSchema.ColumnType.VARCHAR,45);
addColumn(DBFIELDNAME_acc,ALIAS_acc,TableSchema.ColumnType.VARCHAR,45);
addColumn(DBFIELDNAME_pos,ALIAS_pos,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_aa1,ALIAS_aa1,TableSchema.ColumnType.VARCHAR,5);
addColumn(DBFIELDNAME_aa2,ALIAS_aa2,TableSchema.ColumnType.VARCHAR,20);
addColumn(DBFIELDNAME_nt1,ALIAS_nt1,TableSchema.ColumnType.VARCHAR,5);
addColumn(DBFIELDNAME_nt2,ALIAS_nt2,TableSchema.ColumnType.VARCHAR,20);
addColumn(DBFIELDNAME_prediction,ALIAS_prediction,TableSchema.ColumnType.VARCHAR,45);
addColumn(DBFIELDNAME_pph2_class,ALIAS_pph2_class,TableSchema.ColumnType.VARCHAR,45);
addColumn(DBFIELDNAME_pph2_prob,ALIAS_pph2_prob,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_pph2_FPR,ALIAS_pph2_FPR,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_pph2_TPR,ALIAS_pph2_TPR,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_pph2_FDR,ALIAS_pph2_FDR,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_Transv,ALIAS_Transv,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_CodPos,ALIAS_CodPos,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_CpG,ALIAS_CpG,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_MinDJnc,ALIAS_MinDJnc,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_PfamHit,ALIAS_PfamHit,TableSchema.ColumnType.VARCHAR,45);
addColumn(DBFIELDNAME_IdPmax,ALIAS_IdPmax,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_IdPSNP,ALIAS_IdPSNP,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_IdQmin,ALIAS_IdQmin,TableSchema.ColumnType.FLOAT,11);
addColumn(DBFIELDNAME_sift_prediction,ALIAS_sift_prediction,TableSchema.ColumnType.FLOAT,45);
addColumn(DBFIELDNAME_name,ALIAS_name,TableSchema.ColumnType.VARCHAR,30);
addColumn(DBFIELDNAME_name2,ALIAS_name2,TableSchema.ColumnType.VARCHAR,30);
addColumn(DBFIELDNAME_transcriptStrand,ALIAS_transcriptStrand,TableSchema.ColumnType.VARCHAR,1);
addColumn(DBFIELDNAME_positionType,ALIAS_positionType,TableSchema.ColumnType.VARCHAR,50);
addColumn(DBFIELDNAME_frame,ALIAS_frame,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_mrnaCoord,ALIAS_mrnaCoord,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_codonCoord,ALIAS_codonCoord,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_spliceDist,ALIAS_spliceDist,TableSchema.ColumnType.INTEGER,11);
addColumn(DBFIELDNAME_referenceCodon,ALIAS_referenceCodon,TableSchema.ColumnType.VARCHAR,5);
addColumn(DBFIELDNAME_referenceAA,ALIAS_referenceAA,TableSchema.ColumnType.VARCHAR,50);
addColumn(DBFIELDNAME_variantCodon,ALIAS_variantCodon,TableSchema.ColumnType.VARCHAR,5);
addColumn(DBFIELDNAME_variantAA,ALIAS_variantAA,TableSchema.ColumnType.VARCHAR,50);
addColumn(DBFIELDNAME_changesAA,ALIAS_changesAA,TableSchema.ColumnType.VARCHAR,255);
addColumn(DBFIELDNAME_functionalClass,ALIAS_functionalClass,TableSchema.ColumnType.VARCHAR,30);
addColumn(DBFIELDNAME_codingCoordStr,ALIAS_codingCoordStr,TableSchema.ColumnType.VARCHAR,30);
addColumn(DBFIELDNAME_proteinCoordStr,ALIAS_proteinCoordStr,TableSchema.ColumnType.VARCHAR,20);
addColumn(DBFIELDNAME_inCodingRegion,ALIAS_inCodingRegion,TableSchema.ColumnType.VARCHAR,255);
addColumn(DBFIELDNAME_spliceInfo,ALIAS_spliceInfo,TableSchema.ColumnType.VARCHAR,30);
addColumn(DBFIELDNAME_uorfChange,ALIAS_uorfChange,TableSchema.ColumnType.VARCHAR,10);

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
                (Float) dbResult.get(INDEX_HQB-1),
                
                (String) dbResult.get(INDEX_position_a-1),
                (String) dbResult.get(INDEX_position_b-1),
                (String) dbResult.get(INDEX_o_acc-1),
                (Integer) dbResult.get(INDEX_o_pos-1),
                (String) dbResult.get(INDEX_o_aa1-1),
                (String) dbResult.get(INDEX_o_aa2-1),
                (String) dbResult.get(INDEX_snp_id-1),
                (String) dbResult.get(INDEX_acc-1),
                (Integer) dbResult.get(INDEX_pos-1),
                (String) dbResult.get(INDEX_aa1-1),
                (String) dbResult.get(INDEX_aa2-1),
                (String) dbResult.get(INDEX_nt1-1),
                (String) dbResult.get(INDEX_nt2-1),
                (String) dbResult.get(INDEX_prediction-1),
                (String) dbResult.get(INDEX_pph2_class-1),
                (Float) dbResult.get(INDEX_pph2_prob-1),
                (Float) dbResult.get(INDEX_pph2_FPR-1),
                (Float) dbResult.get(INDEX_pph2_TPR-1),
                (Float) dbResult.get(INDEX_pph2_FDR-1),
                (Integer) dbResult.get(INDEX_Transv-1),
                (Integer) dbResult.get(INDEX_CodPos-1),
                (Integer) dbResult.get(INDEX_CpG-1),
                (Float) dbResult.get(INDEX_MinDJnc-1),
                (String) dbResult.get(INDEX_PfamHit-1),
                (Float) dbResult.get(INDEX_IdPmax-1),
                (Float) dbResult.get(INDEX_IdPSNP-1),
                (Float) dbResult.get(INDEX_IdQmin-1),
                (Float) dbResult.get(INDEX_sift_prediction-1),
                (String) dbResult.get(INDEX_name-1),
                (String) dbResult.get(INDEX_name2-1),
                (String) dbResult.get(INDEX_transcriptStrand-1),
                (String) dbResult.get(INDEX_positionType-1),
                (Integer) dbResult.get(INDEX_frame-1),
                (Integer) dbResult.get(INDEX_mrnaCoord-1),
                (Integer) dbResult.get(INDEX_codonCoord-1),
                (Integer) dbResult.get(INDEX_spliceDist-1),
                (String) dbResult.get(INDEX_referenceCodon-1),
                (String) dbResult.get(INDEX_referenceAA-1),
                (String) dbResult.get(INDEX_variantCodon-1),
                (String) dbResult.get(INDEX_variantAA-1),
                (String) dbResult.get(INDEX_changesAA-1),
                (String) dbResult.get(INDEX_functionalClass-1),
                (String) dbResult.get(INDEX_codingCoordStr-1),
                (String) dbResult.get(INDEX_proteinCoordStr-1),
                (String) dbResult.get(INDEX_inCodingRegion-1),
                (String) dbResult.get(INDEX_spliceInfo-1),
                (String) dbResult.get(INDEX_uorfChange-1)
                );
    }

}
