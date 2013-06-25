package org.ut.biolab.medsavant.server.solr;

import java.util.*;

/**
 *  Convenient class for constructing a Solr query.
 *
 */
public class SimpleSolrQuery {

    private static final String QUERY_TERMS_DELIMITER = "q";
    private static final String FILTER_QUERY_TERMS_DELIMITER = "fq";

    private Map<String, String> queryTerms;
    private Map<String, String> filterQueryTerms;

    private String operatorQueryTerms = " AND ";
    private String operatorFilterQueryTerms = " AND ";

    public SimpleSolrQuery() {
        queryTerms = new HashMap<String, String>();
        filterQueryTerms = new HashMap<String, String>();
    }

    public String constructQuerySectionFromTerms(String queryToken, String termJoinOperator, Map<String, String> terms) {

        StringBuilder sb = new StringBuilder();


        Iterator<Map.Entry<String,String>> iterator = terms.entrySet().iterator();

        if (iterator.hasNext()) {
            sb.append(queryToken + "=");

            Map.Entry<String, String> term = iterator.next();

            String currentTermString = term.getKey() + ":" + term.getValue();
            sb.append(currentTermString);

            while (iterator.hasNext()) {
                term = iterator.next();
                currentTermString = term.getKey() + ":" + term.getValue();
                sb.append(termJoinOperator);
                sb.append(currentTermString);
            }

        }

        return sb.toString();
    }


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

    public String getNormalQuery() {
        return constructQuerySectionFromTerms(QUERY_TERMS_DELIMITER,operatorQueryTerms, queryTerms);
    }

    public String getFilterQuery() {
        return constructQuerySectionFromTerms(FILTER_QUERY_TERMS_DELIMITER, operatorFilterQueryTerms,  filterQueryTerms);
    }

    public void addQueryTerm(String term, String value) {
        queryTerms.put(term, value);
    }

    public void addFilterQueryTerm(String term, String value) {
        filterQueryTerms.put(term, value);
    }

    public void clearQueryTerms() {
        queryTerms.clear();
    }

    public void clearFilterQueryTerms() {
        filterQueryTerms.clear();
    }

    public String constructQuery() {

        return null;
    }


    public String getOperatorQueryTerms() {
        return operatorQueryTerms;
    }

    public void setOperatorQueryTerms(String operatorQueryTerms) {
        this.operatorQueryTerms = operatorQueryTerms;
    }

    public String getOperatorFilterQueryTerms() {
        return operatorFilterQueryTerms;
    }

    public void setOperatorFilterQueryTerms(String operatorFilterQueryTerms) {
        this.operatorFilterQueryTerms = operatorFilterQueryTerms;
    }
}
