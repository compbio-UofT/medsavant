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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Utils for aggregation with group by.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class AggregateGroupUtils {

    /**
     * Aggregates the results across shards. Can handle a single aggregation and
     * a single group by criterion, which it expects to be on the second
     * position in the result.
     * 
     * @param results
     * @param ag
     * @return
     */
    public static List<Object> collide(List<Object> results, SupportedAggregations ag, int keyIndex, int valueIndex) {
        // use LinkedHashMap to preserve ordering
        LinkedHashMap<Comparable<Object>, BigDecimal> res = new LinkedHashMap<Comparable<Object>, BigDecimal>();

        for (Object pair : results) {
            Comparable<Object> key = (Comparable<Object>) ((Object[]) pair)[keyIndex];
            // BigDecimal can handle all numbers
            BigDecimal value = new BigDecimal(((Number) ((Object[]) pair)[valueIndex]).toString());

            if (res.containsKey(key)) {
                switch (ag) {
                case MAX:
                    res.put(key, value.max(res.get(key)));
                    break;
                case MIN:
                    res.put(key, value.min(res.get(key)));
                    break;
                case SUM:
                    res.put(key, value.add(res.get(key)));
                    break;
                case COUNT:
                    res.put(key, value.add(res.get(key)));
                    break;
                case DISTINCT_COUNT:
                    res.put(key, value.add(res.get(key)));
                    break;
                case NONE:
                    // no aggregation, keep data from an arbitrary shard
                    break;
                default:
                    // unsupported aggregation
                    throw new UnsupportedOperationException("Aggregation is unsupported: " + ag);
                }
            } else {
                res.put(key, value);
            }
        }

        List<Object> out = new ArrayList<Object>();
        for (Entry e : res.entrySet()) {
            out.add(new Object[] { e.getKey(), e.getValue() });
        }
        return out;
    }
}
