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
     public static List<CustomField> getDefaultAnnotationFormat(){

        List<CustomField> result = new ArrayList<CustomField>();
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "int(11)", false, "Patient ID", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "varchar(100)", false, "Family ID", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "varchar(100)", false, "Hospital ID", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM, "varchar(100)", false, "ID of Mom", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD, "varchar(100)", false, "ID of Dad", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER, "int(11)", false, "Gender", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_AFFECTED, "int(11)", false, "Affected", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS, "varchar(1000)", false, "DNA ID(s)", "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_BAM_URL, "varchar(5000)", false, "BAM URL(s)", "", Category.PATIENT));

        return result;
    }

}
