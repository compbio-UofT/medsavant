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
package org.ut.biolab.medsavant.shared.util;

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
    private static File tmpDir;

    static {
        String username = System.getProperty("user.name");
        DirectorySettings.setTmpDirectory((new File(System.getProperty("java.io.tmpdir"), "msavant_"+username)).getAbsolutePath());
        DirectorySettings.setMedSavantDirectory((new File(System.getProperty("user.home"), MiscUtils.WINDOWS ? "medsavant" : ".medsavant")).getAbsolutePath());
    }

    /**
     * Retrieve the directory where MedSavant stores supporting files.
     *
     * @return
     */
    public static File getMedSavantDirectory() {

        return medSavantDir;
    }

    private static File getDirectory(String parent, String dirName) {
        File result = new File(parent, dirName);
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    public static File getTmpDirectory() {
        return tmpDir;
    }

    public static File getCacheDirectory() {
        return getDirectory(getMedSavantDirectory().getAbsolutePath(), "cache");
    }

    /**
     * Create a directory whose name includes a date-stamp.
     *
     * @param parent the parent directory, typically <code>new File(".")</code>
     */
    public static File generateDateStampDirectory(File parent) throws IOException {
        Calendar today = new GregorianCalendar();
        String dateStamp = today.get(Calendar.YEAR) + "_" + (today.get(Calendar.MONTH) + 1) + "_" + today.get(Calendar.DAY_OF_MONTH) + "_" + today.get(Calendar.HOUR_OF_DAY) + "_" + today.get(Calendar.MINUTE) + "_" + today.get(Calendar.SECOND) + "_" + today.get(Calendar.MILLISECOND);
        File dir = new File(parent.getCanonicalFile(), dateStamp);
        dir.mkdirs();
        return dir;
    }

    public static void setTmpDirectory(String path) {
        tmpDir = new File(path);
        tmpDir.mkdirs();
    }

    public static void setMedSavantDirectory(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        medSavantDir = f;
    }
}
