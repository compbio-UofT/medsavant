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

package org.ut.biolab.medsavant.shared.format;

import org.ut.biolab.medsavant.shared.db.ColumnType;


/**
 * Class which defines the basic columns which are always required for patient tables.
 * Used by both client and server.
 */
public interface BasicPatientColumns {

    public static final CustomField PATIENT_ID = new CustomField("patient_id", ColumnType.INTEGER, 11, true, true, true, null, true, "Individual ID", "");
    public static final CustomField FAMILY_ID = new CustomField("family_id", ColumnType.VARCHAR, 100, false, false, false, null, true, "Family ID", "");
    public static final CustomField HOSPITAL_ID = new CustomField("hospital_id", ColumnType.VARCHAR, 100, false, true, false, null, true, "Hospital ID", "");
    public static final CustomField IDBIOMOM = new CustomField("idbiomom", ColumnType.VARCHAR, 100, false, false, false, null, true, "ID of Mother", "");
    public static final CustomField IDBIODAD = new CustomField("idbiodad", ColumnType.VARCHAR, 100, false, false, false, null, true, "ID of Father", "");
    public static final CustomField GENDER = new CustomField("gender", ColumnType.INTEGER, 1, false, false, false, null, true, "Gender", "");
    public static final CustomField AFFECTED = new CustomField("affected", ColumnType.BOOLEAN, 1, false, false, false, null, true, "Affected", "");
    public static final CustomField DNA_IDS = new CustomField("dna_ids", ColumnType.VARCHAR, 1000, false, false, false, null, true, "DNA ID(s)", "");
    public static final CustomField BAM_URL = new CustomField("bam_url", ColumnType.VARCHAR, 5000, false, false, false, null, true, "BAM URL(s)", "");
    public static final CustomField PHENOTYPES = new CustomField("phenotypes", ColumnType.VARCHAR, 10000, false, false, false, null, true, "Phenotypes", "");

    public static CustomField[] REQUIRED_PATIENT_FIELDS = new CustomField[] {
        PATIENT_ID, FAMILY_ID, HOSPITAL_ID, IDBIOMOM, IDBIODAD, GENDER, AFFECTED, DNA_IDS, BAM_URL, PHENOTYPES
    };
}
