/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.common.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.medsavant.api.common.InvalidConfigurationException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import static org.medsavant.api.common.ScheduleStatus.SCHEDULED_AS_LONGJOB;
import static org.medsavant.api.common.ScheduleStatus.SCHEDULED_AS_SHORTJOB;
import org.medsavant.api.vcfstorage.MedSavantFileDirectory;

/**
 * 
 * @author jim
 */
public class MedSavantServerContextFactory {               
        
    public MedSavantServerContext getInstance() throws InvalidConfigurationException{
        if(MedSavantServerContextImpl.INSTANCE.tmpDir == null || MedSavantServerContextImpl.INSTANCE.cacheDir == null){
            throw new InvalidConfigurationException("MeSavantServerContext has not been initialized!");
        }
        return MedSavantServerContextImpl.INSTANCE;
    }
    
    public synchronized MedSavantServerContext create(File tmpDir, File cacheDir){        
        MedSavantServerContextImpl.INSTANCE.tmpDir = tmpDir;
        MedSavantServerContextImpl.INSTANCE.cacheDir = cacheDir;
        return MedSavantServerContextImpl.INSTANCE;
    }
    
    //MedSavantServerContextImpl is a singleton (per JVM).  Each distributed component will have 
    //their own singleton instance.
    private enum MedSavantServerContextImpl implements MedSavantServerContext {
        INSTANCE;
        private File tmpDir;
        private File cacheDir;
        private ExecutorService longThreadPool;
        private ExecutorService shortThreadPool;

        //Setup some defaults.  These can be overridden with the appropriate setters.
        private int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        private int blockingQueueSize = 100;
        private int maxThreadKeepAliveTime = 86400;//in seconds

        private MedSavantFileDirectory medSavantFileDirectory = null;

        private void initThreadPools() {
        //Long thread pool runs a maximum of maxThreads simultaneous threads, and queues a maximum of 
            //blockQueueSize threads before blocking.        
            BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(blockingQueueSize);
            RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
            longThreadPool = new ThreadPoolExecutor(maxThreads, maxThreads, maxThreadKeepAliveTime, TimeUnit.SECONDS, blockingQueue, rejectedExecutionHandler);
            shortThreadPool = Executors.newCachedThreadPool();
        }
        
        void init(File tmpDir, File cacheDir) {
            this.tmpDir = tmpDir;
            this.cacheDir = cacheDir;
            initThreadPools();
        }
      
        public void setMaxRunningLongJobs(int m) {
            this.maxThreads = m;
        }

        public void setMaxQueuedLongJobs(int m) {
            this.blockingQueueSize = m;
        }

        public void setMaxThreadKeepAliveTime(int seconds) {
            this.maxThreadKeepAliveTime = seconds;
        }

        public MedSavantFileDirectory getMedSavantFileDirectory() {
            return medSavantFileDirectory;
        }

        public void setMedSavantFileDirectory(MedSavantFileDirectory d) {
            this.medSavantFileDirectory = d;
        }

        @Override
        public File getTemporaryDirectory() {
            return tmpDir;
        }

        @Override
        public File getTemporaryFile(MedSavantSession session) throws IOException {
            if (session != null) {
                String prefix = session.getUsernameOfOwner() + "_" + session.getDatabaseName() + "_" + session.getProjectId();
                return File.createTempFile(prefix, null, tmpDir);
            } else {
                return File.createTempFile("tmp", null, tmpDir);
            }
        }

        @Override
        public File getPersistentCacheDirectory() {
            return cacheDir;
        }

       

    }
}
