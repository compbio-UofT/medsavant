/**
 * Copyright (C) 2007 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.hibernate.shards.strategy.exit;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Projection;
import org.hibernate.criterion.SQLProjection;

/**
 * Exit operation for sql projections.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class SQLGroupExitOperation implements ProjectionExitOperation {

    private final Projection proj;
    private Map<Integer, SupportedAggregations> ags;
    private List<Integer> keyIndices;

    public SQLGroupExitOperation(final SQLProjection projection) {
        this.proj = projection;
        ags = AggregateGroupUtils.getAggregations(projection);
        keyIndices = AggregateGroupUtils.getGroupIndices(projection);
    }

    @Override
    public List<Object> apply(final List<Object> results) {

        final List<Object> nonNullResults = ExitOperationUtils.getNonNullList(results);

        if (nonNullResults.size() == 0) {
            return Collections.singletonList(null);
        } else {
            return AggregateGroupUtils.collide(nonNullResults, ags, keyIndices);
        }
    }
}
