/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shared.format;

import org.ut.biolab.medsavant.shared.db.ColumnType;


/**
 * Class which defines the basic columns which are always required for patient tables.
 * Used by both client and server.
 */
public interface BasicPatientColumns {

    public static int INDEX_OF_PATIENT_ID = 0;
    public static int INDEX_OF_FAMILY_ID = 1;
    public static int INDEX_OF_HOSPITAL_ID = 2;
    public static int INDEX_OF_IDBIOMOM = 3;
    public static int INDEX_OF_IDBIODAD = 4;
    public static int INDEX_OF_GENDER = 5;
    public static int INDEX_OF_AFFECTED = 6;
    public static int INDEX_OF_DNA_IDS = 7;
    public static int INDEX_OF_BAM_URL = 8;
    public static int INDEX_OF_PHENOTYPES = 9;

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
