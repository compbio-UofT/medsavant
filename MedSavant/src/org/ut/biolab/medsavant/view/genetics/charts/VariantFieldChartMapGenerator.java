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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.util.ArrayList;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;

/**
 *
 * @author mfiume
 */
public class VariantFieldChartMapGenerator implements ChartMapGenerator {

    private final CustomField field;
    private final Table whichTable;
    
    private enum Table {PATIENT, VARIANT};
    
    public static VariantFieldChartMapGenerator createVariantChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, Table.VARIANT);
    }

    public static VariantFieldChartMapGenerator createPatientChart(CustomField field) {
        return new VariantFieldChartMapGenerator(field, Table.PATIENT);
    }
    
    private VariantFieldChartMapGenerator(CustomField field, Table whichTable) {
        this.field = field;
        this.whichTable = whichTable;
    }
    
    private List<Range> generateBins(Range r, boolean isLogScaleX){
        List<Range> bins = new ArrayList<Range>();
        
        if(isLogScaleX){
            bins.add(new Range(0,1));
            for(int i = 1; i < r.getMax(); i *= 10){
                bins.add(new Range(i, i*10));
            }         
        } else {
            
            int min = (int)(r.getMin() - Math.abs(r.getMin() % (int)Math.pow(10, getNumDigits((int)(r.getMax() - r.getMin()))-1)));
            int step1 = (int)Math.ceil((r.getMax() - min) / 25.0);
            int step2 = (int)Math.pow(10, getNumDigits(step1));
            int step = step2;
            while (step * 0.5 > step1) step *= 0.5;
            step = Math.max(step, 1);
            
            int numSteps = (int)(((int)(r.getMax() - min) / step) + 1);
            
            for(int i = 0; i < numSteps; i++){
                bins.add(new Range(min + i * step, min + (i + 1) * step));
            }
        }
              
        return bins;
    }
    
    private int getNumDigits(int x){    
        x = Math.abs(x);
        int digits = 1;
        while (Math.pow(10, digits) < x){
            digits++;
        }
        return digits;
    }
    
    public ChartFrequencyMap generateChartMap(boolean isLogScaleX) throws SQLException, NonFatalDatabaseException {
        ChartFrequencyMap chartMap = new ChartFrequencyMap();
                     
        if (isNumeric()) {

            String tablename = null;
            if (whichTable == Table.VARIANT) {
                tablename = ProjectController.getInstance().getCurrentTableName();
            } else if (whichTable == Table.PATIENT) {
                tablename = ProjectController.getInstance().getCurrentPatientTableName();
            }
            Range r = new Range(VariantQueryUtil.getExtremeValuesForColumn(tablename, field.getColumnName()));
            
            List<Range> bins = generateBins(r, isLogScaleX);
            
            for(Range binrange : bins){
                if(Thread.currentThread().isInterrupted()){
                    return null;
                }
                if (whichTable == Table.VARIANT) {                    
                    chartMap.addEntry(
                            binrange.toString(), 
                            VariantQueryUtil.getFilteredFrequencyValuesForColumnInRange(
                                ProjectController.getInstance().getCurrentProjectId(), 
                                ReferenceController.getInstance().getCurrentReferenceId(), 
                                FilterController.getQueryFilterConditions(), 
                                field.getColumnName(), 
                                binrange.getMin(),
                                binrange.getMax()) 
                            );
                } else if (whichTable == Table.PATIENT) {

                    //get dna ids
                    List<String> individuals = PatientQueryUtil.getDNAIdsWithValuesInRange(ProjectController.getInstance().getCurrentProjectId(), field.getColumnName(), binrange);

                    //create new dna id filter
                    Condition[] dnaConditions = new Condition[individuals.size()];
                    int pos = 0;
                    for (String ind : individuals) {
                        dnaConditions[pos++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                    }
                    ComboCondition dnaCondition = ComboCondition.or(dnaConditions);

                    //add dna ids as new condition
                    Condition[][] filterConditions = FilterController.getQueryFilterConditions();
                    Condition[][] conditions = new Condition[filterConditions.length][];
                    for (int j = 0; j < filterConditions.length; j++) {
                        conditions[j] = new Condition[filterConditions[j].length+1];
                        System.arraycopy(filterConditions[j], 0, conditions[j], 0, filterConditions[j].length);
                        conditions[j][filterConditions[j].length] = dnaCondition;
                    }
                    if (filterConditions.length == 0) {
                        conditions = new Condition[1][];
                        conditions[0] = new Condition[]{dnaCondition};
                    }

                    //get num variants
                    int numVariants = 0;
                    if (individuals.size() > 0) {
                        numVariants = VariantQueryUtil.getNumFilteredVariants(
                                ProjectController.getInstance().getCurrentProjectId(), 
                                ReferenceController.getInstance().getCurrentReferenceId(), 
                                conditions);
                    }

                    //add entry
                    chartMap.addEntry(binrange.toString(),numVariants);
                }
            }

        } else {
            if(Thread.currentThread().isInterrupted()){
                return null;
            }
            if (whichTable == Table.VARIANT) {
                chartMap.addAll(VariantQueryUtil.getFilteredFrequencyValuesForColumn(
                        ProjectController.getInstance().getCurrentProjectId(), 
                        ReferenceController.getInstance().getCurrentReferenceId(), 
                        FilterController.getQueryFilterConditions(), 
                        field.getColumnName()));
            } else if (whichTable == Table.PATIENT) {
                Map<Object, List<String>> map = PatientQueryUtil.getDNAIdsForValues(ProjectController.getInstance().getCurrentProjectId(), field.getColumnName());
                Condition[][] filterConditions = FilterController.getQueryFilterConditions();
                for (Object key : map.keySet()) {

                    if(Thread.currentThread().isInterrupted()){
                        return null;
                    }

                    //create dnaid list
                    List<String> individuals = map.get(key);
                    Condition[] dnaConditions = new Condition[individuals.size()];
                    int pos = 0;
                    for (String ind : individuals) {
                        dnaConditions[pos++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                    }
                    ComboCondition dnaCondition = ComboCondition.or(dnaConditions);

                    //create new condition set
                    Condition[][] conditions = new Condition[filterConditions.length][];
                    for (int j = 0; j < filterConditions.length; j++) {
                        conditions[j] = new Condition[filterConditions[j].length+1];
                        System.arraycopy(filterConditions[j], 0, conditions[j], 0, filterConditions[j].length);
                        conditions[j][filterConditions[j].length] = dnaCondition;
                    }
                    if (filterConditions.length == 0) {
                        conditions = new Condition[1][];
                        conditions[0] = new Condition[]{dnaCondition};
                    }

                    //calculate number of variants satisfying conditions
                    int numVariants = 0;
                    if (individuals.size() > 0) {
                        numVariants = VariantQueryUtil.getNumFilteredVariants(
                                ProjectController.getInstance().getCurrentProjectId(), 
                                ReferenceController.getInstance().getCurrentReferenceId(), 
                                conditions);
                    }

                    //add entry
                    chartMap.addEntry(key.toString(),numVariants);

                }
            }

            Collections.sort(chartMap.getEntries());
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
}
