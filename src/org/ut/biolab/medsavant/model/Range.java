package org.ut.biolab.medsavant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mfiume
 */
public class Range implements Comparable{
    
    private double min;
    private double max;

    public Range(double min, double max) {
        this.min = min;
        this.max = max;
    }
    
    
    public double getMin(){
        return min;
    }
    
    public double getMax(){
        return max;
    }
    
    /**
     * Return the list of ranges which result from merging the ranges from 
     * range1 with those from range2.
     * @param range1
     * @param range2
     * @return 
     */
    public List<Range> merge(List<Range> range1, List<Range> range2){
        
        List<Range> mergedList = new ArrayList<Range>();
        // Arrange first in order by starting positions.
        TreeSet<Range> allRangesInOrder = new TreeSet<Range>();
        range1.addAll(range2);
        for (Range currRange:range1){
            allRangesInOrder.add(currRange);
        }
        // Merge now.
        Range currMerged = null;

        for (Range currRange: allRangesInOrder){
            if (currMerged == null){
                currMerged = currRange;
                mergedList.add(currMerged);
            }
            else if (currMerged.canBeMergedWith(currRange)){
                // merge them here
                currMerged.max = Math.max(currMerged.max, currRange.max);
            }
            else{
                currMerged = currRange;
                mergedList.add(currMerged);
            }
        }
        return mergedList;
    }

    /**
     * Compare two Range objects by starting point.
     * @param rangeObj
     * @return 
     */
    public int compareTo(Object rangeObj) {
        Range range = (Range) rangeObj;
        if (this.min < range.min)
            return -1;
        else if (this.min > range.min)
            return 1;
        else
            return 0;
    }
    
    /**
     * Returns true iff this range and the range given intersect.
     * @param range
     * @return 
     */
    public boolean canBeMergedWith(Range range){
        return (this.min <= range.max && range.max <= this.max) || 
                (range.min <= this.max && this.max <= range.max);
    }
    
}
