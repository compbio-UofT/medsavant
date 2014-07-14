/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.filestorage;

import java.io.FileNotFoundException;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.storage.MedSavantFile;

/**
 *
 * @author jim
 */
public interface MedSavantFileSubDirectory {
     /**     
      * 
     * @param session The session of the user requesting the file.
     * @param path The identifier of the file
     * @return The MedSavantFile corresponding to the given path.  This file can be read via the inputstream provided by MedSavantFile
     * @throws FileNotFoundException 
     * @see MedSavantFile
     */
    public MedSavantFile getMedSavantFile(MedSavantSession session, String path) throws MedSavantSecurityException, FileNotFoundException;         
    
    
    /**
     * Registers a new file under the identity 'path'.  Both the path and the file
     * will be converted to lower case first.  The file is initially empty, but can be written
     * to via the outputstream provided by MedSavantFile.getOutputStreamForAppend
     * 
     * @param session The session of the user requesting file registration.
     * @param path The identifier of the file.  On a local filesystem, this could be the full canonical pathname plus filename.
     * @param inputStream Inputstream from which the file contents can be read.  This stream will be closed after this method is invoked.
     * @return A MedSavantFile The newly registered MedSavantFile.
     * @throws org.medsavant.api.vcfstorage.MedSavantFileDirectoryException If the file already exists, or couldn't be created.
     * @see MedSavantFile
    */
    public MedSavantFile registerMedSavantFile(MedSavantSession session, String path) throws MedSavantSecurityException, MedSavantFileDirectoryException;
    
    /**
     * Registers a new file with a unique identifier.  
     * 
     * @param session
     * @return
     * @throws MedSavantSecurityException 
     */
    public MedSavantFile registerMedSavantFile(MedSavantSession session) throws MedSavantSecurityException;
    
    /**
     * Deletes the file corresponding to the path.
     * 
     * @param session The session of the user requesting file deletion.
     * @param path
     * @throws MedSavantFileDirectoryException 
     */
    public void deleteMedSavantFile(MedSavantSession session, String path) throws MedSavantSecurityException, MedSavantFileDirectoryException;
}
