package org.ut.biolab.medsavant.shared.query.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryExecutor;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.JPQLToSolrTranslator;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.mapper.MapperRegistry;
import org.ut.biolab.medsavant.shared.solr.mapper.ResultMapper;
import org.ut.biolab.medsavant.shared.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.shared.solr.service.SolrServiceRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * Query executor for Solr queries
 */
public class SolrQueryExecutor implements QueryExecutor {

    private JPQLToSolrTranslator translator = new JPQLToSolrTranslator();

    private static final Log LOG = LogFactory.getLog(SolrQueryExecutor.class);

    @Override
    public <T> List<T> execute(Query query) {

        AbstractSolrQuery abstractSolrQuery = (AbstractSolrQuery) query;
        SolrQuery solrQuery = abstractSolrQuery.getSolrQuery();

        String entityName = abstractSolrQuery.getEntity();
        AbstractSolrService solrService = null;
        try {
            solrService = SolrServiceRegistry.getService(entityName);
            SolrDocumentList results = solrService.search(solrQuery);
            ResultMapper mapper = MapperRegistry.getMapper(entityName);
            return mapper.map(results);
        } catch (InitializationException e) {
            LOG.error("Error retrieving necessary Solr service ", e);
        }

        return null;
    }

    @Override
    public List<ResultRow> executeForRows(Query query) {
        AbstractSolrQuery abstractSolrQuery = (AbstractSolrQuery) query;
        SolrQuery solrQuery = abstractSolrQuery.getSolrQuery();

        String entityName = abstractSolrQuery.getEntity();
        AbstractSolrService solrService = null;

        try {
            solrService = SolrServiceRegistry.getService(entityName);
            SolrDocumentList results = solrService.search(solrQuery);
            ResultMapper mapper = MapperRegistry.getResultRowMapper();
            return mapper.map(results);
        } catch (InitializationException e) {
            LOG.error("Error retrieving necessary Solr service ", e);
        }

        return null;
    }
}