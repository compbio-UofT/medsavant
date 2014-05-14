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
package org.ut.biolab.medsavant.client.view.genetics.charts;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public class VariantFieldChartMapGenerator implements ChartMapGenerator, BasicPatientColumns, BasicVariantColumns {

    private final CustomField field;
    private final WhichTable whichTable;
    Map<String, ChartFrequencyMap> unfilteredMapCache = new HashMap<String, ChartFrequencyMap>();
    Map<String, ChartFrequencyMap> filteredMapCache = new HashMap<String, ChartFrequencyMap>();

    private VariantFieldChartMapGenerator(CustomField field, WhichTable whichTable) {
        this.field = field;
        this.whichTable = whichTable;

        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                if (!filteredMapCache.isEmpty()) {
                    filteredMapCache.clear();
                }
            }
        });
    }

    public static VariantFieldChartMapGenerator createVariantChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, WhichTable.VARIANT);
    }

    public static VariantFieldChartMapGenerator createPatientChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, WhichTable.PATIENT);
    }

    public CustomField getField() {
        return field;
    }

    public ChartFrequencyMap generateCategoricalChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws InterruptedException, SQLException, RemoteException {

        ChartFrequencyMap chartMap = new ChartFrequencyMap();

        try {

            if (Thread.currentThread().isInterrupted()) {
                return null;
            }

            Condition[][] filterConditions;
            if (useFilteredCounts) {
                filterConditions = FilterController.getInstance().getAllFilterConditions();
            } else {
                filterConditions = new Condition[][]{};
            }

            if (whichTable == WhichTable.VARIANT) {

                Map<String, Integer> m = MedSavantClient.VariantManager.getFilteredFrequencyValuesForCategoricalColumn(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        filterConditions,
                        field.getColumnName());
                                              
                chartMap.addAll(m);

            } else if (whichTable == WhichTable.PATIENT) {

                //get dna ids for each distinct value
                Map<Object, List<String>> map = MedSavantClient.PatientManager.getDNAIDsForValues(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        field.getColumnName());

                if (field.getColumnName().equals(GENDER.getColumnName())) {
                    map = ClientMiscUtils.modifyGenderMap(map);
                }

                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }

                //get a count for each dna id
                List<String> dnaIDs = getDNAIDs();
                Map<String, Integer> dnaIdToCount = MedSavantClient.VariantManager.getDNAIDHeatMap(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        filterConditions,
                        dnaIDs);

                for (Object key : map.keySet()) {

                    //generate sum from each dna id
                    int numVariants = 0;
                    for (String dnaId : map.get(key)) {
                        Integer value = dnaIdToCount.get(dnaId);
                        if (value != null) {
                            numVariants += value;
                        }
                    }

                    //add entry
                    if (numVariants > 0) {                       
                        chartMap.addEntry(key.toString(), numVariants);
                    }
                }
            }

            //sort results
            if (whichTable == WhichTable.VARIANT && field.getColumnName().equals(CHROM.getColumnName())) {
                chartMap.sortKaryotypically();
            } else {
                chartMap.sort();
            }
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }

        return chartMap;
    }

    private String checkInt(double d) {
        if (Math.round(d) == d) {
            return ViewUtil.numToString((int) d);
        } else {
            return ViewUtil.numToString(d);
        }
    }

    public ChartFrequencyMap generateNumericChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws InterruptedException, SQLException, RemoteException {

        ChartFrequencyMap chartMap = new ChartFrequencyMap();

        try {
            Condition[][] conditions;
            if (useFilteredCounts) {
                conditions = FilterController.getInstance().getAllFilterConditions();
            } else {
                conditions = new Condition[][]{};
            }

            if (whichTable == WhichTable.VARIANT) {

                Map<Range, Long> resultMap = MedSavantClient.VariantManager.getFilteredFrequencyValuesForNumericColumn(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        conditions,
                        field,
                        isLogScaleX);

                Object[] keySet = resultMap.keySet().toArray();
                if (!isLogScaleX) {
                    double startFirstRange = ((Range) (keySet[0])).getMin();
                    double startLastRange = ((Range) (keySet[keySet.length - 1])).getMin();
                    double binSize = ((Range) (keySet[0])).getMax() - ((Range) (keySet[0])).getMin();
                    for (double start = startFirstRange; start <= startLastRange; start += binSize) {
                        Long value = resultMap.get(new Range(start, start + binSize));
                        if (value == null) {
                            value = 0L;
                        }
                        chartMap.addEntry(
                                checkInt(start) + " - " + checkInt(start + binSize),
                                value);
                    }
                } else {
                    for (Object key : keySet) {
                        chartMap.addEntry(checkInt(((Range) key).getMin()) + " - " + checkInt(((Range) key).getMax()), resultMap.get((Range) key));
                    }
                }
            } else {

                //TODO: This hasn't been properly tested. Need a numeric field in patients.
                Range r = MedSavantClient.DBUtils.getExtremeValuesForColumn(LoginController.getSessionID(), whichTable.getName(), field.getColumnName());

                double binSize = org.ut.biolab.medsavant.client.util.ClientMiscUtils.generateBins(field, r, isLogScaleX);

                //get dna ids for each distinct value
                Map<Object, List<String>> map = MedSavantClient.PatientManager.getDNAIDsForValues(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        field.getColumnName());
                int maxBin = 0;
                for (Object key : map.keySet()) {
                    double value = ClientMiscUtils.getDouble(key);
                    if ((int) (value / binSize) > maxBin) {
                        maxBin = (int) (value / binSize);
                    }
                }

                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }

                List<String> dnaIds = getDNAIDs();
                //get a count for each dna id
                Map<String, Integer> dnaIDToCount = MedSavantClient.VariantManager.getDNAIDHeatMap(
                        LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        conditions,
                        dnaIds);

                int[] counts = new int[maxBin + 1];
                Arrays.fill(counts, 0);
                for (Object key : map.keySet()) {

                    double value = ClientMiscUtils.getDouble(key);
                    int bin = (int) (value / binSize);

                    for (String dnaId : map.get(key)) {
                        Integer count = dnaIDToCount.get(dnaId);
                        if (count != null) {
                            counts[bin] += count;
                        }
                    }
                }

                for (int i = 0; i < counts.length; i++) {
                    double min = i * binSize;
                    double max = min + binSize;
                    chartMap.addEntry(
                            checkInt(min) + " - " + checkInt(max),
                            counts[i]);
                }

            }
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
        return chartMap;
    }

    public ChartFrequencyMap generateChartMap(boolean isLogScaleX) throws InterruptedException, SQLException, RemoteException {
        return generateChartMap(true, isLogScaleX);
    }

    @Override
    public ChartFrequencyMap generateChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws InterruptedException, SQLException, RemoteException {

        String cacheKey = ProjectController.getInstance().getCurrentProjectID()
                + "_" + ReferenceController.getInstance().getCurrentReferenceID()
                + "_" + field.getColumnName()
                + "_" + isLogScaleX;

        boolean noConditions = !useFilteredCounts || (FilterController.getInstance().getAllFilterConditions().length == 0);

        if (!useFilteredCounts) {
            if (unfilteredMapCache.containsKey(cacheKey)) {
                return unfilteredMapCache.get(cacheKey);
            }
        } else {
            if (filteredMapCache.containsKey(cacheKey)) {
                return filteredMapCache.get(cacheKey);
            }
        }

        ChartFrequencyMap chartMap;


        if (isNumeric() && !field.getColumnName().equals(BasicPatientColumns.GENDER.getColumnName())) {

            /*String tablename = null;
             if (whichTable == Table.VARIANT) {
             tablename = ProjectController.getInstance().getCurrentVariantTableName();
             } else if (whichTable == Table.PATIENT) {
             tablename = ProjectController.getInstance().getCurrentPatientTableName();
             }
             Range r = new Range(MedSavantClient.VariantQueryUtilAdapter.getExtremeValuesForColumn(
             LoginController.getSessionID(),
             tablename,
             field.getColumnName()));

             double binSize = org.ut.biolab.medsavant.db.util.shared.MiscUtils.generateBins(field, r, isLogScaleX);*/
            chartMap = generateNumericChartMap(useFilteredCounts, isLogScaleX);

        } else {
            chartMap = generateCategoricalChartMap(useFilteredCounts, isLogScaleX);
            if (field.getColumnType() == ColumnType.BOOLEAN) {
                for (FrequencyEntry fe : chartMap.getEntries()) {
                    if (fe.getKey().equals("0")) {
                        fe.setKey("False");
                    } else if (fe.getKey().equals("1")) {
                        fe.setKey("True");
                    }
                }
            }
        }

        if (!useFilteredCounts) {
            unfilteredMapCache.put(cacheKey, chartMap);
        } else if (useFilteredCounts || noConditions) {
            filteredMapCache.put(cacheKey, chartMap);
        }

        return chartMap;
    }

    @Override
    public boolean isNumeric() {
        return field.getColumnType().isNumeric();
    }

    @Override
    public String getName() {
        return field.getAlias();
    }

    @Override
    public WhichTable getTable() {
        return whichTable;
    }

    @Override
    public String getFilterId() {
        return field.getColumnName();
    }

    private List<String> getDNAIDs() throws InterruptedException, SQLException, RemoteException {
        try {
            List<String> dnaIDs = MedSavantClient.DBUtils.getDistinctValuesForColumn(LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentVariantTableName(),
                    DNA_ID.getColumnName(), true);
            if (dnaIDs == null) {
                dnaIDs = MedSavantClient.DBUtils.getDistinctValuesForColumn(LoginController.getSessionID(),
                        ProjectController.getInstance().getCurrentVariantSubTableName(),
                        DNA_ID.getColumnName(), false);
            }
            return dnaIDs;
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }
}
