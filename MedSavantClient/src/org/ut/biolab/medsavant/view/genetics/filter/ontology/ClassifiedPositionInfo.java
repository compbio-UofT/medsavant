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
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author Nirvana Nursimulu
 */
public class ClassifiedPositionInfo {
    
    
    /**
     * Name of the column with chromosome info.
     */
    public final static String CHROM_COL = MedSavantDatabase.DefaultvariantTableSchema.getFieldAlias(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM);
    
    /**
     * Name of the column with position info.
     */
    public final static String POSITION_COL = MedSavantDatabase.DefaultvariantTableSchema.getFieldAlias(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION);

    
    /**
     * Map of chromosomes to a list of ranges for the corresponding chromosome)
     */
    private HashMap<String, List<Range>> mapChrToLocations;
    
    
    public ClassifiedPositionInfo() {
        
        // initialise the keys for the hashmap.
        initMap();
    }
    
    
    private void initMap() {
        mapChrToLocations = new HashMap<String, List<Range>>();
        for (int counter = 1; counter <= 22; counter++) {
            mapChrToLocations.put("chr" + counter, new ArrayList<Range>());
        }
        mapChrToLocations.put("chrX", new ArrayList<Range>());
        mapChrToLocations.put("chrY", new ArrayList<Range>());
    }
    
    
    /**
     * Add a condition to this statement.
     * @param chromosome the chromosome in question
     * @param start the start position
     * @param end the end position
     */
    public void addCondition(String chromosome, double start, double end) {
        List<Range> ranges = mapChrToLocations.get(chromosome);
        // for "invalid" chromosome IDs.
        if (ranges == null) {
            return;
        }
        Range newRange = new Range(start, end);
        ranges.add(newRange);
        
    }
    
    public HashMap<String, List<Range>> getConditions() {
        
        HashMap<String, List<Range>> map = new HashMap<String, List<Range>>();
        
        for (String key: mapChrToLocations.keySet()) {
//            List<Range> ranges = Range.merge(mapChrToLocations.get(key));
            List<Range> ranges = mapChrToLocations.get(key);
            if (ranges.isEmpty())
                ;
            else{
                map.put(key, Range.merge(ranges));
            }
        }
        return map;
    }
    
    public List<String> getAllMergedRanges() {
        
        List<String> mergedRanges = new ArrayList<String>();
        HashMap<String, List<Range>> mapRanges = this.getConditions();
        
        for (String key: mapRanges.keySet()) {
            List<Range> ranges = mapRanges.get(key);
            for (Range currRange: ranges) {
                
                String formatted = 
                        key.trim() + "\t" + (long)currRange.getMin() + "\t" + (long)currRange.getMax();
                mergedRanges.add(formatted);
            }
        }
        return mergedRanges;
    }
    
}
