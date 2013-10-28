/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shared.db.shard.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.shared.util.SQLUtils;

/**
 * A Factory class that aggregates multiple result sets from aggregate queries, if necessary
 * @author mfiume
 */
public class ShardedResultSetFactory {

    /**
     * For SELECT queries
     * @param resultSets
     * @return
     * @throws SQLException
     */
    public static ResultSet ShardedSelectQueryResultSet(List<ResultSet> resultSets) throws SQLException {
        return new ShardedResultSet(resultSets);
    }

    /**
     * For COUNT(*) queries, with no GROUP BY clause (all result sets must have at most 1 row)
     * @param resultSets
     * @param countColumn
     * @return
     * @throws SQLException
     */
    public static ResultSet ShardedCountQueryResultSet(List<ResultSet> resultSets, int countColumn) throws SQLException {

        // verify that all result sets have no more than 1 row
        for (ResultSet s : resultSets) {
            if (SQLUtils.resultHasMoreThanRows(s,1)) {
                throw new SQLException("Count query has more than 1 result");
            }
        }

        // get the first rs (we'll update the count field in this one)
        ResultSet mainRs = resultSets.get(0);
        mainRs.next();

        // increment the count column in the first rs with the values in the remaining ones
        for (int i = 1; i < resultSets.size(); i++) {

            if (resultSets.get(i).next()) {
                // where the counting occurs
                mainRs.updateInt(countColumn, mainRs.getInt(countColumn) + resultSets.get(i).getInt(countColumn));
            }

            resultSets.get(i).beforeFirst();
        }

        // reset the cursor
        mainRs.beforeFirst();

        // make a list, and return a sharded result set
        List<ResultSet> sets = new ArrayList<ResultSet>();
        sets.add(mainRs);

        return new ShardedResultSet(sets);
    }

}
