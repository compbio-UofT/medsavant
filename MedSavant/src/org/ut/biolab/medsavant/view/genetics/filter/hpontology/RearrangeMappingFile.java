/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.hpontology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.ut.biolab.medsavant.db.model.Range;

/**
 *
 * @author Nirvana Nursimulu
 */
public class RearrangeMappingFile {
    
    /**
     * Location from which we are getting the mapping file.
     */
    public static final String LOCATION_FROM = 
            "http://savantbrowser.com/nirvana/maps/geneNameToLoc_hg19.bed";
    
    /**
     * Location to which we are moving the mapping file.
     */
    public static final String LOCATION_TO = "C:\\Documents and Settings\\Nirvana Nursimulu\\Desktop\\ReducedMap.txt";
    
    public static void main(String[] args) throws Exception{
        // Run this only once!
        reduceRepetition();
    }

    /**
     * RUN THIS FUNCTION ONLY ONCE.
     * Reduces repetition in the mapping file by merging gene locations if 
     * possible.
     */
    private static void reduceRepetition() throws Exception{
        
        URL url = new URL(LOCATION_FROM);
        BufferedReader reader = 
                new BufferedReader(new InputStreamReader(url.openStream()));
        
        // Map from gene to a map X.
        // Map X is intended to have key a chromosome name, and a list of ranges
        // of genomic positions.
        HashMap<String, HashMap<String, List<Range>>> map = 
                new HashMap<String, HashMap<String, List<Range>>>();
        String line;
        
        // Fill in this map.
        while ((line = reader.readLine()) != null){
            
            String[] split = line.split("\t"); 

            String geneName = split[3].trim();
            String chrName = split[0].trim();
            
            // Ignore this entry if it contains an underscore ("_").
            if (chrName.matches(".*_.*")){
                continue;
            }
            
            int start = Integer.parseInt(split[1].trim());
            int end = Integer.parseInt(split[2].trim());
            Range range = new Range(start, end);
            
            HashMap<String, List<Range>> mapFromGene = map.get(geneName);
            
            if (mapFromGene == null){
                mapFromGene = new HashMap<String, List<Range>>();
                map.put(geneName, mapFromGene);
            }
            
            List<Range> valueFromChrom = mapFromGene.get(chrName);
            
            if (valueFromChrom == null){
                valueFromChrom = new ArrayList<Range>();
                mapFromGene.put(chrName, valueFromChrom);
            }
            
            valueFromChrom.add(range);
        }
        
        reader.close();
        
        // Write the info from the map into 
        writeInfoFile(map);
    }

    /**
     * Write the info from the hashmap into file, by merging records 
     * if necessary.
     * @param map maps gene name to a map having key chromosome name and value 
     * a list of ranges (for gene location).
     */
    private static void writeInfoFile
            (HashMap<String, HashMap<String, List<Range>>> map) throws Exception{
        
        // Create the file to write to.
        File file = new File(LOCATION_TO);
        Writer writer = new BufferedWriter(new FileWriter(file));
        
        Set<String> geneNames = map.keySet();
        for (String geneName: geneNames){
            
            // Get the inner map from chromosome name to list of ranges.
            HashMap<String, List<Range>> innerMap = map.get(geneName);
            
            // The chromosomes for this gene.
            Set<String> chromosomes = innerMap.keySet();
            for (String chromosome: chromosomes){
                
                List<Range> unmerged = innerMap.get(chromosome);
                // Merge the gene locations.
                List<Range> merged = Range.merge(unmerged);
                
                // Write to the file now.
                for (Range range: merged){
                    
                    String line = chromosome + "\t" + 
                            (long)range.getMin() + "\t" + (long)range.getMax() + "\t" + geneName;
                    writer.write(line + "\n");
                }
                
            }
        }
        
        writer.flush();
        writer.close();
    }
    
}
