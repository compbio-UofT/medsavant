package org.medsavant.api.common.impl;

import java.io.Serializable;
import org.apache.commons.lang.NumberUtils;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.common.VariantType;
import org.medsavant.api.common.VariantUtils;
import org.medsavant.api.common.Zygosity;

/**
 * Creates genomic variant objects.
 *
 * @author jim
 */
public class GenomicVariantFactory {

    public static GenomicVariant create(
            String chrom,
            int start_position,
            int end_position,
            String dbSNPID,
            String ref,
            String alt,
            int altNumber,
            float qual,
            String filter,
            String customInfo) {

        GenomicVariantImpl instance = new GenomicVariantImpl();
        instance.chrom = chrom;

        if (!chrom.toLowerCase().startsWith("chr")) {
            String s = VariantUtils.homogenizeSequence(chrom);
            instance.chrom = "chr" + s;

            //Comment out chromosome check
            //checkChrom(s);            
        }

        instance.start_position = start_position;
        instance.end_position = end_position;
        instance.dbSNPID = dbSNPID;
        instance.ref = ref;
        instance.alt = alt;
        instance.altNumber = altNumber;
        instance.qual = qual;
        instance.filter = filter;
        instance.setCustomInfo(customInfo);
        return instance;
    }

    public static GenomicVariant createFromTabString(String[] t) {
        
        String chrom = GenomicVariantImpl.getString(t[0]);
        int start_position = Integer.parseInt(t[1]);
        int end_position = Integer.parseInt(t[2]);
        String dbSNPId = t[3];
        String ref = t[4];
        String alt = t[5];
        int altNumber = Integer.parseInt(t[6]);
        float qual = Float.parseFloat(t[7]);
        String filter = t[8];
        //what about, type, zygosity, genotype?
        String customInfo = t[12];
        
        return create(chrom, start_position, end_position, dbSNPId, ref, alt, altNumber, qual, filter, customInfo);

        //int, float, string, string
                 /*
         instance.start_position = instance.getString(t[1]);
                 
                 
         
         = getString(this.chrom) + "\"" + delim
         + "\"" + getString(this.start_position) + "\"" + delim
         + "\"" + getString(this.end_position) + "\"" + delim
         + "\"" + getString(this.dbSNPID) + "\"" + delim
         + "\"" + getString(this.ref) + "\"" + delim
         + "\"" + getString(this.alt) + "\"" + delim
         + "\"" + getString(this.altNumber) + "\"" + delim
         + "\"" + getString(this.qual) + "\"" + delim
         + "\"" + getString(this.filter) + "\"" + delim //8
         + "\"" + getString(this.type) + "\"" + delim //9
         + "\"" + getString(this.zygosity) + "\"" + delim //10
         + "\"" + getString(this.genotype) + "\"" + delim//11
         + "\"" + getString(this.customInfo) + "\"";// + delim;   *///12
    }

    public static GenomicVariant createFrom(GenomicVariant r) {
        GenomicVariantImpl instance = new GenomicVariantImpl();
        //this.setVariantID(r.getVariantID());
        //this.setDnaID(r.getDnaID());
        //this.setGenomeID(r.getGenomeID());        
        instance.setChrom(r.getChrom());
        //this.setPosition(r.getPosition());
        instance.setStartPosition(r.getStartPosition());
        instance.setEndPosition(r.getEndPosition());
        instance.setDbSNPID(r.getDbSNPID());
        instance.setRef(r.getRef());
        instance.setAlt(r.getAlt());
        instance.setAltNumber(r.getAltNumber());
        instance.setQual(r.getQual());
        instance.setFilter(r.getFilter());
        instance.setCustomInfo(r.getCustomInfo());
        //this.setCustomFields(r.getCustomFields());
        instance.setType(r.getType());
        return instance;
    }

    public static class GenomicVariantImpl implements GenomicVariant, Serializable {
        private static final String NULL_STRING = ".";

        private String dnaID;
        private String chrom;
        private Integer start_position;
        private Integer end_position;

        private String ref;
        private String alt;
        private int altNumber;

        private String dbSNPID;
        private Float qual;
        private String filter;
        private String customInfo;

