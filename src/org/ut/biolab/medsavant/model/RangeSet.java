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
import java.util.Set;

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
     * nn: actually this is incorrect; see intersectAdd2
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
    
    public void intersectAdd2(RangeSet newRangeSet){
        
        if (this.isEmpty()){
            this.ranges = newRangeSet.ranges;
            return;
        }
        
        // This will be the new dictionary/map for this rangeset.
        Map<String, List<Range>> newRanges = new HashMap<String, List<Range>>();
        
        // Get only the chromosomes between this range set and the other rangeset.
        HashSet<String> chrsInCommon = new HashSet<String>();
        Object[] thisChrs = this.getChrs();
        Object[] newChrs = newRangeSet.getChrs();
        
        HashSet<String> thisChrSet = new HashSet<String>();
        for (Object o: thisChrs){
            thisChrSet.add(o + "");
        }
        
        for (Object o: newChrs){
            if (thisChrSet.contains(o + "")){
                chrsInCommon.add(o + "");
            }
        }
        
        // Find only the ranges for chromosomes in common.
        for (String chr: chrsInCommon){                        
            List<Range> thisList = this.getRanges(chr);
            List<Range> newList = newRangeSet.getRanges(chr);
            List<List<Range>> allRanges = new ArrayList<List<Range>>();
            allRanges.add(thisList);
            allRanges.add(newList);
            
            List<Range> intersectingRanges = Range.getIntersectionList(allRanges);
            if (!intersectingRanges.isEmpty()){
                newRanges.put(chr, intersectingRanges);
            }
        }
        
        if (newRanges.isEmpty()){
            Range range = new Range(23.0, 22.0);
            List<Range> ls = new ArrayList<Range>();
            ls.add(range);
            newRanges.put("chr1", ls);
        }        
        this.ranges = newRanges;
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
    
    public boolean isEmpty(){
        return this.ranges.isEmpty();
    }
    
    @Override
    public String toString(){
        return this.ranges + "";
    }
    
    /**
     * Does this range set make sense from a logical point of view?
     * @return 
     */
    public boolean isValidRangeSet(){
        
        Set<String> chroms = this.ranges.keySet();
        for (String chrom: chroms){
            List<Range> rangeList = this.ranges.get(chrom);
            for (Range range: rangeList){
                if (!range.isProperRange()){
                    return false;
                }
            }
        }
        return true;
    }
}
