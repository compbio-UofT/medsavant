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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.StatsParams;
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Responsible for analyzing the queries.
 */
public class QueryAnalyzer extends DepthFirstAdapter {

    private QueryContext context;

    private SolrQuery solrQuery;

    private static final Log LOG = LogFactory.getLog(QueryAnalyzer.class);

    public QueryAnalyzer(QueryContext queryContext) {
        this.context = queryContext;
    }

    @Override
    public void inASelectStatement(ASelectStatement node) {
        LOG.debug("Entering Select " + node.toString());

        solrQuery = new SolrQuery();

        super.inASelectStatement(node);
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
    public void inAOrderbyItem(AOrderbyItem node) {
        LOG.debug("Entering order by clause item");

        SortsAnalyzer sortsAnalyzer = new SortsAnalyzer();
        node.apply(sortsAnalyzer);

        String field = sortsAnalyzer.getField();
        SolrQuery.ORDER direction = sortsAnalyzer.getDirection();

        solrQuery.addSortField(field, direction);

        super.outAOrderbyItem(node);
    }

    @Override
    public void inASingleFromList(ASingleFromList node) {
        LOG.debug("Single entity: " + node.toString());
        CoreAnalyzer coreAnalyzer = new CoreAnalyzer();
        node.apply(coreAnalyzer);

        context.setCoreName(coreAnalyzer.getCoreName());

        super.outASingleFromList(node);
    }

    @Override
    public void inASelectClause(ASelectClause node) {
        LOG.debug("Select from single: " + node.toString());
        ResultFieldAnalyzer resultFieldAnalyzer = new ResultFieldAnalyzer(context);

        node.apply(resultFieldAnalyzer);

        List<String> resultFields = resultFieldAnalyzer.getField();
        addFieldsFromList(resultFields);

        Map<String, String> aggregates = resultFieldAnalyzer.getAggregates();
        addAggregateTerms(aggregates);
        context.setAggregates(aggregates);

        super.outASelectClause(node);
    }

    @Override
    public void caseAGroupbyClause(AGroupbyClause node) {

        LOG.debug("Analyzing group by clause: " + node.toString());

        AggregateAnalyzer aggregateAnalyzer = new AggregateAnalyzer(context);
        List<String> groupByTerms = new ArrayList<String>();
        aggregateAnalyzer.getQueryContext().setGroupByTerms(groupByTerms);
        node.apply(aggregateAnalyzer);

        groupByTerms = aggregateAnalyzer.getQueryContext().getGroupByTerms();
        addFacetFields(groupByTerms);
    }

    public SolrQuery getSolrQuery() {
        return addDefaultQueryParameters(solrQuery);
    }

    public void setSolrQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
    }

    private void addFieldsFromList(List<String> fields) {
        for (String field : fields) {
            solrQuery.addField(field);
        }
    }

    private void addFacetFields(List<String> fields) {
        if (fields != null && fields.size() == 1) {
            solrQuery.addFacetField(fields.get(0));
        } else {
            addFacePivotFields(fields);
        }
    }

    private void addFacePivotFields(List<String> fields) {

        String pivotFields = StringUtils.join(fields, ",");
        solrQuery.add(FacetParams.FACET, "true");
        solrQuery.add(FacetParams.FACET_PIVOT, pivotFields);
    }

    private void addAggregateTerms(Map<String, String> aggregates) {

        solrQuery.add(StatsParams.STATS, "true");

        for (Map.Entry<String, String> entry : aggregates.entrySet()) {
            addAggregateTerm(entry.getKey(), entry.getValue());
        }
    }

    private SolrQuery addAggregateTerm(String aggregateExpression, String column) {

        if ("".equals(column)) {
            solrQuery.add(StatsParams.STATS_FIELD, "uuid");
        } else {
            solrQuery.add(StatsParams.STATS_FIELD, column);
        }

        return solrQuery;
    }
    
    private SolrQuery addDefaultQueryParameters(SolrQuery solrQuery) {

        String params = solrQuery.get(CommonParams.Q);

        if (params == null || "".equals(params)) {
            solrQuery.add(CommonParams.Q, "*:*");
        }

        return solrQuery;
    }



}
