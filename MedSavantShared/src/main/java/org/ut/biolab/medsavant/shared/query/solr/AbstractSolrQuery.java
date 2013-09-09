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

import org.apache.commons.lang.ArrayUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.base.AbstractQuery;
import org.ut.biolab.medsavant.shared.query.parser.JPQLToSolrTranslator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide a Query implementation for the Solr backend.
 */
public class AbstractSolrQuery extends AbstractQuery {

    private JPQLToSolrTranslator translator;

    private SolrQuery solrQuery;

    private Map<String, String> aggregateFields;

    public AbstractSolrQuery() {
        translator = new JPQLToSolrTranslator();
        executor = new SolrQueryExecutor();
    }

    public Map<String, String> getAggregateFields() {
        return aggregateFields;
    }

    public void setAggregateFields(Map<String, String> aggregateFields) {
        this.aggregateFields = aggregateFields;
    }

    @Override
    public void setStatement(String statement) {
        super.setStatement(statement);

        //extract named parameters to a map, don't translate yet
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

            aggregateFields = translator.getContext().getAggregates();
        }

        return solrQuery;
    }


    /**
     * Create a SolrInputDocument containing the changed values for the fields.
     *
     * @return              A SolrInputDocument that can be indexed by a SolrServer object.
     */
    public SolrInputDocument getSolrDocumentForUpdate() {
        Map<String, Map<String, Object>> updateFields =  this.translator.getContext().getUpdateFields();
        SolrInputDocument document = new SolrInputDocument();
        String key;
        Object value;

        for (Map.Entry<String, Map<String, Object>> field : updateFields.entrySet()) {
            key = field.getKey();
            value = tryConvertPrimitiveArray(field.getValue().get("set"));

            //check if the field to be added is a list or an array and add it as a multi-valued field
            if (value instanceof List) {
                for (final Object e : (List) value) {
                    document.addField(key, new HashMap<String,Object>() { { put("set", e); }});
                }
            } else if (value instanceof Object[]) {
                for (final Object e : (Object[]) value) {
                    document.addField(key, new HashMap<String,Object>() { { put("set", e); }});
                }
            } else {
                document.addField(field.getKey(), field.getValue());
            }
        }
        return document;
    }

    /**
     *  If the value is a primitive array, cast each element to its corresponding wrapper class and re-create the
     *  array.
     * @param arr           A new value for a Solr field.
     * @return              If arr is a primitive array, returns the corresponding Object array. If not, simply return
     *                      the input.
     */
    private Object tryConvertPrimitiveArray(Object arr) {
        Object result;
        if (arr instanceof byte[]) {
            result = ArrayUtils.toObject((byte[]) arr);
        } else if (arr instanceof short[]) {
            result = ArrayUtils.toObject((short[]) arr);
        } else if (arr instanceof int[]) {
            result = ArrayUtils.toObject((int[]) arr);
        } else if (arr instanceof long[]) {
            result = ArrayUtils.toObject((long[]) arr);
        } else if (arr instanceof float[]) {
            result = ArrayUtils.toObject((float[]) arr);
        } else if (arr instanceof double[]) {
            result = ArrayUtils.toObject((double[]) arr);
        } else if (arr instanceof boolean[]) {
            result = ArrayUtils.toObject((boolean[]) arr);
        } else {
            result = arr;
        }
        return result;
    }
}
