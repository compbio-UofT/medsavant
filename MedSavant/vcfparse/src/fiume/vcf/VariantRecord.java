/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiume.vcf;

import java.io.Serializable;
import java.util.Vector;

/**
 *
 * @author mfiume
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
    public static final Class CLASS_OF_CHROM = String.class;
    public static final Class CLASS_OF_POS = Long.class;
    public static final Class CLASS_OF_ID = String.class;
    public static final Class CLASS_OF_REF = String.class;
    public static final Class CLASS_OF_ALT = String.class;
    public static final Class CLASS_OF_QUAL = Float.class;
    public static final Class CLASS_OF_FILTER = String.class;
    public static final Class CLASS_OF_INFO = String.class;
    public static final String nullString = ".";

    private String sampleID;
    private String chrom;
    private Long pos;
    private String id;
    private String ref;
    private String alt;
    private Float qual;
    private String filter;
    private String info;

    public VariantRecord(String[] line) {
        sampleID =  null;
        chrom =     (String)    parse(CLASS_OF_CHROM, line[FILE_INDEX_OF_CHROM]);
        pos =       (Long)      parse(CLASS_OF_POS, line[FILE_INDEX_OF_POS]);
        id =        (String)    parse(CLASS_OF_ID, line[FILE_INDEX_OF_ID]);
        ref =       (String)    parse(CLASS_OF_REF, line[FILE_INDEX_OF_REF]);
        alt =       (String)    parse(CLASS_OF_ALT, line[FILE_INDEX_OF_ALT]);
        qual =      (Float)      parse(CLASS_OF_QUAL, line[FILE_INDEX_OF_QUAL]);
        filter =    (String)    parse(CLASS_OF_FILTER, line[FILE_INDEX_OF_FILTER]);
        info =      (String)    parse(CLASS_OF_INFO, line[FILE_INDEX_OF_INFO]);
    }

    public VariantRecord(
            String sampleID,
            String chrom,
            long pos,
            String id,
            String ref,
            String alt,
            float qual,
            String filter,
            String info) {
        this.sampleID = sampleID;
        this.chrom = chrom;
        this.pos = pos;
        this.id = id;
        this.ref = ref;
        this.alt = alt;
        this.qual = qual;
        this.filter = filter;
        this.info = info;
    }



    public VariantRecord(VariantRecord r) {
        this.setChrom(r.getChrom());
        this.setPos(r.getPos());
        this.setId(r.getId());
        this.setRef(r.getRef());
        this.setAlt(r.getAlt());
        this.setQual(r.getQual());
        this.setFilter(r.getFilter());
        this.setInfo(r.getInfo());
    }

    private Object parse(Class c, String value) {

        if (value.equals(nullString)) {
            return null;
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

        throw new UnsupportedOperationException("Parser doesn't deal with objects of type " + c);
    }

    private void reportErrorConverting(Class c, String value) {
        System.err.println("Error: parsing " + value + " as " + c);
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Long getPos() {
        return pos;
    }

    public void setPos(Long pos) {
        this.pos = pos;
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

    public String getSampleID() {
        return sampleID;
    }

    public void setSampleID(String sampleID) {
        this.sampleID = sampleID;
    }

    @Override
    public String toString() {
        return "VariantRecord{" + "sampleID=" + sampleID + "chrom=" + chrom + "pos=" + pos + "id=" + id + "ref=" + ref + "alt=" + alt + "qual=" + qual + "filter=" + filter + "info=" + info + '}';
    }

    private static String delim = "\t";

    public String toTabString() {
        return sampleID + delim + chrom + delim + pos + delim + id + delim + ref + delim + alt + delim + qual + delim + filter + delim + info;
    }

}
