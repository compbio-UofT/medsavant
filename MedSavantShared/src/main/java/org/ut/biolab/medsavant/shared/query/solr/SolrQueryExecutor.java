package org.ut.biolab.medsavant.shared.query.solr;

import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryExecutor;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.JQPLToSolrTranslator;
import org.ut.biolab.medsavant.shared.solr.service.AbstractSolrService;
import org.ut.biolab.medsavant.shared.solr.service.VariantService;

import java.util.Arrays;
import java.util.List;

/**
 * Query executor for Solr queries
 */
public class SolrQueryExecutor implements QueryExecutor {

    private AbstractSolrService solrService;

    private JQPLToSolrTranslator translator;

    @Override
    public <T> List<T> execute(Query query) {

        String statement = query.getStatement();
        SimpleSolrQuery simpleSolrQuery = translator.translate(statement);

        SolrDocumentList results = solrService.search(simpleSolrQuery);

        return (List<T>) Arrays.asList(results);
    }
}
