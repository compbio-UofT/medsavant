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
package org.ut.biolab.medsavant.shared.query.parser.analyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
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
        String statement = solrQuery.get(CommonParams.Q);
        solrQuery.setQuery(ParserUtil.addEntityField(statement, context.getCoreName()));
        return ParserUtil.addDefaultQueryParameters(solrQuery);
    }

    public void setSolrQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
    }
}
