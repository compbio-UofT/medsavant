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

import org.hibernate.shards.cfg.ShardConfiguration;
import org.ut.biolab.medsavant.shard.variant.ShardedSessionManager;

/**
 * Helpers for manipulation of shard configuration.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardConfigurationUtil {

    /**
     * Retrieves connection URL to a certain shard.
     * 
     * @param shardId
     *            ID of a shard
     * @return connection URL
     */
    public static String getConnectionUrlForShard(int shardId) {
        ShardConfiguration s = ShardedSessionManager.getConfig(shardId);

        return (s == null) ? null : s.getShardUrl();
    }

    /**
     * Retrieves the database name for a shard.
     * 
     * @param shardId
     *            ID of the shard
     * @return database name
     */
    public static String getDbForShard(int shardId) {
        String url = getConnectionUrlForShard(shardId);

        String db = null;
        if (url != null) {
            db = url.substring(url.lastIndexOf("/".charAt(0)) + 1);
        }

        return db;
    }

    /**
     * Retrieves the server URL for a shard.
     * 
     * @param shardId
     *            ID of the shard
     * @return server URL
     */
    public static String getServerForShard(int shardId) {
        String url = getConnectionUrlForShard(shardId);

        String server = null;
        if (url != null) {
            server = url.substring(0, url.lastIndexOf("/".charAt(0)) + 1);
        }

        return server;
    }
}
