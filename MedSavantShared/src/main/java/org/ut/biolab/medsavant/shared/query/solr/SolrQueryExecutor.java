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
package org.ut.biolab.medsavant.shared.query.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryExecutor;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.mapper.MapperRegistry;
import org.ut.biolab.medsavant.shared.solr.mapper.ResultMapper;
import org.ut.biolab.medsavant.shared.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.shared.solr.service.SolrServiceRegistry;

import java.util.List;

/**
 * Query executor for Solr queries
 */
public class SolrQueryExecutor implements QueryExecutor {

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
            SolrDocumentList results = solrService.search(solrQuery, abstractSolrQuery.getAggregateFields());
            ResultMapper mapper = MapperRegistry.getResultRowMapper();
            return mapper.map(results);
        } catch (InitializationException e) {
            LOG.error("Error retrieving necessary Solr service ", e);
        }

        return null;
    }

    @Override
    public long count(Query query) {
        AbstractSolrQuery abstractSolrQuery = (AbstractSolrQuery) query;
        SolrQuery solrQuery = abstractSolrQuery.getSolrQuery();

        String entityName = abstractSolrQuery.getEntity();
        AbstractSolrService solrService = null;

        long count = 0;

        try {
            solrService = SolrServiceRegistry.getService(entityName);
            count = solrService.count(solrQuery);
        } catch (InitializationException e) {
            LOG.error("Error retrieving necessary Solr service ", e);
        }

        return count;
    }


}