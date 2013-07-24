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

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.ShardStrategy;
import org.hibernate.shards.strategy.ShardStrategyFactory;
import org.hibernate.shards.strategy.ShardStrategyImpl;
import org.hibernate.shards.strategy.access.ParallelShardAccessStrategy;
import org.hibernate.shards.strategy.access.ShardAccessStrategy;
import org.hibernate.shards.strategy.resolution.ShardResolutionStrategy;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

/**
 * Position-based shard strategy factory.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class PositionShardStrategyFactory implements ShardStrategyFactory {

    private Long maxPos;
    private Integer shardNo;
    private ThreadPoolExecutor exec;

    public PositionShardStrategyFactory(Long maxPos, Integer shardNo, ThreadPoolExecutor exec) {
        this.maxPos = maxPos;
        this.shardNo = shardNo;
        this.exec = exec;
    }

    @Override
    public ShardStrategy newShardStrategy(List<ShardId> shardIds) {
        ShardSelectionStrategy pss = new VariantShardSelectionStrategy(maxPos, shardNo);
        ShardResolutionStrategy prs = new VariantShardResolutionStrategy(maxPos, shardNo);
        ShardAccessStrategy pas = new ParallelShardAccessStrategy(exec);

        return new ShardStrategyImpl(pss, prs, pas);
    }
}
