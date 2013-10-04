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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * I/O-related utility methods. Functions for manipulating Savant files are in SavantFileUtils.
 * 
 * @author mfiume, tarkvara
 */
public class IOUtils
{

    /**
     * Cheesy method which lets us read from an InputStream without having to instantiate a BufferedReader. Intended to
     * get around some glitches reading GZIPInputStreams over an HTTP stream.
     */
    public static String readLine(InputStream input) throws IOException
    {
        StringBuilder buf = new StringBuilder();
        int c;
        while ((c = input.read()) >= 0 && c != '\n') {
            buf.append((char) c);
        }
        return c >= 0 ? buf.toString() : null;
    }

    /**
     * Checks if an input stream is gzipped.
     * 
     * @param in
     * @return
     */
    public static boolean isGZipped(InputStream in)
    {
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
    public static boolean isGZipped(File f)
    {
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
    public static boolean isZipped(File f)
    {

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
    public static List<File> unzipFile(File file, String toPath) throws ZipException, IOException
    {
        int BUFFER = 2048;

        ZipFile zip = new ZipFile(file);

        // new File(newPath).mkdir();
        Enumeration< ? extends ZipEntry> zipFileEntries = zip.entries();

        List<File> files = new ArrayList<File>();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = zipFileEntries.nextElement();
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
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

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

    public static File zipDirectory(File dir, File outFile) throws IOException
    {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
        zipRecursively(".", dir, out);
        out.close();
        return outFile;
    }

    private static void zipRecursively(String path, File dir, ZipOutputStream out) throws IOException
    {
        for (File f : dir.listFiles()) {
            writeFileToZipStream(path, f, out);
            if (f.isDirectory()) {
                zipRecursively(path + "/" + f.getName() + "/", f, out);
            }
        }
    }

    private static void writeFileToZipStream(String path, File f, ZipOutputStream out) throws IOException
    {
        // name the file inside the zip file

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
}