        private VariantType type;
        private Zygosity zygosity;
        private String genotype;
        //private Object[] customFields;

        private String ancestralAllele;
        private Integer alleleCount;
        private Float alleleFrequency;
        private Integer numberOfAlleles;
        private Integer baseQuality;
        private String cigar;
        private Boolean dbSNPMembership;
        private Integer depthOfCoverage;
        // private Long endPosition;
        private Boolean hapmap2Membership;
        private Boolean hapmap3Membership;
        private Integer mappingQuality;
        private Integer numberOfZeroMQ;
        private Integer numberOfSamplesWithData;
        private Float strandBias;
        private Boolean isSomatic;
        private Boolean isValidated;
        private Boolean isInThousandGenomes;                      
        
        private GenomicVariantImpl() {

        }

        private void checkChrom(String s) {
            if (!s.equalsIgnoreCase("x") && !s.equalsIgnoreCase("y") && !s.equalsIgnoreCase("m")) {

                if (NumberUtils.isNumber(s)) {
                    int x = Integer.parseInt(s);
                    if (x < 1 || x > 22) {
                        throw new IllegalArgumentException("Invalid chromosome " + chrom) {
                            @Override
                            public Throwable fillInStackTrace() {
                                return this;
                            }
                        };
                    }
                } else {
                    throw new IllegalArgumentException("Invalid chromosome " + chrom) {
                        @Override
                        public Throwable fillInStackTrace() {
                            return this;
                        }
                    };
                }

            }
        }
        /*
         public GenomicVariantImpl(String[] line, long start, long end, String ref, String alt, int altNumber, VariantType vt) throws Exception {//, String[] infoKeys, Class[] infoClasses) {
         dnaID = null;
         chrom = (String) parse(CLASS_OF_CHROM, line[FILE_INDEX_OF_CHROM]);
         if (!chrom.toLowerCase().startsWith("chr")) {
         String s = MiscUtils.homogenizeSequence(chrom);
         chrom = "chr" + s;

         //Comment out chromosome check
         //checkChrom(s);            
         }

         dbSNPID = (String) parse(CLASS_OF_DBSNPID, line[FILE_INDEX_OF_DBSNPID]);

         qual = (Float) parse(CLASS_OF_QUAL, line[FILE_INDEX_OF_QUAL]);
         filter = (String) parse(CLASS_OF_FILTER, line[FILE_INDEX_OF_FILTER]);
         customInfo = (String) parse(CLASS_OF_CUSTOMINFO, line[FILE_INDEX_OF_INFO]);
        
         this.start_position = start;
         this.end_position = end;
         this.alt = alt;
         this.altNumber = altNumber;
         this.ref = ref;
         this.type = vt;

         extractInfo(customInfo);
         }
         */

        @Override
        public void setAltNumber(int a) {
            this.altNumber = a;
        }

        @Override
        public int getAltNumber() {
            return this.altNumber;
        }

        private Object parse(Class c, String value) {

            if (c == String.class) {
                if (value.equals(NULL_STRING)) {
                    return "";
                }
                return value;
            }

            if (value.equals(NULL_STRING)) {
                return null;
            }

            if (c == Long.class) {
                try {
                    return NumberUtils.isDigits(value) ? Long.parseLong(value) : null;
                } catch (Exception e) {
                    return null;
                }
            }

            if (c == Float.class) {
                try {
                    return NumberUtils.isNumber(value) ? Float.parseFloat(value) : null;
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
                    return NumberUtils.isDigits(value) ? Integer.parseInt(value) : null;
                } catch (Exception e) {
                    return null;
                }
            }

            throw new UnsupportedOperationException("Parser doesn't deal with objects of type " + c);
        }

