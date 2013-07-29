package org.ut.biolab.medsavant.shared.query.parser.util;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;

/**
 * Misc util functionality for parsing JPQL
 */
public class ParserUtil {

    public static SolrQuery addDefaultQueryParameters(SolrQuery solrQuery) {

        String params = solrQuery.get(CommonParams.Q);

        if (params == null || "".equals(params)) {
            solrQuery.add(CommonParams.Q, "*:*");
        }

        return solrQuery;
    }

    public static boolean greaterThan(String token) {
        token = token.trim();
        return (">".equals(token) || ">=".equals(token)) ? true : false;
    }

    public static boolean lessThan(String token) {
        token = token.trim();
        return ("<".equals(token) || "<=".equals(token)) ? true : false;
    }

    public static boolean equal(String token) {
        token = token.trim();
        return ("=".equals(token)) ? true : false;
    }
}
