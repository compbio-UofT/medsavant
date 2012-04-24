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
package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;

public class MedSavantDatabase {

    public static class AnnotationTableSchema extends TableSchema {

        public static final String TABLE_NAME = "annotation";

        public AnnotationTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // annotation.annotation_id
        public static final int INDEX_OF_ANNOTATION_ID = 0;
        public static final ColumnType TYPE_OF_ANNOTATION_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_ANNOTATION_ID = 11;
        public static final String COLUMNNAME_OF_ANNOTATION_ID = "annotation_id";
        // annotation.program
        public static final int INDEX_OF_PROGRAM = 1;
        public static final ColumnType TYPE_OF_PROGRAM = ColumnType.VARCHAR;
        public static final int LENGTH_OF_PROGRAM = 100;
        public static final String COLUMNNAME_OF_PROGRAM = "program";
        // annotation.version
        public static final int INDEX_OF_VERSION = 2;
        public static final ColumnType TYPE_OF_VERSION = ColumnType.VARCHAR;
        public static final int LENGTH_OF_VERSION = 100;
        public static final String COLUMNNAME_OF_VERSION = "version";
        // annotation.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 3;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // annotation.path
        public static final int INDEX_OF_PATH = 4;
        public static final ColumnType TYPE_OF_PATH = ColumnType.VARCHAR;
        public static final int LENGTH_OF_PATH = 500;
        public static final String COLUMNNAME_OF_PATH = "path";
        // annotation.has_ref
        public static final int INDEX_OF_HAS_REF = 5;
        public static final ColumnType TYPE_OF_HAS_REF = ColumnType.INTEGER;
        public static final int LENGTH_OF_HAS_REF = 1;
        public static final String COLUMNNAME_OF_HAS_REF = "has_ref";
        // annotation.has_alt
        public static final int INDEX_OF_HAS_ALT = 6;
        public static final ColumnType TYPE_OF_HAS_ALT = ColumnType.INTEGER;
        public static final int LENGTH_OF_HAS_ALT = 1;
        public static final String COLUMNNAME_OF_HAS_ALT = "has_alt";
        // annotation.type
        public static final int INDEX_OF_TYPE = 7;
        public static final ColumnType TYPE_OF_TYPE = ColumnType.INTEGER;
        public static final int LENGTH_OF_TYPE = 11;
        public static final String COLUMNNAME_OF_TYPE = "type";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_ANNOTATION_ID, COLUMNNAME_OF_ANNOTATION_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PROGRAM, COLUMNNAME_OF_PROGRAM, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_VERSION, COLUMNNAME_OF_VERSION, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PATH, COLUMNNAME_OF_PATH, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_HAS_REF, COLUMNNAME_OF_HAS_REF, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_HAS_ALT, COLUMNNAME_OF_HAS_ALT, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_TYPE, COLUMNNAME_OF_TYPE, ColumnType.INTEGER, 11);
        }
    }

    public static class AnnotationFormatTableSchema extends TableSchema {

        public static final String TABLE_NAME = "annotation_format";

        public AnnotationFormatTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // annotation_format.annotation_id
        public static final int INDEX_OF_ANNOTATION_ID = 0;
        public static final ColumnType TYPE_OF_ANNOTATION_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_ANNOTATION_ID = 11;
        public static final String COLUMNNAME_OF_ANNOTATION_ID = "annotation_id";
        // annotation_format.position
        public static final int INDEX_OF_POSITION = 1;
        public static final ColumnType TYPE_OF_POSITION = ColumnType.INTEGER;
        public static final int LENGTH_OF_POSITION = 11;
        public static final String COLUMNNAME_OF_POSITION = "position";
        // annotation_format.column_name
        public static final int INDEX_OF_COLUMN_NAME = 2;
        public static final ColumnType TYPE_OF_COLUMN_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_NAME = 200;
        public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
        // annotation_format.column_type
        public static final int INDEX_OF_COLUMN_TYPE = 3;
        public static final ColumnType TYPE_OF_COLUMN_TYPE = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_TYPE = 45;
        public static final String COLUMNNAME_OF_COLUMN_TYPE = "column_type";
        // annotation_format.filterable
        public static final int INDEX_OF_FILTERABLE = 4;
        public static final ColumnType TYPE_OF_FILTERABLE = ColumnType.INTEGER;
        public static final int LENGTH_OF_FILTERABLE = 1;
        public static final String COLUMNNAME_OF_FILTERABLE = "filterable";
        // annotation_format.alias
        public static final int INDEX_OF_ALIAS = 5;
        public static final ColumnType TYPE_OF_ALIAS = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ALIAS = 200;
        public static final String COLUMNNAME_OF_ALIAS = "alias";
        // annotation_format.description
        public static final int INDEX_OF_DESCRIPTION = 6;
        public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DESCRIPTION = 500;
        public static final String COLUMNNAME_OF_DESCRIPTION = "description";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_ANNOTATION_ID, COLUMNNAME_OF_ANNOTATION_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_POSITION, COLUMNNAME_OF_POSITION, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_COLUMN_NAME, COLUMNNAME_OF_COLUMN_NAME, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_COLUMN_TYPE, COLUMNNAME_OF_COLUMN_TYPE, ColumnType.VARCHAR, 45);
            addColumn(COLUMNNAME_OF_FILTERABLE, COLUMNNAME_OF_FILTERABLE, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_ALIAS, COLUMNNAME_OF_ALIAS, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_DESCRIPTION, COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 500);
        }
    }

    public static class ChromosomeTableSchema extends TableSchema {

        public static final String TABLE_NAME = "chromosome";

        public ChromosomeTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // chromosome.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 0;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // chromosome.contig_id
        public static final int INDEX_OF_CONTIG_ID = 1;
        public static final ColumnType TYPE_OF_CONTIG_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_CONTIG_ID = 11;
        public static final String COLUMNNAME_OF_CONTIG_ID = "contig_id";
        // chromosome.contig_name
        public static final int INDEX_OF_CONTIG_NAME = 2;
        public static final ColumnType TYPE_OF_CONTIG_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_CONTIG_NAME = 100;
        public static final String COLUMNNAME_OF_CONTIG_NAME = "contig_name";
        // chromosome.contig_length
        public static final int INDEX_OF_CONTIG_LENGTH = 3;
        public static final ColumnType TYPE_OF_CONTIG_LENGTH = ColumnType.INTEGER;
        public static final int LENGTH_OF_CONTIG_LENGTH = 11;
        public static final String COLUMNNAME_OF_CONTIG_LENGTH = "contig_length";
        // chromosome.centromere_pos
        public static final int INDEX_OF_CENTROMERE_POS = 4;
        public static final ColumnType TYPE_OF_CENTROMERE_POS = ColumnType.INTEGER;
        public static final int LENGTH_OF_CENTROMERE_POS = 11;
        public static final String COLUMNNAME_OF_CENTROMERE_POS = "centromere_pos";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_CONTIG_ID, COLUMNNAME_OF_CONTIG_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_CONTIG_NAME, COLUMNNAME_OF_CONTIG_NAME, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_CONTIG_LENGTH, COLUMNNAME_OF_CONTIG_LENGTH, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_CENTROMERE_POS, COLUMNNAME_OF_CENTROMERE_POS, ColumnType.INTEGER, 11);
        }
    }

    public static class CohortTableSchema extends TableSchema {

        public static final String TABLE_NAME = "cohort";

        public CohortTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // cohort.cohort_id
        public static final int INDEX_OF_COHORT_ID = 0;
        public static final ColumnType TYPE_OF_COHORT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_COHORT_ID = 11;
        public static final String COLUMNNAME_OF_COHORT_ID = "cohort_id";
        // cohort.project_id
        public static final int INDEX_OF_PROJECT_ID = 1;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // cohort.name
        public static final int INDEX_OF_NAME = 2;
        public static final ColumnType TYPE_OF_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_NAME = 255;
        public static final String COLUMNNAME_OF_NAME = "name";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_COHORT_ID, COLUMNNAME_OF_COHORT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_NAME, COLUMNNAME_OF_NAME, ColumnType.VARCHAR, 255);
        }
    }

    public static class CohortMembershipTableSchema extends TableSchema {

        public static final String TABLE_NAME = "cohort_membership";

        public CohortMembershipTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // cohort_membership.cohort_id
        public static final int INDEX_OF_COHORT_ID = 0;
        public static final ColumnType TYPE_OF_COHORT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_COHORT_ID = 11;
        public static final String COLUMNNAME_OF_COHORT_ID = "cohort_id";
        // cohort_membership.patient_id
        public static final int INDEX_OF_PATIENT_ID = 1;
        public static final ColumnType TYPE_OF_PATIENT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PATIENT_ID = 11;
        public static final String COLUMNNAME_OF_PATIENT_ID = "patient_id";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_COHORT_ID, COLUMNNAME_OF_COHORT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PATIENT_ID, COLUMNNAME_OF_PATIENT_ID, ColumnType.INTEGER, 11);
        }
    }

    public static class DefaultpatientTableSchema extends TableSchema {

        public static final String TABLE_NAME = "default_patient";

        public DefaultpatientTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }

        public DefaultpatientTableSchema(DbSchema s, String tablename) {
            super(s.addTable(tablename));
            addColumns();
        }
        // default_patient.patient_id
        public static final int INDEX_OF_PATIENT_ID = 0;
        public static final ColumnType TYPE_OF_PATIENT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PATIENT_ID = 11;
        public static final String COLUMNNAME_OF_PATIENT_ID = "patient_id";
        // default_patient.family_id
        public static final int INDEX_OF_FAMILY_ID = 1;
        public static final ColumnType TYPE_OF_FAMILY_ID = ColumnType.VARCHAR;
        public static final int LENGTH_OF_FAMILY_ID = 100;
        public static final String COLUMNNAME_OF_FAMILY_ID = "family_id";
        // default_patient.hospital_id
        public static final int INDEX_OF_HOSPITAL_ID = 2;
        public static final ColumnType TYPE_OF_HOSPITAL_ID = ColumnType.VARCHAR;
        public static final int LENGTH_OF_HOSPITAL_ID = 100;
        public static final String COLUMNNAME_OF_HOSPITAL_ID = "hospital_id";
        // default_patient.idbiomom
        public static final int INDEX_OF_IDBIOMOM = 3;
        public static final ColumnType TYPE_OF_IDBIOMOM = ColumnType.VARCHAR;
        public static final int LENGTH_OF_IDBIOMOM = 100;
        public static final String COLUMNNAME_OF_IDBIOMOM = "idbiomom";
        // default_patient.idbiodad
        public static final int INDEX_OF_IDBIODAD = 4;
        public static final ColumnType TYPE_OF_IDBIODAD = ColumnType.VARCHAR;
        public static final int LENGTH_OF_IDBIODAD = 100;
        public static final String COLUMNNAME_OF_IDBIODAD = "idbiodad";
        // default_patient.gender
        public static final int INDEX_OF_GENDER = 5;
        public static final ColumnType TYPE_OF_GENDER = ColumnType.INTEGER;
        public static final int LENGTH_OF_GENDER = 11;
        public static final String COLUMNNAME_OF_GENDER = "gender";
        // default_patient.affected
        public static final int INDEX_OF_AFFECTED = 6;
        public static final ColumnType TYPE_OF_AFFECTED = ColumnType.INTEGER;
        public static final int LENGTH_OF_AFFECTED = 1;
        public static final String COLUMNNAME_OF_AFFECTED = "affected";
        // default_patient.dna_ids
        public static final int INDEX_OF_DNA_IDS = 7;
        public static final ColumnType TYPE_OF_DNA_IDS = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DNA_IDS = 1000;
        public static final String COLUMNNAME_OF_DNA_IDS = "dna_ids";
        // default_patient.bam_url
        public static final int INDEX_OF_BAM_URL = 8;
        public static final ColumnType TYPE_OF_BAM_URL = ColumnType.VARCHAR;
        public static final int LENGTH_OF_BAM_URL = 5000;
        public static final String COLUMNNAME_OF_BAM_URL = "bam_url";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PATIENT_ID, COLUMNNAME_OF_PATIENT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FAMILY_ID, COLUMNNAME_OF_FAMILY_ID, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_HOSPITAL_ID, COLUMNNAME_OF_HOSPITAL_ID, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_IDBIOMOM, COLUMNNAME_OF_IDBIOMOM, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_IDBIODAD, COLUMNNAME_OF_IDBIODAD, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_GENDER, COLUMNNAME_OF_GENDER, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_AFFECTED, COLUMNNAME_OF_AFFECTED, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_DNA_IDS, COLUMNNAME_OF_DNA_IDS, ColumnType.VARCHAR, 1000);
            addColumn(COLUMNNAME_OF_BAM_URL, COLUMNNAME_OF_BAM_URL, ColumnType.VARCHAR, 5000);
        }
    }

    public static class DefaultVariantTableSchema extends TableSchema {

        public static final String TABLE_NAME = "default_variant";

        public DefaultVariantTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }

        public DefaultVariantTableSchema(DbSchema s, String tablename) {
            super(s.addTable(tablename));
            addColumns();
        }
        // default_variant.upload_id
        public static final int INDEX_OF_UPLOAD_ID = 0;
        public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPLOAD_ID = 11;
        public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
        // default_variant.file_id
        public static final int INDEX_OF_FILE_ID = 1;
        public static final ColumnType TYPE_OF_FILE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_FILE_ID = 11;
        public static final String COLUMNNAME_OF_FILE_ID = "file_id";
        // default_variant.variant_id
        public static final int INDEX_OF_VARIANT_ID = 2;
        public static final ColumnType TYPE_OF_VARIANT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_VARIANT_ID = 11;
        public static final String COLUMNNAME_OF_VARIANT_ID = "variant_id";
        // default_variant.dna_id
        public static final int INDEX_OF_DNA_ID = 3;
        public static final ColumnType TYPE_OF_DNA_ID = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DNA_ID = 100;
        public static final String COLUMNNAME_OF_DNA_ID = "dna_id";
        // default_variant.chrom
        public static final int INDEX_OF_CHROM = 4;
        public static final ColumnType TYPE_OF_CHROM = ColumnType.VARCHAR;
        public static final int LENGTH_OF_CHROM = 5;
        public static final String COLUMNNAME_OF_CHROM = "chrom";
        // default_variant.position
        public static final int INDEX_OF_POSITION = 5;
        public static final ColumnType TYPE_OF_POSITION = ColumnType.INTEGER;
        public static final int LENGTH_OF_POSITION = 11;
        public static final String COLUMNNAME_OF_POSITION = "position";
        // default_variant.dbsnp_id
        public static final int INDEX_OF_DBSNP_ID = 6;
        public static final ColumnType TYPE_OF_DBSNP_ID = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DBSNP_ID = 45;
        public static final String COLUMNNAME_OF_DBSNP_ID = "dbsnp_id";
        // default_variant.ref
        public static final int INDEX_OF_REF = 7;
        public static final ColumnType TYPE_OF_REF = ColumnType.VARCHAR;
        public static final int LENGTH_OF_REF = 30;
        public static final String COLUMNNAME_OF_REF = "ref";
        // default_variant.alt
        public static final int INDEX_OF_ALT = 8;
        public static final ColumnType TYPE_OF_ALT = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ALT = 30;
        public static final String COLUMNNAME_OF_ALT = "alt";
        // default_variant.qual
        public static final int INDEX_OF_QUAL = 9;
        public static final ColumnType TYPE_OF_QUAL = ColumnType.FLOAT;
        public static final int LENGTH_OF_QUAL = 10;
        public static final String COLUMNNAME_OF_QUAL = "qual";
        // default_variant.filter
        public static final int INDEX_OF_FILTER = 10;
        public static final ColumnType TYPE_OF_FILTER = ColumnType.VARCHAR;
        public static final int LENGTH_OF_FILTER = 500;
        public static final String COLUMNNAME_OF_FILTER = "filter";
        // default_variant.variant_type
        public static final int INDEX_OF_VARIANT_TYPE = 11;
        public static final ColumnType TYPE_OF_VARIANT_TYPE = ColumnType.VARCHAR;
        public static final int LENGTH_OF_VARIANT_TYPE = 10;
        public static final String COLUMNNAME_OF_VARIANT_TYPE = "variant_type";
        // default_variant.zygosity
        public static final int INDEX_OF_ZYGOSITY = 12;
        public static final ColumnType TYPE_OF_ZYGOSITY = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ZYGOSITY = 20;
        public static final String COLUMNNAME_OF_ZYGOSITY = "zygosity";
        // default_variant.gt
        public static final int INDEX_OF_GT = 13;
        public static final ColumnType TYPE_OF_GT = ColumnType.VARCHAR;
        public static final int LENGTH_OF_GT = 10;
        public static final String COLUMNNAME_OF_GT = "gt";
        // default_variant.custom_info
        public static final int INDEX_OF_CUSTOM_INFO = 14;
        public static final ColumnType TYPE_OF_CUSTOM_INFO = ColumnType.VARCHAR;
        public static final int LENGTH_OF_CUSTOM_INFO = 1000;
        public static final String COLUMNNAME_OF_CUSTOM_INFO = "custom_info";
        // default_variant.aa
        public static final int INDEX_OF_AA = 15;
        public static final ColumnType TYPE_OF_AA = ColumnType.VARCHAR;
        public static final int LENGTH_OF_AA = 500;
        public static final String COLUMNNAME_OF_AA = "aa";
        // default_variant.ac
        public static final int INDEX_OF_AC = 16;
        public static final ColumnType TYPE_OF_AC = ColumnType.VARCHAR;
        public static final int LENGTH_OF_AC = 500;
        public static final String COLUMNNAME_OF_AC = "ac";
        // default_variant.af
        public static final int INDEX_OF_AF = 17;
        public static final ColumnType TYPE_OF_AF = ColumnType.VARCHAR;
        public static final int LENGTH_OF_AF = 500;
        public static final String COLUMNNAME_OF_AF = "af";
        // default_variant.an
        public static final int INDEX_OF_AN = 18;
        public static final ColumnType TYPE_OF_AN = ColumnType.INTEGER;
        public static final int LENGTH_OF_AN = 11;
        public static final String COLUMNNAME_OF_AN = "an";
        // default_variant.bq
        public static final int INDEX_OF_BQ = 19;
        public static final ColumnType TYPE_OF_BQ = ColumnType.FLOAT;
        public static final int LENGTH_OF_BQ = -1;
        public static final String COLUMNNAME_OF_BQ = "bq";
        // default_variant.cigar
        public static final int INDEX_OF_CIGAR = 20;
        public static final ColumnType TYPE_OF_CIGAR = ColumnType.VARCHAR;
        public static final int LENGTH_OF_CIGAR = 500;
        public static final String COLUMNNAME_OF_CIGAR = "cigar";
        // default_variant.db
        public static final int INDEX_OF_DB = 21;
        public static final ColumnType TYPE_OF_DB = ColumnType.INTEGER;
        public static final int LENGTH_OF_DB = 1;
        public static final String COLUMNNAME_OF_DB = "db";
        // default_variant.dp
        public static final int INDEX_OF_DP = 22;
        public static final ColumnType TYPE_OF_DP = ColumnType.INTEGER;
        public static final int LENGTH_OF_DP = 11;
        public static final String COLUMNNAME_OF_DP = "dp";
        // default_variant.end
        public static final int INDEX_OF_END = 23;
        public static final ColumnType TYPE_OF_END = ColumnType.VARCHAR;
        public static final int LENGTH_OF_END = 500;
        public static final String COLUMNNAME_OF_END = "end";
        // default_variant.h2
        public static final int INDEX_OF_H2 = 24;
        public static final ColumnType TYPE_OF_H2 = ColumnType.INTEGER;
        public static final int LENGTH_OF_H2 = 1;
        public static final String COLUMNNAME_OF_H2 = "h2";
        // default_variant.mq
        public static final int INDEX_OF_MQ = 25;
        public static final ColumnType TYPE_OF_MQ = ColumnType.VARCHAR;
        public static final int LENGTH_OF_MQ = 500;
        public static final String COLUMNNAME_OF_MQ = "mq";
        // default_variant.mq0
        public static final int INDEX_OF_MQ0 = 26;
        public static final ColumnType TYPE_OF_MQ0 = ColumnType.VARCHAR;
        public static final int LENGTH_OF_MQ0 = 500;
        public static final String COLUMNNAME_OF_MQ0 = "mq0";
        // default_variant.ns
        public static final int INDEX_OF_NS = 27;
        public static final ColumnType TYPE_OF_NS = ColumnType.INTEGER;
        public static final int LENGTH_OF_NS = 11;
        public static final String COLUMNNAME_OF_NS = "ns";
        // default_variant.sb
        public static final int INDEX_OF_SB = 28;
        public static final ColumnType TYPE_OF_SB = ColumnType.VARCHAR;
        public static final int LENGTH_OF_SB = 500;
        public static final String COLUMNNAME_OF_SB = "sb";
        // default_variant.somatic
        public static final int INDEX_OF_SOMATIC = 29;
        public static final ColumnType TYPE_OF_SOMATIC = ColumnType.INTEGER;
        public static final int LENGTH_OF_SOMATIC = 1;
        public static final String COLUMNNAME_OF_SOMATIC = "somatic";
        // default_variant.validated
        public static final int INDEX_OF_VALIDATED = 30;
        public static final ColumnType TYPE_OF_VALIDATED = ColumnType.INTEGER;
        public static final int LENGTH_OF_VALIDATED = 1;
        public static final String COLUMNNAME_OF_VALIDATED = "validated";


        private void addColumns() {
            addColumn(COLUMNNAME_OF_UPLOAD_ID, COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FILE_ID, COLUMNNAME_OF_FILE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_VARIANT_ID, COLUMNNAME_OF_VARIANT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_DNA_ID, COLUMNNAME_OF_DNA_ID, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_CHROM, COLUMNNAME_OF_CHROM, ColumnType.VARCHAR, 5);
            addColumn(COLUMNNAME_OF_POSITION, COLUMNNAME_OF_POSITION, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_DBSNP_ID, COLUMNNAME_OF_DBSNP_ID, ColumnType.VARCHAR, 45);
            addColumn(COLUMNNAME_OF_REF, COLUMNNAME_OF_REF, ColumnType.VARCHAR, 30);
            addColumn(COLUMNNAME_OF_ALT, COLUMNNAME_OF_ALT, ColumnType.VARCHAR, 30);
            addColumn(COLUMNNAME_OF_QUAL, COLUMNNAME_OF_QUAL, ColumnType.FLOAT, 10);
            addColumn(COLUMNNAME_OF_FILTER, COLUMNNAME_OF_FILTER, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_VARIANT_TYPE, COLUMNNAME_OF_VARIANT_TYPE, ColumnType.VARCHAR, 10);
            addColumn(COLUMNNAME_OF_ZYGOSITY, COLUMNNAME_OF_ZYGOSITY, ColumnType.VARCHAR, 20);
            addColumn(COLUMNNAME_OF_GT, COLUMNNAME_OF_GT, ColumnType.VARCHAR, 10);
            addColumn(COLUMNNAME_OF_CUSTOM_INFO, COLUMNNAME_OF_CUSTOM_INFO, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_AA, COLUMNNAME_OF_AA, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_AC, COLUMNNAME_OF_AC, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_AF, COLUMNNAME_OF_AF, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_AN, COLUMNNAME_OF_AN, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_BQ, COLUMNNAME_OF_BQ, ColumnType.FLOAT, -1);
            addColumn(COLUMNNAME_OF_CIGAR, COLUMNNAME_OF_CIGAR, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_DB, COLUMNNAME_OF_DB, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_DP, COLUMNNAME_OF_DP, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_END, COLUMNNAME_OF_END, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_H2, COLUMNNAME_OF_H2, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_MQ, COLUMNNAME_OF_MQ, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_MQ0, COLUMNNAME_OF_MQ0, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_NS, COLUMNNAME_OF_NS, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_SB, COLUMNNAME_OF_SB, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_SOMATIC, COLUMNNAME_OF_SOMATIC, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_VALIDATED, COLUMNNAME_OF_VALIDATED, ColumnType.INTEGER, 1);
        }
    }

    public static class PatientFormatTableSchema extends TableSchema {

        public static final String TABLE_NAME = "patient_format";

        public PatientFormatTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // patient_format.project_id
        public static final int INDEX_OF_PROJECT_ID = 0;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // patient_format.position
        public static final int INDEX_OF_POSITION = 1;
        public static final ColumnType TYPE_OF_POSITION = ColumnType.INTEGER;
        public static final int LENGTH_OF_POSITION = 11;
        public static final String COLUMNNAME_OF_POSITION = "position";
        // patient_format.column_name
        public static final int INDEX_OF_COLUMN_NAME = 2;
        public static final ColumnType TYPE_OF_COLUMN_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_NAME = 200;
        public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
        // patient_format.column_type
        public static final int INDEX_OF_COLUMN_TYPE = 3;
        public static final ColumnType TYPE_OF_COLUMN_TYPE = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_TYPE = 45;
        public static final String COLUMNNAME_OF_COLUMN_TYPE = "column_type";
        // patient_format.filterable
        public static final int INDEX_OF_FILTERABLE = 4;
        public static final ColumnType TYPE_OF_FILTERABLE = ColumnType.INTEGER;
        public static final int LENGTH_OF_FILTERABLE = 1;
        public static final String COLUMNNAME_OF_FILTERABLE = "filterable";
        // patient_format.alias
        public static final int INDEX_OF_ALIAS = 5;
        public static final ColumnType TYPE_OF_ALIAS = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ALIAS = 200;
        public static final String COLUMNNAME_OF_ALIAS = "alias";
        // patient_format.description
        public static final int INDEX_OF_DESCRIPTION = 6;
        public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DESCRIPTION = 500;
        public static final String COLUMNNAME_OF_DESCRIPTION = "description";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_POSITION, COLUMNNAME_OF_POSITION, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_COLUMN_NAME, COLUMNNAME_OF_COLUMN_NAME, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_COLUMN_TYPE, COLUMNNAME_OF_COLUMN_TYPE, ColumnType.VARCHAR, 45);
            addColumn(COLUMNNAME_OF_FILTERABLE, COLUMNNAME_OF_FILTERABLE, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_ALIAS, COLUMNNAME_OF_ALIAS, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_DESCRIPTION, COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 500);
        }
    }

    public static class PatientTablemapTableSchema extends TableSchema {

        public static final String TABLE_NAME = "patient_tablemap";

        public PatientTablemapTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // patient_tablemap.project_id
        public static final int INDEX_OF_PROJECT_ID = 0;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // patient_tablemap.patient_tablename
        public static final int INDEX_OF_PATIENT_TABLENAME = 1;
        public static final ColumnType TYPE_OF_PATIENT_TABLENAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_PATIENT_TABLENAME = 100;
        public static final String COLUMNNAME_OF_PATIENT_TABLENAME = "patient_tablename";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PATIENT_TABLENAME, COLUMNNAME_OF_PATIENT_TABLENAME, ColumnType.VARCHAR, 100);
        }
    }

    public static class ProjectTableSchema extends TableSchema {

        public static final String TABLE_NAME = "project";

        public ProjectTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // project.project_id
        public static final int INDEX_OF_PROJECT_ID = 0;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // project.name
        public static final int INDEX_OF_NAME = 1;
        public static final ColumnType TYPE_OF_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_NAME = 50;
        public static final String COLUMNNAME_OF_NAME = "name";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_NAME, COLUMNNAME_OF_NAME, ColumnType.VARCHAR, 50);
        }
    }

    public static class ReferenceTableSchema extends TableSchema {

        public static final String TABLE_NAME = "reference";

        public ReferenceTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // reference.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 0;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // reference.name
        public static final int INDEX_OF_NAME = 1;
        public static final ColumnType TYPE_OF_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_NAME = 50;
        public static final String COLUMNNAME_OF_NAME = "name";
        // reference.url
        public static final int INDEX_OF_URL = 2;
        public static final ColumnType TYPE_OF_URL = ColumnType.VARCHAR;
        public static final int LENGTH_OF_URL = 200;
        public static final String COLUMNNAME_OF_URL = "url";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_NAME, COLUMNNAME_OF_NAME, ColumnType.VARCHAR, 50);
            addColumn(COLUMNNAME_OF_URL, COLUMNNAME_OF_URL, ColumnType.VARCHAR, 200);
        }
    }

    public static class RegionSetTableSchema extends TableSchema {

        public static final String TABLE_NAME = "region_set";

        public RegionSetTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // region_set.region_set_id
        public static final int INDEX_OF_REGION_SET_ID = 0;
        public static final ColumnType TYPE_OF_REGION_SET_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REGION_SET_ID = 11;
        public static final String COLUMNNAME_OF_REGION_SET_ID = "region_set_id";
        // region_set.name
        public static final int INDEX_OF_NAME = 1;
        public static final ColumnType TYPE_OF_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_NAME = 255;
        public static final String COLUMNNAME_OF_NAME = "name";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_REGION_SET_ID, COLUMNNAME_OF_REGION_SET_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_NAME, COLUMNNAME_OF_NAME, ColumnType.VARCHAR, 255);
        }
    }

    public static class RegionSetMembershipTableSchema extends TableSchema {

        public static final String TABLE_NAME = "region_set_membership";

        public RegionSetMembershipTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // region_set_membership.region_set_id
        public static final int INDEX_OF_REGION_SET_ID = 0;
        public static final ColumnType TYPE_OF_REGION_SET_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REGION_SET_ID = 11;
        public static final String COLUMNNAME_OF_REGION_SET_ID = "region_set_id";
        // region_set_membership.genome_id
        public static final int INDEX_OF_GENOME_ID = 1;
        public static final ColumnType TYPE_OF_GENOME_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_GENOME_ID = 11;
        public static final String COLUMNNAME_OF_GENOME_ID = "genome_id";
        // region_set_membership.chrom
        public static final int INDEX_OF_CHROM = 2;
        public static final ColumnType TYPE_OF_CHROM = ColumnType.VARCHAR;
        public static final int LENGTH_OF_CHROM = 255;
        public static final String COLUMNNAME_OF_CHROM = "chrom";
        // region_set_membership.start
        public static final int INDEX_OF_START = 3;
        public static final ColumnType TYPE_OF_START = ColumnType.INTEGER;
        public static final int LENGTH_OF_START = 11;
        public static final String COLUMNNAME_OF_START = "start";
        // region_set_membership.end
        public static final int INDEX_OF_END = 4;
        public static final ColumnType TYPE_OF_END = ColumnType.INTEGER;
        public static final int LENGTH_OF_END = 11;
        public static final String COLUMNNAME_OF_END = "end";
        // region_set_membership.description
        public static final int INDEX_OF_DESCRIPTION = 5;
        public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DESCRIPTION = 255;
        public static final String COLUMNNAME_OF_DESCRIPTION = "description";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_REGION_SET_ID, COLUMNNAME_OF_REGION_SET_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_GENOME_ID, COLUMNNAME_OF_GENOME_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_CHROM, COLUMNNAME_OF_CHROM, ColumnType.VARCHAR, 255);
            addColumn(COLUMNNAME_OF_START, COLUMNNAME_OF_START, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_END, COLUMNNAME_OF_END, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_DESCRIPTION, COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 255);
        }
    }

    public static class ServerLogTableSchema extends TableSchema {

        public static final String TABLE_NAME = "server_log";

        public ServerLogTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // server_log.id
        public static final int INDEX_OF_ID = 0;
        public static final ColumnType TYPE_OF_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_ID = 11;
        public static final String COLUMNNAME_OF_ID = "id";
        // server_log.user
        public static final int INDEX_OF_USER = 1;
        public static final ColumnType TYPE_OF_USER = ColumnType.VARCHAR;
        public static final int LENGTH_OF_USER = 50;
        public static final String COLUMNNAME_OF_USER = "user";
        // server_log.event
        public static final int INDEX_OF_EVENT = 2;
        public static final ColumnType TYPE_OF_EVENT = ColumnType.VARCHAR;
        public static final int LENGTH_OF_EVENT = 50;
        public static final String COLUMNNAME_OF_EVENT = "event";
        // server_log.description
        public static final int INDEX_OF_DESCRIPTION = 3;
        public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DESCRIPTION = -1;
        public static final String COLUMNNAME_OF_DESCRIPTION = "description";
        // server_log.timestamp
        public static final int INDEX_OF_TIMESTAMP = 4;
        public static final ColumnType TYPE_OF_TIMESTAMP = ColumnType.DATE;
        public static final int LENGTH_OF_TIMESTAMP = -1;
        public static final String COLUMNNAME_OF_TIMESTAMP = "timestamp";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_ID, COLUMNNAME_OF_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_USER, COLUMNNAME_OF_USER, ColumnType.VARCHAR, 50);
            addColumn(COLUMNNAME_OF_EVENT, COLUMNNAME_OF_EVENT, ColumnType.VARCHAR, 50);
            addColumn(COLUMNNAME_OF_DESCRIPTION, COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, -1);
            addColumn(COLUMNNAME_OF_TIMESTAMP, COLUMNNAME_OF_TIMESTAMP, ColumnType.DATE, -1);
        }
    }

    public static class VariantPendingUpdateTableSchema extends TableSchema {

        public static final String TABLE_NAME = "variant_pending_update";

        public VariantPendingUpdateTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // variant_pending_update.upload_id
        public static final int INDEX_OF_UPLOAD_ID = 0;
        public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPLOAD_ID = 11;
        public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
        // variant_pending_update.project_id
        public static final int INDEX_OF_PROJECT_ID = 1;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // variant_pending_update.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 2;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // variant_pending_update.action
        public static final int INDEX_OF_ACTION = 3;
        public static final ColumnType TYPE_OF_ACTION = ColumnType.INTEGER;
        public static final int LENGTH_OF_ACTION = 11;
        public static final String COLUMNNAME_OF_ACTION = "action";
        // variant_pending_update.status
        public static final int INDEX_OF_STATUS = 4;
        public static final ColumnType TYPE_OF_STATUS = ColumnType.INTEGER;
        public static final int LENGTH_OF_STATUS = 5;
        public static final String COLUMNNAME_OF_STATUS = "status";
        // variant_pending_update.timestamp
        public static final int INDEX_OF_TIMESTAMP = 5;
        public static final ColumnType TYPE_OF_TIMESTAMP = ColumnType.DATE;
        public static final int LENGTH_OF_TIMESTAMP = -1;
        public static final String COLUMNNAME_OF_TIMESTAMP = "timestamp";
        // variant_pending_update.user
        public static final int INDEX_OF_USER = 6;
        public static final ColumnType TYPE_OF_USER = ColumnType.VARCHAR;
        public static final int LENGTH_OF_USER = 200;
        public static final String COLUMNNAME_OF_USER = "user";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_UPLOAD_ID, COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_ACTION, COLUMNNAME_OF_ACTION, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_STATUS, COLUMNNAME_OF_STATUS, ColumnType.INTEGER, 5);
            addColumn(COLUMNNAME_OF_TIMESTAMP, COLUMNNAME_OF_TIMESTAMP, ColumnType.DATE, -1);
            addColumn(COLUMNNAME_OF_USER, COLUMNNAME_OF_USER, ColumnType.VARCHAR, 200);
        }
    }

    public static class VariantTablemapTableSchema extends TableSchema {

        public static final String TABLE_NAME = "variant_tablemap";

        public VariantTablemapTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // variant_tablemap.project_id
        public static final int INDEX_OF_PROJECT_ID = 0;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // variant_tablemap.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 1;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // variant_tablemap.update_id
        public static final int INDEX_OF_UPDATE_ID = 2;
        public static final ColumnType TYPE_OF_UPDATE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPDATE_ID = 11;
        public static final String COLUMNNAME_OF_UPDATE_ID = "update_id";
        // variant_tablemap.published
        public static final int INDEX_OF_PUBLISHED = 3;
        public static final ColumnType TYPE_OF_PUBLISHED = ColumnType.BOOLEAN;
        public static final int LENGTH_OF_PUBLISHED = 1;
        public static final String COLUMNNAME_OF_PUBLISHED = "published";
        // variant_tablemap.variant_tablename
        public static final int INDEX_OF_VARIANT_TABLENAME = 4;
        public static final ColumnType TYPE_OF_VARIANT_TABLENAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_VARIANT_TABLENAME = 100;
        public static final String COLUMNNAME_OF_VARIANT_TABLENAME = "variant_tablename";
        // variant_tablemap.annotation_ids
        public static final int INDEX_OF_ANNOTATION_IDS = 5;
        public static final ColumnType TYPE_OF_ANNOTATION_IDS = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ANNOTATION_IDS = 500;
        public static final String COLUMNNAME_OF_ANNOTATION_IDS = "annotation_ids";
        // variant_tablemap.variant_subset_tablename
        public static final int INDEX_OF_VARIANT_SUBSET_TABLENAME = 6;
        public static final ColumnType TYPE_OF_VARIANT_SUBSET_TABLENAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_VARIANT_SUBSET_TABLENAME = 100;
        public static final String COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME = "variant_subset_tablename";
        // variant_tablemap.subset_multiplier
        public static final int INDEX_OF_SUBSET_MULTIPLIER = 7;
        public static final ColumnType TYPE_OF_SUBSET_MULTIPLIER = ColumnType.FLOAT;
        public static final int LENGTH_OF_SUBSET_MULTIPLIER = -1;
        public static final String COLUMNNAME_OF_SUBSET_MULTIPLIER = "subset_multiplier";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_UPDATE_ID, COLUMNNAME_OF_UPDATE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PUBLISHED, COLUMNNAME_OF_PUBLISHED, ColumnType.BOOLEAN, 1);
            addColumn(COLUMNNAME_OF_VARIANT_TABLENAME, COLUMNNAME_OF_VARIANT_TABLENAME, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_ANNOTATION_IDS, COLUMNNAME_OF_ANNOTATION_IDS, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME, COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_SUBSET_MULTIPLIER, COLUMNNAME_OF_SUBSET_MULTIPLIER, ColumnType.FLOAT, -1);
        }
    }

    public static class VariantFormatTableSchema extends TableSchema {

        public static final String TABLE_NAME = "variant_format";

        public VariantFormatTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // patient_format.project_id
        public static final int INDEX_OF_PROJECT_ID = 0;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // patient_format.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 1;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // patient_format.update_id
        public static final int INDEX_OF_UPDATE_ID = 2;
        public static final ColumnType TYPE_OF_UPDATE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPDATE_ID = 11;
        public static final String COLUMNNAME_OF_UPDATE_ID = "update_id";
        // patient_format.position
        public static final int INDEX_OF_POSITION = 3;
        public static final ColumnType TYPE_OF_POSITION = ColumnType.INTEGER;
        public static final int LENGTH_OF_POSITION = 11;
        public static final String COLUMNNAME_OF_POSITION = "position";
        // patient_format.column_name
        public static final int INDEX_OF_COLUMN_NAME = 4;
        public static final ColumnType TYPE_OF_COLUMN_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_NAME = 200;
        public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
        // patient_format.column_type
        public static final int INDEX_OF_COLUMN_TYPE = 5;
        public static final ColumnType TYPE_OF_COLUMN_TYPE = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_TYPE = 45;
        public static final String COLUMNNAME_OF_COLUMN_TYPE = "column_type";
        // patient_format.filterable
        public static final int INDEX_OF_FILTERABLE = 6;
        public static final ColumnType TYPE_OF_FILTERABLE = ColumnType.INTEGER;
        public static final int LENGTH_OF_FILTERABLE = 1;
        public static final String COLUMNNAME_OF_FILTERABLE = "filterable";
        // patient_format.alias
        public static final int INDEX_OF_ALIAS = 7;
        public static final ColumnType TYPE_OF_ALIAS = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ALIAS = 200;
        public static final String COLUMNNAME_OF_ALIAS = "alias";
        // patient_format.description
        public static final int INDEX_OF_DESCRIPTION = 8;
        public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DESCRIPTION = 500;
        public static final String COLUMNNAME_OF_DESCRIPTION = "description";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_UPDATE_ID, COLUMNNAME_OF_UPDATE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_POSITION, COLUMNNAME_OF_POSITION, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_COLUMN_NAME, COLUMNNAME_OF_COLUMN_NAME, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_COLUMN_TYPE, COLUMNNAME_OF_COLUMN_TYPE, ColumnType.VARCHAR, 45);
            addColumn(COLUMNNAME_OF_FILTERABLE, COLUMNNAME_OF_FILTERABLE, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_ALIAS, COLUMNNAME_OF_ALIAS, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_DESCRIPTION, COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 500);
        }
    }

    public static class VarianttagTableSchema extends TableSchema {

        public static final String TABLE_NAME = "variant_tag";

        public VarianttagTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // variant_tag.upload_id
        public static final int INDEX_OF_UPLOAD_ID = 0;
        public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPLOAD_ID = 11;
        public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
        // variant_tag.key
        public static final int INDEX_OF_TAGKEY = 1;
        public static final ColumnType TYPE_OF_TAGKEY = ColumnType.VARCHAR;
        public static final int LENGTH_OF_TAGKEY = 500;
        public static final String COLUMNNAME_OF_TAGKEY = "tagkey";
        // variant_tag.value
        public static final int INDEX_OF_TAGVALUE = 2;
        public static final ColumnType TYPE_OF_TAGVALUE = ColumnType.VARCHAR;
        public static final int LENGTH_OF_TAGVALUE = 1000;
        public static final String COLUMNNAME_OF_TAGVALUE = "tagvalue";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_UPLOAD_ID, COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_TAGKEY, COLUMNNAME_OF_TAGKEY, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_TAGVALUE, COLUMNNAME_OF_TAGVALUE, ColumnType.VARCHAR, 1000);
        }
    }

    public static class SettingsTableSchema extends TableSchema {

        public static final String TABLE_NAME = "settings";

        public SettingsTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // settings.key
        public static final int INDEX_OF_KEY = 0;
        public static final ColumnType TYPE_OF_KEY = ColumnType.VARCHAR;
        public static final int LENGTH_OF_KEY = 100;
        public static final String COLUMNNAME_OF_KEY = "setting_key";
        // settings.value
        public static final int INDEX_OF_VALUE = 1;
        public static final ColumnType TYPE_OF_VALUE = ColumnType.VARCHAR;
        public static final int LENGTH_OF_VALUE = 300;
        public static final String COLUMNNAME_OF_VALUE = "setting_value";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_KEY, COLUMNNAME_OF_KEY, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_VALUE, COLUMNNAME_OF_VALUE, ColumnType.VARCHAR, 100);
        }
    }

    public static class VariantStarredTableSchema extends TableSchema {

        public static final String TABLE_NAME = "variant_starred";

        public VariantStarredTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // variant_starred.project_id
        public static final int INDEX_OF_PROJECT_ID = 0;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // variant_starred.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 1;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // variant_starred.upload_id
        public static final int INDEX_OF_UPLOAD_ID = 2;
        public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPLOAD_ID = 11;
        public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
        // variant_starred.file_id
        public static final int INDEX_OF_FILE_ID = 3;
        public static final ColumnType TYPE_OF_FILE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_FILE_ID = 11;
        public static final String COLUMNNAME_OF_FILE_ID = "file_id";
        // variant_starred.variant_id
        public static final int INDEX_OF_VARIANT_ID = 4;
        public static final ColumnType TYPE_OF_VARIANT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_VARIANT_ID = 11;
        public static final String COLUMNNAME_OF_VARIANT_ID = "variant_id";
        // variant_starred.user
        public static final int INDEX_OF_USER = 5;
        public static final ColumnType TYPE_OF_USER = ColumnType.VARCHAR;
        public static final int LENGTH_OF_USER = 200;
        public static final String COLUMNNAME_OF_USER = "user";
        // variant_starred.description
        public static final int INDEX_OF_DESCRIPTION = 6;
        public static final ColumnType TYPE_OF_DESCRIPTION = ColumnType.VARCHAR;
        public static final int LENGTH_OF_DESCRIPTION = 500;
        public static final String COLUMNNAME_OF_DESCRIPTION = "description";
        // variant_starred.timestamp
        public static final int INDEX_OF_TIMESTAMP = 7;
        public static final ColumnType TYPE_OF_TIMESTAMP = ColumnType.DATE;
        public static final int LENGTH_OF_TIMESTAMP = -1;
        public static final String COLUMNNAME_OF_TIMESTAMP = "timestamp";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_UPLOAD_ID, COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FILE_ID, COLUMNNAME_OF_FILE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_VARIANT_ID, COLUMNNAME_OF_VARIANT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_USER, COLUMNNAME_OF_USER, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_DESCRIPTION, COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_TIMESTAMP, COLUMNNAME_OF_TIMESTAMP, ColumnType.DATE, -1);
        }
    }

    public static class VariantFileTableSchema extends TableSchema {

        public static final String TABLE_NAME = "variant_file";

        public VariantFileTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // variant_file.upload_id
        public static final int INDEX_OF_UPLOAD_ID = 0;
        public static final ColumnType TYPE_OF_UPLOAD_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPLOAD_ID = 11;
        public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
        // variant_file.file_id
        public static final int INDEX_OF_FILE_ID = 1;
        public static final ColumnType TYPE_OF_FILE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_FILE_ID = 11;
        public static final String COLUMNNAME_OF_FILE_ID = "file_id";
        // variant_file.file_name
        public static final int INDEX_OF_FILE_NAME = 2;
        public static final ColumnType TYPE_OF_FILE_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_FILE_NAME = 500;
        public static final String COLUMNNAME_OF_FILE_NAME = "file_name";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_UPLOAD_ID, COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FILE_ID, COLUMNNAME_OF_FILE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FILE_NAME, COLUMNNAME_OF_FILE_NAME, ColumnType.VARCHAR, 500);
        }
    }

    /*public static class VariantDistinctFieldTableSchema extends TableSchema {

        public static final String TABLE_NAME = "variant_distinct_field";

        public VariantDistinctFieldTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }
        // variant_distinct_field.project_id
        public static final int INDEX_OF_PROJECT_ID = 0;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
        // variant_distinct_field.reference_id
        public static final int INDEX_OF_REFERENCE_ID = 1;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
        // variant_distinct_field.update_id
        public static final int INDEX_OF_UPDATE_ID = 2;
        public static final ColumnType TYPE_OF_UPDATE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_UPDATE_ID = 11;
        public static final String COLUMNNAME_OF_UPDATE_ID = "update_id";
        // variant_distinct_field.column_name
        public static final int INDEX_OF_COLUMN_NAME = 3;
        public static final ColumnType TYPE_OF_COLUMN_NAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_COLUMN_NAME = 200;
        public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
        // variant_distinct_field.values
        public static final int INDEX_OF_VALUES = 4;
        public static final ColumnType TYPE_OF_VALUES = ColumnType.VARCHAR;
        public static final int LENGTH_OF_VALUES = XXX;
        public static final String COLUMNNAME_OF_VALUES = "values";

        private void addColumns() {
            addColumn(COLUMNNAME_OF_PROJECT_ID, COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_UPDATE_ID, COLUMNNAME_OF_UPDATE_ID, ColumnType.INTEGER, 11);
            addColumn(XXX);
            addColumn(XXX);
        }
    }*/

    public static final DbSchema schema = (new DbSpec()).addDefaultSchema();
    //AnnotationTableSchema
    public static final AnnotationTableSchema AnnotationTableSchema = new AnnotationTableSchema(schema);
    //AnnotationformatTableSchema
    public static final AnnotationFormatTableSchema AnnotationformatTableSchema = new AnnotationFormatTableSchema(schema);
    //ChromosomeTableSchema
    public static final ChromosomeTableSchema ChromosomeTableSchema = new ChromosomeTableSchema(schema);
    //CohortTableSchema
    public static final CohortTableSchema CohortTableSchema = new CohortTableSchema(schema);
    //CohortmembershipTableSchema
    public static final CohortMembershipTableSchema CohortmembershipTableSchema = new CohortMembershipTableSchema(schema);
    //DefaultpatientTableSchema
    public static final DefaultpatientTableSchema DefaultpatientTableSchema = new DefaultpatientTableSchema(schema);
    //DefaultvariantTableSchema
    public static final DefaultVariantTableSchema DefaultvariantTableSchema = new DefaultVariantTableSchema(schema);
    //PatientformatTableSchema
    public static final PatientFormatTableSchema PatientformatTableSchema = new PatientFormatTableSchema(schema);
    //PatienttablemapTableSchema
    public static final PatientTablemapTableSchema PatienttablemapTableSchema = new PatientTablemapTableSchema(schema);
    //ProjectTableSchema
    public static final ProjectTableSchema ProjectTableSchema = new ProjectTableSchema(schema);
    //ReferenceTableSchema
    public static final ReferenceTableSchema ReferenceTableSchema = new ReferenceTableSchema(schema);
    //RegionsetTableSchema
    public static final RegionSetTableSchema RegionsetTableSchema = new RegionSetTableSchema(schema);
    //RegionsetmembershipTableSchema
    public static final RegionSetMembershipTableSchema RegionsetmembershipTableSchema = new RegionSetMembershipTableSchema(schema);
    //ServerlogTableSchema
    public static final ServerLogTableSchema ServerlogTableSchema = new ServerLogTableSchema(schema);
    //VariantpendingupdateTableSchema
    public static final VariantPendingUpdateTableSchema VariantpendingupdateTableSchema = new VariantPendingUpdateTableSchema(schema);
    //VarianttablemapTableSchema
    public static final VariantTablemapTableSchema VarianttablemapTableSchema = new VariantTablemapTableSchema(schema);
    //VariantformatTableSchema
    public static final VariantFormatTableSchema VariantformatTableSchema = new VariantFormatTableSchema(schema);
    //VarianttagTableSchema
    public static final VarianttagTableSchema VarianttagTableSchema = new VarianttagTableSchema(schema);
    //SettingsTableSchema
    public static final SettingsTableSchema SettingsTableSchema = new SettingsTableSchema(schema);
    //VariantStarredTableSchema
    public static final VariantStarredTableSchema VariantStarredTableSchema = new VariantStarredTableSchema(schema);
    //VariantFileTableSchema
    public static final VariantFileTableSchema VariantFileTableSchema = new VariantFileTableSchema(schema);
    //VariantDistinctFieldTableSchema
    //public static final VariantDistinctFieldTableSchema VariantDistinctFieldTableSchema = new VariantDistinctFieldTableSchema(schema);
}