        private VariantType variantTypeHelper(VariantType currentType, VariantType newType) {
            if (currentType == null || currentType == newType) {
                return newType;
            } else {
                return VariantType.Various;
            }
        }
                
        
        /*
        public String[] getCustomInfoFieldValues(List<MedSavantField> fields){
            
            String[] infoKeys = new String[fields.size()];
            Class[] infoClasses =  new Class[fields.size()];
            
            int j = 0;
            for(MedSavantField field : fields){
                infoKeys[j] = field.getAlias();
                infoClasses[j] = field.getClass();
                
            }
            Object[] values = new Object[infoKeys.length];

            String infoString = customInfo.trim();
            String[] list = infoString.split(";");

            for (String element : list) {
                String name = element;
                String value = "";
                int equals = element.indexOf("=");
                if (equals != -1) {
                    name = element.substring(0, equals);
                    value = element.substring(equals + 1);
                }
                for (int i = 0; i < infoKeys.length; i++) {
                    if (name.toLowerCase().equals(infoKeys[i].toLowerCase())) {
                        values[i] = parse(infoClasses[i], value);
                    }
                }
            }

            return values;
        }*/
         /*       
        private Object[] parseInfo(String[] infoKeys, Class[] infoClasses) {
            
            Object[] values = new Object[infoKeys.length];

            String infoString = customInfo.trim();
            String[] list = infoString.split(";");

            for (String element : list) {
                String name = element;
                String value = "";
                int equals = element.indexOf("=");
                if (equals != -1) {
                    name = element.substring(0, equals);
                    value = element.substring(equals + 1);
                }
                for (int i = 0; i < infoKeys.length; i++) {
                    if (name.toLowerCase().equals(infoKeys[i].toLowerCase())) {
                        values[i] = parse(infoClasses[i], value);
                    }
                }
            }

            return values;
        }*/

        /*
         public int getUploadID() {
         return uploadID;
         }

         public void setUploadIDID(int uploadID) {
         this.uploadID = uploadID;
         }

         public int getFileID() {
         return fileID;
         }

         public void setFileID(int fileID) {
         this.fileID = fileID;
         }

         public int getVariantID() {
         return variantID;
         }

         public void setVariantID(int variantID) {
         this.variantID = variantID;
         }

         public int getGenomeID() {
         return genomeID;
         }

         public void setGenomeID(int genomeID) {
         this.genomeID = genomeID;
         }

         public int getPipelineID() {
         return pipelineID;
         }

         public void setPipelineID(int pipelineID) {
         this.pipelineID = pipelineID;
         }
         */
        @Override
        public String getAlt() {
            return alt;
        }

        @Override
        public void setAlt(String alt) {
            this.alt = alt;
        }

        @Override
        public String getChrom() {
            return chrom;
        }

        @Override
        public void setChrom(String chrom) {
            this.chrom = chrom;
        }

        @Override
        public String getDbSNPID() {
            return dbSNPID;
        }

        @Override
        public void setDbSNPID(String id) {
            this.dbSNPID = id;
        }

        @Override
        public String getFilter() {
            return filter;
        }

        @Override
        public void setFilter(String filter) {
            this.filter = filter;
        }

        @Override
        public Integer getStartPosition() {
            return start_position;
        }

        @Override
        public void setStartPosition(Integer pos) {
            this.start_position = pos;
        }

        @Override
        public Integer getEndPosition() {
            return end_position;
        }

        @Override
        public void setEndPosition(Integer pos) {
            this.end_position = pos;
        }

        @Override
        public Float getQual() {
            return qual;
        }

        @Override
        public void setQual(Float qual) {
            this.qual = qual;
        }

        @Override
        public String getRef() {
            return ref;
        }

        @Override
        public void setRef(String ref) {
            this.ref = ref;
        }
        /*
         public String getDnaID() {
         return dnaID;
         }

         public void setDnaID(String dnaID) {
         this.dnaID = dnaID;
         }
         */

        @Override
        public String getCustomInfo() {
            return customInfo;
        }

        @Override
        public void setCustomInfo(String customInfo) {
            this.customInfo = customInfo;
            extractInfo(customInfo);
        }

        /*
         public Object[] getCustomFields() {
         return customFields;
         }
         */
        @Override
        public VariantType getType() {
            return type;
        }

        @Override
        public void setType(VariantType type) {
            this.type = type;
        }

        @Override
        public Zygosity getZygosity() {
            return zygosity;
        }

        @Override
        public void setZygosity(Zygosity zygosity) {
            this.zygosity = zygosity;
        }

