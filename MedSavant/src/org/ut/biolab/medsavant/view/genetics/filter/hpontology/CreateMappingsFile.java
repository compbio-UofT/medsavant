/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.hpontology;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Nirvana Nursimulu
 */
public class CreateMappingsFile {

    /**
     * The location of the XML file containing the location of the mapping file.
     */
    public static String LOCATION_XML = "http://savantbrowser.com/nirvana/HPOmap.xml";
    
    /**
     * The location of the file mapping from HPO ID to gene name.
     */
    public static String LOCATION_HPO_TO_GENENAME = 
            "http://compbio.charite.de/svn/hpo/trunk/src/annotation/genes_to_phenotype.txt";
    
    public static HashMap<String, HashSet<String>> getMappings() 
            throws Exception{
        
        String locFileGeneNameToLoc = getNameOfFileMapGeneNameToLoc();

        // Map from gene name to loc.
        HashMap<String, TreeSet<String>> mapFromGeneNameToLoc = 
                getMapGeneNameToLoc(locFileGeneNameToLoc);
     
        // Map from HPO to loc by using map from gene name to loc.
        HashMap<String, HashSet<String>> mapFromHPOToLoc = 
                getMapHPOtoLoc(mapFromGeneNameToLoc);
        
        return mapFromHPOToLoc;
    }
    
    /**
     * Get and return a map from HPO ID to locations.
     * @param mapFromGeneNameToLoc map from gene name to location.
     * @return 
     */
    private static HashMap<String, HashSet<String>> getMapHPOtoLoc
            (HashMap<String, TreeSet<String>> mapFromGeneNameToLoc) 
            throws Exception{
        
        HashMap<String, HashSet<String>> mapHPOtoLoc = 
                new HashMap<String, HashSet<String>>();
        
        URL url = new URL(LOCATION_HPO_TO_GENENAME);
        BufferedReader reader = 
                new BufferedReader(new InputStreamReader(url.openStream()));
        
        String line;
        while ((line = reader.readLine()) != null){
            
            // Ignore any comment.
            if (line.trim().startsWith("#")){
                continue;
            }       
            String[] split = line.split("\t");
            String geneName = split[1].trim();
            TreeSet<String> locsFromName = mapFromGeneNameToLoc.get(geneName);
            
            // If the locations are available from gene name, carry on; 
            // otherwise, it's pointless to even think of proceeding.
            if (locsFromName == null){
                continue;
            }
            
            // Get the HPO Ids associated with this gene name.
            List<String> listHPOids = getHPOIDs(split[2]);
            
            // For each HPO id associated with this gene name...
            for (String hPOid: listHPOids){
                
                HashSet<String> locsFromHPO = mapHPOtoLoc.get(hPOid);
                
                if (locsFromHPO == null){
                    locsFromHPO = new HashSet<String>();
                    mapHPOtoLoc.put(hPOid, locsFromHPO);
                }
                
                locsFromHPO.addAll(locsFromName);
            }
        }
        
        return mapHPOtoLoc;
    }
    
    /**
     * Return the HPO IDs concealed within this string.
     * @param locationString
     * @return 
     */
    private static List<String> getHPOIDs(String locationString){
        
        List<String> listHPOids = new ArrayList<String>();
        boolean isAcc = false;
        String acc = "";
        // for each character, browse for the beginning of a parens.
        for (int i = 0; i < locationString.length(); i++){
            
            if (locationString.charAt(i) == '('){
                isAcc = true;
            }
            else if (locationString.charAt(i) == ')'){
                isAcc = false;
                listHPOids.add(acc);
                acc = "";
            }
            
            if (isAcc && locationString.charAt(i) != '('){
                acc = acc + locationString.charAt(i);
            }
        }
        return listHPOids;
    }
    
    /**
     * Parses XML file to find the name of the file that maps from gene name to
     * location.
     * @return the name of the file with the mapping.
     */
    private static String getNameOfFileMapGeneNameToLoc() throws Exception{
        
        String locationFile = "";
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build((new URL(LOCATION_XML)).openStream());
    
        List versionChildren = doc.getRootElement().getChildren("branch");
        List children = null;
        
        // Find the appropriate version.
        for (Object versionChild: versionChildren){
            
            Element versionElem = (Element)versionChild;
            // if this is the version we want...
            if (versionElem.getAttributeValue("name").matches
                    ("Current Version.*")){
                
                children = versionElem.getChildren();
            }
        }
        
        for (Object outerChildObj: children){
            
            Element outerChild = (Element)outerChildObj;

            if (outerChild.getAttribute("name").getValue().equals
                    ("Map of gene names to genomic locations.")){
                
                locationFile = 
                        outerChild.getChild("leaf").getChild("url").getText();
            }
        }
        return locationFile;
    }
    
    /**
     * Creates and returns a map of gene name to location.
     * @param locFile the location of the file containing mapping of gene name
     * to location.
     * @param hpoIDtoGeneName the map from HPO ID to geneName.
     * @return the mapping in the form key = HPO ID (String), and 
     * value = List<String> and each element of the list being a gene location, 
     * in the form chromName_startPos_endPos.
     */
    private static HashMap<String, TreeSet<String>> getMapGeneNameToLoc
            (String locFile) throws Exception{
        
        HashMap<String, TreeSet<String>> mapHPOtoGenes = 
                new HashMap<String, TreeSet<String>>();
        
        URL url = new URL(locFile);
        BufferedReader reader = 
                new BufferedReader(new InputStreamReader(url.openStream()));
        
        String line;
        
        // For each line that we are reading...
        while ((line = reader.readLine()) != null){
            
            String[] split = line.split("\t");
            // If chrom. name contains underscore, don't record this.
            if (split.length < 4 || split[0].contains("_")){
                continue;
            }
            String key = split[3];
            // Fix the end value (bed file)
            int end = Integer.parseInt(split[2].trim()) - 1;
            String value = split[0].trim() + "\t" + split[1].trim() + "\t" + end;
            
            TreeSet<String> listValue = mapHPOtoGenes.get(key);
            
            if (listValue == null){
                listValue = new TreeSet<String>();
                mapHPOtoGenes.put(key, listValue);
            }
            
            listValue.add(value);
        }
        reader.close();
        return mapHPOtoGenes;
    }
    
}
