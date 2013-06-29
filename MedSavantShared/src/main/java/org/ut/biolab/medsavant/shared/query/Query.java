package org.ut.biolab.medsavant.shared.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for the queries, implementation agnostic.
 */
public interface Query {


    /**
     * Execute the current query and return a list of the type T objects.
     * @param <T>               The type of entity returned by the query.
     * @return                  A list of type T objects
     * @throws QueryException
     */
    <T> List<T> execute() throws QueryException;

    /**
     * Bind a paramter value to the named parameter paramName in the query.
     * @param parameterName             The name of the parameter, appears as :parameterName in the query.
     * @param value                     The object corresponding to the parameter.
     * @return                          The query.
     * @throws IllegalArgumentException If the parameter name does not corresponding to a parameter in the query.
     */
    public Query setParameter(String parameterName, Object value);

    /**
     * Get the parameter objects associated with this query.
     *
     * @return      The set of parameter objects associated with this query.
     */
    public Collection<Object> getParameters();

    /**
     * Get the names of the parameters associated with this query.
     * @return      The set of parameters associated with this query.
     */
    public Set<String> getParameterNames();

    /**
     *
     * @return
     */
    public Map<String, Object> getParameterMap();

    /**
     * Specifies where in the result set should the returned results start.
     * @param start     start of the results
     * @return          The query
     */
    Query setStart(int start);

    /**
     * @return          the start of the result list.
     */
    public int getStart();

    /**
     * Specifies the maximum number of results returned by this query. Similar to the SQL limit.
     * @param limit      The maximum number of results to be returned
     * @return           The query
     */
    Query setLimit(int limit);

    /**
     * @return          the limit of the result list.
     */
    public int getLimit();

    /**
     * Set an array of sorts for the current query.
     * @param sorts     The sorts for the query
     * @return          The query
     */
    public Query setSorts(QuerySort[] sorts);

    /**
     * Return the sorts for the current query
     * @return          The sorts for this query
     */
    public QuerySort[] getSorts();

    /**
     * Add a new sort to the query
     * @param sort      The new Sort
     * @return          The query
     */
    public Query addSort(QuerySort sort);

    /**
     * Add an array of query sorts to the query.
     * @param sorts     The new sorts
     * @return          The query
     */
    public Query addSorts(QuerySort[] sorts);

}
