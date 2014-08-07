/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.MedSavantUpdate;

/**
 *
 * @author jim
 */
public interface AnnotationPipeline {        
        /**
     * Adds a preprocessor that operates on whole VCF files.  This should be avoided, but
     * it's included here to support certain closed source applications (e.g. Beagle) that
     * only accept entire files as input.
     * 
     * @param vpp The variant preprocessor to add to the pipeline.
     * @return this object
     * @throws InvalidAnnotationPipelineException if adding this preprocessor would result in an invalid pipeline.
     */
    public void addVCFPreProcessor(VCFPreProcessor vpp) throws InvalidAnnotationPipelineException;    
    public void addVariantAnnotator(VariantAnnotator ann) throws InvalidAnnotationPipelineException;        
    public void start(MedSavantSession session, JobProgressMonitor jpm, MedSavantUpdate update) throws MedSavantSecurityException;
    
}
