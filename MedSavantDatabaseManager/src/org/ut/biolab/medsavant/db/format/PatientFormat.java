package org.ut.biolab.medsavant.db.format;

import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.format.AnnotationField.Category;

/**
 *
 * @author mfiume
 */
public class PatientFormat {
     public static List<AnnotationField> getDefaultAnnotationFormat(){
         
        List<AnnotationField> result = new ArrayList<AnnotationField>();
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "int(11)", false, "Patient ID", "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "varchar(100)", false, "Family ID", "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID, "varchar(100)", false, "Pedigree ID", "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "varchar(100)", false, "Hospital ID", "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, "varchar(1000)", false, "DNA ID(s)", "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, "varchar(5000)", false, "BAM URL(s)", "", Category.PATIENT));
        
        return result;
    }
   
}
