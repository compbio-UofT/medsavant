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

    private static ChromosomeMapper chromosomeMapper;

    private static GeneMapper geneMapper;

    private static GenomicRegionMapper genomicRegionMapper;

    private static OntologyMapper ontologyMapper;

    private static OntologyTermMapper ontologyTermMapper;

    private static ReferenceMapper referenceMapper;

    private static RegionSetMapper regionSetMapper;

    private static AnnotationMapper annotationMapper;

    private static UserMapper userMapper;

    private static SettingMapper settingMapper;

    private static AnnotationFormatMapper annotationFormatMapper;

    private static ResultMapper geneSetMapper;

    private static AnnotationColumnMapper annotationColumnMapper;

    /**
     * Get the appropriate mapper based on the entity name.
     *
     * @param entity The name of the entity
     * @return The appropriate mapper.
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
        } else if (Entity.PROJECT.equals(entity)) {
            return getProjectMapper();
        } else if (Entity.VARIANT_FILE.equals(entity)) {
            return getVariantFileMapper();
        } else if (Entity.CHROMOSOME.equals(entity)) {
            return getChromosomeMapper();
        } else if (Entity.GENE.equals(entity)) {
            return getGeneMapper();
        } else if (Entity.GENOMIC_REGION.equals(entity)) {
            return getGenomicRegionMapper();
        } else if (Entity.ONTOLOGY.equals(entity)) {
            return getOntologyMapper();
        } else if (Entity.ONTOLOGY_TERM.equals(entity)) {
            return getOntologyTermMapper();
        } else if (Entity.REFERENCE.equals(entity)) {
            return getReferenceMapper();
        } else if (Entity.REGION_SET.equals(entity)) {
            return getRegionSetMapper();
        } else if (Entity.ANNOTATION.equals(entity)) {
            return getAnnotationMapper();
        } else if (Entity.USER.equals(entity)) {
            return getUserMapper();
        } else if (Entity.SETTING.equals(entity)) {
            return getSettingMapper();
        } else if (Entity.ANNOTATION_FORMAT.equals(entity)) {
            return getAnnotationFormatMapper();
        } else if (Entity.GENE_SET.equals(entity)) {
            return getGeneSetMapper();
        } else if (Entity.ANNOTATION_COLUMN.equals(entity)) {
            return getAnnotationColumnMapper();
        }

        return null;
    }

    /**
     * Return the ontology term mapper instance. Creates one if it doesn't yet exist.
     * @return          The ontology term mapper instance.
     */
    private static OntologyTermMapper getOntologyTermMapper() {

        if (ontologyTermMapper == null) {
            ontologyTermMapper = new OntologyTermMapper();
        }
        return ontologyTermMapper;
    }

    /**
     * Return the ontology mapper instance. Creates one if it doesn't yet exist.
     * @return          The ontology mapper instance.
     */
    private static OntologyMapper getOntologyMapper() {

        if (ontologyMapper == null) {
            ontologyMapper = new OntologyMapper();
        }
        return ontologyMapper;
    }

    /**
     * Return the gene mapper instance. Creates one if it doesn't yet exist.
     * @return          The gene mapper instance.
     */
    private static GeneMapper getGeneMapper() {

        if (geneMapper == null) {
            geneMapper = new GeneMapper();
        }
        return geneMapper;
    }

    /**
     * Return the chromosome mapper instance. Creates one if it doesn't yet exist.
     * @return          The chromosome mapper instance.
     */
    private static ChromosomeMapper getChromosomeMapper() {

        if (chromosomeMapper == null) {
            chromosomeMapper = new ChromosomeMapper();
        }
        return chromosomeMapper;
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
     * Return the Annotation mapper instance. Creates one if it doesn't yet exist.
     * @return          The Annotation mapper instance.
     */
    public static AnnotationMapper getAnnotationMapper() {
        if (annotationMapper == null) {
            annotationMapper = new AnnotationMapper();
        }
        return annotationMapper;
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
    private static VariantFileMapper getVariantFileMapper() {
        if (variantFileMapper == null) {
            variantFileMapper = new VariantFileMapper();
        }
        return variantFileMapper;
    }

    /**
     * Return the genomic region mapper instance. Creates one if it doesn't yet exist.
     * @return          The genomic region mapper instance.
     */
    public static GenomicRegionMapper getGenomicRegionMapper() {

        if (genomicRegionMapper == null) {
            genomicRegionMapper = new GenomicRegionMapper();
        }
        return genomicRegionMapper;
    }

    /**
     * Return the reference mapper instance. Creates one if it doesn't yet exist.
     * @return          The reference mapper instance.
     */
    public static ReferenceMapper getReferenceMapper() {
        if (referenceMapper == null) {
            referenceMapper = new ReferenceMapper();
        }
        return referenceMapper;
    }

    /**
     * Return the region set mapper instance. Creates one if it doesn't yet exist.
     * @return          The region set mapper instance.
     */
    public static RegionSetMapper getRegionSetMapper() {
        if (regionSetMapper == null) {
            regionSetMapper = new RegionSetMapper();
        }
        return regionSetMapper;
    }

    /**
     * Return the user mapper instance. Creates one if it doesn't yet exist.
     * @return          The user mapper instance.
     */
    public static UserMapper getUserMapper() {
        if (userMapper == null) {
            userMapper = new UserMapper();
        }
        return userMapper;
    }

    /**
     * Return the setting mapper instance. Creates one if it doesn't yet exist.
     * @return          The setting mapper instance.
     */
    public static SettingMapper getSettingMapper() {
        if (settingMapper == null) {
            settingMapper = new SettingMapper();
        }
        return settingMapper;
    }

    /**
     * Return the gene set mapper instance. Creates one if it doesn't yet exist.
     * @return          The gene set mapper instance.
     */
    public static ResultMapper getGeneSetMapper() {
        if (geneSetMapper == null) {
            geneSetMapper = new GeneSetMapper();
        }
        return geneSetMapper;
    }

    /**
     * Return the annotation format mapper instance. Creates one if it doesn't yet exist.
     * @return          The annotation format mapper instance.
     */
    public static AnnotationFormatMapper getAnnotationFormatMapper() {
        if (annotationFormatMapper == null) {
            annotationFormatMapper = new AnnotationFormatMapper();
        }
        return annotationFormatMapper;
    }

    /**
     * Return the annotation column mapper instance. Creates one if it doesn't yet exist.
     * @return          The annotation column mapper instance.
     */
    public static AnnotationColumnMapper getAnnotationColumnMapper() {
        if (annotationColumnMapper == null) {
            annotationColumnMapper = new AnnotationColumnMapper();
        }
        return annotationColumnMapper;
    }
}
