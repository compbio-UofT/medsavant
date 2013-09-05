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
package org.ut.biolab.medsavant.shard.db;

import org.hibernate.Session;
import org.ut.biolab.medsavant.shard.nonshard.ShardedConnectionController;
import org.ut.biolab.medsavant.shard.variant.ShardedSessionManager;

/**
 * Helper class for setup of a sharded database.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedDatabaseSetupHelper {

    /**
     * Creates database shards.
     */
    public void createDatabase() {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.createShards();

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Removes database shards.
     */
    public void removeDatabase() {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.dropShards();

        ShardedSessionManager.closeSession(session);
    }

    /**
     * Creates variant tables on shards.
     * 
     * @param query
     */
    public void createVariantTables(String query) {
        Session session = ShardedSessionManager.openSession();

        ShardedConnectionController.executeUpdateOnAllShards(query, false);

        ShardedSessionManager.closeSession(session);
    }
}
