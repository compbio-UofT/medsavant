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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;

/**
 * //FIXME this class is more like a DAO than a service, refactor
 */
public abstract class AbstractSolrService {

    private static final Log LOG = LogFactory.getLog(AbstractSolrService.class);

    private static final String SOLR_HOST = "http://localhost:8983/solr/";

    /** The Solr server instance used. */
    protected SolrServer server;

    public void initialize() throws InitializationException {

        try {
            this.server = new HttpSolrServer(SOLR_HOST + this.getName() + "/");

        } catch (RuntimeException ex) {
            LOG.error("Invalid URL specified for the Solr server: {}",ex);
        }
    }

    //todo add paging and sort support
    /**
     * Get the name of the Solr "core" to be used by this service instance.
     *
     * @return the core name
     */
    protected abstract String getName();


    /**
     * Perform search by user a SolrParams object.
     * @param solrParams            The SolrParams containing the parameters.
     * @return                      A SolrDocumentList containing the query result.
     */
    public SolrDocumentList search(SolrParams solrParams) {

        SolrDocumentList result = null;

        try {
            result = this.server.query(solrParams).getResults();
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrParams.toString());
        }

        return result;
    }

    /**
     * Perform search by user a SolrQuery object.
     * @param solrQuery             The SolrQuery to execute
     * @return                      A SolrDocumentList containing the query result.
     */
    public SolrDocumentList search(SolrQuery solrQuery) {
        QueryResponse result = null;

        try {
            result = this.server.query(solrQuery);
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrQuery.toString());
        }

        return result.getResults();
    }

    /**
     * Return the number of results for a query.
     * @param solrQuery             The SolrQuery to execute
     * @return                      the number of results
     */
    public long count(SolrQuery solrQuery) {
        QueryResponse result = null;

        int tempStart = solrQuery.getStart();
        int tempRows = solrQuery.getRows();

        //no need for any results
        solrQuery.setRows(0);
        solrQuery.setStart(0);

        try {
            result = this.server.query(solrQuery);
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrQuery.toString());
        } finally {
            solrQuery.setStart(tempStart);
            solrQuery.setRows(tempRows);
        }

        return result.getResults().getNumFound();
    }

    public SolrDocumentList search(SimpleSolrQuery simpleSolrQuery) {

        return search(simpleSolrQuery.toSolrParams());
    }

}
