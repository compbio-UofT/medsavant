/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author mfiume
 */
public class Range implements Comparable<Range>, Serializable {

    private static final Log LOG = LogFactory.getLog(Range.class);
    private static final String RANGE_STRING = " - ";


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

    /**
     * Get the list you get by merging the Range objects in the collection
     *
     * @param range
     * @return the merged list.
     */
    public static List<Range> merge(Collection<Range> range) {
        List<Range> mergedList = new ArrayList<Range>();

        // Arrange first in order by starting positions.
        TreeSet<Range> allRangesInOrder = new TreeSet<Range>();
        allRangesInOrder.addAll(range);

        // Merge now.
        Range curMerged = null;

        for (Range r: allRangesInOrder) {
            if (curMerged != null && curMerged.canBeMergedWith(r)) {
                // merge them here
                curMerged.max = Math.max(curMerged.max, r.max);
                curMerged.min = Math.min(curMerged.min, r.min);
            } else {
                if (curMerged != null) {
                    mergedList.add(curMerged);
                }
                curMerged = new Range(r.min, r.max);
            }
        }
        // Merge in the last one.
        if (curMerged != null) {
            mergedList.add(curMerged);
        }
        return mergedList;
    }


    /**
     * Compare two Range objects by starting point.  When starting points are
     * the same, differentiate by ending point.
     * @param range
     * @return
     * @author Nirvana
     */
    @Override
    public int compareTo(Range range) {
        if (min < range.min) {
            return -1;
        } else if (min > range.min) {
            return 1;
        } else{
            // If both have the same min, differentiate by max.
            if (max < range.max) {
                return -1;
            } else if (max > range.max) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * This range object equals another object iff the other is a range object,
     * and has the same min and max as this object.  This complies with the
     * comparator method (compareTo).
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof Range) {
            Range r = (Range)that;
            return r.min == min && r.max == max;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(this.min) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(this.max) >>> 32));
        return hash;
    }

    /**
     * Returns true iff this range and the range given intersect (i.e. can be merged).
     */
    public boolean canBeMergedWith(Range r) {
        return min <= r.max && max >= r.min;
    }

    @Override
    public String toString() {
        return NumberFormat.getInstance().format(min) + RANGE_STRING + NumberFormat.getInstance().format(max);
    }

    public static Range rangeFromString(String range) {
        String[] values = range.split(RANGE_STRING);
        double minValue = 0;
        double maxValue = 0;
        try {
            minValue = NumberFormat.getInstance().parse(values[0]).doubleValue();
            maxValue = NumberFormat.getInstance().parse(values[1]).doubleValue();
        } catch (ParseException ex) {
            LOG.error("Unable to parse " + range + " as a range.", ex);
        }
        return new Range(minValue, maxValue);
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