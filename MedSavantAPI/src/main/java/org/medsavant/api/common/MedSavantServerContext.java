/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

import org.medsavant.api.common.impl.MedSavantServerJob;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import org.medsavant.api.vcfstorage.MedSavantFileDirectory;

/**
 * 
 * @author jim
 */
public interface MedSavantServerContext {
    public File getTemporaryDirectory();
    
    /**
     * Returns a new, empty, temporary file.  Temporary files are deleted when the server exits.  
     * Temporary files may be deleted before then by a scheduled external process.  
     *    
     * @param session The session of the user requesting the temporary file, or null.  If non-null, 
     * the temporary file name will include the username, database, and project id.
     * 
     * @return The temporary file.          
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
     * @return the maximum number of simultaneous long jobs that can be executed simultaneously on this server.
     */
    public int getMaxRunningLongJobs();    
    
    /**
     *
     * @return the maximum number of long jobs that can be queued for execution.
     */
    public int getMaxQueuedLongJobs();
    
    /**     
     * 
     * @return A MedSavantFileDirectory, or null if no file directory has been set.  
     */
    public MedSavantFileDirectory getMedSavantFileDirectory();

    /**
     * Runs the given job in the current thread.  BLOCKING.
     * 
     * @param msj The job to run
     * @return Void
     * @throws Exception 
     */
    public Void runJobInCurrentThread(MedSavantServerJob msj) throws Exception;

    /**
     * Submits and runs the given job using the short job executor service,
     * and immediately returns. An unlimited number of short jobs can be
     * executing simultaneously.  This should only be used for jobs with a short running time (e.g. <10s)
     *
     * NON_BLOCKING.
     *
     * @return The pending result of the job. Trying to fetch the result with
     * the 'get' method of Future will BLOCK. get() will return null upon
     * successful completion.
     */    
    public Future submitShortJob(MedSavantServerJob msj); 
    
    /**
     * Submits and runs the given job using the long job executor service,
     * and immediately returns. Up to getMaxRunningLongJobs() long jobs can be
 executing simultaneously, and up to getMaxQueuedLongJobs() can be queued.  If a long job is submitted
 that would cuase > getMaxRunningLongJobs() to be running, then that job will be queued if the total number of long jobs
 (running + queued) is < getMaxQueuedLongJobs(). If (running+queued)==getMaxQueuedLongJobs(), then this method will
     * BLOCK until a queue slot becomes available.  
     *
     * NON_BLOCKING, unless queue size is exceeded.
     *
     * @return The pending result of the job. Trying to fetch the result with
     * the 'get' method of Future will BLOCK. get() will return null upon
     * successful completion.
     */    
    public Future submitLongJob(MedSavantServerJob msj);    
    
    /**
     * Equivalent to executing submitShortJob for each of the jobs in the given list, except that this
     * method will block until all jobs in the list are completed.  This function does not perform
     * error checking: if you want to know if a job at index i was successful,
     * invoke returnVal.get(i).get(); and catch the ExecutionException.
     * 
     * BLOCKING
     * 
     * @param msjs
     * @return
     * @throws InterruptedException 
     */
    public List<Future<Void>> submitShortJobs(List<MedSavantServerJob> msjs) throws InterruptedException;
    public List<Future<Void>> submitLongJobs(List<MedSavantServerJob> msjs) throws InterruptedException;
   
}
