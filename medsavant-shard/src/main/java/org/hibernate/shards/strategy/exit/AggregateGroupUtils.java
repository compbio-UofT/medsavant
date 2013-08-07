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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.criterion.SQLProjection;

/**
 * Utils for aggregation with group by.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class AggregateGroupUtils {

    /**
     * Aggregates the results across shards.
     * 
     * @param results
     *            results to collide
     * @param ag
     *            aggregation function
     * @param keyIndices
     *            indices of columns in group by
     * @return overall result across all shards
     */
    public static List<Object> collide(List<Object> results, Map<Integer, SupportedAggregations> ags, List<Integer> keyIndices) {
        // use LinkedHashMap to preserve ordering
        LinkedHashMap<List<Comparable<Object>>, List<BigDecimal>> res = new LinkedHashMap<List<Comparable<Object>>, List<BigDecimal>>();

        Object[] tuple = new Object[0];
        for (Object r : results) {
            List<Comparable<Object>> keys = new ArrayList<Comparable<Object>>();
            List<BigDecimal> values = new ArrayList<BigDecimal>();
            tuple = (Object[]) r;
            for (int i = 0; i < tuple.length; i++) {
                if (keyIndices.contains(i)) {
                    keys.add((Comparable<Object>) tuple[i]);
                } else {
                    values.add(new BigDecimal(((Number) (tuple)[i]).toString()));
                }
            }

            // handle multiple aggregate functions
            List<BigDecimal> v = null;
            if (res.containsKey(keys)) {
                v = res.get(keys);
                for (Entry<Integer, SupportedAggregations> ag : ags.entrySet()) {
                    switch (ag.getValue()) {
                    case MAX:
                        v.set(ag.getKey(), v.get(ag.getKey()).max(values.get(ag.getKey())));
                        break;
                    case MIN:
                        v.set(ag.getKey(), v.get(ag.getKey()).min(values.get(ag.getKey())));
                        break;
                    case SUM:
                        v.set(ag.getKey(), v.get(ag.getKey()).add(values.get(ag.getKey())));
                        break;
                    case COUNT:
                        v.set(ag.getKey(), v.get(ag.getKey()).add(values.get(ag.getKey())));
                        break;
                    case DISTINCT_COUNT:
                        v.set(ag.getKey(), v.get(ag.getKey()).add(values.get(ag.getKey())));
                        break;
                    case NONE:
                        // no aggregation, keep data from an arbitrary shard
                        break;
                    default:
                        // unsupported aggregation
                        throw new UnsupportedOperationException("Aggregation is unsupported: " + ag);
                    }
                }
                res.put(keys, v);
            } else {
                res.put(keys, values);
            }
        }

        List<Object> out = new ArrayList<Object>();
        for (Entry<List<Comparable<Object>>, List<BigDecimal>> e : res.entrySet()) {
            // construct tuples from keys and values
            Object[] wrap = new Object[tuple.length];
            int usedValues = 0;
            for (int i = 0; i < tuple.length; i++) {
                int pos = keyIndices.indexOf(i);
                if (pos < 0) {
                    wrap[i] = e.getValue().get(usedValues);
                    usedValues++;
                } else {
                    // key
                    wrap[i] = e.getKey().get(pos);
                }
            }

            out.add(wrap);
        }
        return out;
    }

    /**
     * Extracts aggregations from an SQLProjection instance.
     * 
     * @param p
     *            projection
     * @return map of positions of aggregations as a column index and
     *         aggregations
     */
    public static Map<Integer, SupportedAggregations> getAggregations(SQLProjection p) {
        Map<Integer, SupportedAggregations> res = new LinkedHashMap<Integer, SupportedAggregations>();

        String[] selections = p.toString().split(",");
        for (int i = 0; i < selections.length; i++) {
            if (selections[i].toLowerCase().contains(" count(")) {
                res.put(i, SupportedAggregations.COUNT);
            } else if (selections[i].toLowerCase().contains(" max(")) {
                res.put(i, SupportedAggregations.MAX);
            } else if (selections[i].toLowerCase().contains(" min(")) {
                res.put(i, SupportedAggregations.MIN);
            } else if (selections[i].toLowerCase().contains(" sum(")) {
                res.put(i, SupportedAggregations.SUM);
            } else if (selections[i].toLowerCase().contains(" distinct count(")) {
                res.put(i, SupportedAggregations.DISTINCT_COUNT);
            }
        }

        return res;
    }

    /**
     * Extracts positions of columns used in GROUP BY part of an SQL projection.
     * Expects to be those values named value*.
     * 
     * @param p
     *            projection
     * @return list of positions of columns used in GROUP BY
     */
    public static List<Integer> getGroupIndices(SQLProjection p) {
        List<Integer> res = new ArrayList<Integer>();
        String[] selections = p.toString().split("as ");

        for (int i = 1; i < selections.length; i++) {
            if (selections[i].startsWith("value")) {
                res.add(i - 1);
            }
        }

        return res;
    }

    /**
     * Uses grouping to compute correct distinct count.
     * 
     * @param results
     *            list of results from all shards
     * @return collapsed (grouped) list of results
     */
    public static List<Object> getDistinctCount(List<Object> result) {
        Set<String> group = new HashSet<String>();
        for (Object o : result) {
            group.add(o.toString());
        }

        List<Object> res = new ArrayList<Object>();
        res.add(group.size());

        return res;
    }
}
