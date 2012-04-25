/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class DistinctValuesCache {
    
    private static final String CACHE_DIR = "cache";
    public static final int CACHE_LIMIT = 10000;
    private static final String CACHE_NULL = "##NULL";
    
    private static File getDirectory(String dbName, String tableName){
        return new File(CACHE_DIR + File.separator + dbName + File.separator + tableName);
    }
    
    private static File getFile(File dir, String columnName){
        return new File(dir, columnName);
    }
    
    private static File getFile(String dbName, String tableName, String columnName){
        return getFile(getDirectory(dbName, tableName), columnName);
    }
    
    public static boolean isCached(String dbName, String tableName, String columnName){
        return getFile(dbName, tableName, columnName).exists();
    }
    
    public static void cacheResults(String dbName, String tableName, String columnName, List<Object> result){
        File dir = getDirectory(dbName, tableName);
        if(!dir.exists() && !dir.mkdirs()){
            System.err.println("Failed to create dir: " + dir.getAbsolutePath());
            return; //couldn't create directory
        }
        
        File file = getFile(dir, columnName);
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
            if(result == null){
                out.write(CACHE_NULL);
            } else {
                for(Object o : result){
                    out.write(o.toString());
                    out.newLine();
                }
            }
            out.close();
        }catch (Exception e){
            System.err.println("Failed to cache results");
            e.printStackTrace();
            file.delete();
        }
    }
    
    private static List<String> getResults(String dbName, String tableName, String columnName) throws FileNotFoundException, IOException{
        List<String> result = new ArrayList<String>();
        
        BufferedReader in = new BufferedReader(new FileReader(getFile(dbName, tableName, columnName)));
        String line;
        while ((line = in.readLine()) != null)   {
            if(result.isEmpty() && line.startsWith(CACHE_NULL)){
                in.close();
                return null;
            }
            result.add(line);
        }
        in.close();
        
        return result;
    }
    
    public static List<String> getCachedStringList(String dbName, String tableName, String columnName) throws FileNotFoundException, IOException{
        return getResults(dbName, tableName, columnName);
    }
    
    public static double[] getCachedRange(String dbName, String tableName, String columnName) throws FileNotFoundException, IOException{
        List<String> results = getResults(dbName, tableName, columnName);
        if(results == null || results.size() != 2) return null;
        return new double[]{Double.parseDouble(results.get(0)), Double.parseDouble(results.get(1))};
    }
    
}
