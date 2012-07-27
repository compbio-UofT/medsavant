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

import org.ut.biolab.medsavant.db.DefaultPatientTableSchema;


/**
 *
 * @author mfiume
 */
public class PatientFormat implements Serializable {
    
    public static String ALIAS_OF_PATIENT_ID = "Patient ID";
    public static String ALIAS_OF_FAMILY_ID = "Family ID";
    public static String ALIAS_OF_HOSPITAL_ID = "Hospital ID";
    public static String ALIAS_OF_IDBIOMOM = "ID of Mother";
    public static String ALIAS_OF_IDBIODAD = "ID of Father";
    public static String ALIAS_OF_GENDER = "Gender";
    public static String ALIAS_OF_AFFECTED = "Affected";
    public static String ALIAS_OF_DNA_IDS = "DNA ID(s)";
    public static String ALIAS_OF_BAM_URL = "BAM URL(s)"; 
    
    public static CustomField[] getDefaultAnnotationFormat() {
        return new CustomField[] {
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "int(11)", true, ALIAS_OF_PATIENT_ID, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "varchar(100)", true, ALIAS_OF_FAMILY_ID, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "varchar(100)", true, ALIAS_OF_HOSPITAL_ID, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIOMOM, "varchar(100)", true, ALIAS_OF_IDBIOMOM, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIODAD, "varchar(100)", true, ALIAS_OF_IDBIODAD, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER, "int(11)", true, ALIAS_OF_GENDER, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_AFFECTED, "int(1)", true, ALIAS_OF_AFFECTED, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, "varchar(1000)", false, ALIAS_OF_DNA_IDS, ""),
            new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, "varchar(5000)", false, ALIAS_OF_BAM_URL, "")
        };
    }
}
