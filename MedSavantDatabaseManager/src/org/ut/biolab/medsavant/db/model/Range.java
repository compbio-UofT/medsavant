/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.db.model;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author mfiume
 */
public class Range implements Comparable<Range> {
    
    private double min;
    private double max;

    public Range(double min, double max) {
        this.min = min;
        this.max = max;
    }
    
    public Range(double[] rangeArray) {
        this.min = rangeArray[0];
        this.max = rangeArray[1];
    }
    
    
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }
    
    public static void main(String[] args) {
        
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
    public static List<Range> merge(List<Range> range1, List<Range> range2) {

        List<Range> mergedList = new ArrayList<Range>();
        
        // Arrange first in order by starting positions.
        TreeSet<Range> allRangesInOrder = new TreeSet<Range>();
        for (Range currRange:range1) {
            allRangesInOrder.add(currRange);
        }
        for (Range currRange: range2) {
            allRangesInOrder.add(currRange);
        }
        
        // Merge now.
        Range currMerged = null;

        for (Range currRange: allRangesInOrder) {
            // To detect the beginning:
            if (currMerged == null) {
                currMerged = new Range(currRange.min, currRange.max);
                mergedList.add(currMerged);
            }
            else if (currMerged.canBeMergedWith(currRange)) {
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
     * @author Nirvana
     */
    public static List<Range> merge(List<Range> range) {
        return merge(range, new ArrayList<Range>());
    }
    


    /**
     * Compare two Range objects by starting point.  When starting points are 
     * the same, differentiate by ending point.
     * @param range
     * @return 
     * @author Nirvana
     */
    public int compareTo(Range range) {
        if (this.min < range.min) {
            return -1;
        }
        else if (this.min > range.min) {
            return 1;
        }
        // If both have the same min, differentiate by max.
        else{
            if (this.max < range.max) {
                return -1;
            }
            else if (this.max > range.max) {
                return 1;
            }
            else{
                return 0;
            }
        }
    }
    
    /**
     * This range object equals another object iff the other is a range object,
     * and has the same min and max as this object.  This complies with the 
     * comparator method (compareTo).
     * @author Nirvana
     */
    @Override
    public boolean equals(Object r) {
        try{
            Range range = (Range)r;
            return range.min == this.min && range.max == this.max;
        }
        catch(Exception e) {
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
    public boolean canBeMergedWith(Range range) {
        return (this.min <= range.max && range.max <= this.max) || 
                (range.min <= this.max && this.max <= range.max) || 
                (this.min >= range.min && this.max <= range.max) ||
                (range.min >= this.min && range.max <= this.max);
    }
    
    @Override
    public String toString() {
        return NumberFormat.getInstance().format(min) + " - " + NumberFormat.getInstance().format(max);
    }
    
    /**
     * Forces proper ordering and limits. 
     */
    public void bound(int min, int max, boolean defaultMin) {
        if(this.min > this.max) {
            if(defaultMin) {
                this.max = this.min;
            } else {
                this.min = this.max;
            }
        }
        if(this.min < min || this.min > max) {
            this.min = Math.min(max, Math.max(min, this.min));
        }
        if(this.max > max || this.max < min) {
            this.max = Math.max(min, Math.min(max, this.max));
        }
    }
    
}