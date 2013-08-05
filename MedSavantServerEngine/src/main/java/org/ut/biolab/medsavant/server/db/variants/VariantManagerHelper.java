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
package org.ut.biolab.medsavant.server.db.variants;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;

/**
 * Helper class for VariantManager.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantManagerHelper implements Serializable {
    private static final long serialVersionUID = 5329416217138693373L;

    public int getNumFilteredVariants(String sessID, SelectQuery q) throws SQLException, RemoteException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());
        rs.next();

        return rs.getInt(1);
    }

    public List<Object[]> getVariants(String sessID, SelectQuery q, int start, int limit, String[] orderByCols) throws SQLException, RemoteException, SessionExpiredException {
        if (orderByCols != null) {
            q.addCustomOrderings((Object[]) orderByCols);
        }

        String queryString = q.toString();
        if (limit != -1) {
            if (start != -1) {
                queryString += " LIMIT " + start + ", " + limit;
            } else {
                queryString += " LIMIT " + limit;
            }
        }

        ResultSet rs = ConnectionController.executeQuery(sessID, queryString);

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberColumns = rsMetaData.getColumnCount();

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            Object[] v = new Object[numberColumns];
            for (int i = 1; i <= numberColumns; i++) {
                v[i - 1] = rs.getObject(i);
            }
            result.add(v);
        }

        return result;
    }

    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sid, SelectQuery q, TableSchema table, CustomField column, float multiplier, boolean logBins)
            throws SQLException, SessionExpiredException, RemoteException, InterruptedException {

        Range range = DBUtils.getInstance().getExtremeValuesForColumn(sid, table.getTableName(), column.getColumnName());
        double binSize = MiscUtils.generateBins(column, range, logBins);

        String round;
        if (logBins) {
            round = "floor(log10(" + column.getColumnName() + ")) as m";
        } else {
            round = "floor(" + column.getColumnName() + " / " + binSize + ") as m";
        }

        String query = q.toString().replace("COUNT(*)", "COUNT(*), " + round);
        query += " GROUP BY m ORDER BY m ASC";

        ResultSet rs = ConnectionController.executeQuery(sid, query);

        Map<Range, Long> results = new TreeMap<Range, Long>();
        while (rs.next()) {
            int binNo = rs.getInt(2);
            Range r;
            if (logBins) {
                r = new Range(Math.pow(10, binNo), Math.pow(10, binNo + 1));
            } else {
                r = new Range(binNo * binSize, (binNo + 1) * binSize);
            }
            long count = (long) (rs.getLong(1) * multiplier);
            results.put(r, count);
        }

        return results;
    }

    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, SelectQuery q, String colName, Condition c, float multiplier) throws SQLException,
            SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());
        Map<String, Integer> map = new HashMap<String, Integer>();

        while (rs.next()) {
            String key = rs.getString(1);
            if (key == null) {
                key = "";
            }
            map.put(key, (int) (rs.getInt(2) * multiplier));
        }

        return map;
    }
}
