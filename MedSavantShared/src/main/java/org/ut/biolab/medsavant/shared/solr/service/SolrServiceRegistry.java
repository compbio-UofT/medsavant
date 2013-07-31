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
package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.solr.*;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.Entity;


/**
 * Keep track of references of Solr services.
 */
public class SolrServiceRegistry {


    private static AbstractSolrService variantService;

    private static AbstractSolrService variantCommentService;

    private static AbstractSolrService variantFileService;

    private static AbstractSolrService patientService;

    private static AbstractSolrService cohortService;

    private static AbstractSolrService annotationLogService;

    private static AbstractSolrService generalLogService;

    private static AbstractSolrService projectDetailsService;

    /**
     * Get the appropriate solr service based on the entity string.
     * @param entityName            The entity name.
     * @return                      An instance of a solr service.
     */
    public static AbstractSolrService getService(String entityName) throws InitializationException {

        if (Entity.VARIANT.equals(entityName)) {
            return getVariantService();
        } else if (Entity.COMMENT.equals(entityName)) {
            return getVariantCommentService();
        } else if (Entity.ANNOTATION_LOG.equals(entityName)) {
            return getAnnotationLogService();
        } else if (Entity.GENERAL_LOG.equals(entityName)) {
            return getGeneralLogService();
        } else if (Entity.PATIENT.equals(entityName)) {
            return getPatientService();
        } else if (Entity.COHORT.equals(entityName)) {
            return getCohortService();
        }  else if (Entity.PROJECT.equals(entityName)) {
            return getProjectDetailsService();
        } else if (Entity.VARIANT_FILE.equals(entityName)) {
            return getVariantFileService();
        }

        return null;
    }

    public static AbstractSolrService getService(Class clazz) throws InitializationException {

        if (SearcheableVariantComment.class.getName().equals(clazz.getName())) {
            return getVariantCommentService();
        } else if (SearcheableAnnotationLog.class.getName().equals(clazz.getName())) {
            return getAnnotationLogService();
        } else if (SearcheableGeneralLog.class.getName().equals(clazz.getName())) {
            return getGeneralLogService();
        } else if (SearcheablePatient.class.getName().equals(clazz.getName())) {
            return getPatientService();
        } else if (SearcheableProjectDetails.class.getName().equals(clazz.getName())) {
            return getProjectDetailsService();
        } else if (SearcheableCohort.class.getName().equals(clazz.getName())) {
            return getCohortService();
        } else if (SearcheableVariant.class.getName().equals(clazz.getName())) {
            return getVariantCommentService();
        } else if (SearcheableVariantFile.class.getName().equals(clazz.getName())) {
            return getVariantFileService();
        }

        return null;
    }

    private static AbstractSolrService getVariantService() throws InitializationException {
        if (variantService == null) {
            variantService =  new VariantService();
            variantService.initialize();
        }
        return variantService;
    }

    private static AbstractSolrService getVariantCommentService() throws InitializationException {
        if (variantCommentService == null) {
            variantCommentService =  new VariantCommentService();
            variantCommentService.initialize();
        }
        return variantCommentService;
    }

    public static AbstractSolrService getVariantFileService() throws InitializationException {
        if (variantFileService == null) {
            variantFileService =  new VariantFileService();
            variantFileService.initialize();
        }
        return variantFileService;
    }

    public static AbstractSolrService getPatientService() throws InitializationException {
        if (patientService == null) {
            patientService =  new PatientService();
            patientService.initialize();
        }
        return patientService;
    }

    public static AbstractSolrService getCohortService() {
        if (cohortService == null) {
            cohortService =  new CohortService();
        }
        return cohortService;
    }

    public static AbstractSolrService getAnnotationLogService() throws InitializationException {
        if (annotationLogService == null) {
            annotationLogService =  new AnnotationLogService();
            annotationLogService.initialize();
        }
        return annotationLogService;
    }

    public static AbstractSolrService getGeneralLogService() throws InitializationException {
        if (generalLogService == null) {
            generalLogService =  new GeneralLogService();
            generalLogService.initialize();
        }
        return generalLogService;
    }

    public static AbstractSolrService getProjectDetailsService() throws InitializationException {
        if (projectDetailsService == null) {
            projectDetailsService =  new ProjectDetailsService();
            projectDetailsService.initialize();
        }
        return projectDetailsService;
    }
}
