package org.ut.biolab.medsavant.shared.query;

import java.util.List;

/**
 * The execution of query should be delegated to implementations of this interface.
 */
public interface QueryExecutor {

    /**
     * Execute the query.
     * @param query             A query that needs to be executed.
     * @param <T>               The type T that needs to be returned.
     * @return                  A list of objects of type T
     */
    <T>List<T> execute(Query query);

    /**
     * Execute the current query and return a list of the type ResultRow, each containing
     * a subset of the entity fields.
     * @param query             A query that needs to be executed.
     * @return                  A list of ResultRow objects.
     */
    public List<ResultRow> executeForRows(Query query);


}
