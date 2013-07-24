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
package org.ut.biolab.medsavant.shared.db.shard;

import javassist.expr.Instanceof;

import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

/**
 * Mechanism determining the shard on which a new object should be created.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantShardSelectionStrategy implements ShardSelectionStrategy {

    private ShardSelector<Long> shardSelector;

    /**
     * Determine shard based on position.
     */
    public ShardId selectShardIdForNewObject(Object obj) {
        if (!(obj instanceof Long)) {
            throw new IllegalArgumentException(obj.toString());
        }
        
        return shardSelector.getShard((Long) obj);
    }

    public VariantShardSelectionStrategy(Long maxPos, Integer shardNo) {
        shardSelector = new PositionShardSelector(maxPos, shardNo);
    }
}