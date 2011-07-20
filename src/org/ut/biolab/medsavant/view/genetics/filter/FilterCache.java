/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author AndrewBrook
 */
public class FilterCache {
    
    private static boolean cacheRetrieved = false;    
    private static Map<String, List<String>> cache = new HashMap<String, List<String>>();
    
    private static String CACHE_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + "medsavant";
    private static String CACHE_STRING =  "filterCache";
    private static File cacheFile;
    private static Date lastDate;
    
    public static void retrieveCache(){  
        
        //last date db was modified
        lastDate = Date.valueOf("3000-01-01"); //unreachable date, ensures always new
        try {          
            lastDate = QueryUtil.getMaxUpdateTimeForCache();
        } catch (SQLException ex) {
            Logger.getLogger(FilterCache.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(FilterCache.class.getName()).log(Level.SEVERE, null, ex);
        }
   
        //get cache file
        File cacheDir = new File(CACHE_DIR);
        cacheDir.mkdirs();
        cacheFile = new File(cacheDir, CACHE_STRING);   
        
        //parse cache file
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(cacheFile));
            String line = bufferedReader.readLine();
            Date cacheDate = new Date(Long.parseLong(line.trim()));
            if(cacheDate.before(lastDate)){
                cacheRetrieved = true; 
                return; //need to rebuild cache
            }
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineArray = line.split("\t");
                if(lineArray.length < 1) continue;
                List<String> list = new ArrayList<String>();
                for(int i = 1; i < lineArray.length; i++){
                    list.add(lineArray[i]);
                }  
                cache.put(lineArray[0], list);             
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {           
            cacheRetrieved = true;
        } catch (IOException ex) {
            Logger.getLogger(FilterCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        cacheRetrieved = true;
    }
    
    public static synchronized List<String> getDefaultValues(String filterName){
        if(!cacheRetrieved) retrieveCache();
        List<String> list = cache.get(filterName);
        if(list == null) return null;    
        return list;
    }
    
    public static Range getDefaultValuesRange(String filterName){
        List<String> list = getDefaultValues(filterName);
        if(list == null || list.size() < 2) return null;
        return new Range(Double.parseDouble(list.get(0)), Double.parseDouble(list.get(1)));
    }
    
    public static void addDefaultValues(String filterName, List<String> values){
        cache.put(filterName, values);
    }
    
    public static void addDefaultValues(String filterName, Range range){
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(range.getMin()));
        list.add(String.valueOf(range.getMax()));
        addDefaultValues(filterName, list);
    }
    
    /*
     * Since cache should only be used at init of program, the memory can be 
     * freed as soon as all filters generated. 
     */
    public static void saveAndDispose(){
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile, false));
            
            //write the date
            out.write(String.valueOf(lastDate.getTime()));
            out.newLine();
            
            //write each filter and value set
            for(Object key :cache.keySet().toArray()){
                out.write((String)key + "\t");
                for(String value : cache.get(key)){
                    out.write(value + "\t");
                }
                out.newLine();
            }
 
            out.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }

        cache = null;
        cacheRetrieved = false;
    }
    
    
    
}
