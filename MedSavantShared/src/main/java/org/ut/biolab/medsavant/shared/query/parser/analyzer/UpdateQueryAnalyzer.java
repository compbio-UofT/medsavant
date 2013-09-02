package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.AAbstractSchemaName;
import org.ut.biolab.medsavant.shared.query.parser.node.AUpdateField;
import org.ut.biolab.medsavant.shared.query.parser.node.AUpdateStatement;
import org.ut.biolab.medsavant.shared.query.parser.node.AWhereClause;
import org.ut.biolab.medsavant.shared.query.parser.util.ParserUtil;

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
        Object value = ParserUtil.getValueToken(node.getNewValue().toString(), context);

        //populate map needed for update
        Map<String, Object> currentFieldUpdateInfo = new HashMap<String, Object>();
        currentFieldUpdateInfo.put("set", value);
        context.getUpdateFields().put(property, currentFieldUpdateInfo);

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
        String statement = solrQuery.get(CommonParams.Q);
        solrQuery.setQuery(ParserUtil.addEntityField(statement, context.getCoreName()));
        return ParserUtil.addDefaultQueryParameters(solrQuery);
    }
}
