/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.model.Range;

/**
 *
 * @author Andrew
 */
public class DistinctValuesCache {
    
    private static String CACHE_DIR = "cache";
    
    private static File getDirectory(String tableName){
        return new File(CACHE_DIR + File.separator + tableName);
    }
    
    private static File getFile(File dir, String columnName){
        return new File(dir, columnName);
    }
    
    private static File getFile(String tableName, String columnName){
        return getFile(getDirectory(tableName), columnName);
    }
    
    public static boolean isCached(String tableName, String columnName){
        return getFile(tableName, columnName).exists();
    }
    
    public static void cacheResults(String tableName, String columnName, List<Object> result){
        File dir = getDirectory(tableName);
        if(!dir.exists() && !dir.mkdirs()){
            System.err.println("Failed to create dir: " + dir.getAbsolutePath());
            return; //couldn't create directory
        }
        
        File file = getFile(dir, columnName);
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
            for(Object o : result){
                out.write(o.toString());
                out.newLine();
            }
            out.close();
        }catch (Exception e){
            System.err.println("Failed to cache results");
            e.printStackTrace();
            file.delete();
        }       
    }
    
    private static List<String> getResults(String tableName, String columnName){
        List<String> result = new ArrayList<String>();
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(getFile(tableName, columnName)));
            String line;
            while ((line = in.readLine()) != null)   {
                result.add(line);
            }
            in.close();
        }catch (Exception e){
            System.err.println("Failed to retrieve cached results");
            return null; //indicates error
        }
        
        return result;
    }
    
    public static List<String> getCachedStringList(String tableName, String columnName){
        return getResults(tableName, columnName);
    }
    
    public static double[] getCachedRange(String tableName, String columnName){
        List<String> results = getResults(tableName, columnName);
        if(results == null || results.size() != 2) return null;
        return new double[]{Double.parseDouble(results.get(0)), Double.parseDouble(results.get(1))};
    }

}
