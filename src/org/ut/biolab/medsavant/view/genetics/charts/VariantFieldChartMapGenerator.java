/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import java.sql.SQLException;
import java.util.Collections;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;

/**
 *
 * @author mfiume
 */
public class VariantFieldChartMapGenerator implements ChartMapGenerator {

    //private static final TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
    //private final DbColumn column;
    private final CustomField field;

    public VariantFieldChartMapGenerator(CustomField field) {
        //this.column = table.getDBColumn(colAlias);
        //this.alias = colAlias;
        this.field = field;
    }
    
    /*
    public VariantFieldChartMapGenerator(DbColumn col) {
        this.column = col;
    }
     * 
     */
    
    public ChartFrequencyMap generateChartMap() throws SQLException, NonFatalDatabaseException {
        ChartFrequencyMap chartMap = new ChartFrequencyMap();
            
            //ColumnType type = table.getColumnTypeString(column);
            
            if (isNumeric()) {

                //Range r = QueryUtil.getExtremeValuesForColumn(ConnectionController.connect(), table, column);
                //Range r = new Range(VariantQueryUtil.getExtremeValuesForColumn(ProjectController.getInstance().getCurrentTableName(), column.getColumnNameSQL()));
                Range r = new Range(VariantQueryUtil.getExtremeValuesForColumn(ProjectController.getInstance().getCurrentTableName(), field.getColumnName()));
                
                int numBins = 25;//getNumberOfQuantitativeCategories();
                
                int min = (int) Math.floor(r.getMin());
                int max = (int) Math.ceil(r.getMax());
                
                double step = ((double) (max - min)) / numBins;

                for (int i = 0; i < numBins; i++) {
                    Range binrange = new Range((int) (min + i * step), (int) (min + (i + 1) * step));
                    chartMap.addEntry(
                            binrange.toString(), 
                            VariantQueryUtil.getFilteredFrequencyValuesForColumnInRange(
                                ProjectController.getInstance().getCurrentProjectId(), 
                                ReferenceController.getInstance().getCurrentReferenceId(), 
                                FilterController.getQueryFilterConditions(), 
                                field.getColumnName(), 
                                binrange.getMin(),
                                binrange.getMax()) 
                            //QueryUtil.getFilteredFrequencyValuesForColumnInRange(ConnectionController.connect(), column, binrange)
                            );
                }

            } else {
                try {
                    chartMap.addAll(VariantQueryUtil.getFilteredFrequencyValuesForColumn(
                            ProjectController.getInstance().getCurrentProjectId(), 
                            ReferenceController.getInstance().getCurrentReferenceId(), 
                            FilterController.getQueryFilterConditions(), 
                            field.getColumnName()));
                    //chartMap.addAll(QueryUtil.getFilteredFrequencyValuesForColumn(ConnectionController.connect(), column));
                    
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
        //TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
        //ColumnType type = table.getColumnTypeString(column);
        //return TableSchema.isNumeric(type);
        ColumnType type = field.getColumnType();
        return type.equals(ColumnType.DECIMAL) || type.equals(ColumnType.FLOAT) || type.equals(ColumnType.INTEGER);
    }

    public String getName() {
        //return alias;
        return field.getAlias();
    }
    
    
}
