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

    /**
     * Execute the current query and get first entity.
     * @param query             A query that needs to be executed
     * @param <T>               The type of entity returned by the query.
     * @return                  The first T object in the query result.
     */
    <T> T getFirst(Query query);

    /**
     * Execute the current query and get the first row from the result list.
     * @param query             A query that needs to be executed
     * @return                  The first ResultRow object in the query result list.
     */
    ResultRow getFirstRow(Query query);

    /**
     * Execute the current delete query.
     * @param query             A delete query.
     */
    public void executeDelete(Query query);


    /**
     * Execute the current update query.
     * @param query     An update query.
     */
    void executeUpdate(Query query);

    /**
     * Execute the current query and return the number of results.
     * @param query             A query that needs to be executed.
     * @return                  The number of results
     */
    public long count(Query query);

}
