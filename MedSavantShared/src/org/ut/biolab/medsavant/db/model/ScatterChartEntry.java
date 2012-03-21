/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

import java.io.Serializable;

/**
 *
 * @author Andrew
 */
public class ScatterChartEntry implements Serializable {
    
    private String xRange;
    private String yRange;
    private int frequency;
    
    public ScatterChartEntry(String xRange, String yRange, int frequency){
        this.xRange = xRange;
        this.yRange = yRange;
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getXRange() {
        return xRange;
    }

    public String getYRange() {
        return yRange;
    }
    
}
