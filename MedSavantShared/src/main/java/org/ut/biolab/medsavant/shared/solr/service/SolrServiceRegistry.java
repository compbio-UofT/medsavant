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

import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.model.*;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.Entity;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;


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

    private static AbstractSolrService chromosomeService;

    private static AbstractSolrService geneService;

    private static AbstractSolrService genomicRegionService;

    private static AbstractSolrService ontologyService;

    private static AbstractSolrService ontologyTermService;

    private static AbstractSolrService referenceService;

    private static AbstractSolrService regionSetService;

    private static AbstractSolrService annotationService;

    private static AbstractSolrService annotationFormatService;

    private static AbstractSolrService geneSetService;

    private static AbstractSolrService userService;

    private static AbstractSolrService settingService;

    private static AbstractSolrService annotationColumnService;

    private static AbstractSolrService customColumnService;

    private static AbstractSolrService variantTagService;

    /**
     * Get the appropriate org.ut.biolab.medsavant.persistence.query.solr.solr service based on the entity string.
     *
     * @param entityName The entity name.
     * @return An instance of a org.ut.biolab.medsavant.persistence.query.solr.solr service.
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
        } else if (Entity.PROJECT.equals(entityName)) {
            return getProjectDetailsService();
        } else if (Entity.VARIANT_FILE.equals(entityName)) {
            return getVariantFileService();
        } else if (Entity.CHROMOSOME.equals(entityName)) {
            return getChromosomeService();
        } else if (Entity.GENE.equals(entityName)) {
            return getGeneService();
        } else if (Entity.GENOMIC_REGION.equals(entityName)) {
            return getGenomicRegionService();
        } else if (Entity.ONTOLOGY.equals(entityName)) {
            return getOntologyService();
        } else if (Entity.ONTOLOGY_TERM.equals(entityName)) {
            return getOntologyTermService();
        } else if (Entity.REFERENCE.equals(entityName)) {
            return getReferenceService();
        } else if (Entity.REGION_SET.equals(entityName)) {
            return getRegionSetService();
        } else if (Entity.ANNOTATION.equals(entityName)) {
            return getAnnotationService();
        } else if (Entity.USER.equals(entityName)) {
            return getUserService();
        } else if (Entity.SETTING.equals(entityName)) {
            return getSettingService();
        } else if (Entity.GENE_SET.equals(entityName)) {
            return getGeneSetService();
        } else if (Entity.ANNOTATION_FORMAT.equals(entityName)) {
            return getAnnotationFormatService();
        } else if (Entity.ANNOTATION_COLUMN.equals(entityName)) {
            return getAnnotationColumnService();
        } else if (Entity.CUSTOM_COLUMN.equals(entityName)) {
            return getCustomColumnService();
        } else if (Entity.VARIANT_TAG.equals(entityName)) {
            return getVariantTagService();
        }

        return null;
    }

    public static AbstractSolrService getService(Class clazz) throws InitializationException {

        if (VariantComment.class.getName().equals(clazz.getName())) {
            return getVariantCommentService();
        } else if (AnnotationLog.class.getName().equals(clazz.getName())) {
            return getAnnotationLogService();
        } else if (GeneralLog.class.getName().equals(clazz.getName())) {
            return getGeneralLogService();
        } else if (Patient.class.getName().equals(clazz.getName())) {
            return getPatientService();
        } else if (ProjectDetails.class.getName().equals(clazz.getName())) {
            return getProjectDetailsService();
        } else if (Cohort.class.getName().equals(clazz.getName())) {
            return getCohortService();
        } else if (VariantRecord.class.getName().equals(clazz.getName()) || VariantData.class.getName().equals(clazz.getName())) {
            return getVariantService();
        } else if (SimpleVariantFile.class.getName().equals(clazz.getName())) {
            return getVariantFileService();
        } else if (Chromosome.class.getName().equals(clazz.getName())) {
            return getChromosomeService();
        } else if (Gene.class.getName().equals(clazz.getName())) {
            return getGeneService();
        } else if (GenomicRegion.class.getName().equals(clazz.getName())) {
            return getGenomicRegionService();
        } else if (Ontology.class.getName().equals(clazz.getName())) {
            return getOntologyService();
        } else if (OntologyTerm.class.getName().equals(clazz.getName())) {
            return getOntologyTermService();
        } else if (Reference.class.getName().equals(clazz.getName())) {
            return getReferenceService();
        } else if (RegionSet.class.getName().equals(clazz.getName())) {
            return getRegionSetService();
        } else if (Annotation.class.getName().equals(clazz.getName())) {
            return getAnnotationService();
        } else if (User.class.getName().equals(clazz.getName())) {
            return getUserService();
        } else if (Setting.class.getName().equals(clazz.getName())) {
            return getSettingService();
        } else if (GeneSet.class.getName().equals(clazz.getName())) {
            return getGeneSetService();
        } else if (AnnotationFormat.class.getName().equals(clazz.getName())) {
            return getAnnotationFormatService();
        } else if (AnnotatedColumn.class.getName().equals(clazz.getName())) {
            return getAnnotationColumnService();
        } else if (CustomColumn.class.getName().equals(clazz.getName())) {
            return getCustomColumnService();
        } else if (VariantTag.class.getName().equals(clazz.getName())) {
            return getVariantTagService();
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

    public static AbstractSolrService getCohortService() throws InitializationException {
        if (cohortService == null) {
            cohortService =  new CohortService();
            cohortService.initialize();
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

    public static AbstractSolrService getChromosomeService() throws InitializationException {
        if (chromosomeService == null) {
            chromosomeService = new ChromosomeSolrService();
            chromosomeService.initialize();
        }
        return chromosomeService;
    }

    public static AbstractSolrService getGeneService() throws InitializationException {
        if (geneService == null) {
            geneService = new GeneSolrService();
            geneService.initialize();
        }
        return geneService;
    }

    public static AbstractSolrService getGenomicRegionService() throws InitializationException {
        if (genomicRegionService == null) {
            genomicRegionService = new GenomicRegionSolrService();
            genomicRegionService.initialize();
        }
        return genomicRegionService;
    }

    public static AbstractSolrService getOntologyService() throws InitializationException {
        if (ontologyService == null) {
            ontologyService = new OntologySolrService();
            ontologyService.initialize();
        }
        return ontologyService;
    }

    public static AbstractSolrService getOntologyTermService() throws InitializationException {
        if (ontologyTermService == null) {
            ontologyTermService = new OntologyTermSolrService();
            ontologyTermService.initialize();
        }
        return ontologyTermService;
    }

    public static AbstractSolrService getReferenceService() throws InitializationException {
        if (referenceService == null) {
            referenceService = new ReferenceSolrService();
            referenceService.initialize();
        }
        return referenceService;
    }

    public static AbstractSolrService getRegionSetService() throws InitializationException {
        if (regionSetService == null) {
            regionSetService = new RegionSetSolrService();
            regionSetService.initialize();
        }
        return regionSetService;
    }

    public static AbstractSolrService getAnnotationService() throws InitializationException {
        if (annotationService == null) {
            annotationService = new AnnotationSolrService();
            annotationService.initialize();
        }
        return annotationService;
    }

    public static AbstractSolrService getUserService() throws InitializationException {
        if (userService == null) {
            userService = new UserSolrService();
            userService.initialize();
        }
        return userService;
    }

    public static AbstractSolrService getSettingService() throws InitializationException {
        if (settingService == null) {
            settingService = new SettingSolrService();
            settingService.initialize();
        }
        return settingService;
    }

    public static AbstractSolrService getAnnotationFormatService() throws InitializationException {
        if (annotationFormatService == null) {
            annotationFormatService = new AnnotationFormatSolrService();
            annotationFormatService.initialize();
        }
        return annotationFormatService;
    }

    public static AbstractSolrService getGeneSetService() throws InitializationException {
        if (geneSetService == null) {
            geneSetService = new GeneSetSolrService();
            geneSetService.initialize();
        }
        return geneSetService;
    }

    public static AbstractSolrService getAnnotationColumnService() throws InitializationException {
        if (annotationColumnService == null) {
            annotationColumnService = new AnnotationColumnService();
            annotationColumnService.initialize();
        }
        return annotationColumnService;
    }

    public static AbstractSolrService getCustomColumnService() throws InitializationException {
        if (customColumnService == null) {
            customColumnService = new CustomColumnSolrService();
            customColumnService.initialize();
        }
        return customColumnService;
    }

    public static AbstractSolrService getVariantTagService() throws InitializationException {
        if (variantTagService == null) {
            variantTagService = new VariantTagService();
            variantTagService.initialize();
        }
        return variantTagService;
    }
}
