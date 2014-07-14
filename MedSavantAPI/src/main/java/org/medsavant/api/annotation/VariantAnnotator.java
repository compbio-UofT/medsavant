/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import java.util.List;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantServerComponent;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.Reference;

/**
 *
 * @author jim
 */
public interface VariantAnnotator extends MedSavantServerComponent {       
    
    public List<String> getPrerequisiteVCFPreProcessors();
    public List<VariantAnnotator> getPrerequisiteVariantAnnotators();    
    public MedSavantAnnotation getAnnotation();
    /**
     * Called repeatedly to annotate variants.  
     * 
     * @param username The username of the user who started this annotation job.
     * @param jpm The job progress monitor that will accept progress messages.
     * @param toAnnotate The collection of variants to annotate.     
     * @param subsequentAnnotatorIds The component identifiers of annotators that should be called after this annotator is finished annotating.     
     * @return a list of annotations, in the same order as passed in the variant window.  A null entry indicates 'missing'.  a null return value indicates
     * all values are 'missing'.
     */
    public List<String[]> annotate(MedSavantSession session, JobProgressMonitor jpm, VariantWindow toAnnotate, Reference reference) throws AnnotationException;
}
