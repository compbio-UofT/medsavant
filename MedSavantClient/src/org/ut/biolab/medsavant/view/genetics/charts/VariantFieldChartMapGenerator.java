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
package org.ut.biolab.medsavant.view.genetics.charts;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.db.ColumnType;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class VariantFieldChartMapGenerator implements ChartMapGenerator, FiltersChangedListener {

    private final CustomField field;
    private final Table whichTable;
    Map<String, ChartFrequencyMap> unfilteredMapCache = new HashMap<String,ChartFrequencyMap>();
    Map<String, ChartFrequencyMap> filteredMapCache = new HashMap<String,ChartFrequencyMap>();

    private VariantFieldChartMapGenerator(CustomField field, Table whichTable) {
        FilterController.addFilterListener(this, true);
        this.field = field;
        this.whichTable = whichTable;
    }

    public static VariantFieldChartMapGenerator createVariantChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, Table.VARIANT);
    }

    public static VariantFieldChartMapGenerator createPatientChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, Table.PATIENT);
    }

    public CustomField getField() {
        return field;
    }

    public ChartFrequencyMap generateCategoricalChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws SQLException, RemoteException {

        ChartFrequencyMap chartMap = new ChartFrequencyMap();
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        
        Condition[][] filterConditions;
        if (useFilteredCounts) {
            filterConditions = FilterController.getQueryFilterConditions();
        } else {
            filterConditions = new Condition[][]{};
        }
        
        if (whichTable == Table.VARIANT) {

            chartMap.addAll(MedSavantClient.VariantManager.getFilteredFrequencyValuesForCategoricalColumn(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    filterConditions,
                    field.getColumnName()));
            
        } else if (whichTable == Table.PATIENT) {

            //get dna ids for each distinct value
            Map<Object, List<String>> map = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForValues(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    field.getColumnName());

            if (field.getColumnName().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {
                map = ClientMiscUtils.modifyGenderMap(map);
            }
            
            if (Thread.currentThread().isInterrupted()) return null;
            
            //get a count for each dna id
            List<String> dnaIds = getDnaIds();
            Map<String, Integer> dnaIdToCount = MedSavantClient.VariantManager.getDnaIdHeatMap(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectID(), 
                    ReferenceController.getInstance().getCurrentReferenceID(), 
                    filterConditions, 
                    dnaIds);
            
            for (Object key: map.keySet()) {
                
                //generate sum from each dna id
                int numVariants = 0;
                for (String dnaId : map.get(key)) {
                    Integer value = dnaIdToCount.get(dnaId);
                    if (value != null) numVariants += value;
                }
                
                //add entry
                if (numVariants > 0) {
                    chartMap.addEntry(key.toString(), numVariants);
                }
            }
        }

        //sort results
        if (whichTable == Table.VARIANT && field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM)) {
            chartMap.sortKaryotypically();
        } else {
            chartMap.sort();
        }

        return chartMap;
    }

    private String checkInt(double d) {
        if (Math.round(d) == d) {
            return ViewUtil.numToString((int)d);
        } else {
            return ViewUtil.numToString(d);
        }
    }

    public ChartFrequencyMap generateNumericChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws SQLException, RemoteException {

        ChartFrequencyMap chartMap = new ChartFrequencyMap();

        Condition[][] conditions;
        if (useFilteredCounts) {
            conditions = FilterController.getQueryFilterConditions();
        } else {
            conditions = new Condition[][]{};
        }
        
        if (whichTable == Table.VARIANT) {
                
                Map<Range,Long> resultMap = MedSavantClient.VariantManager.getFilteredFrequencyValuesForNumericColumn(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        conditions,
                        field,
                        isLogScaleX);

                Object[] keySet = resultMap.keySet().toArray();
                if (!isLogScaleX) {
                    double startFirstRange = ((Range)(keySet[0])).getMin();
                    double startLastRange = ((Range)(keySet[keySet.length-1])).getMin();
                    double binSize = ((Range)(keySet[0])).getMax() - ((Range)(keySet[0])).getMin();
                    for (double start = startFirstRange; start <= startLastRange; start+=binSize) {
                        Long value = resultMap.get(new Range(start, start+binSize));
                        if (value == null) value = 0L;
                        chartMap.addEntry(
                                checkInt(start) + " - " + checkInt(start+binSize),
                                value);
                    }
                } else {
                    for (Object key: keySet) {
                        chartMap.addEntry(checkInt(((Range)key).getMin()) + " - " + checkInt(((Range)key).getMax()), resultMap.get((Range)key));
                    }
                }
        } else {
            
            //TODO: This hasn't been properly tested. Need a numeric field in patients. 
            
            String tablename = null;
            if (whichTable == Table.VARIANT) {
                tablename = ProjectController.getInstance().getCurrentVariantTableName();
            } else if (whichTable == Table.PATIENT) {
                tablename = ProjectController.getInstance().getCurrentPatientTableName();
            }
            Range r = new Range(MedSavantClient.VariantManager.getExtremeValuesForColumn(
                    LoginController.sessionId,
                    tablename,
                    field.getColumnName()));

            double binSize = org.ut.biolab.medsavant.util.ClientMiscUtils.generateBins(field, r, isLogScaleX);
            
            //get dna ids for each distinct value
            Map<Object, List<String>> map = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForValues(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    field.getColumnName());
            int maxBin = 0;
            for (Object key : map.keySet()) {
                double value = ClientMiscUtils.getDouble(key);
                if ((int)(value / binSize) > maxBin) {
                    maxBin = (int)(value / binSize);
                }
            }

            if (Thread.currentThread().isInterrupted()) return null;
            
            List<String> dnaIds = getDnaIds();
            //get a count for each dna id
            Map<String, Integer> dnaIdToCount = MedSavantClient.VariantManager.getDnaIdHeatMap(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectID(), 
                    ReferenceController.getInstance().getCurrentReferenceID(), 
                    conditions, 
                    dnaIds);
            
            int[] counts = new int[maxBin+1];
            Arrays.fill(counts, 0);
            for (Object key : map.keySet()) {
                
                double value = ClientMiscUtils.getDouble(key);
                int bin = (int)(value / binSize);
                
                for (String dnaId : map.get(key)) {
                    Integer count = dnaIdToCount.get(dnaId);
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
        return chartMap;
    }

    public ChartFrequencyMap generateChartMap(boolean isLogScaleX) throws SQLException, RemoteException {
        return generateChartMap(true, isLogScaleX);
    }

    @Override
    public ChartFrequencyMap generateChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws SQLException, RemoteException {

        String cacheKey = ProjectController.getInstance().getCurrentProjectID()
                + "_" + ReferenceController.getInstance().getCurrentReferenceID()
                + "_" + field.getColumnName()
                + "_" + isLogScaleX;

        boolean noConditions = !useFilteredCounts || (FilterController.getQueryFilterConditions().length == 0);

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


        if (isNumeric() && !field.getColumnName().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {

            /*String tablename = null;
            if (whichTable == Table.VARIANT) {
                tablename = ProjectController.getInstance().getCurrentVariantTableName();
            } else if (whichTable == Table.PATIENT) {
                tablename = ProjectController.getInstance().getCurrentPatientTableName();
            }
            Range r = new Range(MedSavantClient.VariantQueryUtilAdapter.getExtremeValuesForColumn(
                    LoginController.sessionId,
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
            unfilteredMapCache.put(cacheKey,chartMap);
        }

        else if (useFilteredCounts || noConditions) {
            filteredMapCache.put(cacheKey,chartMap);
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
    public Table getTable() {
        return whichTable;
    }

    @Override
    public String getFilterId() {
        return field.getColumnName();
    }

    @Override
    public void filtersChanged() {
        if (!filteredMapCache.isEmpty()) {
            filteredMapCache.clear();
        }
    }
    
    private List<String> getDnaIds() throws SQLException, RemoteException{
        List<String> dnaIds = MedSavantClient.VariantManager.getDistinctValuesForColumn(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentVariantTableName(), 
                    DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID, true);
        if (dnaIds == null) {
            dnaIds = MedSavantClient.VariantManager.getDistinctValuesForColumn(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentVariantSubTableName(), 
                    DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID,
                    false);
        }
        return dnaIds;
    }
}
