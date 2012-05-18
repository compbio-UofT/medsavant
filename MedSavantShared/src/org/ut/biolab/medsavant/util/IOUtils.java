/*
 *    Copyright 2009-2012 University of Toronto
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


/**
 * I/O-related utility methods.  Functions for manipulating Savant
 * files are in SavantFileUtils.
 *
 * @author mfiume, tarkvara
 */
public class IOUtils {
    public static void copyFile(File srcFile, File destFile) throws IOException {
        if (srcFile.equals(destFile)) {
            return;
        }
        copyStream(new FileInputStream(srcFile), new FileOutputStream(destFile));
    }

    public static void copyDir(File srcDir, File destDir) throws IOException {
        File[] files = srcDir.listFiles();
        if (files != null) {
            for (File f: files) {
                copyFile(f, new File(destDir, f.getName()));
            }
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        try {
            while ((len = input.read(buf)) > 0 ){
                output.write(buf, 0, len);
            }
        } catch (IOException x) {
            // There's a bug in BlockCompressedInputStream where it throws an IOException instead of doing a proper EOF.
            // Suppress this exception, but throw real ones.
            if (!x.getMessage().equals("Unexpected compressed block length: 1")) {
                throw x;
            }
        } finally {
            input.close();
            output.close();
        }
    }

    /**
     * Cheesy method which lets us read from an InputStream without having to instantiate a BufferedReader.
     * Intended to get around some glitches reading GZIPInputStreams over an HTTP stream.
     */
    public static String readLine(InputStream input) throws IOException {
        StringBuilder buf = new StringBuilder();
        int c;
        while ((c = input.read()) >= 0 && c != '\n') {
            buf.append((char)c);
        }
        return c >= 0 ? buf.toString() : null;
    }

    /**
     * Recursively delete a directory.
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
    }
    
    /**
     * Make sure this directory and all its parents have world execute permissions.
     * This is necessary to ensure that MySQL can write files to the given directory.
     *
     * @param base
     * @return 
     */
    public static void checkForWorldExecute(File base) throws IOException {
        File f = base.isDirectory() ? base : base.getParentFile();
        while (hasWorldExecute(f)) {
            f = f.getParentFile();
            if (f == null) {
                // Reached /
                return;
            }
        }
        throw new IOException(f + " did not have execute permissions.");
    }
    
    /**
     * Retrieve the permission string (e.g. "rwxrwxrwx") for the given file.
     * TODO: implement this in a less Unix-specific manner.
     * @param f the file or directory whose permissions we are checking
     */
    private static String getPermissionString(File f) throws IOException {
        Process proc = Runtime.getRuntime().exec("ls -ld " + f);
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = null;
        String result = null;
        while ((line = reader.readLine()) != null) {
            int spacePos = line.indexOf(' ');
            if (spacePos > 0) {
                result = line.substring(1, spacePos);
            }
        }
        return result;
    }
    
    /**
     * Does the given directory have world-execute permissions?
     * TODO: implement this in a less Unix-specific manner.
     * @param dir the directory to be checked
     */
    private static boolean hasWorldExecute(File f) throws IOException {
        String perm = getPermissionString(f);
        if (perm != null && perm.length() >= 9) {
            return perm.charAt(8) == 'x';
        }
        return false;
    }
}