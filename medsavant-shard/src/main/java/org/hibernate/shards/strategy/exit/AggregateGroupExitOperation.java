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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.AggregateProjection;
import org.hibernate.criterion.Projection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exit operation for queries with aggregate operations and group by.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class AggregateGroupExitOperation implements ProjectionExitOperation {

    private final SupportedAggregations aggregate;

    private final String fieldName;
    private final Projection proj;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public AggregateGroupExitOperation(final AggregateProjection projection) {
        /**
         * an aggregateProjection's toString returns min( ..., max( ..., sum(
         * ..., or avg( ... we just care about the name of the function which
         * happens to be before the first left parenthesis
         */
        this.proj = projection;
        final String projectionAsString = projection.toString();
        final String aggregateName = projectionAsString.substring(0, projectionAsString.indexOf("("));
        this.fieldName = projectionAsString.substring(projectionAsString.indexOf("(") + 1, projectionAsString.indexOf(")"));
        try {
            this.aggregate = SupportedAggregations.valueOf(aggregateName.replace(" ", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Use of unsupported aggregate: " + aggregateName);
            throw e;
        }
    }

    @Override
    public List<Object> apply(final List<Object> results) {

        final List<Object> nonNullResults = ExitOperationUtils.getNonNullList(results);

        if (nonNullResults.size() == 0) {
            return Collections.singletonList(null);
        } else {
            Map<Integer, SupportedAggregations> ag = new LinkedHashMap<Integer, SupportedAggregations>();
            ag.put(0, aggregate);
            List<Integer> keyIndeces = new ArrayList<Integer>();
            keyIndeces.add(1);

            return AggregateGroupUtils.collide(nonNullResults, ag, keyIndeces);
        }
    }
}
