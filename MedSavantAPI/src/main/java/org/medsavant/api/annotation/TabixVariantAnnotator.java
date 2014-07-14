/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.annotation;

import java.util.Dictionary;
import java.util.List;
import org.medsavant.api.common.InvalidConfigurationException;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.Reference;

/**
 *
 * An annotator for a tabix file.  Given variants will be annotated with ALL available
 * annotations in the file.  
 * 
 * This class wraps TabixAnnotator to make it compatible with the VariantAnnotator interface. 
 *   
 * @author jim
 */
public class TabixVariantAnnotator implements VariantAnnotator {

    //Note: OSGI R6 defines a prototype service factory, which would allow one to 
    //obtain separate instances of a TabixAnnotator.  Each of these instances could be 
    //configured to handle a particular tabix annotation.  Currently however, it must be assumed
    //that the TabixAnnotator instance is shared, so this wrapper class is necessary to maintain
    //the TabixAnnotation and pass it through to the TabixAnnotator.
    
    private final TabixAnnotator ta;
    private final TabixAnnotation tan;

    public TabixVariantAnnotator(TabixAnnotator ta, TabixAnnotation tan) {
        this.ta = ta;
        this.tan = tan;
    }  

    public List<String> getPrerequisiteVCFPreProcessors() {
        return null;
    }

    @Override
    public List<VariantAnnotator> getPrerequisiteVariantAnnotators() {
        return null;
    }

    @Override
    public MedSavantAnnotation getAnnotation() {
        return tan;
    }

    @Override
    public String getComponentID() {        
        return TabixVariantAnnotator.class.getCanonicalName();
    }

    @Override
    public String getComponentName() {
        return "Tabix Variant Annotator";
    }

    @Override
    public void configure(Dictionary dict) throws InvalidConfigurationException {
        //no configuration
    }

    @Override
    public void configure(String key, Object val) throws InvalidConfigurationException {
        //no configuration
    }

    @Override
    public void setServerContext(MedSavantServerContext context) throws InvalidConfigurationException {
        //no server context is needed.
    }

    @Override
    public List<String[]> annotate(MedSavantSession session, JobProgressMonitor jpm, VariantWindow toAnnotate, Reference reference) throws AnnotationException {
        //pass through to the tabix annotator.
        return ta.annotate(session, jpm, toAnnotate, tan, reference);
    }   

}
