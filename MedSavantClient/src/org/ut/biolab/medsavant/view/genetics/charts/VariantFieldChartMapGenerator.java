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
import java.util.HashMap;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;

/**
 *
 * @author mfiume
 */
public class VariantFieldChartMapGenerator implements ChartMapGenerator {

    private final CustomField field;
    private final Table whichTable;

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

        //log scale
        if(isLogScaleX){
            bins.add(new Range(0,1));
            for(int i = 1; i < r.getMax(); i *= 10){
                bins.add(new Range(i, i*10));
            }

        //percent fields
        } else if ((field.getColumnType() == ColumnType.DECIMAL || field.getColumnType() == ColumnType.FLOAT) && r.getMax() - r.getMin() <= 1 && r.getMax() <= 1) {

            double step = 0.05;
            int numSteps = 20;
            for(int i = 0; i < numSteps; i++){
                bins.add(new Range(step * i, step * (i+1)));
            }

        //boolean fields
        } else if ((field.getColumnType() == ColumnType.INTEGER && Integer.parseInt(field.getColumnLength()) == 1) || field.getColumnType() == ColumnType.BOOLEAN){

            bins.add(new Range(0,1));
            bins.add(new Range(1,2));

        //other fields
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

    public ChartFrequencyMap generateChartMap(boolean isLogScaleX) throws SQLException, NonFatalDatabaseException, RemoteException {
        ChartFrequencyMap chartMap = new ChartFrequencyMap();

        if (isNumeric() && !field.getColumnName().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {

            String tablename = null;
            if (whichTable == Table.VARIANT) {
                tablename = ProjectController.getInstance().getCurrentTableName();
            } else if (whichTable == Table.PATIENT) {
                tablename = ProjectController.getInstance().getCurrentPatientTableName();
            }
            Range r = new Range(MedSavantClient.VariantQueryUtilAdapter.getExtremeValuesForColumn(
                    LoginController.sessionId,
                    tablename,
                    field.getColumnName()));

            List<Range> bins = generateBins(r, isLogScaleX);

            for(Range binrange : bins){
                if(Thread.currentThread().isInterrupted()){
                    return null;
                }
                if (whichTable == Table.VARIANT) {
                    chartMap.addEntry(
                            binrange.toString(),
                            MedSavantClient.VariantQueryUtilAdapter.getFilteredFrequencyValuesForColumnInRange(
                                LoginController.sessionId,
                                ProjectController.getInstance().getCurrentProjectId(),
                                ReferenceController.getInstance().getCurrentReferenceId(),
                                FilterController.getQueryFilterConditions(),
                                field.getColumnName(),
                                binrange.getMin(),
                                binrange.getMax())
                            );
                } else if (whichTable == Table.PATIENT) {

                    //get dna ids
                    List<String> individuals = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsWithValuesInRange(
                            LoginController.sessionId,
                            ProjectController.getInstance().getCurrentProjectId(),
                            field.getColumnName(),
                            binrange);

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
                        numVariants = MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                                LoginController.sessionId,
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
                chartMap.addAll(MedSavantClient.VariantQueryUtilAdapter.getFilteredFrequencyValuesForColumn(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectId(),
                        ReferenceController.getInstance().getCurrentReferenceId(),
                        FilterController.getQueryFilterConditions(),
                        field.getColumnName()));
            } else if (whichTable == Table.PATIENT) {
                Map<Object, List<String>> map = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForValues(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectId(),
                        field.getColumnName());
                Condition[][] filterConditions = FilterController.getQueryFilterConditions();

                if(field.getColumnName().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)){
                    map = modifyGenderMap(map);
                }

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
                        numVariants = MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                                LoginController.sessionId,
                                ProjectController.getInstance().getCurrentProjectId(),
                                ReferenceController.getInstance().getCurrentReferenceId(),
                                conditions);
                    }

                    //add entry
                    chartMap.addEntry(key.toString(),numVariants);

                }
            }

            //sort results
            if(whichTable == Table.VARIANT && field.getColumnName().equals(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM)){
                chartMap.sortKaryotypically();
            } else {
                chartMap.sort();
            }

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

    private Map<Object, List<String>> modifyGenderMap(Map<Object, List<String>> original){
        Map<Object, List<String>> result = new HashMap<Object, List<String>>();
        for (Object key : original.keySet()) {
            String s;
            if(key instanceof Long || key instanceof Integer){
                s = MiscUtils.genderToString(MiscUtils.safeLongToInt((Long)key));
            } else {
                s = MiscUtils.GENDER_UNKNOWN;
            }
            if(result.get(s) == null){
                result.put(s, original.get(key));
            } else {
                result.get(s).addAll(original.get(key));
            }
        }
        return result;
    }
}
