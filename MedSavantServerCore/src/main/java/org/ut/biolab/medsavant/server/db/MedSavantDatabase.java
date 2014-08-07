/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE
 * SOFTWARE BE LIABLE This software is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this software; if not, write
 * to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
 * MA 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.db;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.ut.biolab.medsavant.shared.db.ColumnDefImpl;
import org.medsavant.api.common.storage.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;

public class MedSavantDatabase {

    public static final String FIELD_DELIMITER = "\t";        
    public static final String ENCLOSED_BY = "\"";
    public static final String ESCAPE_CHAR = "\\";
    
    public interface AnnotationColumns {

        static final ColumnDefImpl ANNOTATION_ID = new ColumnDefImpl("annotation_id", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDefImpl PROGRAM = new ColumnDefImpl("program", ColumnType.VARCHAR, 100);
        static final ColumnDefImpl VERSION = new ColumnDefImpl("version", ColumnType.VARCHAR, 100, true, false, true, null);
        static final ColumnDefImpl REFERENCE_ID = new ColumnDefImpl("reference_id", ColumnType.INTEGER, 11);
        static final ColumnDefImpl PATH = new ColumnDefImpl("path", ColumnType.VARCHAR, 500);
        static final ColumnDefImpl HAS_REF = new ColumnDefImpl("has_ref", ColumnType.BOOLEAN, 1);
        static final ColumnDefImpl HAS_ALT = new ColumnDefImpl("has_alt", ColumnType.BOOLEAN, 1);
        static final ColumnDefImpl TYPE = new ColumnDefImpl("type", ColumnType.INTEGER, 11);
        static final ColumnDefImpl IS_END_INCLUSIVE = new ColumnDefImpl("is_end_inclusive", ColumnType.BOOLEAN, 1);
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
            addColumn(COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_CONTIG_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_CONTIG_NAME, ColumnType.VARCHAR, 100);
            addColumn(COLUMNNAME_OF_CONTIG_LENGTH, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_CENTROMERE_POS, ColumnType.INTEGER, 11);
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
            addColumn(COLUMNNAME_OF_COHORT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_NAME, ColumnType.VARCHAR, 255);
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
            addColumn(COLUMNNAME_OF_COHORT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PATIENT_ID, ColumnType.INTEGER, 11);
        }
    }

    public interface GeneSetColumns {

        static final ColumnDefImpl GENOME = new ColumnDefImpl("genome", ColumnType.VARCHAR, 30);
        static final ColumnDefImpl TYPE = new ColumnDefImpl("type", ColumnType.VARCHAR, 30);
        static final ColumnDefImpl NAME = new ColumnDefImpl("name", ColumnType.VARCHAR, 30);
        static final ColumnDefImpl CHROM = new ColumnDefImpl("chrom", ColumnType.VARCHAR, 30);
        static final ColumnDefImpl START = new ColumnDefImpl("start", ColumnType.INTEGER, 11);
        static final ColumnDefImpl END = new ColumnDefImpl("end", ColumnType.INTEGER, 11);
        static final ColumnDefImpl CODING_START = new ColumnDefImpl("codingStart", ColumnType.INTEGER, 11);
        static final ColumnDefImpl CODING_END = new ColumnDefImpl("codingEnd", ColumnType.INTEGER, 11);
        static final ColumnDefImpl EXON_STARTS = new ColumnDefImpl("exonStarts", ColumnType.VARCHAR, 9000);
        static final ColumnDefImpl EXON_ENDS = new ColumnDefImpl("exonEnds", ColumnType.VARCHAR, 9000);
        static final ColumnDefImpl TRANSCRIPT = new ColumnDefImpl("transcript", ColumnType.VARCHAR, 30);
    }

    public interface OntologyColumns {

        static final ColumnDefImpl ONTOLOGY = new ColumnDefImpl("ontology", ColumnType.VARCHAR, 10);
        static final ColumnDefImpl ID = new ColumnDefImpl("id", ColumnType.VARCHAR, 30, false, true, true, null);
        static final ColumnDefImpl NAME = new ColumnDefImpl("name", ColumnType.VARCHAR, 300);
        static final ColumnDefImpl DEF = new ColumnDefImpl("def", ColumnType.TEXT, 120000, false, false, false, null);
        static final ColumnDefImpl ALT_IDS = new ColumnDefImpl("alt_ids", ColumnType.VARCHAR, 300, false, false, false, null);
        static final ColumnDefImpl PARENTS = new ColumnDefImpl("parents", ColumnType.VARCHAR, 120, false, false, false, null);
        static final ColumnDefImpl GENES = new ColumnDefImpl("genes", ColumnType.TEXT, 120000, false, false, false, null);
    }

