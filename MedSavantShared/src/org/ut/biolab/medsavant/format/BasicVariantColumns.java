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

import org.ut.biolab.medsavant.db.ColumnType;


/**
 * Interface which defines <code>CustomField</code> constants for the required columns in every variant table.
 * Used by both client and server.
 */
public interface BasicVariantColumns {

    // Indices for required fields.  The variant manager returns variant records as an array of Object, and the client depends on a
    // fixed ordering of elements in this array.
    public static int INDEX_OF_UPLOAD_ID = 0;
    public static int INDEX_OF_FILE_ID = 1;
    public static int INDEX_OF_VARIANT_ID = 2;
    public static int INDEX_OF_DNA_ID = 3;
    public static int INDEX_OF_CHROM = 4;
    public static int INDEX_OF_POSITION = 5;
    public static int INDEX_OF_DBSNP_ID = 6;
    public static int INDEX_OF_REF = 7;
    public static int INDEX_OF_ALT = 8;
    public static int INDEX_OF_QUAL = 9;
    public static int INDEX_OF_FILTER = 10;
    public static int INDEX_OF_VARIANT_TYPE = 11;
    public static int INDEX_OF_ZYGOSITY = 12;
    public static int INDEX_OF_GT = 13;
    public static int INDEX_OF_CUSTOM_INFO = 14;
    
    // Required fields
    public static final CustomField UPLOAD_ID = new CustomField("upload_id", ColumnType.INTEGER, 11, false, true, false, null, true, "Upload ID", "");
    public static final CustomField FILE_ID = new CustomField("file_id", ColumnType.INTEGER, 11, false, true, false, null, true, "File ID", "");
    public static final CustomField VARIANT_ID = new CustomField("variant_id", ColumnType.INTEGER, 11, false, true, false, null, true, "Variant ID", "");
    public static final CustomField DNA_ID = new CustomField("dna_id", ColumnType.VARCHAR, 100, false, true, false, null, true, "DNA ID", "");
    public static final CustomField CHROM = new CustomField("chrom", ColumnType.VARCHAR, 5, false, true, false, "", true, "Chromosome", "");
    public static final CustomField POSITION = new CustomField("position", ColumnType.INTEGER, 11, false, true, false, null, true, "Position", "");
    public static final CustomField DBSNP_ID = new CustomField("dbsnp_id", ColumnType.VARCHAR, 45, false, false, false, null, true, "dbSNP ID", "");
    public static final CustomField REF = new CustomField("ref", ColumnType.VARCHAR, 30, false, false, false, null, true, "Reference", "");
    public static final CustomField ALT = new CustomField("alt", ColumnType.VARCHAR, 30, false, false, false, null, true, "Alternate", "");
    public static final CustomField QUAL = new CustomField("qual", ColumnType.FLOAT, 10, false, false, false, null, true, "Quality", "");
    public static final CustomField FILTER = new CustomField("filter", ColumnType.VARCHAR, 500, false, false, false, null, true, "Filter", "");
    public static final CustomField VARIANT_TYPE = new CustomField("variant_type", ColumnType.VARCHAR, 10, false, false, false, null, true, "Variant Type", "");
    public static final CustomField ZYGOSITY = new CustomField("zygosity", ColumnType.VARCHAR, 20, false, false, false, null, true, "Zygosity", "");
    public static final CustomField GT = new CustomField("gt", ColumnType.VARCHAR, 10, false, false, false, null, true, "Genotype", "");
    public static final CustomField CUSTOM_INFO = new CustomField("custom_info", ColumnType.VARCHAR, 10000, false, false, false, null, true, "Custom Info", "");
    
    // The following commonly-used VCF fields are not part of the required set.
    public static final CustomField AA = new CustomField("aa", ColumnType.VARCHAR, 500, false, false, false, null, true, "Ancestral Allele", "");
    public static final CustomField AC = new CustomField("ac", ColumnType.INTEGER, 11, false, false, false, null, true, "Allele Count", "");
    public static final CustomField AF = new CustomField("af", ColumnType.FLOAT, -1, false, false, false, null, true, "Allele Frequency", "");
    public static final CustomField AN = new CustomField("an", ColumnType.INTEGER, 11, false, false, false, null, true, "Number of Alleles", "");
    public static final CustomField BQ = new CustomField("bq", ColumnType.FLOAT, -1, false, false, false, null, true, "Base Quality", "");
    public static final CustomField CIGAR = new CustomField("cigar", ColumnType.VARCHAR, 500, false, false, false, null, true, "Cigar", "");
    public static final CustomField DB = new CustomField("db", ColumnType.BOOLEAN, 1, false, false, false, null, true, "dbSNP Membership", "");
    public static final CustomField DP = new CustomField("dp", ColumnType.INTEGER, 11, false, false, false, null, true, "Depth of Coverage", "");
    public static final CustomField END = new CustomField("end", ColumnType.INTEGER, 11, false, false, false, null, true, "End Position", "");
    public static final CustomField H2 = new CustomField("h2", ColumnType.BOOLEAN, 1, false, false, false, null, true, "HapMap Membership", "");
    public static final CustomField MQ = new CustomField("mq", ColumnType.FLOAT, -1, false, false, false, null, true, "Mapping Quality", "");
    public static final CustomField MQ0 = new CustomField("mq0", ColumnType.INTEGER, 11, false, false, false, null, true, "Number of MQ0s", "");
    public static final CustomField NS = new CustomField("ns", ColumnType.INTEGER, 11, false, false, false, null, true, "Number of Samples", "");
    public static final CustomField SB = new CustomField("sb", ColumnType.FLOAT, -1, false, false, false, null, true, "Strand Bias", "");
    public static final CustomField SOMATIC = new CustomField("somatic", ColumnType.BOOLEAN, 1, false, false, false, null, true, "Somatic", "");
    public static final CustomField VALIDATED = new CustomField("validated", ColumnType.BOOLEAN, 1, false, false, false, null, true, "Validated", "");

    public static CustomField[] REQUIRED_VARIANT_FIELDS = new CustomField[] {
        UPLOAD_ID, FILE_ID, VARIANT_ID, DNA_ID, CHROM, POSITION, DBSNP_ID, REF, ALT, QUAL, FILTER, VARIANT_TYPE, ZYGOSITY, GT, CUSTOM_INFO
    };
}
