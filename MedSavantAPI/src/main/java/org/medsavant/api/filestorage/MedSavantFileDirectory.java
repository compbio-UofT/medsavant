/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.filestorage;

import java.io.InputStream;
import java.io.OutputStream;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerComponent;
import org.medsavant.api.common.MedSavantSession;

/**
 * A directory for the storage and retrieval of files. This file directory may
 * or may not be local. Temporary working files are often best stored on the
 * local file system, but this component also provides a centralized temporary
 * file directory to enable certain types of inter-component communication.
 *
 * @author jim
 * @see MedSavantFileSubDirectory
 */
public interface MedSavantFileDirectory extends MedSavantServerComponent {

    public MedSavantFile createFile(MedSavantSession session, MedSavantFileType fileType) throws MedSavantFileDirectoryException, MedSavantSecurityException;

    public MedSavantFile createFile(MedSavantSession session, MedSavantFileType fileType, String name) throws MedSavantFileDirectoryException, MedSavantSecurityException;

    public void deleteMedSavantFile(MedSavantSession session, MedSavantFile msf) throws MedSavantSecurityException, MedSavantFileDirectoryException;

    public InputStream getInputStream(MedSavantSession session, MedSavantFile file) throws MedSavantFileDirectoryException, MedSavantSecurityException;

    public OutputStream getOutputStream(MedSavantSession session, MedSavantFile file) throws MedSavantFileDirectoryException, MedSavantSecurityException;
    
    public boolean fileExists(MedSavantSession session, MedSavantFile file) throws MedSavantSecurityException;
    
    public MedSavantFile getMedSavantFile(MedSavantSession session, MedSavantFileType fileType, String name) throws MedSavantFileDirectoryException, MedSavantSecurityException;

    
    /**
     * Returns a directory instance that is located at the given path. It is not
     * possible to access or store files at a higher hierarchy level than this
     * path using the returned instance.
     *
     * @param session The session of the user requesting the directory
     * @param path
     * @return A file directory located at the given path.
     * @throws org.medsavant.api.common.MedSavantSecurityException
     * @throws org.medsavant.api.vcfstorage.MedSavantFileDirectoryException
     * @see MedSavantFileSubDirectory
     *
     */
    //public MedSavantFileSubDirectory getDirectoryAt(MedSavantSession session, String path) throws MedSavantSecurityException, MedSavantFileDirectoryException;

    /**
     * @param session The session of the user requesting the directory
     * @return a file directory where annotations can be stored
     * @throws org.medsavant.api.common.MedSavantSecurityException
     * @throws org.medsavant.api.vcfstorage.MedSavantFileDirectoryException
     * @see getDirectoryAt
     * @see MedSavantFileSubDirectory
     */
    //public MedSavantFileSubDirectory getAnnotationDirectory(MedSavantSession session) throws MedSavantSecurityException, MedSavantFileDirectoryException;

    /**
     * @param session The session of the user requesting the directory
     * @return a file directory where VCFs can be stored
     * @throws org.medsavant.api.common.MedSavantSecurityException
     * @throws org.medsavant.api.vcfstorage.MedSavantFileDirectoryException
     * @see getDirectoryAt
     * @see MedSavantFileSubDirectory
     */
    //public MedSavantFileSubDirectory getVCFDirectory(MedSavantSession session) throws MedSavantSecurityException, MedSavantFileDirectoryException;

    /**
     * @param session The session of the user requesting the directory
     * @return a file directory where temporary files can be stored. Temporary
     * contents are periodically cleared.
     * @throws org.medsavant.api.common.MedSavantSecurityException
     * @throws org.medsavant.api.vcfstorage.MedSavantFileDirectoryException
     * @see getDirectoryAt
     * @see MedSavantFileSubDirectory
     */
    //public MedSavantFileSubDirectory getTemporaryDirectory(MedSavantSession session) throws MedSavantSecurityException, MedSavantFileDirectoryException;
}
