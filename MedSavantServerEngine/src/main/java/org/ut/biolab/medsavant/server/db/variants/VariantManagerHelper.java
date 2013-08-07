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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.ScatterChartEntry;
import org.ut.biolab.medsavant.shared.model.ScatterChartMap;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

/**
 * Helper class for VariantManager.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantManagerHelper implements Serializable {
    private static final long serialVersionUID = 5329416217138693373L;

    /**
     * Counts the variants according to a given filter.
     * 
     * @param sessID
     *            session ID
     * @param q
     *            query
     * @return number of variants satisfying a given filter
     */
    public int getNumFilteredVariants(String sessID, SelectQuery q) throws SQLException, RemoteException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());
        rs.next();

        return rs.getInt(1);
    }

    /**
     * Retrieves the variants with the given offset/limit.
     * 
     * @param sessID
     *            session ID
     * @param q
     *            query
     * @param start
     *            offset
     * @param limit
     *            limit/number of results
     * @param orderByCols
     *            ordering
     * @return list of variants
     */
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

    /**
     * Computes frequency values for column with an arbitrary number type.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @param table
     *            table to use
     * @param column
     *            column to use
     * @param multiplier
     *            multiplier
     * @param logBins
     *            true if logarithmic bins are used, false otherwise
     * @return map of ranges to counts
     */
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

    /**
     * Retrieves frequency values for columns represented by a string.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @param colName
     *            name of column to use
     * @param c
     *            nucleotide condition or null
     * @param multiplier
     *            multiplier
     * @return map of column values to counts
     */
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, SelectQuery q, String colName, float multiplier) throws SQLException,
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

    /**
     * Retrieves frequency values for scatter.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @param table
     *            table ot use
     * @param columnnameX
     *            name of the first column
     * @param columnnameY
     *            name of the second column
     * @param cx
     *            nucleotide condition for the first column
     * @param cy
     *            nucleotide condition for the second column
     * @param columnXCategorical
     *            true if the first column is nonnumerical, false otherwise
     * @param columnYCategorical
     *            true if the second column is nonnumerical, false otherwise
     * @param sortKaryotypically
     *            true if karyotypical sorting should be done, false otherwise
     * @param multiplier
     *            multiplier
     * @return scatter chart map
     */
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sid, SelectQuery q, TableSchema table, String columnnameX, String columnnameY, boolean columnXCategorical,
            boolean columnYCategorical, boolean sortKaryotypically, float multiplier) throws RemoteException, InterruptedException, SQLException, SessionExpiredException {
        DbColumn columnX = table.getDBColumn(columnnameX);
        DbColumn columnY = table.getDBColumn(columnnameY);

        double binSizeX = 0;
        if (!columnXCategorical) {
            Range rangeX = DBUtils.getInstance().getExtremeValuesForColumn(sid, table.getTableName(), columnnameX);
            binSizeX = MiscUtils.generateBins(new CustomField(columnnameX, columnX.getTypeNameSQL() + "(" + columnX.getTypeLength() + ")", false, "", ""), rangeX, false);
        }

        double binSizeY = 0;
        if (!columnYCategorical) {
            Range rangeY = DBUtils.getInstance().getExtremeValuesForColumn(sid, table.getTableName(), columnnameY);
            binSizeY = MiscUtils.generateBins(new CustomField(columnnameY, columnY.getTypeNameSQL() + "(" + columnY.getTypeLength() + ")", false, "", ""), rangeY, false);
        }

        String m = columnnameX + " as m";
        if (!columnXCategorical) {
            m = "floor(" + columnnameX + " / " + binSizeX + ") as m";
        }
        String n = columnnameY + " as n";
        if (!columnYCategorical) {
            n = "floor(" + columnnameY + " / " + binSizeY + ") as n";
        }
        String query = q.toString().replace("COUNT(*)", "COUNT(*), " + m + ", " + n);
        query += " GROUP BY m, n ORDER BY m, n ASC";

        // String round = "floor(" + columnname + " / " + binSize + ") as m";

        ResultSet rs = ConnectionController.executeQuery(sid, query);

        List<ScatterChartEntry> entries = new ArrayList<ScatterChartEntry>();
        List<String> xRanges = new ArrayList<String>();
        List<String> yRanges = new ArrayList<String>();

        while (rs.next()) {
            String x = rs.getString(2);
            String y = rs.getString(3);

            if (x == null) {
                x = "null"; // prevents NPE when sorting below
            }
            if (y == null) {
                y = "null";
            }

            if (!columnXCategorical) {
                x = MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(x) * binSizeX + binSizeX, 2);
            }
            if (!columnYCategorical) {
                y = MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY, 2) + " - " + MiscUtils.doubleToString(Integer.parseInt(y) * binSizeY + binSizeY, 2);
            }
            ScatterChartEntry entry = new ScatterChartEntry(x, y, (int) (rs.getInt(1) * multiplier));
            entries.add(entry);
            if (!xRanges.contains(entry.getXRange())) {
                xRanges.add(entry.getXRange());
            }
            if (!yRanges.contains(entry.getYRange())) {
                yRanges.add(entry.getYRange());
            }
        }

        if (sortKaryotypically) {
            Collections.sort(xRanges, new ChromosomeComparator());
        } else if (columnXCategorical) {
            Collections.sort(xRanges);
        }
        if (columnYCategorical) {
            Collections.sort(yRanges);
        }

        return new ScatterChartMap(xRanges, yRanges, entries);
    }

    /**
     * Computes chromosome heat map.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @param colName
     *            name of the column to use
     * @param binsize
     *            size of the bins
     * @param multiplier
     *            multiplier
     * @return map of chromosomes to maps of ranges and the respective counts
     */
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, SelectQuery q, String colName, int binsize, float multiplier) throws SQLException,
            SessionExpiredException {
        String roundFunction = "ROUND(" + colName + "/" + binsize + ",0)";

        String query = q.toString().replace("COUNT(*)", "COUNT(*)," + roundFunction) + "," + roundFunction;

        ResultSet rs = ConnectionController.executeQuery(sid, query);

        Map<String, Map<Range, Integer>> results = new HashMap<String, Map<Range, Integer>>();
        while (rs.next()) {

            String chrom = rs.getString(1);

            Map<Range, Integer> chromMap;
            if (!results.containsKey(chrom)) {
                chromMap = new HashMap<Range, Integer>();
            } else {
                chromMap = results.get(chrom);
            }

            int binNo = rs.getInt(3);
            Range binRange = new Range(binNo * binsize, (binNo + 1) * binsize);

            int count = (int) (rs.getInt(2) * multiplier);

            chromMap.put(binRange, count);
            results.put(chrom, chromMap);
        }

        return results;
    }

    /**
     * Retrieves number of patients for given variant ranges based on DNA_ID.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @return patient count
     */
    public int getPatientCountWithVariantsInRange(String sid, SelectQuery q) throws SQLException, SessionExpiredException {
        String query = q.toString();
        query = query.replaceFirst("'", "").replaceFirst("'", "");

        ResultSet rs = ConnectionController.executeQuery(sid, query);
        rs.next();
        int numrows = rs.getInt(1);

        return numrows;
    }

    /**
     * Retrieves bookmark positions for DNA IDs.
     * 
     * @param sessID
     *            session ID
     * @param query
     *            query
     * @param limit
     *            limit on the number of results
     * @return map of results
     */
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, SelectQuery query, int limit, Map<String, List<String>> results) throws SQLException,
            SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));
        while (rs.next()) {
            results.get(rs.getString(1)).add(rs.getString(2) + ":" + (rs.getLong(3) - 100) + "-" + (rs.getLong(3) + 100));
        }

        return results;
    }
}
