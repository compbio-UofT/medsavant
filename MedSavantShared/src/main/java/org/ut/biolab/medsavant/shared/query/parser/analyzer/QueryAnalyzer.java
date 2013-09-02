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
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.query.parser.analysis.DepthFirstAdapter;
import org.ut.biolab.medsavant.shared.query.parser.node.ADelStatement;
import org.ut.biolab.medsavant.shared.query.parser.node.ASelStatement;
import org.ut.biolab.medsavant.shared.query.parser.node.AUpdStatement;

import java.util.HashMap;
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
    public void caseASelStatement(ASelStatement node) {

        SelectQueryAnalyzer selectQueryAnalyzer = new SelectQueryAnalyzer(context);
        node.apply(selectQueryAnalyzer);

        this.solrQuery = selectQueryAnalyzer.getSolrQuery();
     }

    @Override
    public void caseADelStatement(ADelStatement node) {

        DeleteQueryAnalyzer deleteQueryAnalyzer = new DeleteQueryAnalyzer(context);

        node.apply(deleteQueryAnalyzer);

        this.solrQuery = deleteQueryAnalyzer.getSolrQuery();
    }

    @Override
    public void caseAUpdStatement(AUpdStatement node) {
        context.setUpdateFields(new HashMap<String, Map<String, Object>>());
        UpdateQueryAnalyzer updateQueryAnalyzer = new UpdateQueryAnalyzer(context);
        node.apply(updateQueryAnalyzer);

        this.solrQuery = updateQueryAnalyzer.getSolrQuery();
    }

    public QueryContext getContext() {
        return context;
    }

    public void setContext(QueryContext context) {
        this.context = context;
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    public void setSolrQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
    }
}
