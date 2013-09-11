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
package org.ut.biolab.medsavant.shard.variant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.shards.criteria.ShardedCriteriaImpl;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.ut.biolab.medsavant.shard.common.EntityStyle;
import org.ut.biolab.medsavant.shard.db.ShardedDBUtilsHelper;
import org.ut.biolab.medsavant.shard.mapping.VariantMapping;
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
 * Sharded version of the helper class for VariantManager.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedVariantManagerHelper implements Serializable {

    private static final long serialVersionUID = 6242924081283721254L;
    private static final String[] INT_FIELDS = new String[] { "upload_id", "file_id", "variant_id", "position", "ac", "an", "dp", "end", "mq0", "ns" };
    private static final String[] FLOAT_FIELDS = new String[] { "qual", "af", "bq", "mq", "sb" };
    private static final String[] BOOLEAN_FIELDS = new String[] { "db", "h2", "somatic", "validated" };
    private static final String[] STRING_FIELDS = new String[] { "dna_id", "chrom", "dbsnp_id", "ref", "alt", "filter", "variant_type", "zygosity", "gt", "custom_info", "aa",
            "cigar" };

    private ShardedDBUtilsHelper helper = new ShardedDBUtilsHelper();

    private boolean equalsAny(String pattern, String[] match) {
        for (String s : match) {
            if (pattern.equals(s)) {
                return true;
            }
        }

        return false;
    }

    private Object castToItsType(String att, String value) {
        Object res = null;
        if (equalsAny(att, INT_FIELDS)) {
            res = Integer.valueOf(value);
        } else if (equalsAny(att, BOOLEAN_FIELDS)) {
            res = Boolean.valueOf(value);
        } else if (equalsAny(att, FLOAT_FIELDS)) {
            res = Float.valueOf(value);
        } else {
            res = value;
        }

        return res;
    }

    private Object[] getAttributeValues(Variant v) {
        // use reflection to get the list
        String atts = ReflectionToStringBuilder.toString(v, EntityStyle.getInstance());

        // parse
        atts = atts.substring(1, atts.length() - 1);
        String[] pairs = atts.split(EntityStyle.FIELD_SEPARATOR);
        Object[] os = new Object[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            String[] pieces = pairs[i].split("=", 2);
            os[i] = castToItsType(pieces[0], pieces[1]);
        }

        return os;
    }

    private String getWhereClause(SelectQuery q) {
        // replace original table identifiers with Hibernate references
        return q.getWhereClause().toString().replaceAll("t[0-9]+.", "this_.");
    }

    /**
     * Counts the variants according to a given filter.
     * 
     * @param sessID
     *            session ID
     * @param q
     *            query
     * @param table
     *            variants table
     * @return number of variants satisfying a given filter
     */
    public Integer getNumFilteredVariants(String sessID, SelectQuery q, TableSchema table) {
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        Criteria c = s.createCriteria(Variant.class).setProjection(Projections.count(VariantMapping.getIdColumn()));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));
        Object tmp = c.list().get(0);
        Integer res = (tmp == null) ? null : ((BigDecimal) tmp).intValue();

        ShardedSessionManager.closeSession(s);

        return (res == null ? 0 : res);
    }

    /**
     * Retrieves the variants with the given offset/limit.
     * 
     * @param sessID
     *            session ID
     * @param q
     *            query
     * @param table
     *            variants table
     * @param start
     *            offset
     * @param limit
     *            limit/number of results
     * @param orderByCols
     *            ordering
     * @return list of variants
     */
    public List<Object[]> getVariants(String sessID, SelectQuery q, TableSchema table, int start, int limit, String[] orderByCols) {
        ShardedSessionManager.setTable(table.getTableName());

        Session session = ShardedSessionManager.openSession();
        Criteria c = session.createCriteria(Variant.class).setFetchSize(limit).setMaxResults(limit).setFirstResult(start);
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));
        List<Variant> variantList = c.list();

        // convert to rows the editor expects
        int numberColumns = VariantMapping.getColumnCount();
        List<Object[]> res = new ArrayList<Object[]>();
        Iterator<Variant> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Object v = iterator.next();
            if (v != null) {
                res.add(getAttributeValues((Variant) v));
            }
        }

        ShardedSessionManager.closeSession(session);

        return res;
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
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        Range range = helper.getExtremeValuesForColumn(table.getTableName(), column.getColumnName());
        double binSize = MiscUtils.generateBins(column, range, logBins);

        String round;
        if (logBins) {
            round = "floor(log10(" + column.getColumnName() + ")) ";
        } else {
            round = "floor(" + column.getColumnName() + " / " + binSize + ") ";
        }

        // execute query
        // add order by value ASC if needed
        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setProjection(Projections.sqlGroupProjection("count('variant_id') as pos, " + round + "as value",
                "value", new String[] { "pos", "value" }, new Type[] { new IntegerType(), new IntegerType() }));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));
        List<Object[]> os = c.list();

        Map<Range, Long> results = new TreeMap<Range, Long>();
        for (Object[] o : os) {
            if (o != null) {
                Integer binNo = (int) (((Integer) o[1]) * multiplier);
                Long count = ((BigDecimal) o[0]).longValue();
                Range r;
                if (logBins) {
                    r = new Range(Math.pow(10, binNo), Math.pow(10, binNo + 1));
                } else {
                    r = new Range(binNo * binSize, (binNo + 1) * binSize);
                }
                results.put(r, count);
            }
        }

        ShardedSessionManager.closeSession(s);

        return results;
    }

    /**
     * Retrieves frequency values for columns represented by a string.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @param table
     *            variants table
     * @param colName
     *            name of column to use
     * @param multiplier
     *            multiplier
     * @return map of column values to counts
     */
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sid, SelectQuery q, TableSchema table, String colName, float multiplier) throws SQLException,
            SessionExpiredException {
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setProjection(Projections.sqlGroupProjection("count('variant_id') as pos, " + colName + " as value",
                "value", new String[] { "pos", "value" }, new Type[] { new IntegerType(), new StringType() }));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));
        List<Object[]> os = c.list();

        Map<String, Integer> res = new HashMap<String, Integer>();
        if (os != null) {
            for (Object[] o : os) {
                if (o != null) {
                    String key = (o[1] == null) ? "" : (String) o[1];
                    res.put(key, (int) (((BigDecimal) o[0]).intValue() * multiplier));
                }
            }
        }

        ShardedSessionManager.closeSession(s);

        return res;
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
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        DbColumn columnX = table.getDBColumn(columnnameX);
        DbColumn columnY = table.getDBColumn(columnnameY);

        double binSizeX = 0;
        if (!columnXCategorical) {
            Range rangeX = helper.getExtremeValuesForColumn(table.getTableName(), columnnameX);
            binSizeX = MiscUtils.generateBins(new CustomField(columnnameX, columnX.getTypeNameSQL() + "(" + columnX.getTypeLength() + ")", false, "", ""), rangeX, false);
        }

        double binSizeY = 0;
        if (!columnYCategorical) {
            Range rangeY = helper.getExtremeValuesForColumn(table.getTableName(), columnnameY);
            binSizeY = MiscUtils.generateBins(new CustomField(columnnameY, columnY.getTypeNameSQL() + "(" + columnY.getTypeLength() + ")", false, "", ""), rangeY, false);
        }

        // set correct types and aggregation
        String m = columnnameX;
        // todo set string types in criteria
        if (!columnXCategorical) {
            m = "floor(" + columnnameX + " / " + binSizeX + ")";
        }
        String n = columnnameY;
        if (!columnYCategorical) {
            n = "floor(" + columnnameY + " / " + binSizeY + ")";
        }

        // execute query
        // add order by value ASC if needed
        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setProjection(Projections.sqlGroupProjection("count('variant_id') as pos, " + m + " as value1, " + n
                + " as value2", "value1, value2", new String[] { "pos", "value1", "value2" }, new Type[] { new IntegerType(),
                columnXCategorical ? new StringType() : new IntegerType(), columnYCategorical ? new StringType() : new IntegerType() }));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));
        List<Object[]> os = c.list();

        List<ScatterChartEntry> entries = new ArrayList<ScatterChartEntry>();
        List<String> xRanges = new ArrayList<String>();
        List<String> yRanges = new ArrayList<String>();
        for (Object[] o : os) {
            if (o != null) {
                String xs = null;
                if (columnXCategorical) {
                    xs = (String) o[1];
                } else {
                    xs = MiscUtils.doubleToString(((Integer) o[1]) * binSizeX, 2) + " - " + MiscUtils.doubleToString(((Integer) o[1]) * binSizeX + binSizeX, 2);
                }
                String ys = null;
                if (columnYCategorical) {
                    ys = (String) o[2];
                } else {
                    ys = MiscUtils.doubleToString(((Integer) o[2]) * binSizeY, 2) + " - " + MiscUtils.doubleToString(((Integer) o[2]) * binSizeY + binSizeY, 2);
                }

                ScatterChartEntry entry = new ScatterChartEntry(xs, ys, (int) (((BigDecimal) o[0]).intValue() * multiplier));
                entries.add(entry);
                if (!xRanges.contains(entry.getXRange())) {
                    xRanges.add(entry.getXRange());
                }
                if (!yRanges.contains(entry.getYRange())) {
                    yRanges.add(entry.getYRange());
                }
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

        ShardedSessionManager.closeSession(s);

        return new ScatterChartMap(xRanges, yRanges, entries);
    }

    /**
     * Computes chromosome heat map.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @param table
     *            variants table
     * @param colName
     *            name of the column to use
     * @param binsize
     *            size of the bins
     * @param multiplier
     *            multiplier
     * @return map of chromosomes to maps of ranges and the respective counts
     */
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, SelectQuery q, TableSchema table, String colName, int binsize, float multiplier) throws SQLException,
            SessionExpiredException {
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        String round = "ROUND(" + colName + "/" + binsize + ",0)";

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setProjection(Projections.sqlGroupProjection("count('variant_id') as pos, chrom as value1, " + round
                + " as value2", "value1, value2", new String[] { "pos", "value1", "value2" }, new Type[] { new IntegerType(), new StringType(), new IntegerType() }));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));
        List<Object[]> os = c.list();

        Map<String, Map<Range, Integer>> results = new HashMap<String, Map<Range, Integer>>();
        for (Object[] o : os) {
            if (o != null) {
                String chrom = o[1].toString();

                Map<Range, Integer> chromMap;
                if (!results.containsKey(chrom)) {
                    chromMap = new HashMap<Range, Integer>();
                } else {
                    chromMap = results.get(chrom);
                }

                int binNo = (Integer) o[2];
                Range binRange = new Range(binNo * binsize, (binNo + 1) * binsize);
                int count = (int) (((BigDecimal) o[0]).intValue() * multiplier);
                chromMap.put(binRange, count);

                results.put(chrom, chromMap);
            }
        }

        ShardedSessionManager.closeSession(s);

        return results;
    }

    /**
     * Retrieves number of patients for given variant ranges based on DNA_ID.
     * 
     * @param sid
     *            session ID
     * @param q
     *            query
     * @param table
     *            variants table
     * @return patient count
     */
    public int getPatientCountWithVariantsInRange(String sid, SelectQuery q, TableSchema table) throws SQLException, SessionExpiredException {
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setProjection(Projections.sqlGroupProjection("dna_id as value", "value", new String[] { "value" },
                new Type[] { new StringType() }));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));
        List<Object> tmp = c.list();
        Integer res = (tmp.size() == 1 && (tmp.get(0) == null || ((tmp.get(0) instanceof Object[]) && (((Object[]) tmp.get(0))[0] == null)))) ? 0 : (Integer) c.list().size();

        ShardedSessionManager.closeSession(s);

        return res;
    }

    /**
     * Retrieves bookmark positions for DNA IDs.
     * 
     * @param sessID
     *            session ID
     * @param query
     *            query
     * @param table
     *            variants table
     * @param limit
     *            limit on the number of results
     * @return map of results
     */
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, SelectQuery query, TableSchema table, int limit, Map<String, List<String>> results) {
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setProjection(Projections.sqlProjection("dna_id as dna_id, chrom as chrom, position as position",
                new String[] { "dna_id", "chrom", "position" }, new Type[] { new StringType(), new StringType(), new IntegerType() }));
        c.add(Restrictions.sqlRestriction(getWhereClause(query)));
        if (limit != -1) {
            c.setMaxResults(limit).setFetchSize(limit);
        }

        List<Object[]> os = c.list();
        for (Object[] o : os) {
            if (o != null) {
                results.get((String) o[0]).add((String) o[1] + ":" + ((Long) o[2] - 100) + "-" + ((Long) o[2] + 100));
            }
        }

        ShardedSessionManager.closeSession(s);

        return results;
    }

    /**
     * Counts variants per family.
     * 
     * @param sessID
     *            session ID
     * @param q
     *            query to execute
     * @param table
     *            variants table
     * @param dnaIDsToCountMap
     *            map of dna ids to counts
     * @return map of dna ids to counts
     * @throws SessionExpiredException
     * @throws SQLException
     */
    public Map<String, Integer> getNumVariantsInFamily(String sessID, SelectQuery q, TableSchema table, Map<String, Integer> dnaIDsToCountMap) {
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setAggregateGroupByProjection(Projections.count(VariantMapping.getIdColumn()),
                Projections.groupProperty("dna_id"));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));

        List<Object[]> os = c.list();
        for (Object[] o : os) {
            if (o != null) {
                dnaIDsToCountMap.put((String) o[1], ((BigDecimal) o[0]).intValue());
            }
        }

        ShardedSessionManager.closeSession(s);

        return dnaIDsToCountMap;
    }

    /**
     * Creates DNA ID heat map.
     * 
     * @param sessID
     *            session ID
     * @param q
     *            query
     * @param table
     *            variants table
     * @param patientHeatMapThreshold
     *            patient heat map threshold
     * @param multiplier
     *            multiplier
     * @param useThreshold
     *            true if threshold should be used, false otherwise
     * @param map
     *            heat map
     * @return updated heat map
     */
    public Map<String, Integer> getDNAIDHeatMap(String sessID, SelectQuery q, TableSchema table, int patientHeatMapThreshold, float multiplier, boolean useThreshold,
            Map<String, Integer> map) throws SQLException, SessionExpiredException {
        ShardedSessionManager.setTable(table.getTableName());

        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(Variant.class)).setAggregateGroupByProjection(Projections.count(VariantMapping.getIdColumn()),
                Projections.groupProperty("dna_id"));
        c.add(Restrictions.sqlRestriction(getWhereClause(q)));

        List<Object[]> os = c.list();
        for (Object[] o : os) {
            if (o != null) {
                int value = (int) (((BigDecimal) o[0]).intValue() * multiplier);
                if (!useThreshold || value >= patientHeatMapThreshold) {
                    map.put((String) o[1], value);
                }
            }
        }

        ShardedSessionManager.closeSession(s);

        return map;
    }
}
