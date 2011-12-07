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

package org.ut.biolab.medsavant.settings;

import java.io.File;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.db.util.MiscUtils;


/**
 * Class which keeps track of the location of important directories.
 *
 * @author AndrewBrook, tarkvara
 */
public class DirectorySettings {
    private static final String TMP_DIR_KEY = "TmpDir";
    private static final String PLUGINS_DIR_KEY = "PluginsDir";

    private static File medSavantDir;
    private static SettingsController settings = SettingsController.getInstance();

    /**
     * Retrieve the directory where MedSavant stores supporting files.  Right now,
     * this is only plugins.
     * @return 
     */
    public static File getMedSavantDirectory() {
        if (medSavantDir == null) {
            File f = new File(System.getProperty("user.home"), MiscUtils.WINDOWS ? "medsavant" : ".medsavant");
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

    public static File getPluginsDirectory(){
        return getDirectory(PLUGINS_DIR_KEY, "plugins");
    }

    public static File getTmpDirectory() {
        return getDirectory(TMP_DIR_KEY, "tmp");
    }
}
