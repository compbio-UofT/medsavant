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
package org.ut.biolab.medsavant.server.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.ut.biolab.medsavant.server.solr.util.SolrQueryOperator;

import java.util.*;

/**
 *  Convenient class for constructing a Solr query.
 *
 * @author Bogdan Vancea
 */
public class SimpleSolrQuery {

    /**
     * Map holding the terms and term values for the regular "q" query
     */
    private Map<String, String> queryTerms;

    /**
     * Map holding the terms and term values for the filtered "fq" query
     */
    private Map<String, String> filterQueryTerms;

    /**
     * Logical link operator for the "q" terms.
     */
    private SolrQueryOperator operatorQueryTerms = SolrQueryOperator.AND;

    /**
     * Logical link operator for the "fq" terms.
     */
    private SolrQueryOperator operatorFilterQueryTerms =  SolrQueryOperator.AND;


    private static final Log LOG = LogFactory.getLog(SimpleSolrQuery.class);

    /**
     * Construct a new SimpleSolrQuery
     */
    public SimpleSolrQuery() {
        queryTerms = new HashMap<String, String>();
        filterQueryTerms = new HashMap<String, String>();
    }

    /**
     * Create a Lucene query from the map of terms and term values and a join operator.
     * @param termJoinOperator      Logical join operator, either AND or OR.
     * @param terms                 A map containing the fields
     * @return                      Lucene query
     */
    private String constructQuerySectionFromTerms(SolrQueryOperator termJoinOperator, Map<String, String> terms) {
        StringBuilder sb = new StringBuilder();

        Iterator<Map.Entry<String,String>> iterator = terms.entrySet().iterator();

        if (iterator.hasNext()) {
            Map.Entry<String, String> term = iterator.next();

            sb.append(term.getKey() + ":" + ClientUtils.escapeQueryChars(term.getValue()));

            while (iterator.hasNext()) {
                term = iterator.next();
                sb.append(termJoinOperator.nameWithSpace());
                sb.append(term.getKey() + ":" + ClientUtils.escapeQueryChars(term.getValue()));
            }
        }

        return sb.toString();
    }

    /**
     * Construct and return the "q" part of the query.
     * @return  the regular query
     */
    public String getNormalQuery() {
        return constructQuerySectionFromTerms(operatorQueryTerms, queryTerms);
    }

    /**
     * Construct and return the "fq" part of the query.
     * @return  the filtered query
     */
    public String getFilterQuery() {
        return constructQuerySectionFromTerms(operatorFilterQueryTerms,  filterQueryTerms);
    }

    /**
     * Null safe method to add a term and term value to the "q" query.
     * @param term          The term name
     * @param value         The term value
     */
    public void addQueryTerm(String term, String value) {
        if (value != null) {
            queryTerms.put(term, value);
        }
    }

    /**
     * Null safe method to add a term and term value to the "fq" query.
     * @param term          The term name
     * @param value         The term value
     */
    public void addFilterQueryTerm(String term, String value) {
        if (value != null) {
            filterQueryTerms.put(term, value);
        }
    }

    @Deprecated
    public String getFullSolrQuery() {

        String normalQuery = getNormalQuery();

        String filterQuery = getFilterQuery();

        if ("".equals(normalQuery) ) {
            return filterQuery;
        } else if ("".equals(filterQuery)) {
            return normalQuery;
        } else {
            return normalQuery + "&" + filterQuery;
        }
    }

    /**
     * Clear the "q" terms
     */
    public void clearQueryTerms() {
        queryTerms.clear();
    }

    /**
     * Clear the "fq" terms
     */
    public void clearFilterQueryTerms() {
        filterQueryTerms.clear();
    }

    public Map<String, String> getQueryTerms() {
        return queryTerms;
    }

    public void setQueryTerms(Map<String, String> queryTerms) {
        this.queryTerms = queryTerms;
    }

    public Map<String, String> getFilterQueryTerms() {
        return filterQueryTerms;
    }

    public void setFilterQueryTerms(Map<String, String> filterQueryTerms) {
        this.filterQueryTerms = filterQueryTerms;
    }

    public void setOperatorQueryTerms(SolrQueryOperator operatorQueryTerms) {
        this.operatorQueryTerms = operatorQueryTerms;
    }

    /**
     * Construct and return an instance of MapSolrParams corresponding to the "q" and "fq" terms stored.
     * @return      an instance of MapSolrParams
     */
    public SolrParams toSolrParams() {

        Map<String, String> params = new HashMap<String, String>();
        params.put(CommonParams.Q, getNormalQuery());
        params.put(CommonParams.FQ, getFilterQuery());

        MapSolrParams mapSolrParams = new MapSolrParams(params);
        return mapSolrParams;
    }

}