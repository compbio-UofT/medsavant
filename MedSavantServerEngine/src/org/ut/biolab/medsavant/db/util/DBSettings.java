package org.ut.biolab.medsavant.db.util;

/**
 *
 * @author mfiume
 */
public class DBSettings {

    public static String getVariantTableName(int projectId, int referenceId, int updateId){
        return "z_variant_proj" + projectId + "_ref" + referenceId + "_update" + updateId;
    }

    public static String getVariantStagingTableName(int projectId, int referenceId, int updateId){
        return "z_variant_staging_proj" + projectId + "_ref" + referenceId + "_update" + updateId;
    }

    public static String createAnnotationFormatTableName(int annotationId){
        return "z_annotation_format" + annotationId;
    }

    public static String createPatientTableName(int projectId){
        return "z_patient_proj" + projectId;
    }

    public static String createPatientFormatTableName(int projectId){
        return "z_patient_format" + projectId;
    }

}
