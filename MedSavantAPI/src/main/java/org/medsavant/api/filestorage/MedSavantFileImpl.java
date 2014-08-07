/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.filestorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Calendar;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.getFile;
import org.medsavant.api.common.MedSavantFileUtils;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.filestorage.MedSavantFile;
import org.medsavant.api.filestorage.MedSavantFileDirectory;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;
import org.medsavant.api.filestorage.MedSavantFileType;

/**
 *
 * @author jim
 */
public class MedSavantFileImpl implements MedSavantFile {

    private final String name;
    private final String userName;
    private final MedSavantFileType fileType;
    protected final String path;

    public MedSavantFileImpl(String name, String userName, MedSavantFileType fileType) {
        this(name, userName, fileType, null);
    }

    /**     
     * Constructs a new MedSavant File. The 'path' argument may be null, in
 which case file transfers to and from the MedSavantFileDirectory will
 always involve copying a stream to or from a file in the directory. If
 path is set to the actual path to the file (on the
 MedSavantFileDirectory) and the component invoking a file transfer via
 getFile or replaceWithFile is located on the same file system, then file
 transfers can occur by moving the file.

 In distributed configurations, there is no advantage to setting path
 non-null.
 * 
 * 
     *
     * @param name
     * @param userName
     * @param fileType
     * @param path
     * @throws UnknownHostException
     * @see getFile
     * @see putFile#replaceWithFile
     */
    //if path is non null and file type is temporary, then the file is MOVEABLE from the medsavant directory.
    public MedSavantFileImpl(String name, String userName, MedSavantFileType fileType, String path) throws IllegalArgumentException {
        this.name = name;
        this.userName = userName;
        this.fileType = fileType;
        this.path = path;
        if (path != null && this.fileType != MedSavantFileType.TEMPORARY) {
            throw new IllegalArgumentException("Illegal argument: path must be null for non-temporary files");
        }
    }

    @Override
    public MedSavantFileType getType() {
        return fileType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCreatorUsername() {
        return userName;
    }

    /**
     *
     * @param session The user's session.
     * @param dir The directory where the local file should be put.
     * @return A local file located in the 'destDir' with the contents of this
     * file.
     */
    @Override
    public File moveToTmpFile(MedSavantSession session, MedSavantFileDirectory dir, File tmpDir) throws MedSavantSecurityException, MedSavantFileDirectoryException, IOException {
        dir.fileExists(session, this);  //purpose is to execute a security check        

        //If the path exists on the local file system, then return that file.
        if (path != null) {
            File f = new File(path);
            if (f.exists()) {
                return f;
            }
        }

        //Otherwise, make a copy.
        InputStream is = null;
        is = dir.getInputStream(session, this);
        
        Path p = Files.createTempFile(tmpDir.toPath(), session.getUser() + "_" + (Calendar.getInstance().getTimeInMillis() / 100L), null, (FileAttribute<?>) null);
        File outputFile = p.toFile();

        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            MedSavantFileUtils.copy(is, fos);
            dir.deleteMedSavantFile(session, this);
            return outputFile;
        } catch (FileNotFoundException fnfe) {
            String err = "Couldn't create output file " + outputFile.getCanonicalPath();
            throw new IOException(err, fnfe);
        }

    }

    /**
     *
     * @param session
     * @param dir
     * @param src
     * @param dst
     * @return
     */
    //puts the file 'src' into this file (which is the destination)
    //rename to 'replace'?
    @Override
    public void replaceWithFile(MedSavantSession session, MedSavantFileDirectory dir, File src) throws MedSavantSecurityException, MedSavantFileDirectoryException, IOException {
        //make sure this file exists on the remote.
        dir.fileExists(session, this);

        //If the path is located on this file system, then just move the file there.
        if (path != null) {
            File f = new File(path);
            if (f.exists()) {
                FileUtils.moveFile(src, f);
            }
        }
        try {
            OutputStream os = dir.getOutputStream(session, this);
            FileInputStream fis = new FileInputStream(src);
            MedSavantFileUtils.copy(fis, os);
        } catch (FileNotFoundException fnfe) {
            String err = "Couldn't find input file " + src.getCanonicalPath();
            throw new IOException("Couldn't find input file " + src.getName(), fnfe);
        }

    }
}
