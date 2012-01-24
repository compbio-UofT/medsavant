/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;

/**
 *
 * @author mfiume
 */
public interface ChartMapGenerator {

    public ChartFrequencyMap generateChartMap(boolean useFilterConditions, boolean isLogScaleX) throws Exception;

    public boolean isNumeric();

    public String getName();

    public Table getTable();

    // usually the column name
    public String getFilterId();
}
