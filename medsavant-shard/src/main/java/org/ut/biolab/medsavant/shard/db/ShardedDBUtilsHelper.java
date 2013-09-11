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

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.shards.criteria.ShardedCriteriaImpl;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.ut.biolab.medsavant.shard.mapping.VariantMapping;
import org.ut.biolab.medsavant.shard.variant.ShardedSessionManager;
import org.ut.biolab.medsavant.shard.variant.Variant;
import org.ut.biolab.medsavant.shared.model.Range;

/**
 * Sharded version of the helpers for DBUtils.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedDBUtilsHelper {

    /**
     * Retrieves number of variants.
     * 
     * @param tableName
     *            name of the table of variants
     * @return variant count
     */
    public int getNumRecordsInTable(String tableName) {
        ShardedSessionManager.setTable(tableName);

        Session s = ShardedSessionManager.openSession();

        Criteria c = s.createCriteria(Variant.class).setProjection(Projections.count(VariantMapping.getIdColumn()));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();

        ShardedSessionManager.closeSession(s);

        return res;
    }

    /**
     * Retrieves extreme values for a given column.
     * 
     * @param tableName
     *            name of the table of variants
     * @param colName
     *            column name
     * @return min and max values for the given column
     */
    public Range getExtremeValuesForColumn(String tableName, String colName) {
        ShardedSessionManager.setTable(tableName);

        Session s = ShardedSessionManager.openSession();

        Object oMin = s.createCriteria(Variant.class).setProjection(Projections.min(colName)).list().get(0);
        Object oMax = s.createCriteria(Variant.class).setProjection(Projections.max(colName)).list().get(0);

        double min;
        if (oMin instanceof Integer) {
            min = (double) (Integer) oMin;
        } else if (oMin instanceof Float) {
            min = (double) (Float) oMin;
        } else if (oMin instanceof Double) {
            min = (Double) oMin;
        } else {
            throw new ClassCastException("Min is not double");
        }
        double max;
        if (oMax instanceof Integer) {
            max = (double) (Integer) oMax;
        } else if (oMax instanceof Float) {
            max = (double) (Float) oMax;
        } else if (oMax instanceof Double) {
            max = (Double) oMax;
        } else {
            throw new ClassCastException("Max is not double");
        }

        ShardedSessionManager.closeSession(s);

        return new Range(min, max);
    }

    /**
     * Retrieves distinct values for a column.
     * 
     * @param tableName
     *            name of the table of variants
     * @param colName
     *            column name
     * @param limit
     *            limit for results, <0 if no limit needed
     * @return list of distinct values
     */
    public List<Object> getDistinctValuesForColumn(String tableName, String colName, int limit) {
        ShardedSessionManager.setTable(tableName);

        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setProjection(Projections.sqlGroupProjection(colName + " as value", "value", new String[] { "value" },
                new Type[] { new StringType() }));
        if (limit >= 0) {
            c.setFetchSize(limit).setMaxResults(limit);
        }
        List<Object> res = c.list();

        ShardedSessionManager.closeSession(s);

        return res;
    }
}
