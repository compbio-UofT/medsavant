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
package org.ut.biolab.medsavant.shared.query.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.base.AbstractQuery;
import org.ut.biolab.medsavant.shared.query.parser.JPQLToSolrTranslator;

/**
 * Provide a Query implementation for the Solr backend.
 */
public class AbstractSolrQuery extends AbstractQuery {

    private JPQLToSolrTranslator translator;

    private SolrQuery solrQuery;

    public AbstractSolrQuery() {
        translator = new JPQLToSolrTranslator();
        executor = new SolrQueryExecutor();
    }

    @Override
    public void setStatement(String statement) {
        super.setStatement(statement);

        parameters = translator.getNamedParameter(statement);
    }

    @Override
    public Query setStart(int start) {
        return super.setStart(start);
    }

    @Override
    public int getStart() {
        return super.getStart();
    }

    @Override
    public Query setLimit(int limit) {
        return super.setLimit(limit);
    }

    @Override
    public int getLimit() {
        return super.getLimit();
    }

    @Override
    public Query setParameter(String parameterName, Object value) {

        if (parameters.containsKey(parameterName)) {
            parameters.put(parameterName, value);
        } else {
            throw new IllegalArgumentException("No parameter with the name " + parameterName + " found in the query.");
        }

        return this;
    }

    @Override
    public Query setParameter(int index, Object value) {
        if (parameters.size() > index ) {
            return setParameter(new Integer(index).toString(), value);
        } else {
            throw new IllegalArgumentException("There is no parameter with index " + index + " found in the query.");
        }
    }

    @Override
    public String toString() {

        this.solrQuery = translator.translate(statement);

        return solrQuery.toString();
    }

    public SolrQuery getSolrQuery() {

        return constructQuery();
    }

    public String getEntity() {
        return translator.getContext().getCoreName();
    }

    private SolrQuery constructQuery() {

        if (statement != null) {
            solrQuery = translator.translate(statement);
            solrQuery.setStart(getStart());
            solrQuery.setRows(getLimit());
        }

        return solrQuery;
    }

}
