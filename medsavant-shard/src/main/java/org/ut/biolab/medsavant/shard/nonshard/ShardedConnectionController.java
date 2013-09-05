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
package org.ut.biolab.medsavant.shard.nonshard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hibernate.shards.cfg.ShardConfiguration;
import org.ut.biolab.medsavant.shard.variant.ShardedSessionManager;

/**
 * Controller for connections to individual shards.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedConnectionController {

    private static final String CREATE_DB_QUERY = "CREATE DATABASE IF NOT EXISTS ";
    private static final String DROP_DB_QUERY = "DROP DATABASE IF EXISTS ";

    /**
     * Executes a query on a single shard, blocking call.
     * 
     * @param shardId
     *            id of the shard
     * @param query
     *            query to execute
     * @return result of the query (first result)
     */
    public static Object executeQueryOnShard(int shardId, String query) {
        ShardConfiguration config = ShardedSessionManager.getConfig(shardId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> worker = new ShardQueryExecutor(config, query, QueryType.SELECT);
        Future<Object> future = executor.submit(worker);

        Object res = null;
        try {
            res = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        return res;
    }

    /**
     * Executes a query on a single shard, not expecting any return value
     * (typically an update). Non-blocking call.
     * 
     * @param shardId
     *            id of the shard to query
     * @param query
     *            query to execute
     */
    public static void executeQueryWithoutResultOnShard(int shardId, String query) {
        ShardConfiguration config = ShardedSessionManager.getConfig(shardId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> worker = new ShardQueryExecutor(config, query, QueryType.SELECT_WITHOUT_RESULT);
        executor.submit(worker);

        executor.shutdown();
    }

    /**
     * Executes an update on a single shard, not expecting any return value
     * (typically an update). Non-blocking call.
     * 
     * @param shardId
     *            id of the shard to query
     * @param query
     *            query to execute
     */
    public static void executeUpdateOnShard(int shardId, String query) {
        ShardConfiguration config = ShardedSessionManager.getConfig(shardId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> worker = new ShardQueryExecutor(config, query, QueryType.UPDATE);
        executor.submit(worker);

        executor.shutdown();
    }

    private static String instantiateQuery(String template, int value) {
        return String.format(template, value);
    }

    /**
     * Execute a query on all shards. Blocking call, waits for results.
     * 
     * @param query
     *            query to execute
     * @param shardIdAsParam
     *            true if shard ID should be inserted into the query
     * @return results
     */
    public static Object executeQueryOnAllShards(String query, boolean shardIdAsParam) {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        Set<Future<Object>> tempResults = new HashSet<Future<Object>>();
        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, shardIdAsParam ? instantiateQuery(query, i) : query, QueryType.SELECT);
            Future<Object> future = executor.submit(worker);
            tempResults.add(future);
        }

        // collect results
        List<Object> finalResults = new ArrayList<Object>();
        for (Future<Object> f : tempResults) {
            try {
                finalResults.add(f.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        return finalResults;
    }

    /**
     * Executes a query on all shards without waiting for the results.
     * Non-blocking call.
     * 
     * @param query
     *            query to execute
     * @param shardIdAsParam
     *            true if shard ID should be inserted into the query
     */
    public static void executeQueryWithoutResultOnAllShards(String query, boolean shardIdAsParam) {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, shardIdAsParam ? instantiateQuery(query, i) : query, QueryType.SELECT_WITHOUT_RESULT);
            executor.submit(worker);
        }

        executor.shutdown();
    }

    /**
     * Executes an update on all shards without waiting for the results.
     * Non-blocking call.
     * 
     * @param query
     *            query to execute
     * @param shardIdAsParam
     *            true if shard ID should be inserted into the query
     */
    public static void executeUpdateOnAllShards(String query, boolean shardIdAsParam) {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, shardIdAsParam ? instantiateQuery(query, i) : query, QueryType.UPDATE);
            executor.submit(worker);
        }

        executor.shutdown();
    }

    /**
     * Creates databases for shards if they don't exist.
     */
    public static void createShards() {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, CREATE_DB_QUERY + ShardConfigurationUtil.getDbForShard(i), QueryType.DB);
            executor.submit(worker);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            // wait until everything is done
        }
    }

    /**
     * Deletes databases of shards.
     */
    public static void dropShards() {
        int shardNo = ShardedSessionManager.getShardNo();
        ExecutorService executor = Executors.newFixedThreadPool(shardNo);

        for (int i = 0; i < shardNo; i++) {
            ShardConfiguration config = ShardedSessionManager.getConfig(i);
            Callable<Object> worker = new ShardQueryExecutor(config, DROP_DB_QUERY + ShardConfigurationUtil.getDbForShard(i), QueryType.DB);
            executor.submit(worker);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            // wait until everything is done
        }
    }
}
