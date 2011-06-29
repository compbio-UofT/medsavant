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
    
    public static void main(String[] args){
        
        List<Range> range1 = new ArrayList<Range>();
        List<Range> range2 = new ArrayList<Range>();
        range1.add(new Range(2, 6));
        range1.add(new Range(2, 5));        
        range1.add(new Range(1, 3));
        range2.add(new Range(7, 8));
        range2.add(new Range(4, 6));
        range2.add(new Range(9, 12));
        range1.add(new Range(10, 11));

        List<Range> res = merge(range1, range2);
        System.out.println(res);
    }
    
    /**
     * Return the list of ranges which result from merging the ranges from 
     * range1 with those from range2. Note that we work under the assumption 
     * that min less or equal to max.  Otherwise, use at your own risk 
     * (behaviour untested for).
     * @param range1 the first list of ranges.
     * @param range2 the second list of ranges.
     * @return the merged list.
     * @author Nirvana
     */
    public static List<Range> merge(List<Range> range1, List<Range> range2){

        List<Range> mergedList = new ArrayList<Range>();
        
        // Arrange first in order by starting positions.
        TreeSet<Range> allRangesInOrder = new TreeSet<Range>();
        for (Range currRange:range1){
            allRangesInOrder.add(currRange);
        }
        for (Range currRange: range2){
            allRangesInOrder.add(currRange);
        }
        
        System.out.print("Arranging in order:\t");
        System.out.println(allRangesInOrder);
        
        // Merge now.
        Range currMerged = null;

        for (Range currRange: allRangesInOrder){
            // To detect the beginning:
            if (currMerged == null){
                currMerged = currRange;
                mergedList.add(currMerged);
            }
            else if (currMerged.canBeMergedWith(currRange)){
                // merge them here
                currMerged.max = Math.max(currMerged.max, currRange.max);
                currMerged.min = Math.min(currMerged.min, currRange.min);
            }
            else{
                currMerged = currRange;
                mergedList.add(currMerged);
            }
        }
        return mergedList;
    }
    
    
    /**
     * Get the list you get by merging the Range objects in this list.
     * @param range
     * @return the merged list.
     * @author Nirvana
     */
    public static List<Range> merge(List<Range> range){
        
        return merge(range, new ArrayList());
    }
    


    /**
     * Compare two Range objects by starting point.  When starting points are 
     * the same, differentiate by ending point.
     * @param rangeObj
     * @return 
     * @author Nirvana
     */
    public int compareTo(Object rangeObj) {
        Range range = (Range) rangeObj;
        if (this.min < range.min){
            return -1;
        }
        else if (this.min > range.min){
            return 1;
        }
        // If both have the same min, differentiate by max.
        else{
            if (this.max < range.max){
                return -1;
            }
            else if (this.max > range.max){
                return 1;
            }
            else{
                return 0;
            }
        }
    }
    
    @Override
    /**
     * This range object equals another object iff the other is a range object,
     * and has the same min and max as this object.  This complies with the 
     * comparator method (compareTo).
     * @author Nirvana
     */
    public boolean equals(Object r){
        try{
            Range range = (Range)r;
            return range.min == this.min && range.max == this.max;
        }
        catch(Exception e){
            return false;
        }
    }
    
    /**
     * Returns true iff this range and the range given intersect (ie, can be
     * merged).
     * @param range
     * @return 
     * @author Nirvana
     */
    public boolean canBeMergedWith(Range range){
        return (this.min <= range.max && range.max <= this.max) || 
                (range.min <= this.max && this.max <= range.max) || 
                (this.min >= range.min && this.max <= range.max) ||
                (range.min >= this.min && range.max <= this.max);
    }
    
    @Override
    /**
     * @author Nirvana
     */
    public String toString(){
        return "Range(" + min + ", " + max + ")";
    }
    
}
