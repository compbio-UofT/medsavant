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
package org.ut.biolab.medsavant.shared.persistence.solr;

import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.model.*;
import org.ut.biolab.medsavant.shared.model.solr.*;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.shared.solr.service.SolrServiceRegistry;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs the persistence of the entities to Solr.
 */
public class SolrEntityManager implements EntityManager {

    @Override
    public void persist(Object entity) throws InitializationException {

        AbstractSolrService solrService = SolrServiceRegistry.getService(entity.getClass());
        solrService.index(mapToSolEntity(entity));
    }

    @Override
    public <T> void persistAll(List<T> entities) throws InitializationException {
        AbstractSolrService solrService = SolrServiceRegistry.getService(entities.get(0).getClass());
        solrService.index(mapToSolrEntities(entities));
    }

    /**
     * Map a domain entity to an annotated entity which can be persisted into Solr.
     * @param entity                    Domain entity.
     * @return                          Corresponding annotated entity.
     */
    private Object mapToSolEntity(Object entity) {
        if (entity instanceof VariantRecord) {
            return new SearcheableVariant((VariantRecord)entity);
        } else if (entity instanceof SimpleVariantFile) {
            return new SearcheableVariantFile((SimpleVariantFile) entity);
        } else if (entity instanceof VariantComment) {
            return new SearcheableVariantComment((VariantComment) entity);
        } else if (entity instanceof Patient) {
            return new SearcheablePatient((Patient) entity);
        } else if (entity instanceof Cohort) {
            return new SearcheableCohort((Cohort) entity);
        } else if (entity instanceof AnnotationLog) {
            return new SearcheableAnnotationLog((AnnotationLog) entity);
        } else if (entity instanceof GeneralLog) {
            return new SearcheableGeneralLog((GeneralLog) entity);
        } else if (entity instanceof ProjectDetails) {
            return new SearcheableProjectDetails((ProjectDetails) entity);
        } else if (entity instanceof AnnotatedColumn) {
            return new SearcheableAnnotatedColumn((AnnotatedColumn) entity);
        } else if (entity instanceof Annotation) {
            return new SearcheableAnnotation((Annotation) entity);
        } else if (entity instanceof AnnotationFormat) {
            return new SearcheableAnnotationFormat((AnnotationFormat) entity);
        } else if (entity instanceof Chromosome) {
            return new SearcheableChromosome((Chromosome) entity);
        } else if (entity instanceof Gene) {
            return new SearcheableGene((Gene) entity);
        } else if (entity instanceof GeneSet) {
            return new SearcheableGeneSet((GeneSet) entity);
        } else if (entity instanceof GenomicRegion) {
            return new SearcheableGenomicRegion((GenomicRegion) entity);
        } else if (entity instanceof Ontology) {
            return new SearcheableOntology((Ontology) entity);
        } else if (entity instanceof OntologyTerm) {
            return new SearcheableOntologyTerm((OntologyTerm) entity);
        } else if (entity instanceof Reference) {
            return new SearcheableReference((Reference) entity);
        } else if (entity instanceof RegionSet) {
            return new SearcheableRegionSet((RegionSet) entity);
        } else if (entity instanceof Setting) {
            return new SearcheableSetting((Setting) entity);
        } else if (entity instanceof User) {
            return new SearcheableUser((User) entity);
        } else {
            return entity;
        }
    }

    /**
     * Map a list of domain entities to a list of annotated entities which can be persisted into Solr.
     * @param entities                      A list of domain entities.
     * @return                              A list of annotation entities.
     */
    private <T> List<Object> mapToSolrEntities(List<T> entities) {
        List<Object> solrEntities = new ArrayList<Object>(entities.size());
        for (Object entity : entities) {
            solrEntities.add(mapToSolEntity(entity));
        }
        return solrEntities;
    }

}