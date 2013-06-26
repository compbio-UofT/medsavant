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
package org.ut.biolab.medsavant.server.solr.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.ut.biolab.medsavant.server.solr.SimpleSolrQuery;
import org.ut.biolab.medsavant.server.solr.exception.InitializationException;

/**
 * //FIXME this class is more like a DAO than a service, refactor
 *
 * @author Bogdan Vancea
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


    public SolrDocumentList search(SolrParams solrParams) {

        SolrDocumentList result = null;

        try {
            result = this.server.query(solrParams).getResults();
        } catch (SolrServerException e) {
            LOG.error("Error executing query " + solrParams.toString());
        }

        return result;
    }

    public SolrDocumentList search(SimpleSolrQuery simpleSolrQuery) {

        return search(simpleSolrQuery.toSolrParams());
    }

}
