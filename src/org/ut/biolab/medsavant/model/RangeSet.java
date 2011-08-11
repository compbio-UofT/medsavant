/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    // nn: potentially unstable
    public void addRange(String chr, Range range){
        List<Range> listRanges = ranges.get(chr);
        if(listRanges == null){
            listRanges = new ArrayList<Range>();
            ranges.put(chr, listRanges);
        }
        listRanges.add(range);
    }

    
    /*
     * Find only ranges that exist in both this rangeset and this new rangeset.
     * nn: potentially unstable
     */
    public void intersectAdd(RangeSet newRangeSet){
        // Get all the chromosomes we have in all.
        HashSet<String> allChrs = new HashSet<String>();
        Object[] thisChrs = this.getChrs();
        Object[] newChrs = newRangeSet.getChrs();
        for (Object o: thisChrs){
            allChrs.add((o + "").trim());
        }
        for (Object o: newChrs){
            allChrs.add((o + "").trim());
        }
        
        for (String chr: allChrs){
            List<Range> thisList = this.getRanges(chr);
            List<Range> newList = newRangeSet.getRanges(chr);
            if (thisList.isEmpty()){
                this.ranges.put(chr, newList);
            }
            else if (newList.isEmpty()){
            }
            else{
                List<List<Range>> listRanges = new ArrayList<List<Range>>();
                listRanges.add(thisList);
                listRanges.add(newList);
                List<Range> intersectingRanges = Range.getIntersectionList(listRanges);
                this.ranges.put(chr, intersectingRanges);
            }
        }
    }
    
    public Object[] getChrs(){
        return ranges.keySet().toArray();
    }
    
    public Map<String, List<Range>> getRanges(){
        return ranges;
    }
    
    // nn: potentially unstable
    public List<Range> getRanges(String chrName){
        if (ranges.get(chrName) == null){
            return new ArrayList<Range>();
        }
        return Range.merge(ranges.get(chrName));
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
