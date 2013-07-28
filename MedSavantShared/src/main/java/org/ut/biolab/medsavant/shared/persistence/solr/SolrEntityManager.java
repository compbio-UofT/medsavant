package org.ut.biolab.medsavant.shared.persistence.solr;

import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.shared.solr.service.SolrServiceRegistry;

import java.util.List;

/**
 * Performs the persistence of the entities to Solr.
 */
public class SolrEntityManager implements EntityManager {

    @Override
    public void persist(Object entity) throws InitializationException {

        AbstractSolrService solrService = SolrServiceRegistry.getService(entity.getClass());
        solrService.index(entity);
    }

    @Override
    public <T> void persistAll(List<T> entities) throws InitializationException {
        AbstractSolrService solrService = SolrServiceRegistry.getService(entities.get(0).getClass());
        solrService.index(entities);
    }

}