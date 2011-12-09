package org.ut.biolab.medsavant.db.format;

import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField.Category;

/**
 *
 * @author mfiume
 */
public class PatientFormat {
    
    public static String ALIAS_PATIENT_ID = "Patient ID";
    public static String ALIAS_FAMILY_ID = "Family ID";
    public static String ALIAS_HOSPITAL_ID = "Hospital ID";
    public static String ALIAS_IDBIOMOM = "ID of Mother";
    public static String ALIAS_IDBIODAD = "ID of Father";
    public static String ALIAS_GENDER = "Gender";
    public static String ALIAS_AFFECTED = "Affected";
    public static String ALIAS_DNA_IDS = "DNA ID(s)";
    public static String ALIAS_BAM_URL = "BAM URL(s)"; 
    
    public static List<CustomField> getDefaultAnnotationFormat(){

        List<CustomField> result = new ArrayList<CustomField>();
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "int(11)", false, ALIAS_PATIENT_ID, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "varchar(100)", false, ALIAS_FAMILY_ID, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "varchar(100)", false, ALIAS_HOSPITAL_ID, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM, "varchar(100)", false, ALIAS_IDBIOMOM, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD, "varchar(100)", false, ALIAS_IDBIODAD, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER, "int(11)", false, ALIAS_GENDER, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_AFFECTED, "int(11)", false, ALIAS_AFFECTED, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS, "varchar(1000)", false, ALIAS_DNA_IDS, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_BAM_URL, "varchar(5000)", false, ALIAS_BAM_URL, "", Category.PATIENT));

        return result;
    }

}
