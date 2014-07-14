/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

import java.io.File;
import java.io.IOException;
import org.medsavant.api.executionservice.MedSavantExecutionService;
import org.medsavant.api.filestorage.MedSavantFileDirectory;

/**
 * A context for a medsavant server. 
 * @author jim
 */
public interface MedSavantServerContext {
    /**
     * Returns a new temporary directory on the "local" file system.   Temporary directories are deleted when the server
     * exits, as are all files within them.  Temporary directories may be deleted before then by a scheduled external 
     * process.
     * 
     * @return The temporary directory on the local file system.
     */
    public File getTemporaryDirectory();
    
    /**
     * Returns a new, empty, temporary file on the "local" file system.  Temporary files are deleted when the server exits.  
     * Temporary files may be deleted before then by a scheduled external process.  
     *    
     * @param session The session of the user requesting the temporary file, or null.  If non-null, 
     * the temporary file name will include the username, database, and project id.
     * 
     * @return The temporary directory on the local file system.          
     * @throws IOException If the temporary file could not be created.
     */
    public File getTemporaryFile(MedSavantSession session) throws IOException;       
        
    /**
     * Returns a directory that will not be removed.  This is useful for storing 
     * persistent caches, locally. Long term storage of non-cache data is best done
     * via the MedSavantFileDirectory, which may store the file remotely.
     * 
     * @return The persistent directory.
     */
    public File getPersistentCacheDirectory();     
   
    /**     
     * 
     * @return A MedSavantFileDirectory, or null if no file directory has been set.
     */
    public MedSavantFileDirectory getMedSavantFileDirectory();

    /**
     * 
     * @return A MedSavantExecutionService that can and should be used to execute threads. 
     */
    public MedSavantExecutionService getExecutionService();
   
}
