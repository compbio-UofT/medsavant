/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.common.storage;

import java.io.File;
import java.io.IOException;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.filestorage.MedSavantFileDirectory;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;
import org.medsavant.api.filestorage.MedSavantFileType;

/**
 *
 * @author jim
 */
public interface MedSavantFile {

    public MedSavantFileType getType();

    public String getName();

    public String getCreatorUsername();

    public File moveToTmpFile(MedSavantSession session, MedSavantFileDirectory dir, File tmpDir) throws MedSavantSecurityException, MedSavantFileDirectoryException, IOException;

    public void replaceWithFile(MedSavantSession session, MedSavantFileDirectory dir, File src) throws MedSavantSecurityException, MedSavantFileDirectoryException, IOException;
    /**
     * Uniquely identifies the file, and can be used to fetch this file again
     * from the MedSavantFileDirectory. This may or may not be a local
     * filesystem path, depending on the MedSavantFileDirectory implementation.
     *
     * @see MedSavantFileDirectory
     * @return A unique identifier for this file.
     */
    //public String getPath();

    /**
     *
     * @return The name of the VCF file.
     */
    //public String getName();
    /**
     * @return The username of the user who created this file.
     */
    //public String getCreatorUsername();
    //public InputStream getInputStream();
    //public OutputStream getOutputStreamForAppend();
    /**
     *
     * @return A local file corresponding to this VCF, or null if no such file
     * exists. (e.g. if the file is hosted remotely).
     */
    //public File getLocalFile();        
    /**
     * Closes the file. This operation may not be needed for some
     * implementations.
     */
   // public void close();
}
