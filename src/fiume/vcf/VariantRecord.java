/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiume.vcf;

import java.io.Serializable;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class VariantRecord implements Serializable {


    private static final int FILE_INDEX_OF_CHROM = 0;
    private static final int FILE_INDEX_OF_POS = FILE_INDEX_OF_CHROM + 1;
    private static final int FILE_INDEX_OF_ID = FILE_INDEX_OF_POS + 1;
    private static final int FILE_INDEX_OF_REF = FILE_INDEX_OF_ID + 1;
    private static final int FILE_INDEX_OF_ALT = FILE_INDEX_OF_REF + 1;
    private static final int FILE_INDEX_OF_QUAL = FILE_INDEX_OF_ALT + 1;
    private static final int FILE_INDEX_OF_FILTER = FILE_INDEX_OF_QUAL + 1;
    private static final int FILE_INDEX_OF_INFO = FILE_INDEX_OF_FILTER + 1;
    /*private static final int FILE_INDEX_OF_AA = FILE_INDEX_OF_FILTER + 1;
    private static final int FILE_INDEX_OF_AC = FILE_INDEX_OF_AA + 1;
    private static final int FILE_INDEX_OF_AF = FILE_INDEX_OF_AC + 1;
    private static final int FILE_INDEX_OF_AN = FILE_INDEX_OF_AF + 1;
    private static final int FILE_INDEX_OF_BQ = FILE_INDEX_OF_AN + 1;
    private static final int FILE_INDEX_OF_CIGAR = FILE_INDEX_OF_BQ + 1;
    private static final int FILE_INDEX_OF_DB = FILE_INDEX_OF_CIGAR + 1;
    private static final int FILE_INDEX_OF_DP = FILE_INDEX_OF_DB + 1;
    private static final int FILE_INDEX_OF_END = FILE_INDEX_OF_DP + 1;
    private static final int FILE_INDEX_OF_H2 = FILE_INDEX_OF_END + 1;
    private static final int FILE_INDEX_OF_MQ = FILE_INDEX_OF_H2 + 1;
    private static final int FILE_INDEX_OF_MQ0 = FILE_INDEX_OF_MQ + 1;
    private static final int FILE_INDEX_OF_NS = FILE_INDEX_OF_MQ0 + 1;
    private static final int FILE_INDEX_OF_SB = FILE_INDEX_OF_NS + 1;
    private static final int FILE_INDEX_OF_SOMATIC = FILE_INDEX_OF_SB + 1;
    private static final int FILE_INDEX_OF_VALIDATED = FILE_INDEX_OF_SOMATIC + 1;
    private static final int FILE_INDEX_OF_CUSTOMINFO = FILE_INDEX_OF_VALIDATED + 1;*/

    public static final Class CLASS_OF_CHROM = String.class;
    public static final Class CLASS_OF_POS = Long.class;
    public static final Class CLASS_OF_ID = String.class;
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
    
    public static final Class CLASS_OF_GT = Integer.class;
    public static final Class CLASS_OF_GPHASED = Integer.class;
    public static final Class CLASS_OF_GDP = Integer.class;
    public static final Class CLASS_OF_GFT = String.class;
    public static final Class CLASS_OF_GLHOMOREF = Float.class;
    public static final Class CLASS_OF_GLHET = Float.class;
    public static final Class CLASS_OF_GLHOMOALT = Float.class;
    public static final Class CLASS_OF_GQ = Float.class;
    public static final Class CLASS_OF_HQA = Float.class;
    public static final Class CLASS_OF_HQB = Float.class;
    
    public static final Class CLASS_OF_position_a = String.class;
public static final Class CLASS_OF_position_b = String.class;
public static final Class CLASS_OF_o_acc = String.class;
public static final Class CLASS_OF_o_pos = Integer.class;
public static final Class CLASS_OF_o_aa1 = String.class;
public static final Class CLASS_OF_o_aa2 = String.class;
public static final Class CLASS_OF_snp_id = String.class;
public static final Class CLASS_OF_acc = String.class;
public static final Class CLASS_OF_pos = String.class;
public static final Class CLASS_OF_aa1 = String.class;
public static final Class CLASS_OF_aa2 = String.class;
public static final Class CLASS_OF_nt1 = String.class;
public static final Class CLASS_OF_nt2 = String.class;
public static final Class CLASS_OF_prediction = String.class;
public static final Class CLASS_OF_pph2_class = String.class;
public static final Class CLASS_OF_pph2_prob = Double.class;
public static final Class CLASS_OF_pph2_FPR = Double.class;
public static final Class CLASS_OF_pph2_TPR = Double.class;
public static final Class CLASS_OF_pph2_FDR = Double.class;
public static final Class CLASS_OF_Transv = Integer.class;
public static final Class CLASS_OF_CodPos = Integer.class;
public static final Class CLASS_OF_CpG = Integer.class;
public static final Class CLASS_OF_MinDJnc = Double.class;
public static final Class CLASS_OF_PfamHit = String.class;
public static final Class CLASS_OF_IdPmax = Double.class;
public static final Class CLASS_OF_IdPSNP = Double.class;
public static final Class CLASS_OF_IdQmin = Double.class;
public static final Class CLASS_OF_sift_prediction = Double.class;
public static final Class CLASS_OF_name = String.class;
public static final Class CLASS_OF_name2 = String.class;
public static final Class CLASS_OF_transcriptStrand = String.class;
public static final Class CLASS_OF_positionType = String.class;
public static final Class CLASS_OF_frame = Integer.class;
public static final Class CLASS_OF_mrnaCoord = Integer.class;
public static final Class CLASS_OF_codonCoord = Integer.class;
public static final Class CLASS_OF_spliceDist = Integer.class;
public static final Class CLASS_OF_referenceCodon = String.class;
public static final Class CLASS_OF_referenceAA = String.class;
public static final Class CLASS_OF_variantCodon = String.class;
public static final Class CLASS_OF_variantAA = String.class;
public static final Class CLASS_OF_changesAA = String.class;
public static final Class CLASS_OF_functionalClass = String.class;
public static final Class CLASS_OF_codingCoordStr = String.class;
public static final Class CLASS_OF_proteinCoordStr = String.class;
public static final Class CLASS_OF_inCodingRegion = String.class;
public static final Class CLASS_OF_spliceInfo = String.class;
public static final Class CLASS_OF_uorfChange = String.class;

       
    public static final String nullString = ".";

    private int genomeID;
    private int pipelineID;
    private String dnaID;
    private String chrom;
    private long position;
    private String id;
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
    
    private int gt;
    private int gphased;
    private int gdp;
    private String gft;
    private float glhomoref;
    private float glhet;
    private float glhomoalt;
    private float gq;
    private float hqa;
    private float hqb;
    
    private String position_a;
private String position_b;
private String o_acc;
private int o_pos;
private String o_aa1;
private String o_aa2;
private String snp_id;
private String acc;
private int pos;
private String aa1;
private String aa2;
private String nt1;
private String nt2;
private String prediction;
private String pph2_class;
private double pph2_prob;
private double pph2_FPR;
private double pph2_TPR;
private double pph2_FDR;
private int Transv;
private int CodPos;
private int CpG;
private double MinDJnc;
private String PfamHit;
private double IdPmax;
private double IdPSNP;
private double IdQmin;
private double sift_prediction;
private String name;
private String name2;
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
    
    //private GenotypeField[] format;

    private enum CustomField {
        AA, AC, AF, AN, BQ, CIGAR, DB, DP, END, H2, MQ, MQ0, NS, SB, SOMATIC, VALIDATED;
    }
    
    public enum GenotypeField {
        GT, DP, FT, GL, GQ, HQ, NOTSTANDARD;
    }
    
    public static int ZYGOSITY_UNKNOWN = 0;
    public static int ZYGOSITY_HOMOREF = 1;
    public static int ZYGOSITY_HOMOALT = 2;
    public static int ZYGOSITY_HETERO = 3;
    public static String[] ALIAS_ZYGOSITY = {"Unknown", "HomoRef", "HomoAlt", "Hetero"};
    
    public static int PHASE_NA = 0;
    public static int PHASE_UNPHASED = 1;
    public static int PHASE_PHASED = 2;
    

    public VariantRecord(String[] line) {
        dnaID =  null;
        chrom =     (String)    parse(CLASS_OF_CHROM, line[FILE_INDEX_OF_CHROM]);
        position =       (Long)      parse(CLASS_OF_POS, line[FILE_INDEX_OF_POS]);
        id =        (String)    parse(CLASS_OF_ID, line[FILE_INDEX_OF_ID]);
        ref =       (String)    parse(CLASS_OF_REF, line[FILE_INDEX_OF_REF]);
        alt =       (String)    parse(CLASS_OF_ALT, line[FILE_INDEX_OF_ALT]);
        qual =      (Float)     parse(CLASS_OF_QUAL, line[FILE_INDEX_OF_QUAL]);
        filter =    (String)    parse(CLASS_OF_FILTER, line[FILE_INDEX_OF_FILTER]);
        parseInfo(line[FILE_INDEX_OF_INFO]);        
    }

    public VariantRecord(
            int genomeID,
            int pipelineID,
            String dnaID,
            String chrom,
            long position,
            String id,
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
            int gt,
            int gphased,
            int gdp,
            String gft,
            float glhomoref,
            float glhet,
            float glhomoalt,
            float gq,
            float hqa,
            float hqb,
            String position_a,
String position_b,
String o_acc,
int o_pos,
String o_aa1,
String o_aa2,
String snp_id,
String acc,
int pos,
String aa1,
String aa2,
String nt1,
String nt2,
String prediction,
String pph2_class,
double pph2_prob,
double pph2_FPR,
double pph2_TPR,
double pph2_FDR,
int Transv,
int CodPos,
int CpG,
double MinDJnc,
String PfamHit,
double IdPmax,
double IdPSNP,
double IdQmin,
double sift_prediction,
String name,
String name2,
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
String uorfChange

            ) {
        this.genomeID = genomeID;
        this.pipelineID = pipelineID;
        this.dnaID = dnaID;
        this.chrom = chrom;
        this.position = position;
        this.id = id;
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
        this.gt = gt;
        this.gphased = gphased;
        this.gdp = gdp;
        this.gft = gft;
        this.glhomoref = glhomoref;
        this.glhet = glhet;
        this.glhomoalt = glhomoalt;
        this.gq = gq;
        this.hqa = hqa;
        this.hqb = hqb;
        this.position_a = position_a;
this.position_b = position_b;
this.o_acc = o_acc;
this.o_pos = o_pos;
this.o_aa1 = o_aa1;
this.o_aa2 = o_aa2;
this.snp_id = snp_id;
this.acc = acc;
this.pos = pos;
this.aa1 = aa1;
this.aa2 = aa2;
this.nt1 = nt1;
this.nt2 = nt2;
this.prediction = prediction;
this.pph2_class = pph2_class;
this.pph2_prob = pph2_prob;
this.pph2_FPR = pph2_FPR;
this.pph2_TPR = pph2_TPR;
this.pph2_FDR = pph2_FDR;
this.Transv = Transv;
this.CodPos = CodPos;
this.CpG = CpG;
this.MinDJnc = MinDJnc;
this.PfamHit = PfamHit;
this.IdPmax = IdPmax;
this.IdPSNP = IdPSNP;
this.IdQmin = IdQmin;
this.sift_prediction = sift_prediction;
this.name = name;
this.name2 = name2;
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

    }

    protected VariantRecord(VariantRecord r) {
        this.setDnaID(r.getDnaID());
        this.setGenomeID(r.getGenomeID());
        this.setPipelineID(r.getPipelineID());
        this.setChrom(r.getChrom());
        this.setPos(r.getPos());
        this.setId(r.getId());
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
        this.setGT(r.getGT());
        this.setGPhased(r.getGPhased());
        this.setGDP(r.getGDP());
        this.setGFT(r.getGFT());
        this.setGLHomoRef(r.getGLHomoRef());
        this.setGLHet(r.getGLHet());
        this.setGLHomoAlt(r.getGLHomoAlt());
        this.setGQ(r.getGQ());
        this.setHQA(r.getHQA());
        this.setHQB(r.getHQB());
    }

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
                reportErrorConverting(Long.class, value);
                return null;
            }
        }

        if (c == Float.class) {
            try {
                return Float.parseFloat(value);
            } catch (Exception e) {
                reportErrorConverting(Float.class, value);
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
                reportErrorConverting(Integer.class, value);
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
            
    public void setGenotypeFields(GenotypeField[] format, String s){
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
    }
    
    private void parseHQ(String s){
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
    }
    
    private void parseGQ(String s){
        try {
            setGQ(Float.parseFloat(s));
        } catch (NumberFormatException e){
            //do nothing, leave as null
        }
    }
    
    private void parseGL(String s){
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
    }
    
    private void parseGDP(String s){
        try {
            setGDP(Integer.parseInt(s));
        } catch (NumberFormatException e){
            //do nothing, leave as null
        }
    }
    
    private void parseGT(String s){
        
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
        
    }

    private void reportErrorConverting(Class c, String value) {
        System.err.println("Error: parsing " + value + " as " + c);
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Long getPos() {
        return position;
    }

    public void setPos(Long pos) {
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
    
    public int getGT(){
        return gt;
    }
    
    public void setGT(int gt){
        this.gt = gt;
    }
    
    public int getGPhased(){
        return gphased;
    }
    
    public void setGPhased(int gphased){
        this.gphased = gphased;
    }
    
    public int getGDP(){
        return gdp;
    }
    
    public void setGDP(int gdp){
        this.gdp = gdp;
    }
    
    public String getGFT(){
        return gft;
    }
    
    public void setGFT(String gft){
        this.gft = gft;
    }
    
    public float getGLHomoRef(){
        return glhomoref;
    }
    
    public void setGLHomoRef(float glhomoref){
        this.glhomoref = glhomoref;
    }
    
    public float getGLHet(){
        return glhet;
    }
    
    public void setGLHet(float glhet){
        this.glhet = glhet;
    }
    
    public float getGLHomoAlt(){
        return glhomoalt;
    }
    
    public void setGLHomoAlt(float glhomoalt){
        this.glhomoalt = glhomoalt;
    }

    public float getGQ(){
        return gq;
    }
    
    public void setGQ(float gq){
        this.gq = gq;
    }
    
    public float getHQA(){
        return hqa;
    }
    
    public void setHQA(float hqa){
        this.hqa = hqa;
    }
    
    public float getHQB(){
        return hqb;
    }
    
    public void setHQB(float hqb){
        this.hqb = hqb;
    }
    
    public String getposition_a() { return this.position_a; }
public String getposition_b() { return this.position_b; }
public String geto_acc() { return this.o_acc; }
public int geto_pos() { return this.o_pos; }
public String geto_aa1() { return this.o_aa1; }
public String geto_aa2() { return this.o_aa2; }
public String getsnp_id() { return this.snp_id; }
public String getacc() { return this.acc; }
public int getpos() { return this.pos; }
public String getaa1() { return this.aa1; }
public String getaa2() { return this.aa2; }
public String getnt1() { return this.nt1; }
public String getnt2() { return this.nt2; }
public String getprediction() { return this.prediction; }
public String getpph2_class() { return this.pph2_class; }
public double getpph2_prob() { return this.pph2_prob; }
public double getpph2_FPR() { return this.pph2_FPR; }
public double getpph2_TPR() { return this.pph2_TPR; }
public double getpph2_FDR() { return this.pph2_FDR; }
public int getTransv() { return this.Transv; }
public int getCodPos() { return this.CodPos; }
public int getCpG() { return this.CpG; }
public double getMinDJnc() { return this.MinDJnc; }
public String getPfamHit() { return this.PfamHit; }
public double getIdPmax() { return this.IdPmax; }
public double getIdPSNP() { return this.IdPSNP; }
public double getIdQmin() { return this.IdQmin; }
public double getsift_prediction() { return this.sift_prediction; }
public String getname() { return this.name; }
public String getname2() { return this.name2; }
public String gettranscriptStrand() { return this.transcriptStrand; }
public String getpositionType() { return this.positionType; }
public int getframe() { return this.frame; }
public int getmrnaCoord() { return this.mrnaCoord; }
public int getcodonCoord() { return this.codonCoord; }
public int getspliceDist() { return this.spliceDist; }
public String getreferenceCodon() { return this.referenceCodon; }
public String getreferenceAA() { return this.referenceAA; }
public String getvariantCodon() { return this.variantCodon; }
public String getvariantAA() { return this.variantAA; }
public String getchangesAA() { return this.changesAA; }
public String getfunctionalClass() { return this.functionalClass; }
public String getcodingCoordStr() { return this.codingCoordStr; }
public String getproteinCoordStr() { return this.proteinCoordStr; }
public String getinCodingRegion() { return this.inCodingRegion; }
public String getspliceInfo() { return this.spliceInfo; }
public String getuorfChange() { return this.uorfChange; }

    
    
    
    @Override
    public String toString() {
        return "VariantRecord{" + "dnaID=" + dnaID + "chrom=" + chrom + "pos=" + position + "id=" + id + "ref=" + ref + "alt=" + alt + "qual=" + qual + "filter=" + filter + '}';
    }

    private static String delim = "\t";

    public String toTabString() {
        return dnaID + delim + chrom + delim + position + delim + id + delim + ref + delim + alt + delim + qual + delim + filter + delim;
    }

}
