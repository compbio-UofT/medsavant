/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.geneontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;

/**
 *
 * @author Nirvana Nursimulu
 */
public class XTree extends Tree{
    
    
    /**
     * Map from GO ID to locations
     */
    HashMap<String, ArrayList<ArrayList<String>>> idToLocs;
    
    /**
     * The file containing genome locations.
     */
    public String fileGenLocation;
    
    /**
     * Constructor
     * @param fileGenLocation the location of the file mapping GO IDs to 
     * genome locations.
     */
    public XTree(String fileGenLocation) throws Exception{
        
        super();
        this.fileGenLocation = fileGenLocation;
        // Map GO ID to locations on genome.
        this.idToLocs = super.idToLocs;
        mapIDtoLocations();
        
    }
    
     /**
     * Map GO ID to Uniprot ID.
     * @param filename the name of the file containing the information.
     * @return a map of GO ID to Uniprot ID. 
     */
    private void mapIDtoLocations() throws Exception{
        
        String line;
        
        // Get file.
        // File is assumed to have on each line, "uniprotID\tX" where X may be
        // a GO ID.
        File file = new File(fileGenLocation);
        
            
        FileReader fileReader = new FileReader(file);
        BufferedReader buffer = new BufferedReader(fileReader);


        // While there is still something to be read...
        while ((line = buffer.readLine()) != null){

            String[] split = line.split("\t");

            // Get the key
            String key = split[0];
            key = key.replace(':', '_');

            // Get the value
            ArrayList<ArrayList<String>> value = idToLocs.get(key);

            // If the key has not been put yet, create a map.
            if (value == null){

                value = new ArrayList<ArrayList<String>>();
                idToLocs.put(key, value);
            }

            ArrayList<String> location = new ArrayList<String>();

            for (int i = 1; i < split.length; i++){

                location.add(split[i]);
            }
            value.add(location);
        }

        fileReader.close();
        buffer.close();
    }
    

}
