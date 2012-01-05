package org.ut.biolab.medsavant.db.util;

/**
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

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.ut.biolab.medsavant.db.util.shared.MiscUtils;


/**
 * Class which keeps track of the location of important directories.
 *
 * @author AndrewBrook, tarkvara, mfiume
 */
public class ServerDirectorySettings {

    private static File medSavantDir;

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

    private static File getDirectory(String parent, String dirName) {
        File result = new File(parent,dirName);
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    public static File getTmpDirectory() {
        return getDirectory(getMedSavantDirectory().getAbsolutePath(), "tmp");
    }

    public static File getCacheDirectory() {
        return getDirectory(getMedSavantDirectory().getAbsolutePath(), "cache");
    }

    public static File generateDateStampDirectory(File parent) {
        Calendar today = new GregorianCalendar();
        String dateStamp = today.get(Calendar.YEAR) + "-" + today.get(Calendar.MONTH) + "-" + today.get(Calendar.DAY_OF_MONTH) + "-" + today.get(Calendar.HOUR_OF_DAY) + "-" + today.get(Calendar.MINUTE);
        File dir = new File(parent,dateStamp);
        dir.mkdirs();
        return dir;
    }
}

