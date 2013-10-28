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
package org.ut.biolab.medsavant.client.settings;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;


/**
 * Class which keeps track of the location of important directories.
 *
 * @author AndrewBrook, tarkvara, mfiume
 */
public class DirectorySettings {
    private static final String CACHE_DIR_KEY = "CacheDir";
    private static final String TMP_DIR_KEY = "TmpDir";
    private static final String PLUGINS_DIR_KEY = "PluginsDir";
    private static final String FILTERS_DIR_KEY = "FiltersDir";
    private static final String GENEMANIA_DIR_KEY = "GeneManiaDir";
    public static final String GENEMANIA_CHECK_FILE = "gmdataOK.txt"; //If this file exists, we assume GeneMANIA has been downloaded & installed.
    //private static final String DATA_PATH = DirectorySettings.getCacheDirectory().getAbsolutePath() + File.separator + "gmdata";
    //public static final String DOWNLOAD_COMPLETE_CHECKFILE = DATA_PATH + File.separator+"gmdataOK.txt";
    
    private static File medSavantDir;
    private static SettingsController settings = SettingsController.getInstance();

    /**
     * Checks if GeneMANIA is installed.
     * @return 
     */
    public static boolean isGeneManiaInstalled(){
        return new File(getGeneManiaDirectory(), GENEMANIA_CHECK_FILE).exists();        
    }
    
    /**
     * Retrieve the directory where MedSavant stores supporting files.  Right now,
     * this is only plugins.
     * @return
     */
    public static File getMedSavantDirectory() {
        if (medSavantDir == null) {
            File f = new File(System.getProperty("user.home"), ClientMiscUtils.WINDOWS ? "medsavant" : ".medsavant");
            if (!f.exists()) {
                f.mkdir();
            }
            medSavantDir = f;
        }
        return medSavantDir;
    }

    private static File getDirectory(String key, String dirName) {
        File result = settings.getFile(key);
        if (result == null) {
            result = new File(getMedSavantDirectory(), dirName);
        }
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    private static void setDirectory(String key, File value) {
        if (!value.exists()) {
            value.mkdirs();
        }
        settings.setFile(key, value);
    }
    
    public static File getGeneManiaDirectory(){
        return getDirectory(GENEMANIA_DIR_KEY, "cache"+File.separator+"gmdata");
    }

    public static File getPluginsDirectory(){
        return getDirectory(PLUGINS_DIR_KEY, "plugins");
    }

    public static File getTmpDirectory() {
        return getDirectory(TMP_DIR_KEY, "tmp");
    }

    public static File getCacheDirectory() {
        return getDirectory(CACHE_DIR_KEY, "cache");
    }

    public static File getFiltersDirectory() {
        return getDirectory(FILTERS_DIR_KEY, "filters");
    }

    public static File generateDateStampDirectory(File parent) {
        Calendar today = new GregorianCalendar();
        String dateStamp = today.get(Calendar.YEAR) + "-" + today.get(Calendar.MONTH) + "-" + today.get(Calendar.DAY_OF_MONTH) + "-" + today.get(Calendar.HOUR_OF_DAY) + "-" + today.get(Calendar.MINUTE);
        File dir = new File(parent,dateStamp);
        dir.mkdirs();
        return dir;
    }

    public static File getDirectoryForPlugin(String pluginName) {
        File dir = new File(getMedSavantDirectory(),pluginName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
