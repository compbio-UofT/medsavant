package org.ut.biolab.medsavant.shared.query;

import java.util.List;

/**
 * The execution of query should be delegated to implementations of this interface.
 */
public interface QueryExecutor {

    /**
     * Execute the query.
     * @param query         A query that needs to be execute.
     * @param <T>           The type T that needs to be returned.
     * @return              A list of objects of type T
     */
    <T>List<T> execute(Query query);
}