        @Override
        public String getGenotype() {
            return genotype;
        }

        @Override
        public void setGenotype(String genotype) {
            this.genotype = genotype;
        }

        /*
         public void setCustomFields(Object[] customFields) {
         this.customFields = customFields;
         }
         */
        @Override
        public int compareTo(GenomicVariant other) {
            return compareTo(other.getChrom(), other.getStartPosition(), other.getEndPosition());
        }

        @Override
        public int compareTo(String chrom, int startpos, int endpos) {
            int chromCompare = compareChrom(this.getChrom(), chrom);
            if (chromCompare != 0) {
                return chromCompare;
            }

            int i1 = this.getStartPosition().compareTo(startpos);
            int i2 = this.getEndPosition().compareTo(endpos);
            if (i1 == 0 && i2 == 0) {
                return 0;
            }
            //sort by startpos, then endpos.
            if (i1 == 0) {
                return i2;
            } else {
                return i1;
            }

            //return this.getStartPosition().compareTo(startpos);// && this.getEndPosition().compareTo(endpos);
            //return this.getPosition().compareTo(pos);
        }

        public static int compareChrom(String chrom1, String chrom2) {
            chrom1 = chrom1.substring(3);
            chrom2 = chrom2.substring(3);
            try {
                if (NumberUtils.isNumber(chrom1) && NumberUtils.isNumber(chrom2)) {
                    Integer a = Integer.parseInt(chrom1);
                    Integer b = Integer.parseInt(chrom2);
                    return a.compareTo(b);
                }

            } catch (NumberFormatException e) {
                //return chrom1.compareTo(chrom2);
            }
            return chrom1.compareTo(chrom2);
        }

        /*
         * CUSTOM INFO
         */
        @Override
        public Integer getAlleleCount() {
            return alleleCount;
        }

        @Override
        public Float getAlleleFrequency() {
            return alleleFrequency;
        }

        @Override
        public String getAncestralAllele() {
            return ancestralAllele;
        }

        @Override
        public Integer getBaseQuality() {
            return baseQuality;
        }

        @Override
        public String getCigar() {
            return cigar;
        }

        @Override
        public Boolean getDbSNPMembership() {
            return dbSNPMembership;
        }

        @Override
        public Integer getDepthOfCoverage() {
            return depthOfCoverage;
        }

        @Override
        public Boolean getHapmap2Membership() {
            return hapmap2Membership;
        }

        @Override
        public Boolean getHapmap3Membership() {
            return hapmap3Membership;
        }

        @Override
        public Boolean getIsInThousandGenomes() {
            return isInThousandGenomes;
        }

        @Override
        public Boolean getIsSomatic() {
            return isSomatic;
        }

        @Override
        public Boolean getIsValidated() {
            return isValidated;
        }

        @Override
        public Integer getMappingQuality() {
            return mappingQuality;
        }

        @Override
        public Integer getNumberOfAlleles() {
            return numberOfAlleles;
        }

        @Override
        public Integer getNumberOfSamplesWithData() {
            return numberOfSamplesWithData;
        }

        @Override
        public Integer getNumberOfZeroMQ() {
            return numberOfZeroMQ;
        }

        @Override
        public Float getStrandBias() {
            return strandBias;
        }

        private Float extractFloatFromInfo(String key, String customInfo) {
            String val = extractValueFromInfo(key, customInfo);
            return (Float) parse(Float.class, val);
        }

        private String extractStringFromInfo(String key, String customInfo) {
            String val = extractValueFromInfo(key, customInfo);
            return val;
        }

        private Integer extractIntegerFromInfo(String key, String customInfo) {
            String val = extractValueFromInfo(key, customInfo);
            return (Integer) parse(Integer.class, val);
        }

        private Boolean extractBooleanFromInfo(String key, String customInfo) {
            Integer val = extractIntegerFromInfo(key, customInfo);
            if (val == null) {
                return null;
            }
            if (val == 1) {
                return true;
            } else {
                return false;
            }
        }

