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
 * An annotator for a tabix file.  Given variants will be annotated with ALL available
 * annotations in the file.
 * 
 * @see TabixVariantAnnotator.
 * @author jim
 */
public interface TabixAnnotator extends MedSavantServerComponent{
    public List<String[]> annotate(MedSavantSession session, JobProgressMonitor jpm, VariantWindow toAnnotate, TabixAnnotation tabixAnnotation, Reference reference) throws AnnotationException;
}
