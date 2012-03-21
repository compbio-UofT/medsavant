/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class ScatterChartMap implements Serializable {
    
    List<String> xRanges;
    List<String> yRanges;
    ScatterChartEntry[][] entries;
    
    public ScatterChartMap(List<String> xRanges, List<String> yRanges, List<ScatterChartEntry> entries){
        this.xRanges = xRanges;
        this.yRanges = yRanges;
        
        this.entries = new ScatterChartEntry[xRanges.size()][yRanges.size()];
        for(ScatterChartEntry e : entries){
            this.entries[xRanges.indexOf(e.getXRange())][yRanges.indexOf(e.getYRange())] = e;
        }
    }
    
    public ScatterChartEntry getValueAt(int x, int y){
        return entries[x][y];
    }
    
    public int getNumX(){
        return xRanges.size();
    }
    
    public int getNumY(){
        return yRanges.size();
    }
    
    public String getXValueAt(int x){
        return xRanges.get(x);
    }
    
    public String getYValueAt(int y){
        return yRanges.get(y);
    }
    
    public List<String> getXRanges(){
        return xRanges;
    }
    
    public List<String> getYRanges(){
        return yRanges;
    }
    
    public int getIndexOnX(String value){
        return xRanges.indexOf(value);
    }
    
    public int getIndexOnY(String value){
        return yRanges.indexOf(value);
    }
    
}