        private String extractValueFromInfo(String key, String customInfo) {

            String eKey = key + "=";
            int startIndex = customInfo.indexOf(eKey);
            if (startIndex == -1) {
                return "";
            }
            startIndex += eKey.length();
            String sub = customInfo.substring(startIndex);
            int endIndex = sub.indexOf(";");

            String result;
            if (endIndex == -1) {
                result = sub;
            } else {
                result = sub.substring(0, endIndex);
            }

            if (result.equals("")) {
                return null;
            } else {
                return result;
            }

        }

        private void extractInfo(String info) {
            this.ancestralAllele = extractStringFromInfo("AA", info);
            this.alleleCount = extractIntegerFromInfo("AC", info);
            this.alleleFrequency = extractFloatFromInfo("AF", info);
            this.numberOfAlleles = extractIntegerFromInfo("AN", info);
            this.baseQuality = extractIntegerFromInfo("BQ", info);
            this.cigar = extractStringFromInfo("CIGAR", info);
            this.dbSNPMembership = extractBooleanFromInfo("DB", info);
            this.depthOfCoverage = extractIntegerFromInfo("DP", info);
            //this.end_position = extractLongFromInfo("END", info); //We figure out END based on start, ref and alt.
            this.hapmap2Membership = extractBooleanFromInfo("H2", info);
            this.hapmap3Membership = extractBooleanFromInfo("H3", info);
            this.mappingQuality = extractIntegerFromInfo("MQ", info);
            this.numberOfZeroMQ = extractIntegerFromInfo("MQ0", info);
            this.numberOfSamplesWithData = extractIntegerFromInfo("NS", info);
            this.strandBias = extractFloatFromInfo("SB", info);
            this.isSomatic = extractBooleanFromInfo("SOMATIC", info);
            this.isValidated = extractBooleanFromInfo("VALIDATED", info);
            this.isInThousandGenomes = extractBooleanFromInfo("1000G", info);
        }

        /**
         * END CUSTOM INFO
         */
        @Override
        public String toString() {
            return "VariantRecord{" + "dnaID=" + dnaID + "chrom=" + chrom + "startpos=" + start_position + "endpos=" + end_position + "id=" + dbSNPID + "ref=" + ref + "alt=" + alt + "qual=" + qual + "filter=" + filter + '}';
        }
        private static String delim = "\t";

        @Override
        public String toTabString() {

            String s
                    = getString(this.chrom) + "\"" + delim
                    + "\"" + getString(this.start_position) + "\"" + delim
                    + "\"" + getString(this.end_position) + "\"" + delim
                    + "\"" + getString(this.dbSNPID) + "\"" + delim
                    + "\"" + getString(this.ref) + "\"" + delim
                    + "\"" + getString(this.alt) + "\"" + delim
                    + "\"" + getString(this.altNumber) + "\"" + delim
                    + "\"" + getString(this.qual) + "\"" + delim
                    + "\"" + getString(this.filter) + "\"" + delim
                    + "\"" + getString(this.type) + "\"" + delim
                    + "\"" + getString(this.zygosity) + "\"" + delim
                    + "\"" + getString(this.genotype) + "\"" + delim
                    + "\"" + getString(this.customInfo) + "\""; //Note custominfo is also emitted.
            return s;
        }

        /*
         public static String createTabString(Object[] values) {
         String s = "";
         if (values.length == 0) {
         return s;
         }
         for (Object o : values) {
         s += "\"" + StringEscapeUtils.escapeJava(getString(o)) + "\"" + delim;
         }
         return s.substring(0, s.length() - 1);
         }*/
        private static String getString(Object value) {
            if (value == null) {
                return "";
            } else if (value instanceof Boolean) {
                if ((Boolean) value) {
                    return "1";
                } else {
                    return "0";
                }
            } else {
                return value.toString();
            }
        }

        @Override
        public void setSampleInformation(String format, String info) {
            String formatted = "FORMAT=" + format + ";SAMPLE_INFO=" + info;
            String newCustomInfo;
            if (customInfo == null) {
                newCustomInfo = formatted;
            } else {
                newCustomInfo = customInfo + ";" + formatted;
            }
            setCustomInfo(newCustomInfo);
        }
    }

}
