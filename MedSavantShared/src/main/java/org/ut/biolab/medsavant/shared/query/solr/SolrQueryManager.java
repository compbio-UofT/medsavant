package org.ut.biolab.medsavant.shared.query.solr;

import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;

/**
 * Solr implementation for the QueryManager.
 */
public class SolrQueryManager implements QueryManager {

    @Override
    public Query createQuery(String queryString) {
        AbstractSolrQuery solrQuery = new AbstractSolrQuery();
        solrQuery.setStatement(queryString);

        return solrQuery;
    }
}
