/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.format;

import java.io.Serializable;

import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.format.CustomField.Category;


/**
 *
 * @author Andrew
 */
public class VariantFormat implements Serializable {

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
    public static String ALIAS_OF_VARIANT_TYPE = "Variant Type";
    public static String ALIAS_OF_ZYGOSITY = "Zygosity";
    public static String ALIAS_OF_GT = "Genotype";
    public static String ALIAS_OF_CUSTOM_INFO = "Custom Info";

    //recommended fields (from vcf)
    public static String ALIAS_OF_AA = "Ancestral Allele";
    public static String ALIAS_OF_AC = "Allele Count";
    public static String ALIAS_OF_AF = "Allele Frequency";
    public static String ALIAS_OF_AN = "Number of Alleles";
    public static String ALIAS_OF_BQ = "Base Quality";
    public static String ALIAS_OF_CIGAR = "Cigar";
    public static String ALIAS_OF_DB = "dbSNP Membership";
    public static String ALIAS_OF_DP = "Depth of Coverage";
    public static String ALIAS_OF_END = "End Position";
    public static String ALIAS_OF_H2 = "HapMap Membership";
    public static String ALIAS_OF_MQ = "Mapping Quality";
    public static String ALIAS_OF_MQ0 = "Number of MQ0s";
    public static String ALIAS_OF_NS = "Number of Samples";
    public static String ALIAS_OF_SB = "Strand Bias";
    public static String ALIAS_OF_SOMATIC = "Somatic";
    public static String ALIAS_OF_VALIDATED = "Validated";

    public static AnnotationFormat getDefaultAnnotationFormat() {
        CustomField[] fields = new CustomField[] {
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID, "INT(11)", true, ALIAS_OF_UPLOAD_ID, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID, "INT(11)", true, ALIAS_OF_FILE_ID, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID, "INT(11)", true, ALIAS_OF_VARIANT_ID, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID, "VARCHAR(10)", true, ALIAS_OF_DNA_ID, "", Category.PATIENT),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM, "VARCHAR(5)", true, ALIAS_OF_CHROM, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION, "INT(11)", true, ALIAS_OF_POSITION, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_DBSNP_ID, "(VARCHAR(45)", false, ALIAS_OF_DBSNP_ID, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_REF, "VARCHAR(30)", true, ALIAS_OF_REF, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_ALT, "VARCHAR(30)", true, ALIAS_OF_ALT, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_QUAL, "FLOAT(10,0)", true, ALIAS_OF_QUAL, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_FILTER, "VARCHAR(500)", false, ALIAS_OF_FILTER, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_TYPE, "VARCHAR(10)", true, ALIAS_OF_VARIANT_TYPE, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_ZYGOSITY, "VARCHAR(20)", true, ALIAS_OF_ZYGOSITY, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_GT, "VARCHAR(10)", false, ALIAS_OF_GT, ""),
            new CustomField(DefaultVariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO, "VARCHAR(500)", false, ALIAS_OF_CUSTOM_INFO, "")
        };

        return new AnnotationFormat("default", "default", 0, "", true, true, AnnotationType.POSITION, fields);
    }
    
    public static AnnotationFormat getCustomFieldAnnotationFormat(CustomField[] customFields) {

        return new AnnotationFormat(ANNOTATION_FORMAT_CUSTOM_VCF, ANNOTATION_FORMAT_CUSTOM_VCF, 0, "", true, true, AnnotationType.POSITION, customFields);
    }    
}
