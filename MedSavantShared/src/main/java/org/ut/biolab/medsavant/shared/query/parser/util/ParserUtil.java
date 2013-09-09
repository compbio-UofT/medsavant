package org.ut.biolab.medsavant.shared.query.parser.util;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.ut.biolab.medsavant.shared.model.solr.FieldMappings;
import org.ut.biolab.medsavant.shared.query.parser.QueryContext;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Misc org.ut.biolab.medsavant.persistence.query.solr.util functionality for parsing JPQL
 */
public class ParserUtil {

    public static String addEntityField(String query, String entity) {
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

    public static Object getValueToken(String value, QueryContext context) {
        Object parsedValue = null;

        if (isNamedParamter(value) && context.getParameters().containsKey(parseNamedParameter(value))) {
            parsedValue = context.getParameters().get(parseNamedParameter(value));
        } else {
            parsedValue = value;
        }

        return parsedValue;
    }

    public static boolean isNamedParamter(String parameter) {
        return  parameter.startsWith(":") ? true : false;
    }

    public static String parseNamedParameter(String nameParameter) {
        return nameParameter.substring(1).trim();
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
