/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantSDKInformation;

import savant.util.CryptoUtils;


/**
 *
 * @author mfiume
 */
public class SettingsController {
    private static final Log LOG = LogFactory.getLog(SettingsController.class);

    /**
     * INSTRUCTIONS: To add a new setting to the map...
     * 1. Add a KEY
     * 2. Add a default value in getDefaultValue() function
     * 3. Add an entry to the resetPersistenceMap() function
     * 4. (Optional) Create helper getter / setter functions
     */

    private static SettingsController instance; // a singleton instance

    private static final String DELIM = "="; // delimiter used in the settings file
    private static final String PERSISTENCE_FILE_PATH = ".medsavant.prop"; // location to save settings file


    /**
     * Settings keys
     */
    public static final String KEY_VERSION = "medsavant.version";
    public static final String KEY_AUTOLOGIN = "general.autologin";
    public static final String KEY_REMEMBER_PASSWORD = "user.rememberpass";
    public static final String KEY_USERNAME = "user.name";
    public static final String KEY_PASSWORD = "user.password";
    public static final String KEY_DB_DRIVER = "db.driver";
    public static final String KEY_DB_NAME = "db.name";
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
        this.persistenceMap = new TreeMap<String,String>();
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
            this.persistenceMap.remove(key);
        } else {
            this.persistenceMap.put(key, value);
        }
    }

    /**
     * Get the value of a setting
     * @param key The key for the setting to get
     * @return The value of the setting
     */
    public String getValue(String key) {
        if (!this.persistenceMap.containsKey(key)) {
            return getDefaultValue(key);
        }
        return this.persistenceMap.get(key);
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
        File pFile = new File(DirectorySettings.getMedSavantDirectory(), PERSISTENCE_FILE_PATH);

        if (!pFile.exists()) {
            resetPersistenceMap();
        } else {

            BufferedReader br = null;

            try {
                br = new BufferedReader(new FileReader(pFile));

                String line;
                while ((line = br.readLine()) != null) {

                    int splitIndex = line.indexOf(DELIM);
                    String key = line.substring(0,splitIndex);
                    String value = line.substring(splitIndex+1);

                    setValueSilent(key,value);
                }
            } catch (Exception ex) {
                LOG.error("Error reading " + PERSISTENCE_FILE_PATH, ex);
                resetPersistenceMap();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    /*
     * Clear existing settings and use defaults
     */
    private void resetPersistenceMap() {
        this.persistenceMap.clear();
        setValueSilent(KEY_VERSION,MedSavantSDKInformation.getSDKVersion());
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
            File pFile = new File(DirectorySettings.getMedSavantDirectory(), PERSISTENCE_FILE_PATH);

            bw = new BufferedWriter(new FileWriter(pFile));
            for (String key : this.persistenceMap.keySet()) {
                bw.write(key + DELIM + this.persistenceMap.get(key) + "\n");
            }
        } catch (Exception ex) {
            LOG.error("Error writing " + PERSISTENCE_FILE_PATH + ".", ex);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
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
            return booleanToString(true);
        } else if (key.equals(KEY_AUTOLOGIN)) {
            return booleanToString(false);
        } else if (key.equals(KEY_DB_DRIVER)) {
            return "com.mysql.jdbc.Driver";
        } else if (key.equals(KEY_SERVER_ADDRESS)) {
            return "";
        } else if (key.equals(KEY_SERVER_PORT)) {
            return "";
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
        setValue(KEY_PASSWORD, CryptoUtils.encrypt(str));
    }

    public String getPassword() {
        return CryptoUtils.decrypt(getValue(SettingsController.KEY_PASSWORD));
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
