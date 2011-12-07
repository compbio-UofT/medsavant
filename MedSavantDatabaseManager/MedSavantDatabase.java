package org.ut.biolab.medsavant.db.model.structure;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;

public class MedSavantDatabase {

	public static class AnnotationTableSchema extends TableSchema {
		public static final String TABLE_NAME = "annotation";
		public AnnotationTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// annotation.annotation_id
		public static final int INDEX_OF_ANNOTATION_ID = 0;
		public static final ColumnType TYPE_OF_ANNOTATION_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_ANNOTATION_ID = 11;
		public static final String COLUMNNAME_OF_ANNOTATION_ID = "annotation_id";
		// annotation.program
		public static final int INDEX_OF_PROGRAM = 1;
		public static final ColumnType TYPE_OF_PROGRAM = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_PROGRAM = 100;
		public static final String COLUMNNAME_OF_PROGRAM = "program";
		// annotation.version
		public static final int INDEX_OF_VERSION = 2;
		public static final ColumnType TYPE_OF_VERSION = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_VERSION = 100;
		public static final String COLUMNNAME_OF_VERSION = "version";
		// annotation.reference_id
		public static final int INDEX_OF_REFERENCE_ID = 3;
		public static final ColumnType TYPE_OF_REFERENCE_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_REFERENCE_ID = 11;
		public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
		// annotation.path
		public static final int INDEX_OF_PATH = 4;
		public static final ColumnType TYPE_OF_PATH = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_PATH = 500;
		public static final String COLUMNNAME_OF_PATH = "path";
		// annotation.has_ref
		public static final int INDEX_OF_HAS_REF = 5;
		public static final ColumnType TYPE_OF_HAS_REF = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_HAS_REF = 1;
		public static final String COLUMNNAME_OF_HAS_REF = "has_ref";
		// annotation.has_alt
		public static final int INDEX_OF_HAS_ALT = 6;
		public static final ColumnType TYPE_OF_HAS_ALT = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_HAS_ALT = 1;
		public static final String COLUMNNAME_OF_HAS_ALT = "has_alt";
		// annotation.type
		public static final int INDEX_OF_TYPE = 7;
		public static final ColumnType TYPE_OF_TYPE = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_TYPE = 11;
		public static final String COLUMNNAME_OF_TYPE = "type";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_ANNOTATION_ID,COLUMNNAME_OF_ANNOTATION_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_PROGRAM,COLUMNNAME_OF_PROGRAM,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_VERSION,COLUMNNAME_OF_VERSION,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_REFERENCE_ID,COLUMNNAME_OF_REFERENCE_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_PATH,COLUMNNAME_OF_PATH,TableSchema.ColumnType.VARCHAR,500);
			addColumn(COLUMNNAME_OF_HAS_REF,COLUMNNAME_OF_HAS_REF,TableSchema.ColumnType.INTEGER,1);
			addColumn(COLUMNNAME_OF_HAS_ALT,COLUMNNAME_OF_HAS_ALT,TableSchema.ColumnType.INTEGER,1);
			addColumn(COLUMNNAME_OF_TYPE,COLUMNNAME_OF_TYPE,TableSchema.ColumnType.INTEGER,11);
		}

	}

	public static class AnnotationformatTableSchema extends TableSchema {
		public static final String TABLE_NAME = "annotation_format";
		public AnnotationformatTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// annotation_format.annotation_id
		public static final int INDEX_OF_ANNOTATION_ID = 0;
		public static final ColumnType TYPE_OF_ANNOTATION_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_ANNOTATION_ID = 11;
		public static final String COLUMNNAME_OF_ANNOTATION_ID = "annotation_id";
		// annotation_format.position
		public static final int INDEX_OF_POSITION = 1;
		public static final ColumnType TYPE_OF_POSITION = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_POSITION = 11;
		public static final String COLUMNNAME_OF_POSITION = "position";
		// annotation_format.column_name
		public static final int INDEX_OF_COLUMN_NAME = 2;
		public static final ColumnType TYPE_OF_COLUMN_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_COLUMN_NAME = 200;
		public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
		// annotation_format.column_type
		public static final int INDEX_OF_COLUMN_TYPE = 3;
		public static final ColumnType TYPE_OF_COLUMN_TYPE = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_COLUMN_TYPE = 45;
		public static final String COLUMNNAME_OF_COLUMN_TYPE = "column_type";
		// annotation_format.filterable
		public static final int INDEX_OF_FILTERABLE = 4;
		public static final ColumnType TYPE_OF_FILTERABLE = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_FILTERABLE = 1;
		public static final String COLUMNNAME_OF_FILTERABLE = "filterable";
		// annotation_format.alias
		public static final int INDEX_OF_ALIAS = 5;
		public static final ColumnType TYPE_OF_ALIAS = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_ALIAS = 200;
		public static final String COLUMNNAME_OF_ALIAS = "alias";
		// annotation_format.description
		public static final int INDEX_OF_DESCRIPTION = 6;
		public static final ColumnType TYPE_OF_DESCRIPTION = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DESCRIPTION = 500;
		public static final String COLUMNNAME_OF_DESCRIPTION = "description";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_ANNOTATION_ID,COLUMNNAME_OF_ANNOTATION_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_POSITION,COLUMNNAME_OF_POSITION,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_COLUMN_NAME,COLUMNNAME_OF_COLUMN_NAME,TableSchema.ColumnType.VARCHAR,200);
			addColumn(COLUMNNAME_OF_COLUMN_TYPE,COLUMNNAME_OF_COLUMN_TYPE,TableSchema.ColumnType.VARCHAR,45);
			addColumn(COLUMNNAME_OF_FILTERABLE,COLUMNNAME_OF_FILTERABLE,TableSchema.ColumnType.INTEGER,1);
			addColumn(COLUMNNAME_OF_ALIAS,COLUMNNAME_OF_ALIAS,TableSchema.ColumnType.VARCHAR,200);
			addColumn(COLUMNNAME_OF_DESCRIPTION,COLUMNNAME_OF_DESCRIPTION,TableSchema.ColumnType.VARCHAR,500);
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
		public static final ColumnType TYPE_OF_REFERENCE_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_REFERENCE_ID = 11;
		public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
		// chromosome.contig_id
		public static final int INDEX_OF_CONTIG_ID = 1;
		public static final ColumnType TYPE_OF_CONTIG_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_CONTIG_ID = 11;
		public static final String COLUMNNAME_OF_CONTIG_ID = "contig_id";
		// chromosome.contig_name
		public static final int INDEX_OF_CONTIG_NAME = 2;
		public static final ColumnType TYPE_OF_CONTIG_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_CONTIG_NAME = 100;
		public static final String COLUMNNAME_OF_CONTIG_NAME = "contig_name";
		// chromosome.contig_length
		public static final int INDEX_OF_CONTIG_LENGTH = 3;
		public static final ColumnType TYPE_OF_CONTIG_LENGTH = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_CONTIG_LENGTH = 11;
		public static final String COLUMNNAME_OF_CONTIG_LENGTH = "contig_length";
		// chromosome.centromere_pos
		public static final int INDEX_OF_CENTROMERE_POS = 4;
		public static final ColumnType TYPE_OF_CENTROMERE_POS = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_CENTROMERE_POS = 11;
		public static final String COLUMNNAME_OF_CENTROMERE_POS = "centromere_pos";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_REFERENCE_ID,COLUMNNAME_OF_REFERENCE_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_CONTIG_ID,COLUMNNAME_OF_CONTIG_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_CONTIG_NAME,COLUMNNAME_OF_CONTIG_NAME,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_CONTIG_LENGTH,COLUMNNAME_OF_CONTIG_LENGTH,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_CENTROMERE_POS,COLUMNNAME_OF_CENTROMERE_POS,TableSchema.ColumnType.INTEGER,11);
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
		public static final ColumnType TYPE_OF_COHORT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_COHORT_ID = 11;
		public static final String COLUMNNAME_OF_COHORT_ID = "cohort_id";
		// cohort.project_id
		public static final int INDEX_OF_PROJECT_ID = 1;
		public static final ColumnType TYPE_OF_PROJECT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PROJECT_ID = 11;
		public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
		// cohort.name
		public static final int INDEX_OF_NAME = 2;
		public static final ColumnType TYPE_OF_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_NAME = 255;
		public static final String COLUMNNAME_OF_NAME = "name";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_COHORT_ID,COLUMNNAME_OF_COHORT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_PROJECT_ID,COLUMNNAME_OF_PROJECT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_NAME,COLUMNNAME_OF_NAME,TableSchema.ColumnType.VARCHAR,255);
		}

	}

	public static class CohortmembershipTableSchema extends TableSchema {
		public static final String TABLE_NAME = "cohort_membership";
		public CohortmembershipTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// cohort_membership.cohort_id
		public static final int INDEX_OF_COHORT_ID = 0;
		public static final ColumnType TYPE_OF_COHORT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_COHORT_ID = 11;
		public static final String COLUMNNAME_OF_COHORT_ID = "cohort_id";
		// cohort_membership.patient_id
		public static final int INDEX_OF_PATIENT_ID = 1;
		public static final ColumnType TYPE_OF_PATIENT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PATIENT_ID = 11;
		public static final String COLUMNNAME_OF_PATIENT_ID = "patient_id";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_COHORT_ID,COLUMNNAME_OF_COHORT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_PATIENT_ID,COLUMNNAME_OF_PATIENT_ID,TableSchema.ColumnType.INTEGER,11);
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
		public static final ColumnType TYPE_OF_PATIENT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PATIENT_ID = 11;
		public static final String COLUMNNAME_OF_PATIENT_ID = "patient_id";
		// default_patient.family_id
		public static final int INDEX_OF_FAMILY_ID = 1;
		public static final ColumnType TYPE_OF_FAMILY_ID = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_FAMILY_ID = 100;
		public static final String COLUMNNAME_OF_FAMILY_ID = "family_id";
		// default_patient.hospital_id
		public static final int INDEX_OF_HOSPITAL_ID = 2;
		public static final ColumnType TYPE_OF_HOSPITAL_ID = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_HOSPITAL_ID = 100;
		public static final String COLUMNNAME_OF_HOSPITAL_ID = "hospital_id";
		// default_patient.idbiomom
		public static final int INDEX_OF_IDBIOMOM = 3;
		public static final ColumnType TYPE_OF_IDBIOMOM = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_IDBIOMOM = 100;
		public static final String COLUMNNAME_OF_IDBIOMOM = "idbiomom";
		// default_patient.idbiodad
		public static final int INDEX_OF_IDBIODAD = 4;
		public static final ColumnType TYPE_OF_IDBIODAD = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_IDBIODAD = 100;
		public static final String COLUMNNAME_OF_IDBIODAD = "idbiodad";
		// default_patient.gender
		public static final int INDEX_OF_GENDER = 5;
		public static final ColumnType TYPE_OF_GENDER = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_GENDER = 11;
		public static final String COLUMNNAME_OF_GENDER = "gender";
		// default_patient.affected
		public static final int INDEX_OF_AFFECTED = 6;
		public static final ColumnType TYPE_OF_AFFECTED = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_AFFECTED = 11;
		public static final String COLUMNNAME_OF_AFFECTED = "affected";
		// default_patient.dna_ids
		public static final int INDEX_OF_DNA_IDS = 7;
		public static final ColumnType TYPE_OF_DNA_IDS = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DNA_IDS = 1000;
		public static final String COLUMNNAME_OF_DNA_IDS = "dna_ids";
		// default_patient.bam_url
		public static final int INDEX_OF_BAM_URL = 8;
		public static final ColumnType TYPE_OF_BAM_URL = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_BAM_URL = 5000;
		public static final String COLUMNNAME_OF_BAM_URL = "bam_url";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_PATIENT_ID,COLUMNNAME_OF_PATIENT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_FAMILY_ID,COLUMNNAME_OF_FAMILY_ID,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_HOSPITAL_ID,COLUMNNAME_OF_HOSPITAL_ID,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_IDBIOMOM,COLUMNNAME_OF_IDBIOMOM,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_IDBIODAD,COLUMNNAME_OF_IDBIODAD,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_GENDER,COLUMNNAME_OF_GENDER,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_AFFECTED,COLUMNNAME_OF_AFFECTED,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_DNA_IDS,COLUMNNAME_OF_DNA_IDS,TableSchema.ColumnType.VARCHAR,1000);
			addColumn(COLUMNNAME_OF_BAM_URL,COLUMNNAME_OF_BAM_URL,TableSchema.ColumnType.VARCHAR,5000);
		}

	}

	public static class DefaultvariantTableSchema extends TableSchema {
		public static final String TABLE_NAME = "default_variant";
		public DefaultvariantTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		public DefaultvariantTableSchema(DbSchema s, String tablename) {
			super(s.addTable(tablename));
			addColumns();
		}

		// default_variant.upload_id
		public static final int INDEX_OF_UPLOAD_ID = 0;
		public static final ColumnType TYPE_OF_UPLOAD_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_UPLOAD_ID = 11;
		public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
		// default_variant.file_id
		public static final int INDEX_OF_FILE_ID = 1;
		public static final ColumnType TYPE_OF_FILE_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_FILE_ID = 11;
		public static final String COLUMNNAME_OF_FILE_ID = "file_id";
		// default_variant.variant_id
		public static final int INDEX_OF_VARIANT_ID = 2;
		public static final ColumnType TYPE_OF_VARIANT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_VARIANT_ID = 11;
		public static final String COLUMNNAME_OF_VARIANT_ID = "variant_id";
		// default_variant.dna_id
		public static final int INDEX_OF_DNA_ID = 3;
		public static final ColumnType TYPE_OF_DNA_ID = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DNA_ID = 100;
		public static final String COLUMNNAME_OF_DNA_ID = "dna_id";
		// default_variant.chrom
		public static final int INDEX_OF_CHROM = 4;
		public static final ColumnType TYPE_OF_CHROM = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_CHROM = 5;
		public static final String COLUMNNAME_OF_CHROM = "chrom";
		// default_variant.position
		public static final int INDEX_OF_POSITION = 5;
		public static final ColumnType TYPE_OF_POSITION = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_POSITION = 11;
		public static final String COLUMNNAME_OF_POSITION = "position";
		// default_variant.dbsnp_id
		public static final int INDEX_OF_DBSNP_ID = 6;
		public static final ColumnType TYPE_OF_DBSNP_ID = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DBSNP_ID = 45;
		public static final String COLUMNNAME_OF_DBSNP_ID = "dbsnp_id";
		// default_variant.ref
		public static final int INDEX_OF_REF = 7;
		public static final ColumnType TYPE_OF_REF = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_REF = 30;
		public static final String COLUMNNAME_OF_REF = "ref";
		// default_variant.alt
		public static final int INDEX_OF_ALT = 8;
		public static final ColumnType TYPE_OF_ALT = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_ALT = 30;
		public static final String COLUMNNAME_OF_ALT = "alt";
		// default_variant.qual
		public static final int INDEX_OF_QUAL = 9;
		public static final ColumnType TYPE_OF_QUAL = TableSchema.ColumnType.FLOAT;
		public static final int LENGTH_OF_QUAL = 10;
		public static final String COLUMNNAME_OF_QUAL = "qual";
		// default_variant.filter
		public static final int INDEX_OF_FILTER = 10;
		public static final ColumnType TYPE_OF_FILTER = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_FILTER = 500;
		public static final String COLUMNNAME_OF_FILTER = "filter";
		// default_variant.custom_info
		public static final int INDEX_OF_CUSTOM_INFO = 11;
		public static final ColumnType TYPE_OF_CUSTOM_INFO = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_CUSTOM_INFO = 500;
		public static final String COLUMNNAME_OF_CUSTOM_INFO = "custom_info";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_UPLOAD_ID,COLUMNNAME_OF_UPLOAD_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_FILE_ID,COLUMNNAME_OF_FILE_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_VARIANT_ID,COLUMNNAME_OF_VARIANT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_DNA_ID,COLUMNNAME_OF_DNA_ID,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_CHROM,COLUMNNAME_OF_CHROM,TableSchema.ColumnType.VARCHAR,5);
			addColumn(COLUMNNAME_OF_POSITION,COLUMNNAME_OF_POSITION,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_DBSNP_ID,COLUMNNAME_OF_DBSNP_ID,TableSchema.ColumnType.VARCHAR,45);
			addColumn(COLUMNNAME_OF_REF,COLUMNNAME_OF_REF,TableSchema.ColumnType.VARCHAR,30);
			addColumn(COLUMNNAME_OF_ALT,COLUMNNAME_OF_ALT,TableSchema.ColumnType.VARCHAR,30);
			addColumn(COLUMNNAME_OF_QUAL,COLUMNNAME_OF_QUAL,TableSchema.ColumnType.FLOAT,10);
			addColumn(COLUMNNAME_OF_FILTER,COLUMNNAME_OF_FILTER,TableSchema.ColumnType.VARCHAR,500);
			addColumn(COLUMNNAME_OF_CUSTOM_INFO,COLUMNNAME_OF_CUSTOM_INFO,TableSchema.ColumnType.VARCHAR,500);
		}

	}

	public static class PatientformatTableSchema extends TableSchema {
		public static final String TABLE_NAME = "patient_format";
		public PatientformatTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// patient_format.project_id
		public static final int INDEX_OF_PROJECT_ID = 0;
		public static final ColumnType TYPE_OF_PROJECT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PROJECT_ID = 11;
		public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
		// patient_format.position
		public static final int INDEX_OF_POSITION = 1;
		public static final ColumnType TYPE_OF_POSITION = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_POSITION = 11;
		public static final String COLUMNNAME_OF_POSITION = "position";
		// patient_format.column_name
		public static final int INDEX_OF_COLUMN_NAME = 2;
		public static final ColumnType TYPE_OF_COLUMN_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_COLUMN_NAME = 200;
		public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
		// patient_format.column_type
		public static final int INDEX_OF_COLUMN_TYPE = 3;
		public static final ColumnType TYPE_OF_COLUMN_TYPE = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_COLUMN_TYPE = 45;
		public static final String COLUMNNAME_OF_COLUMN_TYPE = "column_type";
		// patient_format.filterable
		public static final int INDEX_OF_FILTERABLE = 4;
		public static final ColumnType TYPE_OF_FILTERABLE = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_FILTERABLE = 1;
		public static final String COLUMNNAME_OF_FILTERABLE = "filterable";
		// patient_format.alias
		public static final int INDEX_OF_ALIAS = 5;
		public static final ColumnType TYPE_OF_ALIAS = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_ALIAS = 200;
		public static final String COLUMNNAME_OF_ALIAS = "alias";
		// patient_format.description
		public static final int INDEX_OF_DESCRIPTION = 6;
		public static final ColumnType TYPE_OF_DESCRIPTION = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DESCRIPTION = 500;
		public static final String COLUMNNAME_OF_DESCRIPTION = "description";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_PROJECT_ID,COLUMNNAME_OF_PROJECT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_POSITION,COLUMNNAME_OF_POSITION,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_COLUMN_NAME,COLUMNNAME_OF_COLUMN_NAME,TableSchema.ColumnType.VARCHAR,200);
			addColumn(COLUMNNAME_OF_COLUMN_TYPE,COLUMNNAME_OF_COLUMN_TYPE,TableSchema.ColumnType.VARCHAR,45);
			addColumn(COLUMNNAME_OF_FILTERABLE,COLUMNNAME_OF_FILTERABLE,TableSchema.ColumnType.INTEGER,1);
			addColumn(COLUMNNAME_OF_ALIAS,COLUMNNAME_OF_ALIAS,TableSchema.ColumnType.VARCHAR,200);
			addColumn(COLUMNNAME_OF_DESCRIPTION,COLUMNNAME_OF_DESCRIPTION,TableSchema.ColumnType.VARCHAR,500);
		}

	}

	public static class PatienttablemapTableSchema extends TableSchema {
		public static final String TABLE_NAME = "patient_tablemap";
		public PatienttablemapTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// patient_tablemap.project_id
		public static final int INDEX_OF_PROJECT_ID = 0;
		public static final ColumnType TYPE_OF_PROJECT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PROJECT_ID = 11;
		public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
		// patient_tablemap.patient_tablename
		public static final int INDEX_OF_PATIENT_TABLENAME = 1;
		public static final ColumnType TYPE_OF_PATIENT_TABLENAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_PATIENT_TABLENAME = 100;
		public static final String COLUMNNAME_OF_PATIENT_TABLENAME = "patient_tablename";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_PROJECT_ID,COLUMNNAME_OF_PROJECT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_PATIENT_TABLENAME,COLUMNNAME_OF_PATIENT_TABLENAME,TableSchema.ColumnType.VARCHAR,100);
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
		public static final ColumnType TYPE_OF_PROJECT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PROJECT_ID = 11;
		public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
		// project.name
		public static final int INDEX_OF_NAME = 1;
		public static final ColumnType TYPE_OF_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_NAME = 50;
		public static final String COLUMNNAME_OF_NAME = "name";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_PROJECT_ID,COLUMNNAME_OF_PROJECT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_NAME,COLUMNNAME_OF_NAME,TableSchema.ColumnType.VARCHAR,50);
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
		public static final ColumnType TYPE_OF_REFERENCE_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_REFERENCE_ID = 11;
		public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
		// reference.name
		public static final int INDEX_OF_NAME = 1;
		public static final ColumnType TYPE_OF_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_NAME = 50;
		public static final String COLUMNNAME_OF_NAME = "name";
		// reference.url
		public static final int INDEX_OF_URL = 2;
		public static final ColumnType TYPE_OF_URL = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_URL = 200;
		public static final String COLUMNNAME_OF_URL = "url";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_REFERENCE_ID,COLUMNNAME_OF_REFERENCE_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_NAME,COLUMNNAME_OF_NAME,TableSchema.ColumnType.VARCHAR,50);
			addColumn(COLUMNNAME_OF_URL,COLUMNNAME_OF_URL,TableSchema.ColumnType.VARCHAR,200);
		}

	}

	public static class RegionsetTableSchema extends TableSchema {
		public static final String TABLE_NAME = "region_set";
		public RegionsetTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// region_set.region_set_id
		public static final int INDEX_OF_REGION_SET_ID = 0;
		public static final ColumnType TYPE_OF_REGION_SET_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_REGION_SET_ID = 11;
		public static final String COLUMNNAME_OF_REGION_SET_ID = "region_set_id";
		// region_set.name
		public static final int INDEX_OF_NAME = 1;
		public static final ColumnType TYPE_OF_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_NAME = 255;
		public static final String COLUMNNAME_OF_NAME = "name";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_REGION_SET_ID,COLUMNNAME_OF_REGION_SET_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_NAME,COLUMNNAME_OF_NAME,TableSchema.ColumnType.VARCHAR,255);
		}

	}

	public static class RegionsetmembershipTableSchema extends TableSchema {
		public static final String TABLE_NAME = "region_set_membership";
		public RegionsetmembershipTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// region_set_membership.region_set_id
		public static final int INDEX_OF_REGION_SET_ID = 0;
		public static final ColumnType TYPE_OF_REGION_SET_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_REGION_SET_ID = 11;
		public static final String COLUMNNAME_OF_REGION_SET_ID = "region_set_id";
		// region_set_membership.genome_id
		public static final int INDEX_OF_GENOME_ID = 1;
		public static final ColumnType TYPE_OF_GENOME_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_GENOME_ID = 11;
		public static final String COLUMNNAME_OF_GENOME_ID = "genome_id";
		// region_set_membership.chrom
		public static final int INDEX_OF_CHROM = 2;
		public static final ColumnType TYPE_OF_CHROM = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_CHROM = 255;
		public static final String COLUMNNAME_OF_CHROM = "chrom";
		// region_set_membership.start
		public static final int INDEX_OF_START = 3;
		public static final ColumnType TYPE_OF_START = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_START = 11;
		public static final String COLUMNNAME_OF_START = "start";
		// region_set_membership.end
		public static final int INDEX_OF_END = 4;
		public static final ColumnType TYPE_OF_END = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_END = 11;
		public static final String COLUMNNAME_OF_END = "end";
		// region_set_membership.description
		public static final int INDEX_OF_DESCRIPTION = 5;
		public static final ColumnType TYPE_OF_DESCRIPTION = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DESCRIPTION = 255;
		public static final String COLUMNNAME_OF_DESCRIPTION = "description";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_REGION_SET_ID,COLUMNNAME_OF_REGION_SET_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_GENOME_ID,COLUMNNAME_OF_GENOME_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_CHROM,COLUMNNAME_OF_CHROM,TableSchema.ColumnType.VARCHAR,255);
			addColumn(COLUMNNAME_OF_START,COLUMNNAME_OF_START,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_END,COLUMNNAME_OF_END,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_DESCRIPTION,COLUMNNAME_OF_DESCRIPTION,TableSchema.ColumnType.VARCHAR,255);
		}

	}

	public static class ServerlogTableSchema extends TableSchema {
		public static final String TABLE_NAME = "server_log";
		public ServerlogTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// server_log.id
		public static final int INDEX_OF_ID = 0;
		public static final ColumnType TYPE_OF_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_ID = 11;
		public static final String COLUMNNAME_OF_ID = "id";
		// server_log.user
		public static final int INDEX_OF_USER = 1;
		public static final ColumnType TYPE_OF_USER = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_USER = 50;
		public static final String COLUMNNAME_OF_USER = "user";
		// server_log.event
		public static final int INDEX_OF_EVENT = 2;
		public static final ColumnType TYPE_OF_EVENT = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_EVENT = 50;
		public static final String COLUMNNAME_OF_EVENT = "event";
		// server_log.description
		public static final int INDEX_OF_DESCRIPTION = 3;
		public static final ColumnType TYPE_OF_DESCRIPTION = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DESCRIPTION = -1;
		public static final String COLUMNNAME_OF_DESCRIPTION = "description";
		// server_log.timestamp
		public static final int INDEX_OF_TIMESTAMP = 4;
		public static final ColumnType TYPE_OF_TIMESTAMP = TableSchema.ColumnType.DATE;
		public static final int LENGTH_OF_TIMESTAMP = -1;
		public static final String COLUMNNAME_OF_TIMESTAMP = "timestamp";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_ID,COLUMNNAME_OF_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_USER,COLUMNNAME_OF_USER,TableSchema.ColumnType.VARCHAR,50);
			addColumn(COLUMNNAME_OF_EVENT,COLUMNNAME_OF_EVENT,TableSchema.ColumnType.VARCHAR,50);
			addColumn(COLUMNNAME_OF_DESCRIPTION,COLUMNNAME_OF_DESCRIPTION,TableSchema.ColumnType.VARCHAR,-1);
			addColumn(COLUMNNAME_OF_TIMESTAMP,COLUMNNAME_OF_TIMESTAMP,TableSchema.ColumnType.DATE,-1);
		}

	}

	public static class SettingsTableSchema extends TableSchema {
		public static final String TABLE_NAME = "settings";
		public SettingsTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// settings.setting_key
		public static final int INDEX_OF_SETTING_KEY = 0;
		public static final ColumnType TYPE_OF_SETTING_KEY = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_SETTING_KEY = 100;
		public static final String COLUMNNAME_OF_SETTING_KEY = "setting_key";
		// settings.setting_value
		public static final int INDEX_OF_SETTING_VALUE = 1;
		public static final ColumnType TYPE_OF_SETTING_VALUE = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_SETTING_VALUE = 300;
		public static final String COLUMNNAME_OF_SETTING_VALUE = "setting_value";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_SETTING_KEY,COLUMNNAME_OF_SETTING_KEY,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_SETTING_VALUE,COLUMNNAME_OF_SETTING_VALUE,TableSchema.ColumnType.VARCHAR,300);
		}

	}

	public static class VariantformatTableSchema extends TableSchema {
		public static final String TABLE_NAME = "variant_format";
		public VariantformatTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// variant_format.project_id
		public static final int INDEX_OF_PROJECT_ID = 0;
		public static final ColumnType TYPE_OF_PROJECT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PROJECT_ID = 11;
		public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
		// variant_format.position
		public static final int INDEX_OF_POSITION = 1;
		public static final ColumnType TYPE_OF_POSITION = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_POSITION = 11;
		public static final String COLUMNNAME_OF_POSITION = "position";
		// variant_format.column_name
		public static final int INDEX_OF_COLUMN_NAME = 2;
		public static final ColumnType TYPE_OF_COLUMN_NAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_COLUMN_NAME = 200;
		public static final String COLUMNNAME_OF_COLUMN_NAME = "column_name";
		// variant_format.column_type
		public static final int INDEX_OF_COLUMN_TYPE = 3;
		public static final ColumnType TYPE_OF_COLUMN_TYPE = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_COLUMN_TYPE = 45;
		public static final String COLUMNNAME_OF_COLUMN_TYPE = "column_type";
		// variant_format.filterable
		public static final int INDEX_OF_FILTERABLE = 4;
		public static final ColumnType TYPE_OF_FILTERABLE = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_FILTERABLE = 1;
		public static final String COLUMNNAME_OF_FILTERABLE = "filterable";
		// variant_format.alias
		public static final int INDEX_OF_ALIAS = 5;
		public static final ColumnType TYPE_OF_ALIAS = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_ALIAS = 200;
		public static final String COLUMNNAME_OF_ALIAS = "alias";
		// variant_format.description
		public static final int INDEX_OF_DESCRIPTION = 6;
		public static final ColumnType TYPE_OF_DESCRIPTION = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_DESCRIPTION = 500;
		public static final String COLUMNNAME_OF_DESCRIPTION = "description";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_PROJECT_ID,COLUMNNAME_OF_PROJECT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_POSITION,COLUMNNAME_OF_POSITION,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_COLUMN_NAME,COLUMNNAME_OF_COLUMN_NAME,TableSchema.ColumnType.VARCHAR,200);
			addColumn(COLUMNNAME_OF_COLUMN_TYPE,COLUMNNAME_OF_COLUMN_TYPE,TableSchema.ColumnType.VARCHAR,45);
			addColumn(COLUMNNAME_OF_FILTERABLE,COLUMNNAME_OF_FILTERABLE,TableSchema.ColumnType.INTEGER,1);
			addColumn(COLUMNNAME_OF_ALIAS,COLUMNNAME_OF_ALIAS,TableSchema.ColumnType.VARCHAR,200);
			addColumn(COLUMNNAME_OF_DESCRIPTION,COLUMNNAME_OF_DESCRIPTION,TableSchema.ColumnType.VARCHAR,500);
		}

	}

	public static class VariantpendingupdateTableSchema extends TableSchema {
		public static final String TABLE_NAME = "variant_pending_update";
		public VariantpendingupdateTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// variant_pending_update.upload_id
		public static final int INDEX_OF_UPLOAD_ID = 0;
		public static final ColumnType TYPE_OF_UPLOAD_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_UPLOAD_ID = 11;
		public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
		// variant_pending_update.project_id
		public static final int INDEX_OF_PROJECT_ID = 1;
		public static final ColumnType TYPE_OF_PROJECT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PROJECT_ID = 11;
		public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
		// variant_pending_update.reference_id
		public static final int INDEX_OF_REFERENCE_ID = 2;
		public static final ColumnType TYPE_OF_REFERENCE_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_REFERENCE_ID = 11;
		public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
		// variant_pending_update.action
		public static final int INDEX_OF_ACTION = 3;
		public static final ColumnType TYPE_OF_ACTION = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_ACTION = 11;
		public static final String COLUMNNAME_OF_ACTION = "action";
		// variant_pending_update.status
		public static final int INDEX_OF_STATUS = 4;
		public static final ColumnType TYPE_OF_STATUS = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_STATUS = 5;
		public static final String COLUMNNAME_OF_STATUS = "status";
		// variant_pending_update.timestamp
		public static final int INDEX_OF_TIMESTAMP = 5;
		public static final ColumnType TYPE_OF_TIMESTAMP = TableSchema.ColumnType.DATE;
		public static final int LENGTH_OF_TIMESTAMP = -1;
		public static final String COLUMNNAME_OF_TIMESTAMP = "timestamp";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_UPLOAD_ID,COLUMNNAME_OF_UPLOAD_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_PROJECT_ID,COLUMNNAME_OF_PROJECT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_REFERENCE_ID,COLUMNNAME_OF_REFERENCE_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_ACTION,COLUMNNAME_OF_ACTION,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_STATUS,COLUMNNAME_OF_STATUS,TableSchema.ColumnType.INTEGER,5);
			addColumn(COLUMNNAME_OF_TIMESTAMP,COLUMNNAME_OF_TIMESTAMP,TableSchema.ColumnType.DATE,-1);
		}

	}

	public static class VarianttablemapTableSchema extends TableSchema {
		public static final String TABLE_NAME = "variant_tablemap";
		public VarianttablemapTableSchema(DbSchema s) {
			super(s.addTable(TABLE_NAME));
			addColumns();
		}

		// variant_tablemap.project_id
		public static final int INDEX_OF_PROJECT_ID = 0;
		public static final ColumnType TYPE_OF_PROJECT_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_PROJECT_ID = 11;
		public static final String COLUMNNAME_OF_PROJECT_ID = "project_id";
		// variant_tablemap.reference_id
		public static final int INDEX_OF_REFERENCE_ID = 1;
		public static final ColumnType TYPE_OF_REFERENCE_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_REFERENCE_ID = 11;
		public static final String COLUMNNAME_OF_REFERENCE_ID = "reference_id";
		// variant_tablemap.variant_tablename
		public static final int INDEX_OF_VARIANT_TABLENAME = 2;
		public static final ColumnType TYPE_OF_VARIANT_TABLENAME = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_VARIANT_TABLENAME = 100;
		public static final String COLUMNNAME_OF_VARIANT_TABLENAME = "variant_tablename";
		// variant_tablemap.annotation_ids
		public static final int INDEX_OF_ANNOTATION_IDS = 3;
		public static final ColumnType TYPE_OF_ANNOTATION_IDS = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_ANNOTATION_IDS = 500;
		public static final String COLUMNNAME_OF_ANNOTATION_IDS = "annotation_ids";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_PROJECT_ID,COLUMNNAME_OF_PROJECT_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_REFERENCE_ID,COLUMNNAME_OF_REFERENCE_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_VARIANT_TABLENAME,COLUMNNAME_OF_VARIANT_TABLENAME,TableSchema.ColumnType.VARCHAR,100);
			addColumn(COLUMNNAME_OF_ANNOTATION_IDS,COLUMNNAME_OF_ANNOTATION_IDS,TableSchema.ColumnType.VARCHAR,500);
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
		public static final ColumnType TYPE_OF_UPLOAD_ID = TableSchema.ColumnType.INTEGER;
		public static final int LENGTH_OF_UPLOAD_ID = 11;
		public static final String COLUMNNAME_OF_UPLOAD_ID = "upload_id";
		// variant_tag.tagkey
		public static final int INDEX_OF_TAGKEY = 1;
		public static final ColumnType TYPE_OF_TAGKEY = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_TAGKEY = 500;
		public static final String COLUMNNAME_OF_TAGKEY = "tagkey";
		// variant_tag.tagvalue
		public static final int INDEX_OF_TAGVALUE = 2;
		public static final ColumnType TYPE_OF_TAGVALUE = TableSchema.ColumnType.VARCHAR;
		public static final int LENGTH_OF_TAGVALUE = 1000;
		public static final String COLUMNNAME_OF_TAGVALUE = "tagvalue";
		private void addColumns() {
			addColumn(COLUMNNAME_OF_UPLOAD_ID,COLUMNNAME_OF_UPLOAD_ID,TableSchema.ColumnType.INTEGER,11);
			addColumn(COLUMNNAME_OF_TAGKEY,COLUMNNAME_OF_TAGKEY,TableSchema.ColumnType.VARCHAR,500);
			addColumn(COLUMNNAME_OF_TAGVALUE,COLUMNNAME_OF_TAGVALUE,TableSchema.ColumnType.VARCHAR,1000);
		}

	}

	public static final DbSchema schema = (new DbSpec()).addDefaultSchema();

	//AnnotationTableSchema
	public static final AnnotationTableSchema AnnotationTableSchema = new AnnotationTableSchema(schema);

	//AnnotationformatTableSchema
	public static final AnnotationformatTableSchema AnnotationformatTableSchema = new AnnotationformatTableSchema(schema);

	//ChromosomeTableSchema
	public static final ChromosomeTableSchema ChromosomeTableSchema = new ChromosomeTableSchema(schema);

	//CohortTableSchema
	public static final CohortTableSchema CohortTableSchema = new CohortTableSchema(schema);

	//CohortmembershipTableSchema
	public static final CohortmembershipTableSchema CohortmembershipTableSchema = new CohortmembershipTableSchema(schema);

	//DefaultpatientTableSchema
	public static final DefaultpatientTableSchema DefaultpatientTableSchema = new DefaultpatientTableSchema(schema);

	//DefaultvariantTableSchema
	public static final DefaultvariantTableSchema DefaultvariantTableSchema = new DefaultvariantTableSchema(schema);

	//PatientformatTableSchema
	public static final PatientformatTableSchema PatientformatTableSchema = new PatientformatTableSchema(schema);

	//PatienttablemapTableSchema
	public static final PatienttablemapTableSchema PatienttablemapTableSchema = new PatienttablemapTableSchema(schema);

	//ProjectTableSchema
	public static final ProjectTableSchema ProjectTableSchema = new ProjectTableSchema(schema);

	//ReferenceTableSchema
	public static final ReferenceTableSchema ReferenceTableSchema = new ReferenceTableSchema(schema);

	//RegionsetTableSchema
	public static final RegionsetTableSchema RegionsetTableSchema = new RegionsetTableSchema(schema);

	//RegionsetmembershipTableSchema
	public static final RegionsetmembershipTableSchema RegionsetmembershipTableSchema = new RegionsetmembershipTableSchema(schema);

	//ServerlogTableSchema
	public static final ServerlogTableSchema ServerlogTableSchema = new ServerlogTableSchema(schema);

	//SettingsTableSchema
	public static final SettingsTableSchema SettingsTableSchema = new SettingsTableSchema(schema);

	//VariantformatTableSchema
	public static final VariantformatTableSchema VariantformatTableSchema = new VariantformatTableSchema(schema);

	//VariantpendingupdateTableSchema
	public static final VariantpendingupdateTableSchema VariantpendingupdateTableSchema = new VariantpendingupdateTableSchema(schema);

	//VarianttablemapTableSchema
	public static final VarianttablemapTableSchema VarianttablemapTableSchema = new VarianttablemapTableSchema(schema);

	//VarianttagTableSchema
	public static final VarianttagTableSchema VarianttagTableSchema = new VarianttagTableSchema(schema);

}
