package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.AAbstractSchemaName;
import org.ut.biolab.medsavant.shared.query.parser.node.AUpdateField;
import org.ut.biolab.medsavant.shared.query.parser.node.AUpdateStatement;
import org.ut.biolab.medsavant.shared.query.parser.node.AWhereClause;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Perform the parsing of a update query.
 */
public class UpdateQueryAnalyzer extends DepthFirstAdapter {

    private QueryContext context;

    private SolrQuery solrQuery;

    private static final Log LOG = LogFactory.getLog(UpdateQueryAnalyzer.class);

    @Override
    public void inAUpdateStatement(AUpdateStatement node) {
        this.solrQuery = new SolrQuery();
        super.inAUpdateStatement(node);
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
    public void caseAUpdateField(AUpdateField node) {
        //get property name
        EntityPropertyAnalyzer epa = new EntityPropertyAnalyzer();
        node.getPathExpression().apply(epa);
        String property = epa.getProperty();

        //get new value
        String value = node.getNewValue().toString();

        //populate map needed for update
        Map<String, String> currentFieldUpdateInfo = new HashMap<String, String>();
        currentFieldUpdateInfo.put(property, value);
        context.getUpdateFields().put("set", currentFieldUpdateInfo);

        LOG.debug(String.format("Adding update info for %s New value of %s is %s.", node.getPathExpression(), property, value));
    }

    @Override
    public void caseAAbstractSchemaName(AAbstractSchemaName node) {
        String coreName = node.toString().trim().toLowerCase(Locale.ROOT);
        context.setCoreName(coreName);
    }

    public UpdateQueryAnalyzer(QueryContext context) {
        this.context = context;
    }

    public QueryContext getContext() {
        return context;
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }
}
