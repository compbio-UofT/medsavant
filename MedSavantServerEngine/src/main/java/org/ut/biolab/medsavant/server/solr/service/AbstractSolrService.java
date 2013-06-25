package org.ut.biolab.medsavant.server.solr.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.util.NamedList;
import org.ut.biolab.medsavant.server.solr.exception.InitializationException;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSolrService {

    private static final Log LOG = LogFactory.getLog(AbstractSolrService.class);

    /**
     * Delimiter between the field name and the searched value used in the Lucene query language.
     */
    protected static final String FIELD_VALUE_SEPARATOR = ":";

    /**
     * The name of the ID field.
     */
    protected static final String ID_FIELD_NAME = "id";

    /** The Solr server instance used. */
    protected SolrServer server;


    public void initialize() throws InitializationException {

        try {
            this.server = new HttpSolrServer("http://localhost:8983/solr/" + this.getName() + "/");

        } catch (RuntimeException ex) {
            LOG.error("Invalid URL specified for the Solr server: {}",ex);
        }
    }

    /**
     * Get the name of the Solr "core" to be used by this service instance.
     *
     * @return the simple core name
     */
    protected abstract String getName();

    /**
     * Search for terms matching the specified query, using the Lucene query language.
     *
     * @param queryParameters a Lucene query
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final String queryParameters) {
        MapSolrParams params = new MapSolrParams(getSolrQuery(queryParameters, -1, 0));
        return search(params);
    }

    /**
     * Search for terms matching the specified query, using the Lucene query language.
     *
     * @param queryParameters a Lucene query
     * @param sort sorting criteria
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final String queryParameters, final String sort) {
        MapSolrParams params = new MapSolrParams(getSolrQuery(queryParameters, sort, -1, 0));
        return search(params);
    }

    /**
     * Search for terms matching the specified query, using the Lucene query language.
     *
     * @param queryParameters a Lucene query
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final String queryParameters, final int rows, final int start) {
        MapSolrParams params = new MapSolrParams(getSolrQuery(queryParameters, rows, start));
        return search(params);
    }

    /**
     * Search for terms matching the specified query, using the Lucene query language.
     *
     * @param queryParameters a Lucene query
     * @param sort sorting criteria
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final String queryParameters, final String sort, final int rows, final int start) {
        MapSolrParams params = new MapSolrParams(getSolrQuery(queryParameters, sort, rows, start));
        return search(params);
    }

    /**
     * Search for terms matching the specified query, where the query is specified as a map of field name and keywords.
     *
     * @param fieldValues the map of values to search for, where each key is the name of an indexed field and the value
     *            is the keywords to match for that field
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final Map<String, String> fieldValues) {
        return search(fieldValues, -1, 0);
    }

    /**
     * Search for terms matching the specified query, where the query is specified as a map of field name and keywords.
     *
     * @param fieldValues the map of values to search for, where each key is the name of an indexed field and the value
     *            is the keywords to match for that field
     * @param sort sorting criteria
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final Map<String, String> fieldValues, String sort){
        return search(fieldValues, sort, -1, 0);
    }

    /**
     * Search for terms matching the specified query, where the query is specified as a map of field name and keywords.
     *
     * @param fieldValues the map of values to search for, where each key is the name of an indexed field and the value
     *            is the keywords to match for that field
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final Map<String, String> fieldValues, final int rows, final int start) {
        MapSolrParams params = new MapSolrParams(getSolrQuery(fieldValues, rows, start));
        return search(params);
    }

    /**
     * Search for terms matching the specified query, where the query is specified as a map of field name and keywords.
     *
     * @param fieldValues the map of values to search for, where each key is the name of an indexed field and the value
     *            is the keywords to match for that field
     * @param sort sorting criteria
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList search(final Map<String, String> fieldValues, final String sort, final int rows,
                                   final int start) {
        MapSolrParams params = new MapSolrParams(getSolrQuery(fieldValues, sort, rows, start));
        return search(params);
    }

    /**
     * Advanced search using custom search parameters. At least the {@code q} parameter should be set, but any other
     * parameters supported by Solr can be specified in this map.
     *
     * @param searchParameters a map of parameters, the keys should be parameters that Solr understands
     * @return the list of matching documents, empty if there are no matching terms
     */
    public SolrDocumentList customSearch(final Map<String, String> searchParameters) {
        MapSolrParams params = new MapSolrParams(searchParameters);
        return search(params);
    }

    /**
     * Get the top hit corresponding to the specified query.
     *
     * @param fieldValues the map of values to search for, where each key is the name of an indexed field and the value
     *            is the keywords to match for that field
     * @return the top matching document, {@code null} if there were no matches at all
     * @see #search(Map)
     */
    public SolrDocument get(final Map<String, String> fieldValues)     {
        SolrDocument result = null;

        SolrDocumentList all = search(fieldValues, 1, 0);
        if (all != null && !all.isEmpty()) {
            result = all.get(0);
        }
        return result;
    }

    /**
     * Get the document corresponding to the specified term identifier.
     *
     * @param id the identifier to search for, in the {@code HP:1234567} format (for HPO), or {@code 123456} (for OMIM)
     * @return the matching document, if one was found, or {@code null} otherwise
     */
    public SolrDocument get(final String id) {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put(ID_FIELD_NAME, id);
        return get(queryParameters);
    }

    /**
     * Perform a search, falling back on the suggested spellchecked query if the original query fails to return any
     * results.
     *
     * @param params the Solr parameters to use, should contain at least a value for the "q" parameter; use
     *            {@link #getSolrQuery(String, int, int)} to get the proper parameter expected by this method
     * @return the list of matching documents, empty if there are no matching terms
     */
    private SolrDocumentList search(MapSolrParams params) {

        NamedList<Object> newParams = params.toNamedList();

        SolrDocumentList results = null;
        try {
            LOG.debug("Querying Solr with params: " + params.toString());
            QueryResponse response = this.server.query(MapSolrParams.toSolrParams(newParams));
            results = response.getResults();
        } catch (SolrServerException e) {
            LOG.error("Error querying Solr", e);
        }

        return results;
    }

    /**
     * Create Solr parameters based on the specified search terms. More specifically, concatenates the specified field
     * values into a Lucene query which is used as the "q" parameter, and adds parameters for requesting a spellcheck
     * result.
     *
     * @param fieldValues the map of values to search for, where each key is the name of an indexed field and the value
     *            is the keywords to match for that field
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return a map of Solr query parameter ready to be used for constructing a {@link MapSolrParams} object
     */
    private Map<String, String> getSolrQuery(Map<String, String> fieldValues, int rows, int start) {
        return getSolrQuery(fieldValues, "", rows, start);
    }

    /**
     * Create Solr parameters based on the specified search terms. More specifically, concatenates the specified field
     * values into a Lucene query which is used as the "q" parameter, and adds parameters for requesting a spellcheck
     * result.
     *
     * @param fieldValues the map of values to search for, where each key is the name of an indexed field and the value
     *            is the keywords to match for that field
     * @param sort the sort criteria ("fiel_name order')
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return a map of Solr query parameter ready to be used for constructing a {@link MapSolrParams} object
     */
    private Map<String, String> getSolrQuery(Map<String, String> fieldValues, String sort, int rows, int start) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> field : fieldValues.entrySet()) {
            String value = field.getValue();
            if (value == null) {
                value = "";
            }
            String[] pieces =
                    value.replaceAll("[^a-zA-Z0-9 :]", " ").replace(FIELD_VALUE_SEPARATOR, "\\" + FIELD_VALUE_SEPARATOR)
                            .trim().split("\\s+");
            for (String val : pieces) {
                query.append(field.getKey()).append(FIELD_VALUE_SEPARATOR).append(val).append(" ");
            }
        }
        return getSolrQuery(query.toString().trim(), sort, rows, start);
    }

    /**
     * Convert a Lucene query string into a map of Solr parameters. More specifically, places the input query under the
     * "q" parameter, and adds parameters for requesting a spellcheck result.
     *
     * @param query the lucene query string to use
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return a map of Solr query parameter ready to be used for constructing a {@link MapSolrParams} object
     */
    private Map<String, String> getSolrQuery(String query, int rows, int start) {
        return getSolrQuery(query, "", rows, start);
    }

    /**
     * Convert a Lucene query string into a map of Solr parameters. More specifically, places the input query under the
     * "q" parameter, and adds parameters for requesting a spellcheck result.
     *
     * @param query the lucene query string to use
     * @param sort the sort criteria ("fiel_name order')
     * @param rows the number of items to return, or -1 to use the default number of results
     * @param start the number of items to skip, i.e. the index of the first hit to return, 0-based
     * @return a map of Solr query parameter ready to be used for constructing a {@link MapSolrParams} object
     */
    private Map<String, String> getSolrQuery(String query, String sort, int rows, int start) {
        Map<String, String> result = new HashMap<String, String>();
        result.put(CommonParams.START, start + "");

        if (rows > 0) {
            result.put(CommonParams.ROWS, rows + "");
        }

        result.put(CommonParams.Q, query);
        if (StringUtils.isNotBlank(sort)) {
            result.put(CommonParams.SORT, sort);
        }
        return result;
    }
}
