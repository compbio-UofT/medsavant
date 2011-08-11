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

    /**
     * Declare a Range object.  (1) Note that we are assuming BED-formatting. In 
     * other words, min uses 0-indexing, max uses 1-indexing.  (2) Make sure
     * that min is less than max. Use "illegal" ranges (max less than min) at 
     * your own risk and peril. No guarantee that this code will work at all if
     * you do that.
     * @param min the min defining this Range object
     * @param max the max defining this Range object.
     */
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
//        range1.add(new Range(2, 3));
//        range1.add(new Range(9, 12));        
//        range1.add(new Range(5, 8));
//        range2.add(new Range(1, 2));
//        range2.add(new Range(4, 6));
//        range2.add(new Range(12, 13));
//        range1.add(new Range(10, 11));
//
//        List<Range> res = merge(range1, range2);
//        System.out.println(res);
        
        // Very crude test of whether intersecting method works.
//        List<Range> ls = new ArrayList<Range>();
//        ls.add(new Range(1, 6));
//        ls.add(new Range(2, 8));
//        ls.add(new Range(5, 10));
//        ls.add(new Range(4, 9));
//        ls.add(new Range(3, 7));
//        ls.add(new Range(1, 6));
//        System.out.println(Range.getIntersection(ls));
        
        // Very very crude test that the other (more complicated BUT useful) intersection method works.
//        List<List<Range>> listRangeSet = new ArrayList<List<Range>>();
//        
//        List<Range> list = new ArrayList<Range>();
//        list.add(new Range(1, 3));
//        list.add(new Range(4, 5));
//        list.add(new Range(9, 11));
//        list.add(new Range(13, 15));
//        listRangeSet.add(list);
//        
//        list = new ArrayList<Range>();
//        list.add(new Range(0, 2));
//        list.add(new Range(5, 7));
//        list.add(new Range(10, 14));
//        listRangeSet.add(list);
//        
//        list = new ArrayList<Range>();
//        list.add(new Range(0, 2));
//        list.add(new Range(4, 6));
//        list.add(new Range(8, 12));
//        list.add(new Range(13, 15));
//        listRangeSet.add(list);
//        
//        List<Range> ls = new ArrayList<Range>();
//        ls.add(new Range(3, 7));
//        ls.add(new Range(9, 15));
//        ls.add(new Range(23, 27));
//        ls.add(new Range(29.5, 33.5));
//        listRangeSet.add(ls);
//        
//        ls = new ArrayList<Range>();
//        ls.add(new Range(1, 4));
//        ls.add(new Range(5, 11));
//        ls.add(new Range(18, 21));
//        ls.add(new Range(22, 35)); 
//        listRangeSet.add(ls);
//        
//        ls = new ArrayList<Range>();
//        ls.add(new Range(2, 8));
//        ls.add(new Range(10, 13));
//        ls.add(new Range(16, 17));
//        ls.add(new Range(20, 24));
//        ls.add(new Range(26, 28));
//        ls.add(new Range(31, 32));
//        ls.add(new Range(34, 36));
//        listRangeSet.add(ls);
//        
//        ls = new ArrayList<Range>();
//        ls.add(new Range(6, 12));
//        ls.add(new Range(14, 19));
//        ls.add(new Range(22, 25));
//        ls.add(new Range(29, 30));
//        ls.add(new Range(33, 33.6));
//        listRangeSet.add(ls);
//        System.out.println(Range.getIntersectionList(listRangeSet));
        
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
            if (currMerged == null || !currMerged.canBeMergedWith(currRange)){
                currMerged = new Range(currRange.min, currRange.max);
                mergedList.add(currMerged);
            }
            else{
                // merge them here; do not use the merge method because
                // we need to be using the same object.
                currMerged.min = Math.min(currRange.min, currMerged.min);
                currMerged.max = Math.max(currRange.max, currMerged.max);
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
    
    public static Range merge(Range range1, Range range2){
        if (!range1.canBeMergedWith(range2)){
            return null;
        }
        else{
            return new Range
                    (Math.min(range1.min, range2.min), 
                    Math.max(range1.max, range2.max));
        }
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
     * merged).  Note the assumption that BED formatting is in use.
     * @param range
     * @return 
     * @author nnursimulu
     */
    public boolean intersectsWith(Range range){
        if (this.max == range.min || this.min == range.max){
            return false;
        }
        return (this.min <= range.max && range.min <= this.max) || 
                (range.min <= this.max && this.min <= range.max);
    }
    
    /**
     * Merging and intersection criteria are different!  For example, 1 - 2 and 
     * 2 - 3 do not intersect, but can certainly be merged to get 1 - 3.
     * @param range
     * @return 
     */
    public boolean canBeMergedWith(Range range){
        return (this.min <= range.max && range.min <= this.max) || 
                (range.min <= this.max && this.min <= range.max);
    }
    
    @Override
    public String toString(){
        return ViewUtil.numToString(min) + " - " + ViewUtil.numToString(max);
    }
    
    /**
     * Get the intersection between many lists.  Note the key assumption 
     * that the intervals within each list have already been merged beforehand.
     * @author nnursimulu
     * @param listRanges
     * @return a list containing conflicting intervals.
     */
    public static List<Range> getIntersectionList(List<List<Range>> listRanges){
        
        if (listRanges.isEmpty()){
            return new ArrayList<Range>();
        }
        
        // List to contain conflicting intervals.
        List<Range> conflictingList = new ArrayList<Range>();
        
        // Starts off being the first list.
        for (Range range: listRanges.get(0)){
            conflictingList.add(range);
        }
        
        List<Range> list1 = new ArrayList<Range>();
        
        // For each pair of lists...
        // Start with list 1 and list 2; then, conflict. list and list 3; then
        // conflict. list and list 4 etc.  Note that the conflict. list 
        // is expected to change at each iteration of this for loop.
        for (int indexList = 1; indexList < listRanges.size(); indexList++){
            list1.clear();
            list1.addAll(conflictingList);
            List<Range> list2 = listRanges.get(indexList); 
            conflictingList.clear();

            // Do the following until we are done for all lists.
            // Take the first list and the second list and sort the ranges within by start order.
            TreeSet<Range> sortedRanges = new TreeSet<Range>();
            for (Range range: list1){
                Range rangeCopy = new Range(range.min, range.max);
                sortedRanges.add(rangeCopy);
            }
            for (Range range: list2){
                Range rangeCopy = new Range(range.min, range.max);
                sortedRanges.add(rangeCopy);
            }

//            System.out.println("Sorted Ranges");
//            System.out.println(sortedRanges);
//            System.out.println();
            
            Range mergedRange = null;
            Range newConflictingRange;
            Iterator<Range> ite = sortedRanges.iterator();
            
            // For each sorted element, ask yourself the following:
            // Does this interval intersect with the big merged interval.
            //      Yes : take the conflict, and keep track of it.
            //      No  : only make the merged interval be this interval. Then, carry on.
            while (ite.hasNext()){
                Range curr = ite.next();
                if (mergedRange == null || !curr.intersectsWith(mergedRange)){
                    mergedRange = new Range(curr.min, curr.max);
                }
                else{
                    newConflictingRange = getIntersection(mergedRange, curr);
                    mergedRange = merge(mergedRange, curr);
                    conflictingList.add(newConflictingRange);
                }
            }
        }
        return conflictingList;
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
