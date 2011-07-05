/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author mfiume
 */
public class VariantFieldChartMapGenerator implements ChartMapGenerator {

    private static final TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
    private final DbColumn column;
    private final String alias;

    public VariantFieldChartMapGenerator(String colAlias) {
        this.column = table.getDBColumn(colAlias);
        this.alias = colAlias;
    }
    
    /*
    public VariantFieldChartMapGenerator(DbColumn col) {
        this.column = col;
    }
     * 
     */
    
    public ChartFrequencyMap generateChartMap() throws SQLException, NonFatalDatabaseException {
        ChartFrequencyMap chartMap = new ChartFrequencyMap();
            
            ColumnType type = table.getColumnType(column);
            
            if (TableSchema.isNumeric(type)) {
                
                Range r = QueryUtil.getExtremeValuesForColumn(ConnectionController.connect(), table, column);
                
                int numBins = 15;//getNumberOfQuantitativeCategories();
                
                int min = (int) Math.floor(r.getMin());
                int max = (int) Math.ceil(r.getMax());
                
                double step = ((double) (max - min)) / numBins;

                for (int i = 0; i < numBins; i++) {
                    Range binrange = new Range((int) (min + i * step), (int) (min + (i + 1) * step));
                    chartMap.addEntry(
                            binrange.toString(), 
                            QueryUtil.getFilteredFrequencyValuesForColumnInRange(ConnectionController.connect(), column, binrange)
                            );
                }

            } else {
                try {
                    chartMap.addAll(QueryUtil.getFilteredFrequencyValuesForColumn(ConnectionController.connect(), column));
                    Collections.sort(chartMap.getEntries());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return chartMap;
    }

    public boolean isNumeric() {
        TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
        ColumnType type = table.getColumnType(column);
        return TableSchema.isNumeric(type);
    }

    public String getName() {
        return alias;
    }
    
    
}
