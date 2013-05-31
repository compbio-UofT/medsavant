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
import java.util.*;

import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;


/**
 *
 * @author mfiume
 */
public class GenomicRegion implements Serializable, Comparable<GenomicRegion> {

    private final String name;
    private final String chrom;
    private final int start;
    private final int end;

    public GenomicRegion(String name, String chrom, int start, int end) {
        this.name = name;
        this.chrom = chrom;
        this.start = start;
        this.end = end;
    }

    /**
     * This toString method is used only when serialising a saved filter-state to XML.
     */
    @Override
    public String toString() {
        return String.format("%s%s:%d-%d", name != null ? name + " " : "", chrom, start, end);
    }

    /**
     * The inverse of <c>toString</c>, used when loading a saved filter-state from XML.
     */
    public static GenomicRegion fromString(String str) {
        String name = null, chrom = null;
        int start = 0, end = 0;

        String[] strs = str.split(":");
        if (strs.length == 2) {
            int pos = strs[0].lastIndexOf(' ');
            if (pos > 0) {
                name = strs[0].substring(0, pos);
                chrom = strs[0].substring(pos + 1);
            } else {
                // No name, just a chromosome.
                chrom = strs[0];
            }

            strs = strs[1].split("-");
            if (strs.length == 2) {
                start = Integer.valueOf(strs[0]);
                end = Integer.valueOf(strs[1]);
            }
        }
        return new GenomicRegion(name, chrom, start, end);
    }

    public String getName() {
        return name;
    }

    public String getChrom() {
        return chrom;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public static Map<String, List<Range>> mergeGenomicRegions(Collection<GenomicRegion> regions) {

        //separate by chr
        Map<String, List<Range>> chrMap = new HashMap<String, List<Range>>();
        for (GenomicRegion r: regions) {
            if (r != null) {
                if (chrMap.get(r.getChrom()) == null) {
                    chrMap.put(r.getChrom(), new ArrayList<Range>());
                }
                chrMap.get(r.getChrom()).add(new Range(r.getStart(), r.getEnd()));
            }
        }

        //sort by start position
        Map<String, List<Range>> result = new HashMap<String, List<Range>>();
        for (String chrom: chrMap.keySet()) {
            List<Range> list = chrMap.get(chrom);
            result.put(chrom, Range.merge(list));
        }

        return result;
    }

    public int getLength() {
        return this.getEnd()-this.getStart()+1;
    }

    @Override
    public int compareTo(GenomicRegion t) {
        if (!chrom.equals(t.chrom)) {
            return new ChromosomeComparator().compare(chrom, t.chrom);
        }
        if (start != t.start) {
            return start - t.start;
        }
        if (end != t.end) {
            return end - t.end;
        }
        return name.compareTo(t.name);
    }


}
