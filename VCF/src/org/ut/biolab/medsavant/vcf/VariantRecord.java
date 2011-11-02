/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.vcf;

import java.io.Serializable;
import java.util.Vector;
//import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
//import org.ut.biolab.medsavant.olddb.table.VariantAnnotationSiftTableSchema;
//import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class VariantRecord implements Serializable {

    private static final int FILE_INDEX_OF_CHROM = 0;
    private static final int FILE_INDEX_OF_POS = FILE_INDEX_OF_CHROM + 1;
    private static final int FILE_INDEX_OF_DBSNPID = FILE_INDEX_OF_POS + 1;
    private static final int FILE_INDEX_OF_REF = FILE_INDEX_OF_DBSNPID + 1;
    private static final int FILE_INDEX_OF_ALT = FILE_INDEX_OF_REF + 1;
    private static final int FILE_INDEX_OF_QUAL = FILE_INDEX_OF_ALT + 1;
    private static final int FILE_INDEX_OF_FILTER = FILE_INDEX_OF_QUAL + 1;
    private static final int FILE_INDEX_OF_INFO = FILE_INDEX_OF_FILTER + 1;

    //variant
    public static final Class CLASS_OF_VARIANTID = Integer.class;
    public static final Class CLASS_OF_GENOMEID = Integer.class;
    public static final Class CLASS_OF_PIPELINEID = String.class;
    public static final Class CLASS_OF_DNAID = String.class;
    public static final Class CLASS_OF_CHROM = String.class;
    public static final Class CLASS_OF_POSITION = Long.class;
    public static final Class CLASS_OF_DBSNPID = String.class;
    public static final Class CLASS_OF_REF = String.class;
    public static final Class CLASS_OF_ALT = String.class;
    public static final Class CLASS_OF_QUAL = Float.class;
    public static final Class CLASS_OF_FILTER = String.class;
    public static final Class CLASS_OF_AA = String.class;
    public static final Class CLASS_OF_AC = String.class;
    public static final Class CLASS_OF_AF = String.class;
    public static final Class CLASS_OF_AN = Integer.class;
    public static final Class CLASS_OF_BQ = Float.class;
    public static final Class CLASS_OF_CIGAR = String.class;
    public static final Class CLASS_OF_DB = Boolean.class;
    public static final Class CLASS_OF_DP = Integer.class;
    public static final Class CLASS_OF_END = Long.class;
    public static final Class CLASS_OF_H2 = Boolean.class;
    public static final Class CLASS_OF_MQ = Float.class;
    public static final Class CLASS_OF_MQ0 = Integer.class;
    public static final Class CLASS_OF_NS = Integer.class;
    public static final Class CLASS_OF_SB = Float.class;
    public static final Class CLASS_OF_SOMATIC = Boolean.class;
    public static final Class CLASS_OF_VALIDATED = Boolean.class;
    public static final Class CLASS_OF_CUSTOMINFO = String.class;

    //sift
    public static final Class CLASS_OF_NAME_SIFT = String.class;
    public static final Class CLASS_OF_NAME2_SIFT = String.class;
    public static final Class CLASS_OF_DAMAGEPROBABILITY = Double.class;
    
    //polyphen
    public static final Class CLASS_OF_CDNACOORD = String.class;
    public static final Class CLASS_OF_OPOS = Integer.class;
    public static final Class CLASS_OF_OAA1 = String.class;
    public static final Class CLASS_OF_OAA2 = String.class;
    public static final Class CLASS_OF_SNPID = String.class;
    public static final Class CLASS_OF_ACC = String.class;
    public static final Class CLASS_OF_POS = Integer.class;
    public static final Class CLASS_OF_PREDICTION = String.class;
    public static final Class CLASS_OF_PPH2CLASS = String.class;
    public static final Class CLASS_OF_PPH2PROB = Float.class;
    public static final Class CLASS_OF_PPH2FPR = Float.class;
    public static final Class CLASS_OF_PPH2TPR = Float.class;
    public static final Class CLASS_OF_PPH2FDR = Float.class;
    public static final Class CLASS_OF_TRANSV = Integer.class;
    public static final Class CLASS_OF_CODPOS = Integer.class;
    public static final Class CLASS_OF_CPG = Integer.class;
    public static final Class CLASS_OF_MINDJNC = Float.class;
    public static final Class CLASS_OF_IDPMAX = Float.class;
    public static final Class CLASS_OF_IDPSNP = Float.class;
    public static final Class CLASS_OF_IDQMIN = Float.class;    
    
    //gatk
    public static final Class CLASS_OF_NAME_GATK = String.class;
    public static final Class CLASS_OF_NAME2_GATK = String.class;
    public static final Class CLASS_OF_TRANSCRIPTSTRAND = String.class;
    public static final Class CLASS_OF_POSITIONTYPE = String.class;
    public static final Class CLASS_OF_FRAME = Integer.class;
    public static final Class CLASS_OF_MRNACOORD = Integer.class;
    public static final Class CLASS_OF_CODONCOORD = Integer.class;
    public static final Class CLASS_OF_SPLICEDIST = Integer.class;
    public static final Class CLASS_OF_REFERENCECODON = String.class;
    public static final Class CLASS_OF_REFERENCEAA = String.class;
    public static final Class CLASS_OF_VARIANTCODON = String.class;
    public static final Class CLASS_OF_VARIANTAA = String.class;
    public static final Class CLASS_OF_CHANGESAA = String.class;
    public static final Class CLASS_OF_FUNCTIONALCLASS = String.class;
    public static final Class CLASS_OF_CODINGCOORDSTR = String.class;
    public static final Class CLASS_OF_PROTEINCOORDSTR = String.class;
    public static final Class CLASS_OF_INCODINGREGION = String.class;
    public static final Class CLASS_OF_SPLICEINFO = String.class;
    public static final Class CLASS_OF_UORFCHANGE = String.class;
    public static final Class CLASS_OF_SPLICEINFOCOPY = String.class;
    
    //variant
    
    private int uploadID;
    private int fileID;
    private int variantID;
    
    private int genomeID;
    private int pipelineID;
    private String dnaID;
    private String chrom;
    private long position;
    private String dbSNPID;
    private String ref;
    private String alt;
    private float qual;
    private String filter;
    private String aa;
    private String ac;
    private String af;
    private int an;
    private float bq;
    private String cigar;
    private boolean db;
    private int dp;
    private long end;
    private boolean h2;
    private float mq;
    private int mq0;
    private int ns;
    private float sb;
    private boolean somatic;
    private boolean validated;
    private String customInfo;
     
    //sift
    private String name_sift;
    private String name2_sift;
    private double damage_probability;
    
    //polyphen
    private String cdnacoord;
    private int opos;
    private String oaa1;
    private String oaa2;
    private String snpid;
    private String acc;
    private int pos;
    private String prediction;
    private String pph2class;
    private float pph2prob;
    private float pph2fpr;
    private float pph2tpr;
    private float pph2fdr;
    private int transv;
    private int codpos;
    private int cpg;
    private float mindjnc;
    private float idpmax;
    private float idpsnp;
    private float idqmin;
    
    //gatk
    private String name_gatk;
    private String name2_gatk;
    private String transcriptStrand;
    private String positionType;
    private int frame;
    private int mrnaCoord;
    private int codonCoord;
    private int spliceDist;
    private String referenceCodon;
    private String referenceAA;
    private String variantCodon;
    private String variantAA;
    private String changesAA;
    private String functionalClass;
    private String codingCoordStr;
    private String proteinCoordStr;
    private String inCodingRegion;
    private String spliceInfo;
    private String uorfChange;
    private String spliceInfoCopy;
    
    public static final String nullString = ".";

    private enum CustomField {
        AA, AC, AF, AN, BQ, CIGAR, DB, DP, END, H2, MQ, MQ0, NS, SB, SOMATIC, VALIDATED;
    }
    
    /*public enum GenotypeField {
        GT, DP, FT, GL, GQ, HQ, NOTSTANDARD;
    }
    
    public static int ZYGOSITY_UNKNOWN = 0;
    public static int ZYGOSITY_HOMOREF = 1;
    public static int ZYGOSITY_HOMOALT = 2;
    public static int ZYGOSITY_HETERO = 3;
    public static String[] ALIAS_ZYGOSITY = {"Unknown", "HomoRef", "HomoAlt", "Hetero"};
    
    public static int PHASE_NA = 0;
    public static int PHASE_UNPHASED = 1;
    public static int PHASE_PHASED = 2;*/
    
    /*
     * DO NOT USE THIS UNLESS YOU KNOW WHAT YOU'RE DOING
     * Useful for creating empty record in some circumstances
     */
    public VariantRecord() {}
  
    public VariantRecord(String[] line) {
        dnaID =  null;
        chrom =     (String)    parse(CLASS_OF_CHROM, line[FILE_INDEX_OF_CHROM]);
        position =       (Long)      parse(CLASS_OF_POSITION, line[FILE_INDEX_OF_POS]);
        dbSNPID =        (String)    parse(CLASS_OF_DBSNPID, line[FILE_INDEX_OF_DBSNPID]);
        ref =       (String)    parse(CLASS_OF_REF, line[FILE_INDEX_OF_REF]);
        alt =       (String)    parse(CLASS_OF_ALT, line[FILE_INDEX_OF_ALT]);
        qual =      (Float)     parse(CLASS_OF_QUAL, line[FILE_INDEX_OF_QUAL]);
        filter =    (String)    parse(CLASS_OF_FILTER, line[FILE_INDEX_OF_FILTER]);
        parseInfo(line[FILE_INDEX_OF_INFO]);        
    }

    public VariantRecord(
            
            //variant
            int variantID,
            int genomeID,
            int pipelineID,
            String dnaID,
            String chrom,
            long position,
            String dbSNPID,
            String ref,
            String alt,
            float qual,
            String filter,
            String aa,
            String ac,
            String af,
            int an,
            float bq,
            String cigar,
            boolean db,
            int dp,
            long end,
            boolean h2,
            float mq,
            int mq0,
            int ns,
            float sb,
            boolean somatic,
            boolean validated,
            String customInfo,
            
            //sift
            String name_sift,
            String name2_sift,
            double damage_probability,
            
            //polyphen
            String cdnacoord,
            int opos,
            String oaa1,
            String oaa2,
            String snpid,
            String acc,
            int pos,
            String prediction,
            String pph2class,
            float pph2prob,
            float pph2fpr,
            float pph2tpr,
            float pph2fdr,
            int transv,
            int codpos,
            int cpg,
            float mindjnc,
            float idpmax,
            float idpsnp,
            float idqmin,
            
            //gatk
            String name_gatk,
            String name2_gatk,
            String transcriptStrand,
            String positionType,
            int frame,
            int mrnaCoord,
            int codonCoord,
            int spliceDist,
            String referenceCodon,
            String referenceAA,
            String variantCodon,
            String variantAA,
            String changesAA,
            String functionalClass,
            String codingCoordStr,
            String proteinCoordStr,
            String inCodingRegion,
            String spliceInfo,
            String uorfChange,
            String spliceInfoCopy
            
            ){
        
        //variant
        this.variantID = variantID;
        this.genomeID = genomeID;
        this.pipelineID = pipelineID;
        this.dnaID = dnaID;
        this.chrom = chrom;
        this.position = position;
        this.dbSNPID = dbSNPID;
        this.ref = ref;
        this.alt = alt;
        this.qual = qual;
        this.filter = filter;
        this.aa = aa;
        this.ac = ac;
        this.af = af;
        this.an = an;
        this.bq = bq;
        this.cigar = cigar;
        this.db = db;
        this.dp = dp;
        this.end = end;
        this.h2 = h2;
        this.mq = mq;
        this.mq0 = mq0;
        this.ns = ns;
        this.sb = sb;
        this.somatic = somatic;
        this.validated = validated;
        this.customInfo = customInfo;
        
        //sift
        this.name_sift = name_sift;
        this.name2_sift = name2_sift;
        this.damage_probability = damage_probability;
        
        //polyphen
        this.cdnacoord = cdnacoord;
        this.opos = opos;
        this.oaa1 = oaa1;
        this.oaa2 = oaa2;
        this.snpid = snpid;
        this.acc = acc;
        this.pos = pos;
        this.prediction = prediction;
        this.pph2class = pph2class;
        this.pph2prob = pph2prob;
        this.pph2fpr = pph2fpr;
        this.pph2tpr = pph2tpr;
        this.pph2fdr = pph2fdr;
        this.transv = transv;
        this.codpos = codpos;
        this.cpg = cpg;
        this.mindjnc = mindjnc;
        this.idpmax = idpmax;
        this.idpsnp = idpsnp;
        this.idqmin = idqmin;
        
        //gatk
        this.name_gatk = name_gatk;
        this.name2_gatk = name2_gatk;
        this.transcriptStrand = transcriptStrand;
        this.positionType = positionType;
        this.frame = frame;
        this.mrnaCoord = mrnaCoord;
        this.codonCoord = codonCoord;
        this.spliceDist = spliceDist;
        this.referenceCodon = referenceCodon;
        this.referenceAA = referenceAA;
        this.variantCodon = variantCodon;
        this.variantAA = variantAA;
        this.changesAA = changesAA;
        this.functionalClass = functionalClass;
        this.codingCoordStr = codingCoordStr;
        this.proteinCoordStr = proteinCoordStr;
        this.inCodingRegion = inCodingRegion;
        this.spliceInfo = spliceInfo;
        this.uorfChange = uorfChange;
        this.spliceInfoCopy = spliceInfoCopy;
    }     
    
    protected VariantRecord(VariantRecord r) {
        
        //variant
        this.setVariantID(r.getVariantID());
        this.setDnaID(r.getDnaID());
        this.setGenomeID(r.getGenomeID());
        this.setPipelineID(r.getPipelineID());
        this.setChrom(r.getChrom());
        this.setPosition(r.getPosition());
        this.setDbSNPID(r.getDbSNPID());
        this.setRef(r.getRef());
        this.setAlt(r.getAlt());
        this.setQual(r.getQual());
        this.setFilter(r.getFilter());
        this.setAA(r.getAA());
        this.setAC(r.getAC());
        this.setAF(r.getAF());
        this.setAN(r.getAN());
        this.setBQ(r.getBQ());
        this.setCigar(r.getCigar());
        this.setDB(r.getDB());
        this.setDP(r.getDP());
        this.setEnd(r.getEnd());
        this.setH2(r.getH2());
        this.setMQ(r.getMQ());
        this.setMQ0(r.getMQ0());
        this.setNS(r.getNS());
        this.setSB(r.getSB());
        this.setSomatic(r.getSomatic());
        this.setValidated(r.getValidated());
        this.setCustomInfo(r.getCustomInfo());
        
        //sift
        this.setNameSift(r.getNameSift());
        this.setName2Sift(r.getName2Sift());
        this.setDamageProbability(r.getDamageProbability());
        
        //polyphen
        this.setCdnacoord(r.getCdnacoord());
        this.setOpos(r.getOpos());
        this.setOaa1(r.getOaa1());
        this.setOaa2(r.getOaa2());
        this.setSnpid(r.getSnpid());
        this.setAcc(r.getAcc());
        this.setPos(r.getPos());
        this.setPrediction(r.getPrediction());
        this.setPph2class(r.getPph2class());
        this.setPph2prob(r.getPph2prob());
        this.setPph2fpr(r.getPph2fpr());
        this.setPph2tpr(r.getPph2tpr());
        this.setPph2fdr(r.getPph2fdr());
        this.setTransv(r.getTransv());
        this.setCodpos(r.getCodpos());
        this.setCpg(r.getCpg());
        this.setMindjnc(r.getMindjnc());
        this.setIdpmax(r.getIdpmax());
        this.setIdpsnp(r.getIdpsnp());
        this.setIdqmin(r.getIdqmin());
        
        //gatk
        this.setName_gatk(r.getName_gatk());
        this.setName2_gatk(r.getName2_gatk());
        this.setTranscriptStrand(r.getTranscriptStrand());
        this.setPositionType(r.getPositionType());
        this.setFrame(r.getFrame());
        this.setMrnaCoord(r.getMrnaCoord());
        this.setCodonCoord(r.getCodonCoord());
        this.setSpliceDist(r.getSpliceDist());
        this.setReferenceCodon(r.getReferenceCodon());
        this.setReferenceAA(r.getReferenceAA());
        this.setVariantCodon(r.getVariantCodon());
        this.setVariantAA(r.getVariantAA());
        this.setChangesAA(r.getChangesAA());
        this.setFunctionalClass(r.getFunctionalClass());
        this.setCodingCoordStr(r.getCodingCoordStr());
        this.setProteinCoordStr(r.getProteinCoordStr());
        this.setInCodingRegion(r.getInCodingRegion());
        this.setSpliceInfo(r.getSpliceInfo());
        this.setUorfChange(r.getUorfChange());
        this.setSpliceInfoCopy(r.getSpliceInfoCopy());
    }
        
    /*public static VariantRecord convertToVariantRecord(Vector dbResult) {
        
        VariantTableSchema V = (VariantTableSchema)MedSavantDatabase.getInstance().getVariantTableSchema();      
        VariantAnnotationSiftTableSchema S = (VariantAnnotationSiftTableSchema)MedSavantDatabase.getInstance().getVariantSiftTableSchema();
        int i = 0;
        
        return new VariantRecord(
                
                //variant
                (Integer) dbResult.get(V.INDEX_VARIANTID-1),
                (Integer) dbResult.get(V.INDEX_GENOMEID-1),
                (Integer) dbResult.get(V.INDEX_PIPELINEID-1),
                (String) dbResult.get(V.INDEX_DNAID-1),
                (String) dbResult.get(V.INDEX_CHROM-1),
                new Long((Integer) dbResult.get(V.INDEX_POSITION-1)),
                (String) dbResult.get(V.INDEX_DBSNPID-1),
                (String) dbResult.get(V.INDEX_REFERENCE-1),
                (String) dbResult.get(V.INDEX_ALTERNATE-1),
                (Float)  dbResult.get(V.INDEX_QUALITY-1),
                (String) dbResult.get(V.INDEX_FILTER-1),
                (String) dbResult.get(V.INDEX_AA-1),
                (String) dbResult.get(V.INDEX_AC-1),
                (String) dbResult.get(V.INDEX_AF-1),
                (Integer) dbResult.get(V.INDEX_AN-1),
                (Float) dbResult.get(V.INDEX_BQ-1),
                (String) dbResult.get(V.INDEX_CIGAR-1),
                (Boolean) dbResult.get(V.INDEX_DB-1),
                (Integer) dbResult.get(V.INDEX_DP-1),
                new Long((Integer) dbResult.get(V.INDEX_END-1)),
                (Boolean) dbResult.get(V.INDEX_H2-1),
                (Float) dbResult.get(V.INDEX_MQ-1),
                (Integer) dbResult.get(V.INDEX_MQ0-1),
                (Integer) dbResult.get(V.INDEX_NS-1),
                (Float) dbResult.get(V.INDEX_SB-1),
                (Boolean) dbResult.get(V.INDEX_SOMATIC-1),
                (Boolean) dbResult.get(V.INDEX_VALIDATED-1),
                (String) dbResult.get(V.INDEX_CUSTOMINFO-1),

                //sift
                (String) dbResult.get(V.getNumFields() + i++), //name
                (String) dbResult.get(V.getNumFields() + i++), //name2
                (Double) dbResult.get(V.getNumFields() + i++), //damage probability
                
                //polyphen
                (String) dbResult.get(V.getNumFields() + i++), //cdnacoord
                (Integer) dbResult.get(V.getNumFields() + i++), //opos
                (String) dbResult.get(V.getNumFields() + i++), //oaa1
                (String) dbResult.get(V.getNumFields() + i++), //oaa2
                (String) dbResult.get(V.getNumFields() + i++), //snpid
                (String) dbResult.get(V.getNumFields() + i++), //acc
                (Integer) dbResult.get(V.getNumFields() + i++), //pos
                (String) dbResult.get(V.getNumFields() + i++), //prediction
                (String) dbResult.get(V.getNumFields() + i++), //pph2class
                (Float) dbResult.get(V.getNumFields() + i++), //pph2prob
                (Float) dbResult.get(V.getNumFields() + i++), //pph2fdr
                (Float) dbResult.get(V.getNumFields() + i++), //pph2tpr
                (Float) dbResult.get(V.getNumFields() + i++), //pph2fdr
                (Integer) dbResult.get(V.getNumFields() + i++), //transv
                (Integer) dbResult.get(V.getNumFields() + i++), //codpos
                (Integer) dbResult.get(V.getNumFields() + i++), //cpg
                (Float) dbResult.get(V.getNumFields() + i++), //mindjnc
                (Float) dbResult.get(V.getNumFields() + i++), //idpmax
                (Float) dbResult.get(V.getNumFields() + i++), //idpsnp
                (Float) dbResult.get(V.getNumFields() + i++), //idqmin
                
                //gatk
                (String) dbResult.get(V.getNumFields() + i++), //name
                (String) dbResult.get(V.getNumFields() + i++), //name2
                (String) dbResult.get(V.getNumFields() + i++), //transcriptStrand
                (String) dbResult.get(V.getNumFields() + i++), //positionType
                (Integer) dbResult.get(V.getNumFields() + i++), //frame
                (Integer) dbResult.get(V.getNumFields() + i++), //mrnaCoord
                (Integer) dbResult.get(V.getNumFields() + i++), //codonCoord
                (Integer) dbResult.get(V.getNumFields() + i++), //spliceDist
                (String) dbResult.get(V.getNumFields() + i++), //referenceCodon
                (String) dbResult.get(V.getNumFields() + i++), //referenceAA
                (String) dbResult.get(V.getNumFields() + i++), //variantCodon
                (String) dbResult.get(V.getNumFields() + i++), //variantAA
                (String) dbResult.get(V.getNumFields() + i++), //changesAA
                (String) dbResult.get(V.getNumFields() + i++), //functionalClass
                (String) dbResult.get(V.getNumFields() + i++), //codingCoordStr
                (String) dbResult.get(V.getNumFields() + i++), //proteinCoordStr
                (String) dbResult.get(V.getNumFields() + i++), //inCodingRegion
                (String) dbResult.get(V.getNumFields() + i++), //spliceInfo
                (String) dbResult.get(V.getNumFields() + i++), //uorfChange
                (String) dbResult.get(V.getNumFields() + i++) //spliceInfoCopy
                
                );
    }*/

    private Object parse(Class c, String value) {

        if (value.equals(nullString)) {
            //return null;
            return "";
        }

        if (c == String.class) {
            return value;
        }
        if (c == Long.class) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                return null;
            }
        }

        if (c == Float.class) {
            try {
                return Float.parseFloat(value);
            } catch (Exception e) {
                return null;
            }
        }

        //if flag exists, set to true
        if (c == Boolean.class) {
            return true;
        }

        if (c == Integer.class) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                return null;
            }
        }

        throw new UnsupportedOperationException("Parser doesn't deal with objects of type " + c);
    }

    private void parseInfo(String infoString){
        infoString = infoString.trim();
        String[] list = infoString.split(";");
        for(String element : list){    
            String name = element;
            String value = "";
            int equals = element.indexOf("=");
            if(equals != -1){
                name = element.substring(0, equals);
                value = element.substring(equals+1);
            }
            try {
                switch(CustomField.valueOf(name.toUpperCase())){
                    case AA:
                        aa =     (String)    parse(CLASS_OF_AA, value);
                        break;
                    case AC:
                        ac =     (String)    parse(CLASS_OF_AC, value);
                        break;
                    case AF:
                        af =     (String)    parse(CLASS_OF_AF, value);
                        break;
                    case AN:
                        an =     (Integer)   parse(CLASS_OF_AN, value);
                        break;
                    case BQ:
                        bq =     (Float)     parse(CLASS_OF_BQ, value);
                        break;
                    case CIGAR:
                        cigar =  (String)    parse(CLASS_OF_CIGAR, value);
                        break;
                    case DB:
                        db =     (Boolean)   parse(CLASS_OF_DB, value);
                        break;
                    case DP:
                        dp =     (Integer)   parse(CLASS_OF_DP, value);
                        break;
                    case END:
                        end =    (Long)    parse(CLASS_OF_END, value);
                        break;
                    case H2:
                        h2 =     (Boolean)   parse(CLASS_OF_H2, value);
                        break;
                    case MQ:
                        mq =     (Float)    parse(CLASS_OF_MQ, value);
                        break;
                    case MQ0:
                        mq0 =    (Integer)    parse(CLASS_OF_MQ0, value);
                        break;
                    case NS:
                        ns =     (Integer)   parse(CLASS_OF_NS, value);
                        break;
                    case SB:
                        sb =     (Float)    parse(CLASS_OF_SB, value);
                        break;
                    case SOMATIC:
                        somatic =(Boolean)    parse(CLASS_OF_SOMATIC, value);
                        break;
                    case VALIDATED:
                        validated=(Boolean)   parse(CLASS_OF_VALIDATED, value);
                        break;
                    default:
                        if(customInfo == null) customInfo = "";
                        customInfo += (element + ";");
                        break;
                }
            } catch (IllegalArgumentException ex) {
                if(customInfo == null) customInfo = "";
                customInfo += (element + ";");
            }
        }
    }
            
    /*public void setGenotypeFields(GenotypeField[] format, String s){
        s = s.trim();
        String[] list = s.split(":");
        for(int i = 0; i < list.length; i++){
            GenotypeField field = format[i];
            String value = list[i];
            switch(field){
                case GT:
                    parseGT(value);
                    break;
                case DP:
                    parseGDP(value);
                    break;
                case FT:
                    setGFT(value);
                    break;
                case GL:
                    parseGL(value);
                    break;
                case GQ:
                    parseGQ(value);
                    break;
                case HQ:
                    parseHQ(value);
                    break;
                case NOTSTANDARD:
                    break;
                default:
                    break;
            }
        }   
    }*/
    
    /*private void parseHQ(String s){
        String[] list = s.split(",");
        for(int i = 0; i < list.length; i++){
            try {
                float value = Float.parseFloat(list[i]);
                if(i == 0){
                    this.setHQA(value);
                } else if (i == 1){
                    this.setHQB(value);
                } 
            } catch (NumberFormatException e){
                //do nothing, leave as null
            }
        }
    }*/
    
    /*private void parseGQ(String s){
        try {
            setGQ(Float.parseFloat(s));
        } catch (NumberFormatException e){
            //do nothing, leave as null
        }
    }*/
    
    /*private void parseGL(String s){
        String[] list = s.split(",");
        for(int i = 0; i < list.length; i++){
            try {
                float value = Float.parseFloat(list[i]);
                if(i == 0){
                    this.setGLHomoRef(value);
                } else if (i == 1){
                    this.setGLHet(value);
                } else if (i == 2){
                    this.setGLHomoAlt(value);
                }
            } catch (NumberFormatException e){
                //do nothing, leave as null
            }
        }
    }*/
    
    /*private void parseGDP(String s){
        try {
            setGDP(Integer.parseInt(s));
        } catch (NumberFormatException e){
            //do nothing, leave as null
        }
    }*/
    
    /*private void parseGT(String s){
        
        int num1 = -1;
        int num2 = -1;
        String separator = null;
        
        if(s.contains("|")){
            separator = "|";
            setGPhased(this.PHASE_PHASED);
        } else if (s.contains("/")){
            separator = "/";
            setGPhased(this.PHASE_UNPHASED);
        } else {
            setGPhased(this.PHASE_NA);
        }
        
        if(separator == null){
            try {
                num1 = Integer.parseInt(s);
            } catch (NumberFormatException e){
                setGT(ZYGOSITY_UNKNOWN);
                return;
            }
        } else {
            String[] numbers = s.split(separator);
            if(numbers.length != 2) {
                setGT(ZYGOSITY_UNKNOWN);
                return;
            }
            try {
                num1 = Integer.parseInt(numbers[0]);
                num2 = Integer.parseInt(numbers[1]);
            } catch (NumberFormatException e){
                setGT(ZYGOSITY_UNKNOWN);
                return;
            }
        }
        
        if(num1 != -1 && num2 == -1){
            if(num1 == 0){
                setGT(ZYGOSITY_HOMOREF);
            } else {
                setGT(ZYGOSITY_HOMOALT);
            }
        } else {
            if(num1 == 0 && num2 == 0){
                setGT(ZYGOSITY_HOMOREF);
            } else if (num1 == 0 || num2 == 0){
                setGT(ZYGOSITY_HETERO);
            } else if (num1 != num2){
                setGT(ZYGOSITY_HETERO);
            } else {
                setGT(ZYGOSITY_HOMOALT);
            }
        }
        
    }*/

    public int getVariantID(){
        return variantID;
    }
    
    public void setVariantID(int variantID){
        this.variantID = variantID;
    }

    public int getGenomeID(){
        return genomeID;
    }

    public void setGenomeID(int genomeID) {
        this.genomeID = genomeID;
    }

    public int getPipelineID(){
        return pipelineID;
    }

    public void setPipelineID(int pipelineID){
        this.pipelineID = pipelineID;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public String getDbSNPID() {
        return dbSNPID;
    }

    public void setDbSNPID(String id) {
        this.dbSNPID = id;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long pos) {
        this.position = pos;
    }

    public Float getQual() {
        return qual;
    }

    public void setQual(Float qual) {
        this.qual = qual;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDnaID() {
        return dnaID;
    }

    public void setDnaID(String dnaID) {
        this.dnaID = dnaID;
    }

    public String getAA(){
        return aa;
    }

    public void setAA(String aa){
        this.aa = aa;
    }

    public String getAC(){
        return ac;
    }

    public void setAC(String ac){
        this.ac = ac;
    }

    public String getAF(){
        return af;
    }

    public void setAF(String af){
        this.af = af;
    }

    public int getAN(){
        return an;
    }

    public void setAN(int an){
        this.an = an;
    }

    public float getBQ(){
        return bq;
    }

    public void setBQ(float bq){
        this.bq = bq;
    }

    public String getCigar(){
        return cigar;
    }

    public void setCigar(String cigar){
        this.cigar = cigar;
    }

    public boolean getDB(){
        return db;
    }

    public void setDB(boolean db){
        this.db = db;
    }

    public int getDP(){
        return dp;
    }

    public void setDP(int dp){
        this.dp = dp;
    }

    public long getEnd(){
        return end;
    }
    
    public void setEnd(long end){
        this.end = end;
    }

    public boolean getH2(){
        return h2;
    }
    
    public void setH2(boolean h2){
        this.h2 = h2;
    }

    public float getMQ(){
        return mq;
    }
    
    public void setMQ(float mq){
        this.mq = mq;
    }

    public int getMQ0(){
        return mq0;
    }
    
    public void setMQ0(int mq0){
        this.mq0 = mq0;
    }

    public int getNS(){
        return ns;
    }
    
    public void setNS(int ns){
        this.ns = ns;
    }

    public float getSB(){
        return sb;
    }

    public void setSB(float sb){
        this.sb = sb;
    }

    public boolean getSomatic(){
        return somatic;
    }

    public void setSomatic(boolean somatic){
        this.somatic = somatic;
    }

    public boolean getValidated(){
        return validated;
    }

    public void setValidated(boolean validated){
        this.validated = validated;
    }

    public String getCustomInfo(){
        return customInfo;
    }

    public void setCustomInfo(String customInfo){
        this.customInfo = customInfo;
    }
    
    /*public int getVariantAnnotationSiftId(){
        return variant_annotation_sift_id;
    }
    
    public void setVariantAnnotationSiftId(int id){
        this.variant_annotation_sift_id = id;
    }*/
    
    public String getNameSift(){
        return name_sift;
    }
    
    public void setNameSift(String name){
        this.name_sift = name;
    }
    
    public String getName2Sift(){
        return name2_sift;
    }
    
    public void setName2Sift(String name2){
        this.name2_sift = name2;
    }
    
    public double getDamageProbability(){
        return damage_probability;
    }
    
    public void setDamageProbability(double prob){
        this.damage_probability = prob;
    }

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public String getCdnacoord() {
        return cdnacoord;
    }

    public void setCdnacoord(String cdnacoord) {
        this.cdnacoord = cdnacoord;
    }

    public int getCodpos() {
        return codpos;
    }

    public void setCodpos(int codpos) {
        this.codpos = codpos;
    }

    public int getCpg() {
        return cpg;
    }

    public void setCpg(int cpg) {
        this.cpg = cpg;
    }

    public float getIdpmax() {
        return idpmax;
    }

    public void setIdpmax(float idpmax) {
        this.idpmax = idpmax;
    }

    public float getIdpsnp() {
        return idpsnp;
    }

    public void setIdpsnp(float idpsnp) {
        this.idpsnp = idpsnp;
    }

    public float getIdqmin() {
        return idqmin;
    }

    public void setIdqmin(float idqmin) {
        this.idqmin = idqmin;
    }

    public float getMindjnc() {
        return mindjnc;
    }

    public void setMindjnc(float mindjnc) {
        this.mindjnc = mindjnc;
    }

    public String getOaa1() {
        return oaa1;
    }

    public void setOaa1(String oaa1) {
        this.oaa1 = oaa1;
    }

    public String getOaa2() {
        return oaa2;
    }

    public void setOaa2(String oaa2) {
        this.oaa2 = oaa2;
    }

    public int getOpos() {
        return opos;
    }

    public void setOpos(int opos) {
        this.opos = opos;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getPph2class() {
        return pph2class;
    }

    public void setPph2class(String pph2class) {
        this.pph2class = pph2class;
    }

    public float getPph2fdr() {
        return pph2fdr;
    }

    public void setPph2fdr(float pph2fdr) {
        this.pph2fdr = pph2fdr;
    }

    public float getPph2fpr() {
        return pph2fpr;
    }

    public void setPph2fpr(float pph2fpr) {
        this.pph2fpr = pph2fpr;
    }

    public float getPph2prob() {
        return pph2prob;
    }

    public void setPph2prob(float pph2prob) {
        this.pph2prob = pph2prob;
    }

    public float getPph2tpr() {
        return pph2tpr;
    }

    public void setPph2tpr(float pph2tpr) {
        this.pph2tpr = pph2tpr;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public String getSnpid() {
        return snpid;
    }

    public void setSnpid(String snpid) {
        this.snpid = snpid;
    }

    public int getTransv() {
        return transv;
    }

    public void setTransv(int transv) {
        this.transv = transv;
    }

    public String getChangesAA() {
        return changesAA;
    }

    public void setChangesAA(String changesAA) {
        this.changesAA = changesAA;
    }

    public String getCodingCoordStr() {
        return codingCoordStr;
    }

    public void setCodingCoordStr(String codingCoordStr) {
        this.codingCoordStr = codingCoordStr;
    }

    public int getCodonCoord() {
        return codonCoord;
    }

    public void setCodonCoord(int codonCoord) {
        this.codonCoord = codonCoord;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public String getFunctionalClass() {
        return functionalClass;
    }

    public void setFunctionalClass(String functionalClass) {
        this.functionalClass = functionalClass;
    }

    public String getInCodingRegion() {
        return inCodingRegion;
    }

    public void setInCodingRegion(String inCodingRegion) {
        this.inCodingRegion = inCodingRegion;
    }

    public int getMrnaCoord() {
        return mrnaCoord;
    }

    public void setMrnaCoord(int mrnaCoord) {
        this.mrnaCoord = mrnaCoord;
    }

    public String getName2_gatk() {
        return name2_gatk;
    }

    public void setName2_gatk(String name2_gatk) {
        this.name2_gatk = name2_gatk;
    }

    public String getName_gatk() {
        return name_gatk;
    }

    public void setName_gatk(String name_gatk) {
        this.name_gatk = name_gatk;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public String getProteinCoordStr() {
        return proteinCoordStr;
    }

    public void setProteinCoordStr(String proteinCoordStr) {
        this.proteinCoordStr = proteinCoordStr;
    }

    public String getReferenceAA() {
        return referenceAA;
    }

    public void setReferenceAA(String referenceAA) {
        this.referenceAA = referenceAA;
    }

    public String getReferenceCodon() {
        return referenceCodon;
    }

    public void setReferenceCodon(String referenceCodon) {
        this.referenceCodon = referenceCodon;
    }

    public int getSpliceDist() {
        return spliceDist;
    }

    public void setSpliceDist(int spliceDist) {
        this.spliceDist = spliceDist;
    }

    public String getSpliceInfo() {
        return spliceInfo;
    }

    public void setSpliceInfo(String spliceInfo) {
        this.spliceInfo = spliceInfo;
    }

    public String getSpliceInfoCopy() {
        return spliceInfoCopy;
    }

    public void setSpliceInfoCopy(String spliceInfoCopy) {
        this.spliceInfoCopy = spliceInfoCopy;
    }

    public String getTranscriptStrand() {
        return transcriptStrand;
    }

    public void setTranscriptStrand(String transcriptStrand) {
        this.transcriptStrand = transcriptStrand;
    }

    public String getUorfChange() {
        return uorfChange;
    }

    public void setUorfChange(String uorfChange) {
        this.uorfChange = uorfChange;
    }

    public String getVariantAA() {
        return variantAA;
    }

    public void setVariantAA(String variantAA) {
        this.variantAA = variantAA;
    }

    public String getVariantCodon() {
        return variantCodon;
    }

    public void setVariantCodon(String variantCodon) {
        this.variantCodon = variantCodon;
    }
    
    /*public int compareTo(VariantRecord other){
        int chromCompare = this.getChrom().compareTo(other.getChrom());
        if(chromCompare != 0){
            return chromCompare;
        }   
        return this.getPosition().compareTo(other.getPosition());
    }
    
    public int compareTo(String chrom, long pos){
        int chromCompare = this.getChrom().compareTo(chrom);
        if(chromCompare != 0){
            return chromCompare;
        }   
        return this.getPosition().compareTo(pos);
    }*/
    
    private static String[] chroms = {};
    
    public int compareTo(VariantRecord other){
        return compareTo(other.getChrom(), other.getPosition());
    }
    
    public int compareTo(String chrom, long pos){
        int chromCompare = compareChrom(this.getChrom(), chrom);
        if(chromCompare != 0){
            return chromCompare;
        }   
        return this.getPosition().compareTo(pos);
    }
    
    public static int compareChrom(String chrom1, String chrom2){
        chrom1 = chrom1.substring(3);
        chrom2 = chrom2.substring(3);
        try {
            Integer a = Integer.parseInt(chrom1);
            Integer b = Integer.parseInt(chrom2);
            return a.compareTo(b);
        } catch (NumberFormatException e) {
            return chrom1.compareTo(chrom2);
        }
    }
          
    @Override
    public String toString() {
        return "VariantRecord{" + "dnaID=" + dnaID + "chrom=" + chrom + "pos=" + position + "id=" + dbSNPID + "ref=" + ref + "alt=" + alt + "qual=" + qual + "filter=" + filter + '}';
    }

    private static String delim = "\t";

    //public String toTabString() {
    //    return dnaID + delim + chrom + delim + position + delim + dbSNPID + delim + ref + delim + alt + delim + qual + delim + filter + delim;
   // }
    
    public String toTabString(int uploadId, int fileId, int variantId) {
        return  "\"" + getString(uploadId) + "\"," + 
                "\"" + getString(fileId) + "\"," + 
                "\"" + getString(variantId) + "\"," + 
                "\"" + getString(this.dnaID) + "\"," + 
                "\"" + getString(this.chrom) + "\"," +              
                "\"" + getString(this.position) + "\"," + 
                "\"" + getString(this.dbSNPID) + "\"," + 
                "\"" + getString(this.ref) + "\"," + 
                "\"" + getString(this.alt) + "\"," + 
                "\"" + getString(this.qual) + "\"," + 
                "\"" + getString(this.filter) + "\"," + 
                "\"" + getString(this.aa) + "\"," + 
                "\"" + getString(this.ac) + "\"," + 
                "\"" + getString(this.af) + "\"," + 
                "\"" + getString(this.an) + "\"," + 
                "\"" + getString(this.bq) + "\"," + 
                "\"" + getString(this.cigar) + "\"," + 
                "\"" + getString(this.db, true) + "\"," +
                "\"" + getString(this.dp) + "\"," + 
                "\"" + getString(this.end) + "\"," + 
                "\"" + getString(this.h2, true) + "\"," + 
                "\"" + getString(this.mq) + "\"," + 
                "\"" + getString(this.mq0) + "\"," +
                "\"" + getString(this.ns) + "\"," + 
                "\"" + getString(this.sb) + "\"," + 
                "\"" + getString(this.somatic, true) + "\"," + 
                "\"" + getString(this.validated, true) + "\"," + 
                "\"" + getString(this.customInfo) + "\""; 
    }
    
    private String getString(Object value){
        return getString(value, false);
    }
    
    private String getString(Object value, boolean isBoolean){
        if(value== null){
            return "";
        } else if(isBoolean){
            if((Boolean)value){
                return "1";
            } else {
                return "0";
            }
        } else {
            return value.toString();
        }
    }
    
    /*public String toTabsForUpload() {
        return  getString(this.variantID) + "\t" + 
                getString(this.genomeID) + "\t" + 
                getString(this.pipelineID) + "\t" + 
                getString(this.dnaID) + "\t" + 
                getString(this.chrom) + "\t" + 
                getString(this.position) + "\t" + 
                getString(this.dbSNPID) + "\t" + 
                getString(this.ref) + "\t" + 
                getString(this.alt) + "\t" + 
                getString(this.qual) + "\t" + 
                getString(this.filter) + "\t" + 
                getString(this.aa) + "\t" + 
                getString(this.ac) + "\t" + 
                getString(this.af) + "\t" + 
                getString(this.an) + "\t" + 
                getString(this.bq) + "\t" + 
                getString(this.cigar) + "\t" + 
                getString(this.db) + "\t" + 
                getString(this.dp) + "\t" + 
                getString(this.end) + "\t" + 
                getString(this.h2) + "\t" + 
                getString(this.mq) + "\t" + 
                getString(this.mq0) + "\t" + 
                getString(this.ns) + "\t" + 
                getString(this.sb) + "\t" + 
                getString(this.somatic) + "\t" + 
                getString(this.validated) + "\t" + 
                getString(this.customInfo) + "\t" + 

                //sift
                getString(this.name_sift) + "\t" + 
                getString(this.name2_sift) + "\t" + 
                getString(this.damage_probability) + "\t" + 

                //polyphen
                getString(this.cdnacoord) + "\t" + 
                getString(this.opos) + "\t" + 
                getString(this.oaa1) + "\t" + 
                getString(this.oaa2) + "\t" + 
                getString(this.snpid) + "\t" + 
                getString(this.acc) + "\t" + 
                getString(this.pos) + "\t" + 
                getString(this.prediction) + "\t" + 
                getString(this.pph2class) + "\t" + 
                getString(this.pph2prob) + "\t" + 
                getString(this.pph2fpr) + "\t" + 
                getString(this.pph2tpr) + "\t" + 
                getString(this.pph2fdr) + "\t" + 
                getString(this.transv) + "\t" + 
                getString(this.codpos) + "\t" + 
                getString(this.cpg) + "\t" + 
                getString(this.mindjnc) + "\t" + 
                getString(this.idpmax) + "\t" + 
                getString(this.idpsnp) + "\t" + 
                getString(this.idqmin) + "\t" + 

                //gatk
                getString(this.name_gatk) + "\t" + 
                getString(this.name2_gatk) + "\t" + 
                getString(this.transcriptStrand) + "\t" + 
                getString(this.positionType) + "\t" + 
                getString(this.frame) + "\t" + 
                getString(this.mrnaCoord) + "\t" + 
                getString(this.codonCoord) + "\t" + 
                getString(this.spliceDist) + "\t" + 
                getString(this.referenceCodon) + "\t" + 
                getString(this.referenceAA) + "\t" + 
                getString(this.variantCodon) + "\t" + 
                getString(this.variantAA) + "\t" + 
                getString(this.changesAA) + "\t" + 
                getString(this.functionalClass) + "\t" + 
                getString(this.codingCoordStr) + "\t" + 
                getString(this.proteinCoordStr) + "\t" + 
                getString(this.inCodingRegion) + "\t" + 
                getString(this.spliceInfo) + "\t" + 
                getString(this.uorfChange) + "\t" + 
                getString(this.spliceInfoCopy);      
    }*/
    
}
