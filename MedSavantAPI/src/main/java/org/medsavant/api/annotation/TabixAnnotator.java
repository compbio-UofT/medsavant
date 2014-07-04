/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import java.util.List;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantServerComponent;

/**
 * An annotator for a tabix file.  Given variants will be annotated with ALL available
 * annotations in the file.
 * 
 * @see TabixVariantAnnotator.
 * @author jim
 */
public interface TabixAnnotator extends MedSavantServerComponent{
    public List<String[]> annotate(String username, JobProgressMonitor jpm, VariantWindow toAnnotate, TabixAnnotation tabixAnnotation) throws AnnotationException;
}
