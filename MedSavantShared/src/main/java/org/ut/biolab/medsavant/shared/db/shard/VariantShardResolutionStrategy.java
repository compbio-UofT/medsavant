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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.resolution.AllShardsShardResolutionStrategy;
import org.hibernate.shards.strategy.selection.ShardResolutionStrategyData;

/**
 * Mechanism determining the set of shards on which an object with a given id
 * might reside.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantShardResolutionStrategy extends AllShardsShardResolutionStrategy {

    private ShardSelector<Long> shardSelector;

    public VariantShardResolutionStrategy(List<ShardId> shardIds) {
        super(shardIds);
    }

    public VariantShardResolutionStrategy(Long maxPos, Integer shardNo) {
        super(getShardList(shardNo));
        shardSelector = new PositionShardSelector(maxPos, shardNo);
    }

    private static List<ShardId> getShardList(int shardNo) {
        List<ShardId> res = new ArrayList<ShardId>();
        for (int i = 0; i < shardNo; i++) {
            res.add(new ShardId(i));
        }
        return res;
    }

    public List<ShardId> selectShardIdsFromShardResolutionStrategyData(ShardResolutionStrategyData srsd) {
        if (srsd.getEntityName().equals(Variant.class.getName())) {
            List<ShardId> relevantShards = new ArrayList<ShardId>();
            relevantShards.add(shardSelector.getShard((Long) srsd.getId()));
            return relevantShards;
        }
        return super.selectShardIdsFromShardResolutionStrategyData(srsd);
    }

}
