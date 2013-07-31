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
package org.ut.biolab.medsavant.shared.solr.mapper;

import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Mapper registry. Maintains references to all types of mappers.
 */
public class MapperRegistry {

    private static VariantMapper variantMapper;

    private static VariantCommentMapper variantCommentMapper;

    private static ResultRowMapper resultRowMapper;

    private static PatientMapper patientMapper;

    private static CohortMapper cohortMapper;

    private static ProjectMapper projectMapper;

    private static AnnotationLogMapper annotationLogMapper;

    private static GeneralLogMapper generalLogMapper;

    private static VariantFileMapper variantFileMapper;

    /**
     * Get the appropriate mapper based on the entity name.
     * @param entity    The name of the entity
     * @return          The appropriate mapper.
     */
    public static ResultMapper getMapper(String entity) {

        if (Entity.VARIANT.equals(entity)) {
            return getVariantMapper();
        } else if (Entity.COMMENT.equals(entity)) {
            return getVariantCommentMapper();
        } else if (Entity.ANNOTATION_LOG.equals(entity)) {
            return getAnnotationLogMapper();
        } else if (Entity.GENERAL_LOG.equals(entity)) {
            return getGeneralLogMapper();
        } else if (Entity.PATIENT.equals(entity)) {
            return getPatientMapper();
        } else if (Entity.COHORT.equals(entity)) {
            return getCohortMapper();
        }  else if (Entity.PROJECT.equals(entity)) {
            return getProjectMapper();
        }  else if (Entity.VARIANT_FILE.equals(entity)) {
            return getVariantFileMapper();
        }

        return null;
    }

    /**
     * Return the variant mapper instance. Creates one if it doesn't yet exist.
     * @return          The variant mapper instance.
     */
    private static VariantMapper getVariantMapper() {

        if (variantMapper == null) {
            variantMapper = new VariantMapper();
        }
        return variantMapper;
    }

    /**
     * Return the variant comment mapper instance. Creates one if it doesn't yet exist.
     * @return          The variant comment mapper instance.
     */
    private static VariantCommentMapper getVariantCommentMapper() {

        if (variantCommentMapper == null) {
            variantCommentMapper = new VariantCommentMapper();
        }
        return variantCommentMapper;
    }

    /**
     * Return the Patient mapper instance. Creates one if it doesn't yet exist.
     * @return          The ResultRow mapper instance.
     */
    public static PatientMapper getPatientMapper() {
        if (patientMapper == null) {
            patientMapper = new PatientMapper();
        }
        return patientMapper;
    }

    /**
     * Return the Cohort mapper instance. Creates one if it doesn't yet exist.
     * @return          The Cohort mapper instance.
     */
    public static CohortMapper getCohortMapper() {
        if (cohortMapper == null) {
            cohortMapper = new CohortMapper();
        }
        return cohortMapper;
    }

    /**
     * Return the ProjectDetails mapper instance. Creates one if it doesn't yet exist.
     * @return          The ProjectDetails mapper instance.
     */
    public static ProjectMapper getProjectMapper() {
        if (projectMapper == null) {
            projectMapper = new ProjectMapper();
        }
        return projectMapper;
    }

    /**
     * Return the AnnotationLog mapper instance. Creates one if it doesn't yet exist.
     * @return          The AnnotationLog mapper instance.
     */
    public static AnnotationLogMapper getAnnotationLogMapper() {
        if (annotationLogMapper == null) {
            annotationLogMapper = new AnnotationLogMapper();
        }
        return annotationLogMapper;
    }

    /**
     * Return the GeneralLog mapper instance. Creates one if it doesn't yet exist.
     * @return          The GeneralLog mapper instance.
     */
    public static GeneralLogMapper getGeneralLogMapper() {
        if (generalLogMapper == null) {
            generalLogMapper = new GeneralLogMapper();
        }
        return generalLogMapper;
    }

    /**
     * Return the ResultRow mapper instances. Creates one if it doesn't yet exist.
     * @return          The ResultRow mapper instance.
     */
    public static ResultRowMapper getResultRowMapper() {
        if (resultRowMapper == null) {
            resultRowMapper = new ResultRowMapper();
        }
        return resultRowMapper;
    }


    /**
     * Return the variant file mapper instance. Creates one if it doesn't yet exist.
     * @return          The variant file mapper instance.
     */
    private static ResultMapper getVariantFileMapper() {
        if (variantFileMapper == null) {
            variantFileMapper = new VariantFileMapper();
        }
        return variantFileMapper;
    }

}
