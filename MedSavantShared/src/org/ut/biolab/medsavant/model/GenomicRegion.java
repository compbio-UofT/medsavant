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

package org.ut.biolab.medsavant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.util.ChromosomeComparator;

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
    
    public static Map<String, List<Range>> mergeGenomicRegions(GenomicRegion[] regions) {
        
        //separate by chr
        Map<String, List<Range>> chrMap = new HashMap<String, List<Range>>();
        for (GenomicRegion r: regions) {
            if (chrMap.get(r.getChrom()) == null) {
                chrMap.put(r.getChrom(), new ArrayList<Range>());
            }
            chrMap.get(r.getChrom()).add(new Range(r.getStart(), r.getEnd()));
        }
        
        //sort by start position
        Map<String, List<Range>> result = new HashMap<String, List<Range>>();
        for (String chrom: chrMap.keySet()) {
            List<Range> list = chrMap.get(chrom);
            result.put(chrom, Range.merge(list));
        }
        
        return result;
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
