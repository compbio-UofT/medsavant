/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.format;

import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.db.format.CustomField.Category;

/**
 *
 * @author Andrew
 */
public class VariantFormat {

    public static String ANNOTATION_FORMAT_DEFAULT = "default";
    public static String ANNOTATION_FORMAT_CUSTOM_VCF = "custom vcf";

    //default fields
    public static String ALIAS_OF_UPLOAD_ID = "Upload ID";
    public static String ALIAS_OF_FILE_ID = "File ID";
    public static String ALIAS_OF_VARIANT_ID = "Variant ID";
    public static String ALIAS_OF_DNA_ID = "DNA ID";
    public static String ALIAS_OF_CHROM = "Chromosome";
    public static String ALIAS_OF_POSITION = "Position";
    public static String ALIAS_OF_DBSNP_ID = "dbSNP ID";
    public static String ALIAS_OF_REF = "Reference";
    public static String ALIAS_OF_ALT = "Alternate";
    public static String ALIAS_OF_QUAL = "Quality";
    public static String ALIAS_OF_FILTER = "Filter";
    public static String ALIAS_OF_CUSTOM_INFO = "Custom Info";

    //recommended fields (from vcf)
    public static String ALIAS_OF_AA = "Ancestral Allele";
    public static String ALIAS_OF_AC = "Allele Count";
    public static String ALIAS_OF_AF = "Allele Frequency";
    public static String ALIAS_OF_AN = "Number of Alleles";
    public static String ALIAS_OF_BQ = "Base Quality";
    public static String ALIAS_OF_CIGAR = "Cigar";
    public static String ALIAS_OF_DB = "dbSNP";
    public static String ALIAS_OF_DP = "Depth of Coverage";
    public static String ALIAS_OF_END = "End Position";
    public static String ALIAS_OF_H2 = "HapMap Membership";
    public static String ALIAS_OF_MQ = "Mapping Quality";
    public static String ALIAS_OF_MQ0 = "Number of MQ0s";
    public static String ALIAS_OF_NS = "Number of Samples";
    public static String ALIAS_OF_SB = "Strand Bias";
    public static String ALIAS_OF_SOMATIC = "Somatic";
    public static String ALIAS_OF_VALIDATED = "Validated";

    public static AnnotationFormat getDefaultAnnotationFormat(){
        List<CustomField> fields = new ArrayList<CustomField>();
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID, "INT(11)", true, ALIAS_OF_UPLOAD_ID, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID, "INT(11)", true, ALIAS_OF_FILE_ID, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID, "INT(11)", true, ALIAS_OF_VARIANT_ID, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID, "VARCHAR(10)", true, ALIAS_OF_DNA_ID, "", Category.PATIENT));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM, "VARCHAR(5)", true, ALIAS_OF_CHROM, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION, "INT(11)", true, ALIAS_OF_POSITION, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_DBSNP_ID, "(VARCHAR(45)", true, ALIAS_OF_DBSNP_ID, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_REF, "VARCHAR(30)", true, ALIAS_OF_REF, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_ALT, "VARCHAR(30)", true, ALIAS_OF_ALT, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_QUAL, "FLOAT(10,0)", true, ALIAS_OF_QUAL, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_FILTER, "VARCHAR(500)", false, ALIAS_OF_FILTER, ""));
        fields.add(new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO, "VARCHAR(500)", false, ALIAS_OF_CUSTOM_INFO, ""));

        return new AnnotationFormat("default", "default", 0, "", true, true, AnnotationType.POSITION, fields);
    }
    
    public static AnnotationFormat getCustomFieldAnnotationFormat(List<CustomField> customFields) {

        return new AnnotationFormat(
                ANNOTATION_FORMAT_CUSTOM_VCF, ANNOTATION_FORMAT_CUSTOM_VCF, 0, "", true, true, AnnotationType.POSITION, customFields);
    }    
}
