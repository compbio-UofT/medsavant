/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common.impl;

import java.io.File;
import java.io.IOException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.executionservice.MedSavantExecutionService;
import org.medsavant.api.filestorage.MedSavantFileDirectory;

//MedSavantServerContextImpl is a singleton (per JVM).  
 public enum MedSavantServerContextImpl implements MedSavantServerContext {
    INSTANCE;
    private File tmpDir;
    private File cacheDir;
    private MedSavantFileDirectory medSavantFileDirectory = null;
    private MedSavantExecutionService mes;

    public MedSavantServerContextImpl init(File tmpDir, File cacheDir, MedSavantExecutionService mes) {
        this.tmpDir = tmpDir;
        this.cacheDir = cacheDir;
        return this;
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
            String prefix = session.getUser().getUsername() + "_" + session.getProject().getDatabaseName() + "_" + session.getProject().getProjectId();
            return File.createTempFile(prefix, null, tmpDir);
        } else {
            return File.createTempFile("tmp", null, tmpDir);
        }
    }

    @Override
    public File getPersistentCacheDirectory() {
        return cacheDir;
    }

    @Override
    public MedSavantExecutionService getExecutionService() {
        return mes;
    }
    
}
