/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.vcf;

import java.io.Serializable;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class VariantRecord implements Serializable {
    
    public static enum VariantType {SNP, Insertion, Deletion, Various, Unknown};
    public static enum Zygosity {HomoRef, HomoAlt, Hetero, HeteroTriallelic};

    private static final int FILE_INDEX_OF_CHROM = 0;
    private static final int FILE_INDEX_OF_POS = FILE_INDEX_OF_CHROM + 1;
    private static final int FILE_INDEX_OF_DBSNPID = FILE_INDEX_OF_POS + 1;
    private static final int FILE_INDEX_OF_REF = FILE_INDEX_OF_DBSNPID + 1;
    private static final int FILE_INDEX_OF_ALT = FILE_INDEX_OF_REF + 1;
    private static final int FILE_INDEX_OF_QUAL = FILE_INDEX_OF_ALT + 1;
    private static final int FILE_INDEX_OF_FILTER = FILE_INDEX_OF_QUAL + 1;
    private static final int FILE_INDEX_OF_INFO = FILE_INDEX_OF_FILTER + 1;

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
    public static final Class CLASS_OF_CUSTOMINFO = String.class;


    private int uploadID;
    private int fileID;
    private int variantID;
    
    private int genomeID;
    private int pipelineID;
    private String dnaID;
    private String chrom;
    private Long position;
    private String dbSNPID;
    private String ref;
    private String alt;
    private Float qual;
    private String filter;
    private VariantType type;
    private Zygosity zygosity;
    private String genotype;
    private String customInfo;    
    private Object[] customFields;
     
    public static final String nullString = ".";
    
    /*
     * DO NOT USE THIS UNLESS YOU KNOW WHAT YOU'RE DOING
     * Useful for creating empty record in some circumstances
     */
    public VariantRecord() {}
  
    public VariantRecord(String[] line) throws Exception{//, String[] infoKeys, Class[] infoClasses) {
        dnaID =  null;
        chrom =         (String)    parse(CLASS_OF_CHROM, line[FILE_INDEX_OF_CHROM]);
        position =      (Long)      parse(CLASS_OF_POSITION, line[FILE_INDEX_OF_POS]);
        dbSNPID =       (String)    parse(CLASS_OF_DBSNPID, line[FILE_INDEX_OF_DBSNPID]);
        ref =           (String)    parse(CLASS_OF_REF, line[FILE_INDEX_OF_REF]);
        alt =           (String)    parse(CLASS_OF_ALT, line[FILE_INDEX_OF_ALT]);
        qual =          (Float)     parse(CLASS_OF_QUAL, line[FILE_INDEX_OF_QUAL]);
        filter =        (String)    parse(CLASS_OF_FILTER, line[FILE_INDEX_OF_FILTER]);
        customInfo =    (String)    parse(CLASS_OF_CUSTOMINFO, line[FILE_INDEX_OF_INFO]);
        
        if ((ref == null || ref.length()==0) && (alt == null || alt.length()==0)){
            throw new Exception();
        }
        if(ref != null && ref.length() > 30){
            ref = "(too long to display)";
        }
        if (alt != null && alt.length() > 30){
            alt = "(too long to display)";
        }
                
        type = getVariantType(ref, alt);
        //genotype = getGenotype(ref, alt); // DO THESE TOGETHER?
        //zygosity = calculateZygosity(); //
        
        //parseInfo(line[FILE_INDEX_OF_INFO], infoKeys, infoClasses);        
    }

    public VariantRecord(
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
            String customInfo,
            Object[] customFields
            ){       
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
        this.customInfo = customInfo;    
        this.customFields = customFields;
    }     
    
    protected VariantRecord(VariantRecord r) {
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
        this.setCustomInfo(r.getCustomInfo());    
        this.setCustomFields(r.getCustomFields());
        this.setType(r.getType());
    }

    private static Object parse(Class c, String value) {

        if (c == String.class) {
            if(value.equals(nullString)) return "";
            return value;
        }
        
        if(value.equals(nullString)){
            return null;
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
      
    private VariantType getVariantType(String ref, String alt) {
        if(ref.startsWith("<") || alt.startsWith("<")){
            if(alt.contains("<DEL>") || ref.contains("<DEL>")){
                return VariantType.Deletion;
            } else if (alt.contains("<INS>") || ref.contains("<INS>")){
                return VariantType.Insertion;
            } else {
                return VariantType.Unknown;
            }
        }
        
        VariantType result = null;
        for(String s : alt.split(",")){
            if(ref == null || (s != null && s.length() > ref.length())){
                result = variantTypeHelper(result, VariantType.Insertion);
            } else if (s == null || (ref != null && ref.length() > s.length())){
                result = variantTypeHelper(result, VariantType.Deletion);
            } else {
                result = variantTypeHelper(result, VariantType.SNP);
            }
        }
        return result;
    }
    
    private VariantType variantTypeHelper(VariantType currentType, VariantType newType){
        if(currentType == null || currentType == newType){
            return newType;
        } else {
            return VariantType.Various;
        }
    }
    
    /*private Zygosity calculateZygosity(){
        Object[] field = parseInfo(customInfo, new String[]{"GT"}, new Class[]{String.class});
        if(field[0] == null) return null;
        String value = (String)field[0];
        String[] split = value.split("/|\\\\|\\|"); // splits on / or \ or |
        if(split.length < 2 || split[0] == null || split[1] == null || split[0].length() == 0 || split[1].length() == 0) return null;
        
        try {
            int a = Integer.parseInt(split[0]);
            int b = Integer.parseInt(split[1]);
            if(a == 0 && b == 0){
                return Zygosity.HomoRef;
            } else if (a == b){
                return Zygosity.HomoAlt;
            } else if (a == 0 || b == 0){
                return Zygosity.Hetero;
            } else {
                return Zygosity.HeteroTriallelic;
            }
        } catch (NumberFormatException e){
            return null;
        }
    }*/

    /*private void parseInfoOld(String infoString, String[] infoKeys, Class[] infoClasses){
        
        customFields = new Object[infoKeys.length];
        customInfo = infoString;
        
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
            
            for(int i = 0; i < infoKeys.length; i++){
                if(name.equals(infoKeys[i])){
                    customFields[i] = parse(infoClasses[i], value);
                }
            }
        }
    }*/
    
    public static Object[] parseInfo(String infoString, String[] infoKeys, Class[] infoClasses){
        Object[] values = new Object[infoKeys.length];
        
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
            for(int i = 0; i < infoKeys.length; i++){
                if(name.toLowerCase().equals(infoKeys[i].toLowerCase())){
                    values[i] = parse(infoClasses[i], value);
                }
            }
        }
        
        return values;
    }
       
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
    
    public String getCustomInfo(){
        return customInfo;
    }

    public void setCustomInfo(String customInfo){
        this.customInfo = customInfo;
    }

    public Object[] getCustomFields() {
        return customFields;
    }

    public VariantType getType() {
        return type;
    }

    public void setType(VariantType type) {
        this.type = type;
    }

    public Zygosity getZygosity() {
        return zygosity;
    }

    public void setZygosity(Zygosity zygosity) {
        this.zygosity = zygosity;
    }

    public String getGenotype() {
        return genotype;
    }

    public void setGenotype(String genotype) {
        this.genotype = genotype;
    }

    public void setCustomFields(Object[] customFields) {
        this.customFields = customFields;
    }
    
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

    private static String delim = ",";
    
    public String toTabString(int uploadId, int fileId, int variantId) {
        String s =
                "\"" + getString(uploadId) + "\"" + delim + 
                "\"" + getString(fileId) + "\"" + delim + 
                "\"" + getString(variantId) + "\"" + delim + 
                "\"" + getString(this.dnaID) + "\"" + delim + 
                "\"" + getString(this.chrom) + "\"" + delim +              
                "\"" + getString(this.position) + "\"" + delim + 
                "\"" + getString(this.dbSNPID) + "\"" + delim + 
                "\"" + getString(this.ref) + "\"" + delim + 
                "\"" + getString(this.alt) + "\"" + delim + 
                "\"" + getString(this.qual) + "\"" + delim + 
                "\"" + getString(this.filter) + "\"" + delim + 
                "\"" + getString(this.type) + "\"" + delim + 
                "\"" + getString(this.zygosity) + "\"" + delim +
                "\"" + getString(this.genotype) + "\"" + delim +
                "\"" + getString(this.customInfo) + "\"";// + delim;
        //for(Object o : this.customFields){
        //    s += "\"" + getString(o) + "\"" + delim;
        //}
        //return s.substring(0, s.length()-1); //remove last comma
        return s;
    }
    
    public static String createTabString(Object[] values) {
        String s = "";
        if(values.length == 0) return s;
        for(Object o : values){
            s += "\"" + getString(o) + "\"" + delim;
        }
        return s.substring(0, s.length()-1);
    }
    
    private static String getString(Object value){ 
        if(value == null){
            return "";
        } else if (value instanceof Boolean){
            if((Boolean)value){
                return "1";
            } else {
                return "0";
            }
        } else {
            return value.toString();
        }
    }
    
}
