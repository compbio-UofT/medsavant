/*
 *    Copyright 2011 University of Toronto
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
import java.util.ArrayList;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class VariantFieldChartMapGenerator implements ChartMapGenerator, FiltersChangedListener {

    private final CustomField field;
    private final Table whichTable;

    public static VariantFieldChartMapGenerator createVariantChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, Table.VARIANT);
    }

    public static VariantFieldChartMapGenerator createPatientChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, Table.PATIENT);
    }

    private VariantFieldChartMapGenerator(CustomField field, Table whichTable) {
        FilterController.addFilterListener(this,true);
        this.field = field;
        this.whichTable = whichTable;
    }

    public CustomField getField(){
        return field;
    }

    Map<String,ChartFrequencyMap> unfilteredMapCache = new HashMap<String,ChartFrequencyMap>();
    Map<String,ChartFrequencyMap> filteredMapCache = new HashMap<String,ChartFrequencyMap>();

    public ChartFrequencyMap generateCategoricalChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws SQLException, NonFatalDatabaseException, RemoteException {

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

            chartMap.addAll(MedSavantClient.VariantQueryUtilAdapter.getFilteredFrequencyValuesForCategoricalColumn(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId(),
                    ReferenceController.getInstance().getCurrentReferenceId(),
                    filterConditions,
                    field.getColumnName()));
            
        } else if (whichTable == Table.PATIENT) {

            //get dna ids for each distinct value
            Map<Object, List<String>> map = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForValues(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId(),
                    field.getColumnName());

            if (field.getColumnName().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {
                map = MiscUtils.modifyGenderMap(map);
            }
            
            if (Thread.currentThread().isInterrupted()) return null;
            
            //get a count for each dna id
            List<String> dnaIds = getDnaIds();
            Map<String, Integer> dnaIdToCount = MedSavantClient.VariantQueryUtilAdapter.getDnaIdHeatMap(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId(), 
                    filterConditions, 
                    dnaIds);
            
            for(Object key : map.keySet()){
                
                //generate sum from each dna id
                int numVariants = 0;
                for(String dnaId : map.get(key)){
                    Integer value = dnaIdToCount.get(dnaId);
                    if(value != null) numVariants += value;
                }
                
                //add entry
                if(numVariants > 0){
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

    public ChartFrequencyMap generateNumericChartMap(boolean useFilteredCounts, double binSize, boolean isLogScaleX) throws SQLException, NonFatalDatabaseException, RemoteException {

        ChartFrequencyMap chartMap = new ChartFrequencyMap();

        Condition[][] conditions;
        if(useFilteredCounts){
            conditions = FilterController.getQueryFilterConditions();
        } else {
            conditions = new Condition[][]{};
        }
        
        if (whichTable == Table.VARIANT) {
                
                Map<Range,Long> resultMap = MedSavantClient.VariantQueryUtilAdapter.getFilteredFrequencyValuesForNumericColumn(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectId(),
                        ReferenceController.getInstance().getCurrentReferenceId(),
                        conditions,
                        field.getColumnName(),
                        binSize,
                        isLogScaleX);

                Object[] keySet = resultMap.keySet().toArray();
                double startFirstRange = ((Range)(keySet[0])).getMin();
                double startLastRange = ((Range)(keySet[keySet.length-1])).getMin();
                for(double start = startFirstRange; start <= startLastRange; start+=binSize){
                    Long value = resultMap.get(new Range(start, start+binSize));
                    if(value == null) value = 0L;
                    chartMap.addEntry(
                            checkInt(start) + " - " + checkInt(start+binSize),
                            value);
                }
                
                /*for (Range key : resultMap.keySet()) {
                    chartMap.addEntry(
                        checkInt(key.getMin()) + " - " + checkInt(key.getMax()),
                        resultMap.get(key));
                }*/

        } else {
            
            //TODO: This hasn't been properly tested. Need a numeric field in patients. 
            
            //get dna ids for each distinct value
            Map<Object, List<String>> map = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForValues(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId(),
                    field.getColumnName());
            int maxBin = 0;
            for(Object key : map.keySet()){
                double value = MiscUtils.getDouble(key);
                if((int)(value / binSize) > maxBin){
                    maxBin = (int)(value / binSize);
                }
            }

            if (Thread.currentThread().isInterrupted()) return null;
            
            List<String> dnaIds = getDnaIds();
            //get a count for each dna id
            Map<String, Integer> dnaIdToCount = MedSavantClient.VariantQueryUtilAdapter.getDnaIdHeatMap(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId(), 
                    conditions, 
                    dnaIds);
            
            int[] counts = new int[maxBin+1];
            Arrays.fill(counts, 0);
            for(Object key : map.keySet()){
                
                double value = MiscUtils.getDouble(key);
                int bin = (int)(value / binSize);
                
                for(String dnaId : map.get(key)){
                    Integer count = dnaIdToCount.get(dnaId);
                    if(count != null){
                        counts[bin] += count;
                    }
                }
            }
            
            for(int i = 0; i < counts.length; i++){
                double min = i * binSize;
                double max = min + binSize;
                chartMap.addEntry(
                        checkInt(min) + " - " + checkInt(max),
                        counts[i]);
            }
            
        }
        return chartMap;
    }

    public ChartFrequencyMap generateChartMap(boolean isLogScaleX) throws SQLException, NonFatalDatabaseException, RemoteException {
        return generateChartMap(true, isLogScaleX);
    }

    public ChartFrequencyMap generateChartMap(boolean useFilteredCounts, boolean isLogScaleX) throws SQLException, NonFatalDatabaseException, RemoteException {

        String cacheKey = ProjectController.getInstance().getCurrentProjectId()
                + "_" + ReferenceController.getInstance().getCurrentReferenceId()
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

            String tablename = null;
            if (whichTable == Table.VARIANT) {
                tablename = ProjectController.getInstance().getCurrentVariantTableName();
            } else if (whichTable == Table.PATIENT) {
                tablename = ProjectController.getInstance().getCurrentPatientTableName();
            }
            Range r = new Range(MedSavantClient.VariantQueryUtilAdapter.getExtremeValuesForColumn(
                    LoginController.sessionId,
                    tablename,
                    field.getColumnName()));

            double binSize = org.ut.biolab.medsavant.db.util.shared.MiscUtils.generateBins(field, r, isLogScaleX);
            chartMap = generateNumericChartMap(useFilteredCounts, binSize, isLogScaleX);

        } else {
            chartMap = generateCategoricalChartMap(useFilteredCounts, isLogScaleX);
            if(field.getColumnType() == ColumnType.BOOLEAN){
                for(FrequencyEntry fe : chartMap.getEntries()){
                    if(fe.getKey().equals("0")){
                        fe.setKey("False");
                    } else if (fe.getKey().equals("1")){
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

    public boolean isNumeric() {
        ColumnType type = field.getColumnType();
        return type.equals(ColumnType.DECIMAL) || type.equals(ColumnType.FLOAT) || type.equals(ColumnType.INTEGER);
    }

    public String getName() {
        return field.getAlias();
    }

    public Table getTable() {
        return whichTable;
    }

    public String getFilterId() {
        return field.getColumnName();
    }

    @Override
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        if (!filteredMapCache.isEmpty()) {
            filteredMapCache.clear();
        }
    }
    
    private List<String> getDnaIds() throws SQLException, RemoteException{
        List<String> dnaIds = MedSavantClient.VariantQueryUtilAdapter.getDistinctValuesForColumn(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentVariantTableName(), 
                    DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID);
        if(dnaIds == null) {
            dnaIds = MedSavantClient.VariantQueryUtilAdapter.getDistinctValuesForColumn(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentVariantSubTableName(), 
                    DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID,
                    false);
        }
        return dnaIds;
    }
}
