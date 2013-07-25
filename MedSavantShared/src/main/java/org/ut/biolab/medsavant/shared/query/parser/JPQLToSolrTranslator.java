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
package org.ut.biolab.medsavant.shared.query.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.analyzer.NamedParameterAnalyzer;
import org.ut.biolab.medsavant.shared.query.parser.analyzer.QueryAnalyzer;
import org.ut.biolab.medsavant.shared.query.parser.lexer.LexerException;
import org.ut.biolab.medsavant.shared.query.parser.node.Start;
import org.ut.biolab.medsavant.shared.query.parser.parser.ParserException;

import java.io.IOException;
import java.util.Map;

/**
 * Translate JPQL queries.
 */
public class JPQLToSolrTranslator {

    private QueryContext context;

    private QueryAnalyzer analyzer;

    private NamedParameterAnalyzer parameterAnalyzer;

    private static final Log LOG = LogFactory.getLog(JPQLToSolrTranslator.class);

    public JPQLToSolrTranslator() {
        this.context = new QueryContext();
        this.analyzer = new QueryAnalyzer(context);
        this.parameterAnalyzer = new NamedParameterAnalyzer();
    }

    public JPQLToSolrTranslator(QueryContext context, QueryAnalyzer analyzer, NamedParameterAnalyzer parameterAnalyzer) {
        this.context = context;
        this.analyzer = analyzer;
        this.parameterAnalyzer = parameterAnalyzer;
    }

    public SolrQuery translate(String input) {

        LOG.info("Translating query: " + input);
        JPQLParser parser = new JPQLParser();
        SolrQuery solrQuery = null;
        try {
            Start tree = parser.parse(input);
            tree.apply(analyzer);

            solrQuery = analyzer.getSolrQuery();

        } catch (ParserException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (IOException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (LexerException e) {
            LOG.error("Unable to parse string " + input, e);
        }

        return solrQuery;
    }

    public Map<String, Object> getNamedParameter(String input) {
        JPQLParser parser = new JPQLParser();

        Map<String, Object> parameterMap = null;
        try {
            Start tree = parser.parse(input);
            tree.apply(parameterAnalyzer);

            parameterMap = parameterAnalyzer.getParameters();
            context.setParameters(parameterMap);
        } catch (ParserException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (IOException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (LexerException e) {
            LOG.error("Unable to parse string " + input, e);
        }

        return parameterMap;
    }

    public QueryAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(QueryAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public QueryContext getContext() {
        return context;
    }

    public void setContext(QueryContext context) {
        this.context = context;
    }

    private Map<String, String> getAggregates() {
        return context.getAggregates();
    }
}
