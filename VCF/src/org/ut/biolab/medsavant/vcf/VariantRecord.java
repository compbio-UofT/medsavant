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
    private long position;
    private String dbSNPID;
    private String ref;
    private String alt;
    private float qual;
    private String filter;
    private String customInfo;    
    private Object[] customFields;
     
    public static final String nullString = ".";
    
    /*
     * DO NOT USE THIS UNLESS YOU KNOW WHAT YOU'RE DOING
     * Useful for creating empty record in some circumstances
     */
    public VariantRecord() {}
  
    public VariantRecord(String[] line){//, String[] infoKeys, Class[] infoClasses) {
        dnaID =  null;
        chrom =         (String)    parse(CLASS_OF_CHROM, line[FILE_INDEX_OF_CHROM]);
        position =      (Long)      parse(CLASS_OF_POSITION, line[FILE_INDEX_OF_POS]);
        dbSNPID =       (String)    parse(CLASS_OF_DBSNPID, line[FILE_INDEX_OF_DBSNPID]);
        ref =           (String)    parse(CLASS_OF_REF, line[FILE_INDEX_OF_REF]);
        alt =           (String)    parse(CLASS_OF_ALT, line[FILE_INDEX_OF_ALT]);
        qual =          (Float)     parse(CLASS_OF_QUAL, line[FILE_INDEX_OF_QUAL]);
        filter =        (String)    parse(CLASS_OF_FILTER, line[FILE_INDEX_OF_FILTER]);
        customInfo =    (String)    parse(CLASS_OF_CUSTOMINFO, line[FILE_INDEX_OF_INFO]);
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
    }

    private static Object parse(Class c, String value) {

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

    private void parseInfoOld(String infoString, String[] infoKeys, Class[] infoClasses){
        
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
    }
    
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
