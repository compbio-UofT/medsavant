/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shared.model;

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
        if(x < 0 || x >= getNumX() || y < 0 || y >= getNumY()){
            return null;
        }
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

    public int getMaxFrequency(){
        int max = 0;
        for(int x = 0; x < getNumX(); x++){
            for(int y = 0; y < getNumY(); y++){
                if(getValueAt(x, y) != null){
                    max = Math.max(max, getValueAt(x, y).getFrequency());
                }
            }
        }
        return max;
    }

}
