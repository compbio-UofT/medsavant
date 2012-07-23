/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 *
 * @author AndrewBrook, tarkvara, mfiume
 */
public class DirectorySettings {
    private static File medSavantDir;

    /**
     * Retrieve the directory where MedSavant stores supporting files.
     *
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
        return new File(System.getProperty("java.io.tmpdir"));
    }

    public static File getCacheDirectory() {
        return getDirectory(getMedSavantDirectory().getAbsolutePath(), "cache");
    }

    /**
     * Create a directory whose name is a date-stamp.
     *
     * @param parent the parent directory, typically <code>new File(".")</code>
     */
    public static File generateDateStampDirectory(File parent) throws IOException {
        Calendar today = new GregorianCalendar();
        String dateStamp = today.get(Calendar.YEAR) + "-" + today.get(Calendar.MONTH) + "-" + today.get(Calendar.DAY_OF_MONTH) + "-" + today.get(Calendar.HOUR_OF_DAY) + "-" + today.get(Calendar.MINUTE);
        File dir = new File(parent.getCanonicalFile(), dateStamp);
        dir.mkdirs();
        return dir;
    }
}
