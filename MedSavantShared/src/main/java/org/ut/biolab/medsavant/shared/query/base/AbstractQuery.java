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
package org.ut.biolab.medsavant.shared.query.base;

import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryExecutor;
import org.ut.biolab.medsavant.shared.query.ResultRow;

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
    public <T> List<T> execute(){
        return executor.execute(this);
    }

    @Override
    public List<ResultRow> executeForRows() {
        return executor.executeForRows(this);
    }

    @Override
    public <T> T getFirst() {
        return executor.getFirst(this);
    }

    @Override
    public ResultRow getFirstRow() {
        return executor.getFirstRow(this);
    }

    @Override
    public void executeDelete() {
        executor.executeDelete(this);
    }

    @Override
    public void executeUpdate() {
        executor.executeUpdate(this);
    }

    @Override
    public long count() {
        return executor.count(this);
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