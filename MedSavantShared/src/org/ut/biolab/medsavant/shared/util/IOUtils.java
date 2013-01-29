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
package org.ut.biolab.medsavant.shared.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * I/O-related utility methods. Functions for manipulating Savant files are in
 * SavantFileUtils.
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
            for (File f : files) {
                copyFile(f, new File(destDir, f.getName()));
            }
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        try {
            while ((len = input.read(buf)) > 0) {
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
     * Cheesy method which lets us read from an InputStream without having to
     * instantiate a BufferedReader. Intended to get around some glitches
     * reading GZIPInputStreams over an HTTP stream.
     */
    public static String readLine(InputStream input) throws IOException {
        StringBuilder buf = new StringBuilder();
        int c;
        while ((c = input.read()) >= 0 && c != '\n') {
            buf.append((char) c);
        }
        return c >= 0 ? buf.toString() : null;
    }

    /**
     * Recursively delete a directory.
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
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
     * Make sure this directory and all its parents have world execute
     * permissions. This is necessary to ensure that MySQL can write files to
     * the given directory.
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
     *
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
     * Does the given directory have world-execute permissions? TODO: implement
     * this in a less Unix-specific manner.
     *
     * @param dir the directory to be checked
     */
    private static boolean hasWorldExecute(File f) throws IOException {
        String perm = getPermissionString(f);
        if (perm != null && perm.length() >= 9) {
            return perm.charAt(8) == 'x';
        }
        return false;
    }

    /**
     * Checks if an input stream is gzipped.
     *
     * @param in
     * @return
     */
    public static boolean isGZipped(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(2);
        int magic = 0;
        try {
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    /**
     * Checks if a file is gzipped.
     *
     * @param f
     * @return
     */
    public static boolean isGZipped(File f) {
        int magic = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
            raf.close();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    /**
     * Checks if a file is zipped.
     *
     * @param f
     * @return
     */
    public static boolean isZipped(File f) {

        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            long n = raf.readInt();
            raf.close();
            if (n == 0x504B0304) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return false;
        }

    }

    /**
     * Unzip a zip file
     *
     * @param zipFile Path to the zip file
     * @param toPath Destination path
     * @throws ZipException
     * @throws IOException
     */
    public static List<File> unzipFile(File file, String toPath) throws ZipException, IOException {
        int BUFFER = 2048;

        ZipFile zip = new ZipFile(file);

        //new File(newPath).mkdir();
        Enumeration zipFileEntries = zip.entries();

        List<File> files = new ArrayList<File>();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(toPath, currentEntry);
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos,
                        BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open
                unzipFile(destFile, new File(destFile.getAbsolutePath()).getParent());
            } else {
                files.add(destFile);
            }
        }

        return files;
    }

    public static File zipDirectory(File dir, File outFile) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
        zipRecursively(".", dir, out);
        out.close();
        return outFile;
    }

    private static void zipRecursively(String path, File dir, ZipOutputStream out) throws IOException {
        for (File f : dir.listFiles()) {
            writeFileToZipStream(path,f, out);
            if (f.isDirectory()) {
                zipRecursively(path + "/" + f.getName() + "/",f,out);
            }
        }
    }

    private static void writeFileToZipStream(String path, File f, ZipOutputStream out) throws IOException {
        // name the file inside the zip  file

        if (f.isFile()) {
            out.putNextEntry(new ZipEntry(path + "/" + f.getName()));

            FileInputStream in = new FileInputStream(f);
            byte[] b = new byte[1024];
            int count;
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            in.close();
        } else if (f.isDirectory()) {
            out.putNextEntry(new ZipEntry(f.getName() + "/"));
        }
    }

    public static File zipFile(File file, File outputFile) throws FileNotFoundException, IOException {

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
        writeFileToZipStream(".", file, out);
        out.close();

        return outputFile;
    }
}