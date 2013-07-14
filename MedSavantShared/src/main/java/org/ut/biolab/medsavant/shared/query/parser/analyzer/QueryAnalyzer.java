package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.query.QuerySort;
import org.ut.biolab.medsavant.shared.query.QuerySortDirection;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.*;

import java.util.List;

/**
 * Responsible for analyzing the queries.
 */
public class QueryAnalyzer extends DepthFirstAdapter {

    private QueryContext context;

    private SimpleSolrQuery simpleSolrQuery;

    private static final Log LOG = LogFactory.getLog(QueryAnalyzer.class);

    public QueryAnalyzer(QueryContext queryContext) {
        this.context = queryContext;
    }

    public QueryAnalyzer() {
        super();
    }

    @Override
    public void inASelectStatement(ASelectStatement node) {
        LOG.debug("Entering Select " + node.toString());

        simpleSolrQuery = new SimpleSolrQuery();

        super.inASelectStatement(node);
    }


    @Override
    public void inAConditionalPrimary(AConditionalPrimary node) {
        LOG.debug("Exiting conditional primary " + node.toString());
                 node.getSimpleCondExpression();
        TermAnalyzer termAnalyzer = new TermAnalyzer();
        node.apply(termAnalyzer);
        String term = termAnalyzer.getTerm();
        String value = termAnalyzer.getValue();

        simpleSolrQuery.addQueryTerm(term, value);

        super.outAConditionalPrimary(node);
    }

    @Override
    public void inAOrderbyItem(AOrderbyItem node) {
        LOG.debug("Entering order by clause item");

        SortsAnalyzer sortsAnalyzer = new SortsAnalyzer();
        node.apply(sortsAnalyzer);

        String field = sortsAnalyzer.getField();
        QuerySortDirection direction = sortsAnalyzer.getDirection();

        simpleSolrQuery.addSortTerm(field, direction.name());

        super.inAOrderbyItem(node);
    }

    @Override
    public void inASingleFromList(ASingleFromList node) {
        LOG.debug("Single entity: " + node.toString());
        CoreAnalyzer coreAnalyzer = new CoreAnalyzer();
        node.apply(coreAnalyzer);

        context.setCoreName(coreAnalyzer.getCoreName());

        super.inASingleFromList(node);
    }

    @Override
    public void inASelectClause(ASelectClause node) {
        LOG.debug("Select from single: " + node.toString());
        ResultFieldAnalyzer resultFieldAnalyzer = new ResultFieldAnalyzer();

        node.apply(resultFieldAnalyzer);

        List<String> resultFields = resultFieldAnalyzer.getField();
        context.setResultFieldNames(resultFields);

        super.inASelectClause(node);
    }

    /*@Override
    public void inASingleSelectList(ASingleSelectList node) {
        LOG.debug("Select from single: " + node.toString());
        ResultFieldAnalyzer resultFieldAnalyzer = new ResultFieldAnalyzer();

        node.apply(resultFieldAnalyzer);

        List<String> resultFields = resultFieldAnalyzer.getField();
        context.setResultFieldNames(resultFields);
        super.inASingleSelectList(node);
    }*/

    public SimpleSolrQuery getSimpleSolrQuery() {
        return simpleSolrQuery;
    }

    public void setSimpleSolrQuery(SimpleSolrQuery simpleSolrQuery) {
        this.simpleSolrQuery = simpleSolrQuery;
    }
}
