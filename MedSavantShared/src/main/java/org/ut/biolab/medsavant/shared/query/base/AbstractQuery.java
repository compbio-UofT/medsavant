package org.ut.biolab.medsavant.shared.query.base;

import org.apache.commons.lang3.ArrayUtils;
import org.ut.biolab.medsavant.shared.query.*;

import java.util.*;

/**
 * Partial implementation for the query interface.
 */
public abstract class AbstractQuery implements Query {

    /**
     * The appropriate query executor for this
     */
    protected QueryExecutor executor;

    /**
     * The query statement
     */
    protected String statement;

    /**
     * The start of the result list in the result set.
     */
    protected int start = 0;

    /**
     * The maximum number of results that can be returned.
     */
    protected int limit = 10;

    /**
     * The map holding the query parameter values
     */
    protected Map<String, Object> parameters = new HashMap<String, Object>();

    @Override
    public Query setParameter(String parameterName, Object value) {
        //what to do if the parameter name is not found in the query?
        parameters.put(parameterName, value);
        return this;
    }

    @Override
    public <T> List<T> execute() throws QueryException {
        return executor.execute(this);
    }

    @Override
    public List<ResultRow> executeForRows() {
        return executor.executeForRows(this);
    }


    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    @Override
    public Collection<Object> getParameters() {
        return parameters.values();
    }

    @Override
    public Map<String, Object> getParameterMap() {
        return parameters;
    }

    @Override
    public Query setStart(int start) {
        this.start = start;
        return this;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public Query setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public String getStatement() {
        return statement;
    }

    @Override
    public void setStatement(String statement) {
        this.statement = statement;
    }
}