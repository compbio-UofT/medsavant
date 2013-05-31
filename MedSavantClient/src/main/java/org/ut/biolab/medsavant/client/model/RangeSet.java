/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
