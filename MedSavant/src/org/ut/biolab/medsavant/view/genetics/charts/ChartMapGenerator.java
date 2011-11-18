/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import java.util.Map;

/**
 *
 * @author mfiume
 */
public interface ChartMapGenerator {
    
    public ChartFrequencyMap generateChartMap(boolean isLogScaleX) throws Exception;
    
    public boolean isNumeric();

    public String getName();
}
