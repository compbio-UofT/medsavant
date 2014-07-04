/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import java.io.IOException;
import java.util.List;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantServerComponent;
import org.medsavant.api.common.storage.MedSavantFile;

/**
 * Class for preprocessing of VCF files.  Whenever possible, it is better to 
 * use the VariantAnnotator class to process VCF files, as it may involve less
 * I/O, and is more flexible for parallelization.  
 * 
 * However, some 3rd party tools will only work on whole VCF files (e.g. phasing
 * via Beagle), and some preprocessing steps will speed up subsequent steps 
 * (e.g. sorting).
 * 
 * @author jim
 */
public interface VCFPreProcessor extends MedSavantServerComponent{        
    public List<String> getPrerequisiteVCFPreProcessors();    
    public MedSavantFile preprocess(String username, JobProgressMonitor jpm, MedSavantFile toAnnotate) throws IOException, VCFPreProcessorException;                
    //public File preprocess(File toAnnotate, File tmpDir) throws IOException, VCFPreProcessorException;
}
