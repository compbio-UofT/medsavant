package org.ut.biolab.medsavant.shared.query.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.query.base.AbstractQuery;
import org.ut.biolab.medsavant.shared.query.parser.JPQLToSolrTranslator;

import java.util.List;

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
