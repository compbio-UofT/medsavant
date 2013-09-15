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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.AggregateProjection;
import org.hibernate.criterion.Projection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exit operation for aggregate functions except for count distinct.
 * 
 * @author Maulik Shah
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 */
public class AggregateExitOperation implements ProjectionExitOperation {

    private final SupportedAggregations aggregate;

    private final String fieldName;
    private final Projection proj;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private enum SupportedAggregations {

        SUM("sum"), MIN("min"), MAX("max"), COUNT("count"), DISTINCT_COUNT("distinct count");

        private final String aggregate;

        private SupportedAggregations(final String s) {
            this.aggregate = s;
        }

        public String getAggregate() {
            return aggregate;
        }
    }

    public AggregateExitOperation(final AggregateProjection projection) {
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

        /**
         * aviadl@sentrigo.com (Aviad Lichtenstdadt) added here the return list
         * with one element. As this is what Hibernate does in cases of empty
         * result set
         * 
         */
        switch (aggregate) {
        case MAX:
            if (nonNullResults.size() == 0) {
                return Collections.singletonList(null);
            } else {
                return Collections.singletonList((Object) Collections.max(ExitOperationUtils.getComparableList(nonNullResults)));
            }
        case MIN:
            if (nonNullResults.size() == 0) {
                return Collections.singletonList(null);
            } else {
                return Collections.singletonList((Object) Collections.min(ExitOperationUtils.getComparableList(nonNullResults)));
            }
        case SUM:
            return Collections.<Object> singletonList(getSum(nonNullResults, fieldName));
        case COUNT:
            return Collections.<Object> singletonList(getSum(results));
        case DISTINCT_COUNT:
            return AggregateGroupUtils.getDistinctCount(nonNullResults);
        default:
            log.error("Aggregation Projection is unsupported: " + aggregate);
            throw new UnsupportedOperationException("Aggregation Projection is unsupported: " + aggregate);
        }
    }

    private BigDecimal getSum(final List<Object> results, final String fieldName) {
        BigDecimal sum = BigDecimal.ZERO;
        for (final Object obj : results) {
            if (obj instanceof Number) {
                final Number num = (Number) obj;
                sum = sum.add(new BigDecimal(num.toString()));
            } else {
                final Number num = getNumber(obj, fieldName);
                sum = sum.add(new BigDecimal(num.toString()));
            }
        }
        return sum;
    }

    private BigDecimal getSum(List<Object> results) {
        BigDecimal sum = new BigDecimal(0.0);
        for (Object obj : results) {
            Number num = (Number) obj;
            sum = sum.add(new BigDecimal(num.toString()));
        }
        return sum;
    }

    private int getDistinctSum(List<Object> results) {
        Set<Object> uniqueResult = new HashSet<Object>(results);
        return uniqueResult.size();
    }

    private Number getNumber(final Object obj, final String fieldName) {
        return (Number) ExitOperationUtils.getPropertyValue(obj, fieldName);
    }
}
