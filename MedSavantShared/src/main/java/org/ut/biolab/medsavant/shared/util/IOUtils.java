/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
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
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

/**
 * I/O-related utility methods. Functions for manipulating Savant files are in
 * SavantFileUtils.
 *
 * @author mfiume, tarkvara
 */
public class IOUtils {

    private static final Log LOG = LogFactory.getLog(IOUtils.class);

    /**
     * 
     * @param inputFile - the file to check
     * @param directory - the directory to test for.
     * @return true if 'directory' is an ancestor of 'inputFile', false otherwise.
     * @throws IOException 
     */
    public static boolean isInDirectory(File inputFile, File directory) throws IOException {
        if (inputFile.isDirectory() && inputFile.getAbsolutePath().equals(directory.getAbsolutePath())) {
            return true;
        }

        File parent = inputFile.getParentFile();
        while (parent != null) {
            if (parent.getCanonicalPath().equals(directory.getCanonicalPath())) {
                return true;
            }
            parent = parent.getParentFile();
        }

        return false;
    }
    
    /**
     * Deletes the directory p, recursing upwards through the directory tree and deleting all empty
     * parent directories until it reaches the ancestor directory 'stopAtDir'. Immediately returns
     * if 'stopAtDir' is not an ancestor of p.  
     * 
     * @param p - The nested directory to delete.
     * @param stopAtDir - the ancestor directory to stop at (this directory will NOT be deleted, even if empty).
     * @throws IOException 
     */
    public static void deleteEmptyParents(File p, File stopAtDir) throws IOException{
        if(!isInDirectory(p, stopAtDir)){
            return;
        }
        if(p != null && p.isDirectory() && !p.getAbsolutePath().equals(stopAtDir.getAbsolutePath())){
            if(p.listFiles().length == 0){
                File parent = p.getParentFile();
                p.delete();
                deleteEmptyParents(parent, stopAtDir);
            }
        }  
    }    
    
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
        long tot = 0;
        try {
            while ((len = input.read(buf)) > 0) {
                tot+=len;              
                output.write(buf, 0, len);
            }
            LOG.info("Copied/extracted "+tot+" bytes");            
        } catch (IOException x) {
            // There's a bug in BlockCompressedInputStream where it throws an IOException instead of doing a proper EOF.
            // Suppress this exception, but throw real ones.
            if (!x.getMessage().equals("Unexpected compressed block length: 1")) {
                x.printStackTrace();
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
    public static void checkForWorldExecute(File f) throws IOException {
        //File f = base.isDirectory() ? base : base.getParentFile();
        if (hasWorldExecute(f)) {
            /*f = f.getParentFile();
             if (f == null) {
             // Reached /
             return;
             }*/
            return;
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
        //LOG.info("Permission of " + f.getAbsolutePath() + " is " + result);
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

    private static List<File> unArchive(File dest, ArchiveInputStream ais) throws IOException {
        List<File> files = new ArrayList<File>();
        ArchiveEntry archiveEntry = ais.getNextEntry();
        // tarIn is a TarArchiveInputStream
        while (archiveEntry != null) {// create a file with the same name as the tarEntry            
            File destPath = new File(dest, archiveEntry.getName());
            
            if (archiveEntry.isDirectory()) {
                destPath.mkdirs();
            } else {                
                destPath.createNewFile();            
                byte[] btoRead = new byte[1024];
            
                BufferedOutputStream bout
                        = new BufferedOutputStream(new FileOutputStream(destPath));
                
                int len = 0;
                while ((len = ais.read(btoRead)) != -1) {
                    bout.write(btoRead, 0, len);
                }

                bout.close();
                btoRead = null;
                files.add(destPath);
            }
            
            archiveEntry = ais.getNextEntry();
        }
        ais.close();
        return files;
    }

    /**
     * Strips the last extension from the filename corresponding to f, 
     * and returns a new File for this new filename, or null if 
     * the file was not found to have an extension.    
     */
    public static String stripExtension(File f){
        String filename = f.getName();
        for(int i = filename.length() - 1; i > 0; --i){
            if(filename.charAt(i) == '.'){
                return filename.substring(0, i);
            }
        }
        return null;
    }
        
    /**
     * Moves a file from one location to another using apache io library.
     * Use this instead of File.renameTo
     * 
     * @param src - the path to the source file
     * @param dst - the path to the destination file
     * @return 
     */
    public static boolean moveFile(File src, File dst){
        try{
            FileUtils.moveFile(src, dst);
        }catch(FileExistsException fee){
            LOG.error("Error while moving file", fee);
            return false;
        }catch(IOException ie){
            LOG.error("Error while moving File", ie);
            return false;
        }
        return true;
    }
    
    /**
     * @return true if the File is a compressed file, archive, or compressed archive.
     */   
    public static boolean isArchive(File f){
        String n = f.getName().toLowerCase();
        return n.endsWith(".zip") || n.endsWith(".bz2") || n.endsWith(".tgz") || n.endsWith(".gz") || n.endsWith(".tar");
    }
    
    /**
     * Decompresses/unarchives the file given by 'f' to the destination path
     * given by dest, and returns a list of all the files contained within the archive.
     * If the input file is compressed, but not archived, the destination filename will be 
     * "dest/"+stripExtension(f.getName()).  If the input file is 
     * not compressed or archived, the input file will be moved to the new location 
     * given by dest, and returned in a one-element list.
     * 
     * Note the file given by f will be deleted.
     *
     * @return A list of files that were decompressed from the input file f, or
     * a list containing the input file in its new location if the input file was not compressed/archived.
     * @throws IOException 
     * @see stripExtension
     */
    public static List<File> decompressAndDelete(File f, File dest) throws IOException {
        String lcfn = f.getName().toLowerCase();
        InputStream is = new BufferedInputStream(new FileInputStream(f));
        List<File> files;

        //Detect compression type.
        if (lcfn.endsWith(".gz") || lcfn.endsWith(".tgz")) {
            is = new GzipCompressorInputStream(is, true);
        } else if (lcfn.endsWith(".bz2")) {
            is = new BZip2CompressorInputStream(is, true);
        }

        //Detect archive type.
        if (lcfn.endsWith(".tgz") || lcfn.endsWith(".tar.gz") || lcfn.endsWith(".tar") || lcfn.endsWith(".tar.bz2")) {
            is = new TarArchiveInputStream(is);
            files = unArchive(dest, (ArchiveInputStream)is);
        } else if (lcfn.endsWith(".zip")) {
            is = new ZipArchiveInputStream(is);
            files = unArchive(dest, (ArchiveInputStream)is);
        } else{
            String filename = f.getName();
            if (!(is instanceof BufferedInputStream)) {
                filename = stripExtension(f);
                if(filename == null){
                    filename = f.getName()+".decompressed";
                }
            }
            
            File outputFile = new File(dest, filename);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
            if(!(is instanceof BufferedInputStream)){                
                IOUtils.copyStream(is, bos);
            }else{
                if(!moveFile(f, outputFile)){
                    throw new IOException("Couldn't move file "+f.getAbsolutePath()+" to "+outputFile.getAbsolutePath());
                }
            }
            
            bos.close();
            files = new ArrayList<File>(1);
            files.add(outputFile);            
        } 
        is.close();
        if(f.exists()){
            f.delete();
        }
        return files;
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

            if (currentEntry.toLowerCase().endsWith(".zip") && isZipped(destFile)) {
                // found a zip file, try to open
                files.addAll(unzipFile(destFile, new File(destFile.getAbsolutePath()).getParent()));
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
            writeFileToZipStream(path, f, out);
            if (f.isDirectory()) {
                zipRecursively(path + "/" + f.getName() + "/", f, out);
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
