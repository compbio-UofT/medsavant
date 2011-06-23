/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.controller;

import main.ProgramInformation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
public class SettingsController {

    
    /**
     * INSTRUCTIONS: To add a new setting to the map...
     * 1. Add a KEY
     * 2. Add a default value in getDefaultValue() function
     * 3. Add an entry to the resetPersistenceMap() function
     * 4. (Optional) Create helper getter / setter functions
     */
    
    
    private static SettingsController instance; // a singleton instance 
    
    private static final String DELIM = "="; // delimiter used in the settings file
    private static final String PERSISTENCE_FILE_PATH = "medsavant.prop"; // location to save settings file
    
    /**
     * Settings keys 
     */
    public static final String KEY_VERSION = "medsavant.version";
    public static final String KEY_AUTOLOGIN = "general.autologin";
    public static final String KEY_REMEMBER_PASSWORD = "user.rememberpass";
    public static final String KEY_USERNAME = "user.name";
    public static final String KEY_PASSWORD = "user.password";
    public static final String KEY_DB_DRIVER = "db.driver";
    public static final String KEY_DB_HOST = "db.host";
    public static final String KEY_DB_NAME = "db.name";
    public static final String KEY_DB_PORT = "db.port";

    /**
     * Convert a boolean to a string representation
     * @param bool A boolean
     * @return A string, either "true" or "false"
     */
    public static String booleanToString(boolean bool) {
        return bool ? "true" : "false";
    }
    
    /**
     * Convert a string to a boolean representation
     * @param str A string, usually either "true" or "false"
     * @return A proper boolean
     */
    public static boolean stringToBoolean(String str) {
        return str.equals("true") ? true : false;
    }
    
    // A map containing settings and their values
    Map<String,String> persistenceMap;
    
    /**
     * Get an instance of the SettingsController
     * @return An in stance of the Settings Controller
     */
    public static SettingsController getInstance() {
        if (instance == null) {
            instance = new SettingsController();
        }
        return instance;
    }
    
    /**
     * Initialize the SettingsController
     */
    public SettingsController() {
        persistenceMap = new TreeMap<String,String>();
        readPersistenceMap();
    }
    
    
    /**
     * Add a setting and save
     * @param key The key for the setting
     * @param value  The value for the setting
     */
    public void setValue(String key, String value) {
        System.out.println("Saving setting: " + key + "=" + value);
        setValueSilent(key, value);
        savePersistenceMap();
    }
    
    /**
     * Add a setting without saving.
     * @param key The key for the setting
     * @param value The value for the setting
     */
    private void setValueSilent(String key, String value) {
        if (value == null) {
            persistenceMap.remove(key);
        } else {
            persistenceMap.put(key, value);
        }
    }
    
    /**
     * Get the value of a setting
     * @param key The key for the setting to get
     * @return The value of the setting
     */
    public String getValue(String key) {
        if (!persistenceMap.containsKey(key)) {
            return getDefaultValue(key);
        }
        return persistenceMap.get(key);
    }

    /**
     * Read the settings in from file (usually performed once, on load)
     */
    private void readPersistenceMap() {
        File pFile = new File(PERSISTENCE_FILE_PATH);
        
        System.out.println("Reading map file: " + pFile.getAbsolutePath());
        
        if (!pFile.exists()) {
            resetPersistenceMap();
        } else {
            
            BufferedReader br = null;
            
            try {
                br = new BufferedReader(new FileReader(pFile));
                
                String line = "";
                while ((line = br.readLine()) != null) {
                    
                    int splitIndex = line.indexOf(DELIM);
                    String key = line.substring(0,splitIndex);
                    String value = line.substring(splitIndex+1);
                    
                    //System.out.println("Reading setting: " + key + "=" + value);
                    
                    setValueSilent(key,value);
                }
            } catch (IOException ex) {
                resetPersistenceMap();
            } finally {
            try {
                System.out.println("Done reading map file");
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        }
        
    }

    /*
     * Clear existing settings and use defaults
     */
    private void resetPersistenceMap() {
        persistenceMap.clear();
        setValueSilent(KEY_VERSION,ProgramInformation.getVersion());
        resetSettingSilent(KEY_USERNAME);
        resetSettingSilent(KEY_PASSWORD);
        resetSettingSilent(KEY_REMEMBER_PASSWORD);
        resetSettingSilent(KEY_AUTOLOGIN);
        resetSettingSilent(KEY_DB_DRIVER);
        resetSettingSilent(KEY_DB_HOST);
        resetSettingSilent(KEY_DB_NAME);
        resetSettingSilent(KEY_DB_PORT);
        savePersistenceMap();
    }
    
    /**
     * Helper function. Reset the setting for key to its default
     * @param key 
     */
    private void resetSettingSilent(String key) {
       setValueSilent(key,getDefaultValue(key));
    }

    /**
     * Save the persistence map to file
     */
    private void savePersistenceMap() {
        BufferedWriter bw = null;
        try {
            File pFile = new File(PERSISTENCE_FILE_PATH);

            System.out.println("Saving map file: " + pFile.getAbsolutePath());
            
            if (pFile.exists()) {
                pFile.delete();
            }
            bw = new BufferedWriter(new FileWriter(pFile));
            for (String key : persistenceMap.keySet()) {
                bw.write(key + DELIM + persistenceMap.get(key) + "\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR: saving map file... " + ex.getMessage());
            //ex.printStackTrace();
        } finally {
            try {
                System.out.println("Done saving map file");
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Get default values for persistent settings
     * @param key The key of the value
     * @return The default value or null if none
     */
    public static String getDefaultValue(String key) {
        if (key.equals(KEY_USERNAME)) {
            return "root";
        } else if (key.equals(KEY_PASSWORD)) {
            return "";
        } else if (key.equals(KEY_REMEMBER_PASSWORD)) {
            return booleanToString(false);
        } else if (key.equals(KEY_AUTOLOGIN)) {
            return booleanToString(false);
        } else if (key.equals(KEY_DB_DRIVER)) {
            return "com.mysql.jdbc.Driver";
        } else if (key.equals(KEY_DB_HOST)) {
            return "localhost";
        } else if (key.equals(KEY_DB_PORT)) {
            return "5029";
        } else if (key.equals(KEY_DB_NAME)) {
            return "medsavant";
        } else {
            return null;
        }
    }
    
    /**
     * CONVENIENCE METHODS
     */
    
    public void setRememberPassword(boolean b) {
        setValue(KEY_REMEMBER_PASSWORD,booleanToString(b));
    }
    
    public boolean getRememberPassword() {
         return stringToBoolean(getValue(KEY_REMEMBER_PASSWORD));
    }
    
    public void setAutoLogin(boolean b) {
        setValue(KEY_AUTOLOGIN,booleanToString(b));
    }
    
    public boolean getAutoLogin() {
         return stringToBoolean(getValue(KEY_AUTOLOGIN));
    }

    public void setUsername(String str) {
        setValue(KEY_USERNAME, str);
    }
    
    public String getUsername() {
        return getValue(KEY_USERNAME);
    }
    
    public void setPassword(String str) {
        setValue(KEY_PASSWORD, str);
    }
    
    public String getPassword() {
        return getValue(SettingsController.KEY_PASSWORD);
    }

    public String getDBDriver() {
        return getValue(SettingsController.KEY_DB_DRIVER);
    }

    public String getDBHost() {
        return getValue(SettingsController.KEY_DB_HOST);
    }

    public String getDBURL() {
        return "jdbc:mysql://" + getValue(KEY_DB_HOST) + ":" + getValue(KEY_DB_PORT) + "/" + getValue(KEY_DB_NAME);
    }
    
}