    public interface OntologyInfoColumns {

        static final ColumnDefImpl TYPE = new ColumnDefImpl("type", ColumnType.VARCHAR, 10);
        static final ColumnDefImpl ONTOLOGY_NAME = new ColumnDefImpl("name", ColumnType.VARCHAR, 60, false, true, true, null);
        static final ColumnDefImpl OBO_URL = new ColumnDefImpl("obo_url", ColumnType.VARCHAR, 300);
        static final ColumnDefImpl MAPPING_URL = new ColumnDefImpl("mapping_url", ColumnType.VARCHAR, 300);
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
            addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_POSITION, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_COLUMN_NAME, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_COLUMN_TYPE, ColumnType.VARCHAR, 45);
            addColumn(COLUMNNAME_OF_FILTERABLE, ColumnType.INTEGER, 1);
            addColumn(COLUMNNAME_OF_ALIAS, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 500);
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
            addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_PATIENT_TABLENAME, ColumnType.VARCHAR, 100);
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
            addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_NAME, ColumnType.VARCHAR, 50);
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
            addColumn(COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_NAME, ColumnType.VARCHAR, 50);
            addColumn(COLUMNNAME_OF_URL, ColumnType.VARCHAR, 200);
        }
    }

    public interface RegionSetColumns {

        static final ColumnDefImpl REGION_SET_ID = new ColumnDefImpl("region_set_id", ColumnType.INTEGER, 11, true, true, true, null);
        static final ColumnDefImpl NAME = new ColumnDefImpl("name", ColumnType.VARCHAR, 255);
    }

    public interface RegionSetMembershipColumns {

        static final ColumnDefImpl REGION_SET_ID = new ColumnDefImpl("region_set_id", ColumnType.INTEGER, 11);
        static final ColumnDefImpl GENOME_ID = new ColumnDefImpl("genome_id", ColumnType.INTEGER, 11);
        static final ColumnDefImpl CHROM = new ColumnDefImpl("chrom", ColumnType.VARCHAR, 30);
        static final ColumnDefImpl START = new ColumnDefImpl("start", ColumnType.INTEGER, 11);
        static final ColumnDefImpl END = new ColumnDefImpl("end", ColumnType.INTEGER, 11);
        static final ColumnDefImpl DESCRIPTION = new ColumnDefImpl("description", ColumnType.VARCHAR, 255);
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
            addColumn(COLUMNNAME_OF_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_USER, ColumnType.VARCHAR, 50);
            addColumn(COLUMNNAME_OF_EVENT, ColumnType.VARCHAR, 50);
            addColumn(COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, -1);
            addColumn(COLUMNNAME_OF_TIMESTAMP, ColumnType.DATE, -1);
        }
    }


    public interface VariantTagColumns {

        static final ColumnDefImpl UPLOAD_ID = new ColumnDefImpl("upload_id", ColumnType.INTEGER, 11);
        static final ColumnDefImpl TAGKEY = new ColumnDefImpl("tagkey", ColumnType.VARCHAR, 500);
        static final ColumnDefImpl TAGVALUE = new ColumnDefImpl("tagvalue", ColumnType.VARCHAR, 1000);
    }

    

    public static class UserRoleTableSchema extends TableSchema {

        public static final String TABLE_NAME = "user_role";

        public static final int INDEX_OF_ID = 0;
        public static final ColumnType TYPE_OF_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_ID = 11;
        public static final String COLUMNNAME_OF_ID = "user_role_id";

        public static final int INDEX_OF_ROLENAME = 1;
        public static final ColumnType TYPE_OF_ROLENAME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ROLENAME = 64;
        public static final String COLUMNNAME_OF_ROLENAME = "role_name";

        public static final int INDEX_OF_ROLE_DESCRIPTION = 2;
        public static final ColumnType TYPE_OF_ROLE_DESCRIPTION = ColumnType.TEXT;
        public static final String COLUMNNAME_OF_ROLE_DESCRIPTION = "role_description";
        public static final int LENGTH_OF_ROLE_DESCRIPTION = 0;

        public UserRoleTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }

        private void addColumns() {
            addColumn(COLUMNNAME_OF_ID, ColumnType.INTEGER, LENGTH_OF_ID);
            addColumn(COLUMNNAME_OF_ROLENAME, ColumnType.VARCHAR, LENGTH_OF_ROLENAME);
            addColumn(COLUMNNAME_OF_ROLE_DESCRIPTION, ColumnType.TEXT, LENGTH_OF_ROLE_DESCRIPTION);
        }

    }

    public static class UserRoleAssignmentTableSchema extends TableSchema {

        public static final String TABLE_NAME = "user_role_assignment";

        public static final int INDEX_OF_USERNAME = 0;
        public static final ColumnType TYPE_OF_USERNAME = ColumnType.VARCHAR;
        public static final String COLUMNNAME_OF_USERNAME = "user";
        public static final int LENGTH_OF_USERNAME = 300;

        public static final int INDEX_OF_ROLE_ID = 1;
        public static final ColumnType TYPE_OF_ROLE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_ROLE_ID = 11;
        public static final String COLUMNNAME_OF_ROLE_ID = "fk_user_role_id";

        public UserRoleAssignmentTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }

        private void addColumns() {
            addColumn(COLUMNNAME_OF_USERNAME, ColumnType.VARCHAR, LENGTH_OF_USERNAME);
            addColumn(COLUMNNAME_OF_ROLE_ID, ColumnType.INTEGER, LENGTH_OF_ROLE_ID);
        }
    }

    public static class UserCommentGroupTableSchema extends TableSchema {

        public static final String TABLE_NAME = "user_comment_group";

        public static final int INDEX_OF_USER_COMMENT_GROUP_ID = 0;
        public static final ColumnType TYPE_OF_USER_COMMENT_GROUP_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_USER_COMMENT_GROUP_ID = 11;
        public static final String COLUMNNAME_OF_USER_COMMENT_GROUP_ID = "user_comment_group_id";

        public static final int INDEX_OF_PROJECT_ID = 1;
        public static final ColumnType TYPE_OF_PROJECT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PROJECT_ID = 11;
        public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";

        public static final int INDEX_OF_REFERENCE_ID = 2;
        public static final ColumnType TYPE_OF_REFERENCE_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_REFERENCE_ID = 11;
        public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";

        public static final int INDEX_OF_CHROMOSOME = 3;
        public static final ColumnType TYPE_OF_CHROMOSOME = ColumnType.VARCHAR;
        public static final int LENGTH_OF_CHROMOSOME = 5;
        public static final String COLUMNNAME_OF_CHROMOSOME = "chrom";

        public static final int INDEX_OF_START_POSITION = 4;
        public static final ColumnType TYPE_OF_START_POSITION = ColumnType.INTEGER;
        public static final int LENGTH_OF_START_POSITION = 11;
        public static final String COLUMNNAME_OF_START_POSITION = "start_position";

        public static final int INDEX_OF_END_POSITION = 5;
        public static final ColumnType TYPE_OF_END_POSITION = ColumnType.INTEGER;
        public static final int LENGTH_OF_END_POSITION = 11;
        public static final String COLUMNNAME_OF_END_POSITION = "end_position";

        public static final int INDEX_OF_REF = 6;
        public static final ColumnType TYPE_OF_REF = ColumnType.VARCHAR;
        public static final int LENGTH_OF_REF = 255;
        public static final String COLUMNNAME_OF_REF = "ref";

        public static final int INDEX_OF_ALT = 7;
        public static final ColumnType TYPE_OF_ALT = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ALT = 255;
        public static final String COLUMNNAME_OF_ALT = "alt";

        public UserCommentGroupTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }

        private void addColumns() {
            addColumn(COLUMNNAME_OF_USER_COMMENT_GROUP_ID, TYPE_OF_USER_COMMENT_GROUP_ID, LENGTH_OF_USER_COMMENT_GROUP_ID);
            addColumn(COLUMNNAME_OF_PROJECT_ID, TYPE_OF_PROJECT_ID, LENGTH_OF_PROJECT_ID);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, TYPE_OF_REFERENCE_ID, LENGTH_OF_REFERENCE_ID);
            addColumn(COLUMNNAME_OF_CHROMOSOME, TYPE_OF_CHROMOSOME, LENGTH_OF_CHROMOSOME);
            addColumn(COLUMNNAME_OF_START_POSITION, TYPE_OF_START_POSITION, LENGTH_OF_START_POSITION);
            addColumn(COLUMNNAME_OF_END_POSITION, TYPE_OF_END_POSITION, LENGTH_OF_END_POSITION);
            addColumn(COLUMNNAME_OF_REF, TYPE_OF_REF, LENGTH_OF_REF);
            addColumn(COLUMNNAME_OF_ALT, TYPE_OF_ALT, LENGTH_OF_ALT);
        }
    }

    public static class UserCommentTableSchema extends TableSchema {

        public static final String TABLE_NAME = "user_comment";

        public static final int INDEX_OF_USER_COMMENT_ID = 0;
        public static final ColumnType TYPE_OF_USER_COMMENT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_USER_COMMENT_ID = 11;
        public static final String COLUMNNAME_OF_USER_COMMENT_ID = "user_comment_id";

        public static final int INDEX_OF_USER_COMMENT_GROUP_ID = 1;
        public static final ColumnType TYPE_OF_USER_COMMENT_GROUP_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_USER_COMMENT_GROUP_ID = 11;
        public static final String COLUMNNAME_OF_USER_COMMENT_GROUP_ID = "fk_user_comment_group_id";

        public static final int INDEX_OF_PARENT_USER_COMMENT_ID = 2;
        public static final ColumnType TYPE_OF_PARENT_USER_COMMENT_ID = ColumnType.INTEGER;
        public static final int LENGTH_OF_PARENT_USER_COMMENT_ID = 11;
        public static final String COLUMNNAME_OF_PARENT_USER_COMMENT_ID = "fk_parent_user_comment_id";

        public static final int INDEX_OF_USER = 3;
        public static final ColumnType TYPE_OF_USER = ColumnType.VARCHAR;
        public static final int LENGTH_OF_USER = 200;
        public static final String COLUMNNAME_OF_USER = "user";

        public static final int INDEX_OF_ONTOLOGY_ID = 4;
        public static final ColumnType TYPE_OF_ONTOLOGY_ID = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ONTOLOGY_ID = 10;
        public static final String COLUMNNAME_OF_ONTOLOGY_ID = "ontology_id";

        public static final int INDEX_OF_ONTOLOGY = 5;
        public static final ColumnType TYPE_OF_ONTOLOGY = ColumnType.VARCHAR;
        public static final int LENGTH_OF_ONTOLOGY = 30;
        public static final String COLUMNNAME_OF_ONTOLOGY = "ontology";

        public static final int INDEX_OF_APPROVED = 6;
        public static final ColumnType TYPE_OF_APPROVED = ColumnType.BOOLEAN;
        public static final String COLUMNNAME_OF_APPROVED = "is_approved";
        public static final int LENGTH_OF_APPROVED = 1;

        public static final int INDEX_OF_INCLUDE = 7;
        public static final ColumnType TYPE_OF_INCLUDE = ColumnType.BOOLEAN;
        public static final String COLUMNNAME_OF_INCLUDE = "is_included";
        public static final int LENGTH_OF_INCLUDE = 1;

        public static final int INDEX_OF_DELETED = 8;
        public static final ColumnType TYPE_OF_DELETED = ColumnType.BOOLEAN;
        public static final String COLUMNNAME_OF_DELETED = "is_deleted";
        public static final int LENGTH_OF_DELETED = 1;

        public static final int INDEX_OF_CREATION_DATE = 9;
        public static final ColumnType TYPE_OF_CREATION_DATE = ColumnType.DATETIME;
        public static final String COLUMNNAME_OF_CREATION_DATE = "creation_date";
        public static final int LENGTH_OF_CREATION_DATE = 0;

        public static final int INDEX_OF_MODIFICATION_DATE = 10;
        public static final ColumnType TYPE_OF_MODIFICATION_DATE = ColumnType.TIMESTAMP;
        public static final String COLUMNNAME_OF_MODIFICATION_DATE = "last_modified";
        public static final int LENGTH_OF_MODIFICATION_DATE = 0;

        public static final int INDEX_OF_COMMENT = 11;
        public static final ColumnType TYPE_OF_COMMENT = ColumnType.TEXT;
        public static final String COLUMNNAME_OF_COMMENT = "variant_comment";
        public static final int LENGTH_OF_COMMENT = 0;

        public UserCommentTableSchema(DbSchema s) {
            super(s.addTable(TABLE_NAME));
            addColumns();
        }

        private void addColumns() {
            addColumn(COLUMNNAME_OF_USER_COMMENT_ID, TYPE_OF_USER_COMMENT_ID, LENGTH_OF_USER_COMMENT_ID);
            addColumn(COLUMNNAME_OF_USER_COMMENT_GROUP_ID, TYPE_OF_USER_COMMENT_GROUP_ID, LENGTH_OF_USER_COMMENT_GROUP_ID);
            addColumn(COLUMNNAME_OF_PARENT_USER_COMMENT_ID, TYPE_OF_PARENT_USER_COMMENT_ID, LENGTH_OF_PARENT_USER_COMMENT_ID);
            addColumn(COLUMNNAME_OF_USER, TYPE_OF_USER, LENGTH_OF_USER);
            addColumn(COLUMNNAME_OF_ONTOLOGY, TYPE_OF_ONTOLOGY, LENGTH_OF_ONTOLOGY);
            addColumn(COLUMNNAME_OF_ONTOLOGY_ID, TYPE_OF_ONTOLOGY_ID, LENGTH_OF_ONTOLOGY_ID);
            addColumn(COLUMNNAME_OF_APPROVED, TYPE_OF_APPROVED, LENGTH_OF_APPROVED);
            addColumn(COLUMNNAME_OF_INCLUDE, TYPE_OF_INCLUDE, LENGTH_OF_INCLUDE);
            addColumn(COLUMNNAME_OF_DELETED, TYPE_OF_DELETED, LENGTH_OF_DELETED);
            addColumn(COLUMNNAME_OF_CREATION_DATE, TYPE_OF_CREATION_DATE, LENGTH_OF_CREATION_DATE);
            addColumn(COLUMNNAME_OF_MODIFICATION_DATE, TYPE_OF_MODIFICATION_DATE, LENGTH_OF_MODIFICATION_DATE);
            addColumn(COLUMNNAME_OF_COMMENT, TYPE_OF_COMMENT, LENGTH_OF_COMMENT);

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
            addColumn(COLUMNNAME_OF_PROJECT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_REFERENCE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_UPLOAD_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_FILE_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_VARIANT_ID, ColumnType.INTEGER, 11);
            addColumn(COLUMNNAME_OF_USER, ColumnType.VARCHAR, 200);
            addColumn(COLUMNNAME_OF_DESCRIPTION, ColumnType.VARCHAR, 500);
            addColumn(COLUMNNAME_OF_TIMESTAMP, ColumnType.DATE, -1);
        }
    }

   
    public static final DbSchema schema = (new DbSpec()).addDefaultSchema();
    public static final TableSchema AnnotationTableSchema = new TableSchema(schema, "annotation", AnnotationColumns.class);
    //public static final TableSchema AnnotationFormatTableSchema = new TableSchema(schema, "annotation_format", AnnotationFormatColumns.class);
    public static final ChromosomeTableSchema ChromosomeTableSchema = new ChromosomeTableSchema(schema);
    public static final CohortTableSchema CohortTableSchema = new CohortTableSchema(schema);
    public static final CohortMembershipTableSchema CohortmembershipTableSchema = new CohortMembershipTableSchema(schema);
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
    //public static final VariantPendingUpdateTableSchema VariantpendingupdateTableSchema = new VariantPendingUpdateTableSchema(schema);
    //public static final VariantTablemapTableSchema VarianttablemapTableSchema = new VariantTablemapTableSchema(schema);

    public static final TableSchema VariantTagTableSchema = new TableSchema(schema, "variant_tag", VariantTagColumns.class);
    public static final VariantStarredTableSchema VariantStarredTableSchema = new VariantStarredTableSchema(schema);    
    public static final UserCommentGroupTableSchema UserCommentGroupTableSchema = new UserCommentGroupTableSchema(schema);
    public static final UserCommentTableSchema UserCommentTableSchema = new UserCommentTableSchema(schema);
    public static final UserRoleTableSchema UserRoleTableSchema = new UserRoleTableSchema(schema);
    public static final UserRoleAssignmentTableSchema UserRoleAssignmentTableSchema = new UserRoleAssignmentTableSchema(schema);
}
