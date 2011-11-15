/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mfiume
 */
public class GenomicRegion {
    
    private String chrom;
    private Range range;

    public GenomicRegion(String chrom, Range range) {
        this.chrom = chrom;
        this.range = range;
    }

    public String getChrom() {
        return chrom;
    }

    public Range getRange() {
        return range;
    }

    @Override
    public String toString() {
        return "GenomicRegion{" + "chrom=" + chrom + ", range=" + range + '}';
    }
    
    public static Map<String, List<Range>> mergeGenomicRegions(List<GenomicRegion> regions){
        
        //separate by chr
        Map<String, List<Range>> chrMap = new HashMap<String, List<Range>>();
        for(GenomicRegion r : regions){
            if(chrMap.get(r.getChrom()) == null)
                chrMap.put(r.getChrom(), new ArrayList<Range>());
            chrMap.get(r.getChrom()).add(r.getRange());
        }
        
        //sort by start position
        Map<String, List<Range>> result = new HashMap<String, List<Range>>();
        for(String chrom: chrMap.keySet()){
            List<Range> list = chrMap.get(chrom);
            result.put(chrom, Range.merge(list));
        }
        
        return result;
    }
    
}
