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
        } if (entity instanceof VariantComment) {
            return new SearcheableVariantComment((VariantComment) entity);
        } if (entity instanceof Patient) {
            return new SearcheablePatient((Patient) entity);
        } if (entity instanceof Cohort) {
            return new SearcheableCohort((Cohort) entity);
        } if (entity instanceof SearcheableAnnotationLog) {
            return new SearcheableAnnotationLog((AnnotationLog) entity);
        } if (entity instanceof SearcheableGeneralLog) {
            return new SearcheableGeneralLog((GeneralLog) entity);
        } if (entity instanceof ProjectDetails) {
            return new SearcheableProjectDetails((ProjectDetails) entity);
        }  else {
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