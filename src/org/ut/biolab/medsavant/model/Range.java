package org.ut.biolab.medsavant.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.ut.biolab.medsavant.view.util.ViewUtil;

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
        
//        List<Range> range1 = new ArrayList<Range>();
//        List<Range> range2 = new ArrayList<Range>();
//        range1.add(new Range(2, 6));
//        range1.add(new Range(2, 5));        
//        range1.add(new Range(1, 3));
//        range2.add(new Range(7, 8));
//        range2.add(new Range(4, 6));
//        range2.add(new Range(9, 12));
//        range1.add(new Range(10, 11));
//
//        List<Range> res = merge(range1, range2);
//        System.out.println(res);
        
        // Very crude test of whether intersecting method works.
        List<Range> ls = new ArrayList<Range>();
        ls.add(new Range(1, 6));
        ls.add(new Range(2, 8));
        ls.add(new Range(5, 10));
        ls.add(new Range(4, 9));
        ls.add(new Range(3, 7));
        ls.add(new Range(1, 5));
        System.out.println(Range.getIntersection(ls));
    }
    
    /**
     * Return the list of ranges which result from merging the ranges from 
     * range1 with those from range2. Note that we work under the assumption 
     * that min less or equal to max.  Otherwise, use at your own risk 
     * (behaviour untested for).
     * @param range1 the first list of ranges.
     * @param range2 the second list of ranges.
     * @return the merged list.
     * @author nnursimulu
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
        
//        System.out.print("Arranging in order:\t");
//        System.out.println(allRangesInOrder);
        
        // Merge now.
        Range currMerged = null;

        for (Range currRange: allRangesInOrder){
            // To detect the beginning:
            if (currMerged == null){
                currMerged = new Range(currRange.min, currRange.max);
                mergedList.add(currMerged);
            }
            else if (currMerged.intersectsWith(currRange)){
                // merge them here
                currMerged.max = Math.max(currMerged.max, currRange.max);
                currMerged.min = Math.min(currMerged.min, currRange.min);
            }
            else{
                currMerged = new Range(currRange.min, currRange.max);
                mergedList.add(currMerged);
            }
        }
        return mergedList;
    }
    
    
    /**
     * Get the list you get by merging the Range objects in this list.
     * @param range
     * @return the merged list.
     * @author nnursimulu
     */
    public static List<Range> merge(List<Range> range){
        
        return merge(range, new ArrayList());
    }
    


    /**
     * Compare two Range objects by starting point.  When starting points are 
     * the same, differentiate by ending point.
     * @param rangeObj
     * @return 
     * @author nnursimulu
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
     * @author nnursimulu
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
     * @author nnursimulu
     */
    public boolean intersectsWith(Range range){
        return (this.min <= range.max && range.max <= this.max) || 
                (range.min <= this.max && this.max <= range.max) || 
                (this.min >= range.min && this.max <= range.max) ||
                (range.min >= this.min && range.max <= this.max);
    }
    
    @Override
    /**
     * @author nnursimulu
     */
    public String toString(){
        return ViewUtil.numToString(min) + " - " + ViewUtil.numToString(max);
    }
    
    /**
     * @author nnursimulu
     * @param listRanges
     * @return 
     */
    public static List<Range> getIntersectionList(List<List<Range>> listRanges){
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Return intersection among ranges. This is just the one range common to 
     * many ranges.
     * @author nnursimulu
     * @param ranges ranges to be intersected upon.
     * @return 
     */
    public static Range getIntersection(List<Range> ranges){
        
        // Sort the intervals in the ranges list (size = n) by decreasing size.
        // Henceforth, interval 1 > interval 2 > ... > interval n
        List<Range> sortedByDecreasingSize = sortByDecreasingSize(ranges);
        
        // Initialise intersectingRange to be a copy of interval 1.
        Range intersectingRange = 
                new Range(sortedByDecreasingSize.get(0).min, 
                sortedByDecreasingSize.get(0).max);
        
        Range curr;
        // For interval i = 2 ... n:
        for (int i = 1; i < sortedByDecreasingSize.size(); i++){            
            curr = sortedByDecreasingSize.get(i); 
            
            // intersectingRange = intersect(interval i, intersectingRange)        
            // if intersectingRange is empty: return null.
            intersectingRange = getIntersection(curr, intersectingRange);
            
            if (intersectingRange == null){
                return null;
            }
        }
        // Return intersectingRange.
        return intersectingRange;
    }
    
    /**
     * Get the intersection between range1, and range2.  null in case they don't
     * intersect.
     * @param range1
     * @param range2
     * @return the intersection.
     */
    public static Range getIntersection(Range range1, Range range2){
        // No intersection.
        if (!range1.intersectsWith(range2)){
            return null;
        }
        return new Range(Math.max(range1.min, range2.min), Math.min(range1.max, range2.max));
    }
    
    private static List<Range> sortByDecreasingSize(List<Range> ranges){
        
        TreeSet<Double> sortedLengths = new TreeSet<Double>();
        List<Range> sortedByDecreasingSize = new ArrayList<Range>();
        
        HashMap<Double, List<Range>> map = new HashMap<Double, List<Range>>();
        for (Range range: ranges){
            Double length = range.getLength();
            List<Range> ls = map.get(length);
            
            if (ls == null){
                ls = new ArrayList<Range>();
                map.put(length, ls);
            }
            ls.add(range);
            sortedLengths.add(length);
        }
        
        Iterator ite = sortedLengths.descendingIterator();
        
        while (ite.hasNext()){
            Double length = (Double)ite.next();
            sortedByDecreasingSize.addAll(map.get(length));
        }
        return sortedByDecreasingSize;
    }
    
    public double getLength(){
        return this.max - this.min;
    }
    
    /**
     * Forces proper ordering and limits. 
     */
    public void bound(int min, int max, boolean defaultMin){
        if(this.min > this.max){
            if(defaultMin){
                this.max = this.min;
            } else {
                this.min = this.max;
            }
        }
        if(this.min < min || this.min > max){
            this.min = Math.min(max, Math.max(min, this.min));
        }
        if(this.max > max || this.max < min){
            this.max = Math.max(min, Math.min(max, this.max));
        }
    }
    
}
