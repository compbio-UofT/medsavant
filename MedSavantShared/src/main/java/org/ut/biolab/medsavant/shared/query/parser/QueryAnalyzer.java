package org.ut.biolab.medsavant.shared.query.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.*;

/**
 * Responsible for analyzing the queries.
 */
public class QueryAnalyzer extends DepthFirstAdapter {

    QueryContext context;

    SimpleSolrQuery simpleSolrQuery;

    private static final Log LOG = LogFactory.getLog(QueryAnalyzer.class);


    public QueryAnalyzer(QueryContext queryContext) {
        this.context = queryContext;
    }

    public QueryAnalyzer() {
        super();
    }

    @Override
    public void inASelectStatement(ASelectStatement node) {
        LOG.info("Entering Select " + node.toString());

        simpleSolrQuery = new SimpleSolrQuery();

        super.inASelectStatement(node);
    }

    @Override
    public void inAConditionalPrimary(AConditionalPrimary node) {
        LOG.info("Exiting conditional primary " + node.toString());
                 node.getSimpleCondExpression();
        TermAnalyzer termAnalyzer = new TermAnalyzer();
        node.apply(termAnalyzer);
        String term = termAnalyzer.getTerm();
        String value = termAnalyzer.getValue();

        simpleSolrQuery.addQueryTerm(term, value);

        super.outAConditionalPrimary(node);
    }

    public SimpleSolrQuery getSimpleSolrQuery() {
        return simpleSolrQuery;
    }

    public void setSimpleSolrQuery(SimpleSolrQuery simpleSolrQuery) {
        this.simpleSolrQuery = simpleSolrQuery;
    }
}
