package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.AAbstractSchemaName;
import org.ut.biolab.medsavant.shared.query.parser.node.ADeleteStatement;
import org.ut.biolab.medsavant.shared.query.parser.node.AWhereClause;
import org.ut.biolab.medsavant.shared.query.parser.util.ParserUtil;

import java.util.Locale;

/**
 * Collect the query for delete queries
 */
public class DeleteQueryAnalyzer extends DepthFirstAdapter {

    private QueryContext context;

    private SolrQuery solrQuery;

    private static final Log LOG = LogFactory.getLog(DeleteQueryAnalyzer.class);

    @Override
    public void inADeleteStatement(ADeleteStatement node) {

        this.solrQuery = new SolrQuery();

        super.inADeleteStatement(node);
    }

    @Override
    public void inAWhereClause(AWhereClause node) {
        LOG.debug("Entering main conditional expression " + node.toString());

        node.getConditionalExpression();
        TermAnalyzer termAnalyzer = new TermAnalyzer(context);
        node.apply(termAnalyzer);

        String query = termAnalyzer.getQuery();
        solrQuery.setQuery(query);

        super.outAWhereClause(node);
    }

    @Override
    public void caseAAbstractSchemaName(AAbstractSchemaName node) {

        String coreName = node.toString().trim().toLowerCase(Locale.ROOT);
        context.setCoreName(coreName);
    }

    public DeleteQueryAnalyzer(QueryContext context) {
        this.context = context;
    }

    public QueryContext getContext() {
        return context;
    }

    public void setContext(QueryContext context) {
        this.context = context;
    }

    public SolrQuery getSolrQuery() {
        return ParserUtil.addDefaultQueryParameters(solrQuery);
    }

    public void setSolrQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
    }
}
