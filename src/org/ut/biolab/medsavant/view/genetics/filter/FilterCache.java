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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author AndrewBrook
 */
public class FilterCache {
    
    private static boolean cacheRetrieved = false;    
    private static Map<String, List<String>> cache;
    private static Map<String, String> filterNameToTable = new HashMap<String, String>();
    
    private static String CACHE_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + "medsavant";
    private static String CACHE_STRING =  "filterCache";
    private static File cacheFile;
    
    private static Timestamp lastDateVariant;
    private static Timestamp lastDateCohort;
    private static Timestamp lastDateGeneList;
    
    
    public static void retrieveCache(){
        
        cache = new HashMap<String, List<String>>();
        
        //last date dbs were modified
        try {          
            Map<String, Timestamp> timeMap = QueryUtil.getUpdateTimesForCache();
            lastDateVariant = timeMap.get(MedSavantDatabase.getInstance().getVariantTableSchema().getTable().getTableNameSQL());
            lastDateCohort = timeMap.get(MedSavantDatabase.getInstance().getCohortTableSchema().getTable().getTableNameSQL());
            lastDateGeneList = timeMap.get(MedSavantDatabase.getInstance().getGeneListTableSchema().getTable().getTableNameSQL());
        } catch (SQLException ex) {
            Logger.getLogger(FilterCache.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(FilterCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(lastDateVariant == null) lastDateVariant = Timestamp.valueOf("3000-01-01 01:01:01"); //unreachable date, ensures always new
        if(lastDateCohort == null) lastDateCohort = Timestamp.valueOf("3000-01-01 01:01:01");
        if(lastDateGeneList == null) lastDateGeneList = Timestamp.valueOf("3000-01-01 01:01:01"); 
        
        //get cache file
        File cacheDir = new File(CACHE_DIR);
        cacheDir.mkdirs();
        cacheFile = new File(cacheDir, CACHE_STRING);   
        
        //parse cache file
        BufferedReader bufferedReader;
        try {
            
            bufferedReader = new BufferedReader(new FileReader(cacheFile));
            bufferedReader.readLine(); //skip the first line
            
            //check which tables are valid
            String line = bufferedReader.readLine();
            String[] lineArray = line.split("\t");
            
            //parse entries
            while((line = bufferedReader.readLine()) != null){
                lineArray = line.split("\t");
                if(lineArray.length < 3) continue;
                if(!retrieveTable(lineArray[0])) continue;
                List<String> list = new ArrayList<String>();
                for(int i = 2; i < lineArray.length; i++){
                    list.add(lineArray[i]);
                }  
                cache.put(lineArray[1], list);  
                filterNameToTable.put(lineArray[1], lineArray[0]);
            }
            
            bufferedReader.close();
        } catch (FileNotFoundException ex) {           
            cacheRetrieved = true;
        } catch (IOException ex) {
            Logger.getLogger(FilterCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        cacheRetrieved = true;
    }
    
    private static boolean retrieveTable(String tableName){
        //todo:dbref
        return false;
        /*
        if(tableName.equals(MedSavantDatabase.getInstance().getVariantTableSchema().getTable().getTableNameSQL())){
            return retrieveVariant;
        } else if(tableName.equals(MedSavantDatabase.getInstance().getCohortTableSchema().getTable().getTableNameSQL())){
            return retrieveCohort;
        } else if(tableName.equals(MedSavantDatabase.getInstance().getPatientTableSchema().getTable().getTableNameSQL())){
            return retrievePatient;
        } else if(tableName.equals(MedSavantDatabase.getInstance().getPhenotypeTableSchema().getTable().getTableNameSQL())){
            return retrievePhenotype;
        } else if(tableName.equals(MedSavantDatabase.getInstance().getGeneListTableSchema().getTable().getTableNameSQL())){
            return retrieveGeneList;
        } else if(tableName.equals(MedSavantDatabase.getInstance().getVariantSiftTableSchema().getTable().getTableNameSQL())){
            return retrieveSift;
        }
        return false;
         * 
         */
    }
    
    public static synchronized List<String> getDefaultValues(String filterName){        
        if(!cacheRetrieved) retrieveCache();
        List<String> list = cache.get(filterName);
        if(list == null){
            return null;
        }    
        return list;
    }
    
    public static Range getDefaultValuesRange(String filterName){
        List<String> list = getDefaultValues(filterName);
        if(list == null || list.size() < 2) return null;
        return new Range(Double.parseDouble(list.get(0)), Double.parseDouble(list.get(1)));
    }
    
    public static void addDefaultValues(String tableName, String filterName, List<String> values){
        cache.put(filterName, values);
        filterNameToTable.put(filterName, tableName);
    }
    
    public static void addDefaultValues(String tableName, String filterName, Range range){
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(range.getMin()));
        list.add(String.valueOf(range.getMax()));
        addDefaultValues(tableName, filterName, list);
    }
    
    /*
     * Since cache should only be used at init of program, the memory can be 
     * freed as soon as all filters generated. 
     */
    public static void saveAndDispose(){
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile, false));
            
            //write message
            out.write("***DO NOT MODIFY THIS FILE. TO INVALIDATE, DELETE FILE***");
            out.newLine();
            
            //write timestamps
            out.write(String.valueOf(lastDateVariant.getTime()) + "\t");
            out.write(String.valueOf(lastDateCohort.getTime()) + "\t");
            out.write(String.valueOf(lastDateGeneList.getTime()) + "\t");
            out.newLine();
            
            //write each filter and value set
            for(Object key : cache.keySet().toArray()){
                out.write(filterNameToTable.get((String)key) + "\t");
                out.write((String)key + "\t");
                for(String value : cache.get((String)key)){
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
