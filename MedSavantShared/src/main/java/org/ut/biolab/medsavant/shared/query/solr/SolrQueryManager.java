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
import org.apache.solr.common.params.CommonParams;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.shared.solr.service.SolrServiceRegistry;

import java.io.File;

/**
 * Solr implementation for the QueryManager.
 */
public class SolrQueryManager implements QueryManager {

    private static final Log LOG = LogFactory.getLog(SolrQueryManager.class);

    @Override
    public Query createQuery(String queryString) {
        AbstractSolrQuery solrQuery = new AbstractSolrQuery();
        solrQuery.setStatement(queryString);

        return solrQuery;
    }

    @Override
    public void toTSV(Query query, File file) {
        AbstractSolrQuery abstractSolrQuery = (AbstractSolrQuery) query;
        SolrQuery solrQuery = abstractSolrQuery.getSolrQuery();

        solrQuery.add(CommonParams.WT, "csv");
        solrQuery.add("csv.header", "false");
        String entityName = abstractSolrQuery.getEntity();

        AbstractSolrService solrService = null;
        try {
            solrService = SolrServiceRegistry.getService(entityName);
            solrService.queryAndWriteToFile(solrQuery, file);
        } catch (InitializationException e) {
            LOG.error("Error retrieving solr service.");
        }
    }
}
