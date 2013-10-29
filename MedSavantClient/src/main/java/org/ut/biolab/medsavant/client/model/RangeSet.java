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
package org.ut.biolab.medsavant.client.model;

import org.ut.biolab.medsavant.shared.model.Range;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author AndrewBrook
 */
public class RangeSet {

    Map<String, List<Range>> ranges;

    public RangeSet(){
        ranges = new HashMap<String, List<Range>>();
    }

    public void addRange(String chr, Range range){
        if(ranges.get(chr) == null){
            ranges.put(chr, new ArrayList<Range>());
        }
        //TODO union with current set
    }

    /*
     * Find only ranges that exist in both ranges and newRanges
     */
    public void intersectRanges(Map<String, List<Range>> newRanges){
        //TODO find the intersection of the two sets: ranges and newRanges
    }

    public void merge(RangeSet newRangeSet){
        intersectRanges(newRangeSet.getRanges());
    }

    public Object[] getChrs(){
        return ranges.keySet().toArray();
    }

    public Map<String, List<Range>> getRanges(){
        return ranges;
    }

    public List<Range> getRanges(String chrName){
        return ranges.get(chrName);
    }

    public Range getRange(String chr, int index){
        List<Range> l = ranges.get(chr);
        if(l == null) return null;
        return l.get(index);
    }

    public int getSize(){
        int result = 0;
        Object[] keys = getChrs();
        for(Object o : keys){
            result += ranges.get((String)o).size();
        }
        return result;
    }

}
