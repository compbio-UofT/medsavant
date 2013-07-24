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

import org.hibernate.Criteria;
import org.hibernate.criterion.AvgProjection;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.RowCountProjection;
import org.hibernate.shards.Shard;
import org.hibernate.shards.criteria.CriteriaFactory;
import org.hibernate.shards.criteria.CriteriaId;
import org.hibernate.shards.criteria.ExitOperationsCriteriaCollector;
import org.hibernate.shards.criteria.ShardedCriteriaImpl;
import org.hibernate.shards.strategy.access.ShardAccessStrategy;

/**
 * Extension of ShardedCriteriaImpl from Hibernate shards fixing some of their
 * bugs and adding extra functionality.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class EnhancedShardedCriteriaImpl extends ShardedCriteriaImpl {

    private ExitOperationsCriteriaCollector criteriaCollector;

    public EnhancedShardedCriteriaImpl(CriteriaId criteriaId, List<Shard> shards, CriteriaFactory criteriaFactory, ShardAccessStrategy shardAccessStrategy) {
        super(criteriaId, shards, criteriaFactory, shardAccessStrategy);
    }

    @Override
    public Criteria setProjection(Projection projection) {
        criteriaCollector.addProjection(projection);
        if (projection instanceof AvgProjection) {
            super.setProjection(projection);
        } else if (projection instanceof RowCountProjection) {
            criteriaCollector.addProjection(projection);
        }

        return this;
    }

}
