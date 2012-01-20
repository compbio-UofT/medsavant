/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.controller;

import org.ut.biolab.medsavant.MedSavantProgramInformation;
import java.io.*;
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
    //public static final String KEY_DB_HOST = "db.host";
    public static final String KEY_DB_NAME = "db.name";
    //public static final String KEY_DB_PORT = "db.port";
    public static final String KEY_SERVER_ADDRESS = "server.address";
    public static final String KEY_SERVER_PORT = "server.port";

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
     * Retrieve the value of a boolean setting.
     * @param key the key for the setting to retrieve
     * @param dflt the default value if the key does not currently exist
     * @return value of the setting
     */
    public boolean getBoolean(String key, boolean dflt) {
        String value = getValue(key);
        return value != null ? value.toLowerCase().equals("true") : dflt;
    }

    public void setBoolean(String key, boolean value) {
        setValue(key, Boolean.toString(value));
    }

    /**
     * Retrieve the value of a <code>File</code> setting.
     * @param key the key for the setting to retrieve
     * @param dflt the default value if the key does not currently exist
     * @return value of the setting
     */
    public File getFile(String key) {
        String value = getValue(key);
        return value != null ? new File(value) : null;
    }

    public void setFile(String key, File value) {
        setValue(key, value.getAbsolutePath());
    }


    /**
     * Read the settings in from file (usually performed once, on load)
     */
    private void readPersistenceMap() {
        File pFile = new File(PERSISTENCE_FILE_PATH);
        
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
                    
                    setValueSilent(key,value);
                }
            } catch (IOException ex) {
                resetPersistenceMap();
            } finally {
            try {
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
        setValueSilent(KEY_VERSION,MedSavantProgramInformation.getVersion());
        resetSettingSilent(KEY_USERNAME);
        resetSettingSilent(KEY_PASSWORD);
        resetSettingSilent(KEY_REMEMBER_PASSWORD);
        resetSettingSilent(KEY_AUTOLOGIN);
        resetSettingSilent(KEY_DB_DRIVER);
        //resetSettingSilent(KEY_DB_HOST);
        resetSettingSilent(KEY_DB_NAME);
        //resetSettingSilent(KEY_DB_PORT);
        resetSettingSilent(KEY_SERVER_ADDRESS);
        resetSettingSilent(KEY_SERVER_PORT);
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

            /*if (pFile.exists()) {
                pFile.delete();
            }*/
            bw = new BufferedWriter(new FileWriter(pFile));
            bw.write("");
            for (String key : persistenceMap.keySet()) {
                bw.write(key + DELIM + persistenceMap.get(key) + "\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
            //ex.printStackTrace();
        } finally {
            try {
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
            return "";
        } else if (key.equals(KEY_PASSWORD)) {
            return "";
        } else if (key.equals(KEY_REMEMBER_PASSWORD)) {
            return booleanToString(false);
        } else if (key.equals(KEY_AUTOLOGIN)) {
            return booleanToString(false);
        } else if (key.equals(KEY_DB_DRIVER)) {
            return "com.mysql.jdbc.Driver";
        } else if (key.equals(KEY_SERVER_ADDRESS)) {
            return "localhost";
        } else if (key.equals(KEY_SERVER_PORT)) {
            return "3232";
        } else if (key.equals(KEY_DB_NAME)) {
            return "";
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
    
    public String getDBName() {
        return getValue(SettingsController.KEY_DB_NAME);
    }
    
    public void setDBName(String name) {
        setValue(KEY_DB_NAME, name);
    }
    
    public String getServerAddress() {
        return getValue(SettingsController.KEY_SERVER_ADDRESS);
    }
    
    public void setServerAddress(String address) {
        setValue(KEY_SERVER_ADDRESS, address);
    }
    
    public String getServerPort() {
        return getValue(SettingsController.KEY_SERVER_PORT);
    }
    
    public void setServerPort(String port) {
        setValue(KEY_SERVER_PORT, port);
    }
    
}
