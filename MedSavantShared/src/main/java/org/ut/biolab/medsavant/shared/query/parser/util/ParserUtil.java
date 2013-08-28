package org.ut.biolab.medsavant.shared.query.parser.util;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.ut.biolab.medsavant.shared.model.solr.FieldMappings;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Misc org.ut.biolab.medsavant.persistence.query.solr.util functionality for parsing JPQL
 */
public class ParserUtil {

    private String addEntityField(String query, String entity) {
        String entityClassName = FieldMappings.getClassName(entity);
        String finalQuery;
        if (query == null) {
            finalQuery = Entity.ENTITY_DISCRIMINANT_FIELD + ":" + entityClassName;
        } else {
            finalQuery = query + " AND " + Entity.ENTITY_DISCRIMINANT_FIELD + ":" + entityClassName;
        }
        return finalQuery;
    }

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
