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

    public interface AnnotationColumns {
        static final ColumnDef ANNOTATION_ID = new ColumnDef("annotation_id", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDef PROGRAM = new ColumnDef("program", ColumnType.VARCHAR, 100);
        static final ColumnDef VERSION = new ColumnDef("version", ColumnType.VARCHAR, 100, true, false, true, null);
        static final ColumnDef REFERENCE_ID = new ColumnDef.Integer("reference_id");
        static final ColumnDef PATH = new ColumnDef("path", ColumnType.VARCHAR, 500);
        static final ColumnDef HAS_REF = new ColumnDef.Boolean("has_ref");
        static final ColumnDef HAS_ALT = new ColumnDef.Boolean("has_alt");
        static final ColumnDef TYPE = new ColumnDef.Integer("type");
    }

    public interface AnnotationFormatColumns {
        static final ColumnDef ANNOTATION_ID = new ColumnDef("annotation_id", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDef POSITION = new ColumnDef("position", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDef COLUMN_NAME = new ColumnDef("column_name", ColumnType.VARCHAR, 200);
        static final ColumnDef COLUMN_TYPE = new ColumnDef("column_type", ColumnType.VARCHAR, 45);
        static final ColumnDef FILTERABLE = new ColumnDef.Boolean("filterable");
        static final ColumnDef ALIAS = new ColumnDef("alias", ColumnType.VARCHAR, 200);
        static final ColumnDef DESCRIPTION = new ColumnDef("description", ColumnType.VARCHAR, 500);
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

    public interface GeneSetColumns {
        static final ColumnDef GENOME = new ColumnDef("genome", ColumnType.VARCHAR, 30);
        static final ColumnDef TYPE = new ColumnDef("type", ColumnType.VARCHAR, 30);
        static final ColumnDef NAME = new ColumnDef("name", ColumnType.VARCHAR, 30);
        static final ColumnDef CHROM = new ColumnDef("chrom", ColumnType.VARCHAR, 30);
        static final ColumnDef START = new ColumnDef.Integer("start");
        static final ColumnDef END = new ColumnDef.Integer("end");
        static final ColumnDef CODING_START = new ColumnDef.Integer("codingStart");
        static final ColumnDef CODING_END = new ColumnDef.Integer("codingEnd");
        static final ColumnDef EXON_STARTS = new ColumnDef("exonStarts", ColumnType.VARCHAR, 9000);
        static final ColumnDef EXON_ENDS = new ColumnDef("exonEnds", ColumnType.VARCHAR, 9000);
        static final ColumnDef TRANSCRIPT = new ColumnDef("transcript", ColumnType.VARCHAR, 30);
    }
    
    public interface OntologyColumns {
        static final ColumnDef ONTOLOGY = new ColumnDef("ontology", ColumnType.VARCHAR, 10);
        static final ColumnDef ID = new ColumnDef("id", ColumnType.VARCHAR, 30, false, true, true, null);
        static final ColumnDef NAME = new ColumnDef("name", ColumnType.VARCHAR, 300);
        static final ColumnDef DEF = new ColumnDef("def", ColumnType.TEXT, 120000, false, false, false, null);
        static final ColumnDef ALT_IDS = new ColumnDef("alt_ids", ColumnType.VARCHAR, 300, false, false, false, null);
        static final ColumnDef PARENTS = new ColumnDef("parents", ColumnType.VARCHAR, 120, false, false, false, null);
        static final ColumnDef GENES = new ColumnDef("genes", ColumnType.TEXT, 120000, false, false, false, null);
    }

    public interface OntologyInfoColumns {
        static final ColumnDef TYPE = new ColumnDef("type", ColumnType.VARCHAR, 10);
        static final ColumnDef ONTOLOGY_NAME = new ColumnDef("name", ColumnType.VARCHAR, 60, false, true, true, null);
        static final ColumnDef OBO_URL = new ColumnDef("obo_url", ColumnType.VARCHAR, 300);
        static final ColumnDef MAPPING_URL = new ColumnDef("mapping_url", ColumnType.VARCHAR, 300);
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

    public interface RegionSetColumns {
        static final ColumnDef REGION_SET_ID = new ColumnDef("region_set_id", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDef NAME = new ColumnDef("name", ColumnType.VARCHAR, 255);
    }

    public interface RegionSetMembershipColumns {
        static final ColumnDef REGION_SET_ID = new ColumnDef.Integer("region_set_id");
        static final ColumnDef GENOME_ID = new ColumnDef.Integer("genome_id");
        static final ColumnDef CHROM = new ColumnDef("chrom", ColumnType.VARCHAR, 30);
        static final ColumnDef START = new ColumnDef.Integer("start");
        static final ColumnDef END = new ColumnDef.Integer("end");
        static final ColumnDef DESCRIPTION = new ColumnDef("description", ColumnType.VARCHAR, 255);
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

    public static final DbSchema schema = (new DbSpec()).addDefaultSchema();

    public static final TableSchema AnnotationTableSchema = new TableSchema(schema, "annotation", AnnotationColumns.class);
    public static final TableSchema AnnotationFormatTableSchema = new TableSchema(schema, "annotation_format", AnnotationFormatColumns.class);
    public static final ChromosomeTableSchema ChromosomeTableSchema = new ChromosomeTableSchema(schema);
    public static final CohortTableSchema CohortTableSchema = new CohortTableSchema(schema);
    public static final CohortMembershipTableSchema CohortmembershipTableSchema = new CohortMembershipTableSchema(schema);
    public static final DefaultPatientTableSchema DefaultPatientTableSchema = new DefaultPatientTableSchema(schema);
    public static final DefaultVariantTableSchema DefaultVariantTableSchema = new DefaultVariantTableSchema(schema);
    public static final TableSchema GeneSetTableSchema = new TableSchema(schema, "genes", GeneSetColumns.class);
    public static final TableSchema OntologyInfoTableSchema = new TableSchema(schema, "ontology_info", OntologyInfoColumns.class);
    public static final TableSchema OntologyTableSchema = new TableSchema(schema, "ontology", OntologyColumns.class);
    public static final PatientFormatTableSchema PatientformatTableSchema = new PatientFormatTableSchema(schema);
    public static final PatientTablemapTableSchema PatienttablemapTableSchema = new PatientTablemapTableSchema(schema);
    public static final ProjectTableSchema ProjectTableSchema = new ProjectTableSchema(schema);
    public static final ReferenceTableSchema ReferenceTableSchema = new ReferenceTableSchema(schema);
    public static final TableSchema RegionSetTableSchema = new TableSchema(schema, "region_set", RegionSetColumns.class);
    public static final TableSchema RegionSetMembershipTableSchema = new TableSchema(schema, "region_set_membership", RegionSetMembershipColumns.class);
    public static final ServerLogTableSchema ServerlogTableSchema = new ServerLogTableSchema(schema);
    public static final SettingsTableSchema SettingsTableSchema = new SettingsTableSchema(schema);
    public static final VariantPendingUpdateTableSchema VariantpendingupdateTableSchema = new VariantPendingUpdateTableSchema(schema);
    public static final VariantTablemapTableSchema VarianttablemapTableSchema = new VariantTablemapTableSchema(schema);
    public static final VariantFormatTableSchema VariantformatTableSchema = new VariantFormatTableSchema(schema);
    public static final VarianttagTableSchema VarianttagTableSchema = new VarianttagTableSchema(schema);
    public static final VariantStarredTableSchema VariantStarredTableSchema = new VariantStarredTableSchema(schema);
    public static final VariantFileTableSchema VariantFileTableSchema = new VariantFileTableSchema(schema);
}
