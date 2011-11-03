/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;

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
    
    public ChartFrequencyMap generateChartMap() throws SQLException, NonFatalDatabaseException {
        ChartFrequencyMap chartMap = new ChartFrequencyMap();
            
            
            if (isNumeric()) {

                String tablename = null;
                if(whichTable == Table.VARIANT){
                    tablename = ProjectController.getInstance().getCurrentTableName();
                } else if (whichTable == Table.PATIENT){
                    tablename = ProjectController.getInstance().getCurrentPatientTableName();
                }
                Range r = new Range(VariantQueryUtil.getExtremeValuesForColumn(tablename, field.getColumnName()));
                
                int numBins = 25;
                
                int min = (int) Math.floor(r.getMin());
                int max = (int) Math.ceil(r.getMax());
                
                double step = ((double) (max - min)) / numBins;

                for (int i = 0; i < numBins; i++) {
                    Range binrange = new Range((int) (min + i * step), (int) (min + (i + 1) * step));
                    if(whichTable == Table.VARIANT){                    
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
                    } else if (whichTable == Table.PATIENT){
                        
                        //get dna ids
                        List<String> individuals = PatientQueryUtil.getDNAIdsWithValuesInRange(ProjectController.getInstance().getCurrentProjectId(), field.getColumnName(), binrange);
                        
                        //create new dna id filter
                        Condition[] dnaConditions = new Condition[individuals.size()];
                        int pos = 0;
                        for(String ind : individuals){
                            dnaConditions[pos++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                        }
                        ComboCondition dnaCondition = ComboCondition.or(dnaConditions);
                        
                        //add dna ids as new condition
                        Condition[][] filterConditions = FilterController.getQueryFilterConditions();
                        Condition[][] conditions = new Condition[filterConditions.length][];
                        for(int j = 0; j < filterConditions.length; j++){
                            conditions[j] = new Condition[filterConditions[j].length+1];
                            System.arraycopy(filterConditions[j], 0, conditions[j], 0, filterConditions[j].length);
                            conditions[j][filterConditions[j].length] = dnaCondition;
                        }
                        if(filterConditions.length == 0){
                            conditions = new Condition[1][];
                            conditions[0] = new Condition[]{dnaCondition};
                        }
                        
                        //get num variants
                        int numVariants = 0;
                        if(individuals.size() > 0){
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
                try {
                    if(whichTable == Table.VARIANT){
                        chartMap.addAll(VariantQueryUtil.getFilteredFrequencyValuesForColumn(
                                ProjectController.getInstance().getCurrentProjectId(), 
                                ReferenceController.getInstance().getCurrentReferenceId(), 
                                FilterController.getQueryFilterConditions(), 
                                field.getColumnName()));
                    } else if (whichTable == Table.PATIENT){
                        Map<Object, List<String>> map = PatientQueryUtil.getDNAIdsForValues(ProjectController.getInstance().getCurrentProjectId(), field.getColumnName());
                        Condition[][] filterConditions = FilterController.getQueryFilterConditions();
                        for(Object key : map.keySet()){
                            
                            //create dnaid list
                            List<String> individuals = map.get(key);
                            Condition[] dnaConditions = new Condition[individuals.size()];
                            int pos = 0;
                            for(String ind : individuals){
                                dnaConditions[pos++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                            }
                            ComboCondition dnaCondition = ComboCondition.or(dnaConditions);
                            
                            //create new condition set
                            Condition[][] conditions = new Condition[filterConditions.length][];
                            for(int j = 0; j < filterConditions.length; j++){
                                conditions[j] = new Condition[filterConditions[j].length+1];
                                System.arraycopy(filterConditions[j], 0, conditions[j], 0, filterConditions[j].length);
                                conditions[j][filterConditions[j].length] = dnaCondition;
                            }
                            if(filterConditions.length == 0){
                                conditions = new Condition[1][];
                                conditions[0] = new Condition[]{dnaCondition};
                            }
                            
                            //calculate number of variants satisfying conditions
                            int numVariants = 0;
                            if(individuals.size() > 0){
                                numVariants = VariantQueryUtil.getNumFilteredVariants(
                                        ProjectController.getInstance().getCurrentProjectId(), 
                                        ReferenceController.getInstance().getCurrentReferenceId(), 
                                        conditions);
                            }
                            
                            //add entry
                            chartMap.addEntry(key.toString(),numVariants);
                            
                        }
                    }
                    
                    /*if (alias.equals(VariantTableSchema.ALIAS_GT)) {
                        for (FrequencyEntry fe : chartMap.getEntries()) {
                            if (fe.getKey().equals("0")) { fe.setKey("Unknown"); }
                            else if (fe.getKey().equals("1")) { fe.setKey("HomoRef"); }
                            else if (fe.getKey().equals("2")) { fe.setKey("HomoAlt"); }
                            else if (fe.getKey().equals("3")) { fe.setKey("Hetero"); }
                        }
                    }*/
                    
                    Collections.sort(chartMap.getEntries());
                } catch (Exception e) {
                    e.printStackTrace();
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
    
    
}
