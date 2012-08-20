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

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;


/**
 * Class which defines the default schema for patient tables.
 * Used by both client and server.
 */
public class DefaultPatientTableSchema extends TableSchema {

    public static final String TABLE_NAME = "default_patient";

    public DefaultPatientTableSchema(DbSchema s) {
        super(s.addTable(TABLE_NAME));
        addColumns();
    }

    public DefaultPatientTableSchema(DbSchema s, String tablename) {
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
    // default_patient.phenotype_ids
    public static final int INDEX_OF_PHENOTYPES = 9;
    public static final ColumnType TYPE_OF_PHENOTYPES = ColumnType.VARCHAR;
    public static final int LENGTH_OF_PHENOTYPES = 10000;
    public static final String COLUMNNAME_OF_PHENOTYPES = "phenotypes";

    public static final int NUM_FIELDS = 8;

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
        addColumn(COLUMNNAME_OF_PHENOTYPES, COLUMNNAME_OF_PHENOTYPES, ColumnType.VARCHAR,10000);
    }
}
