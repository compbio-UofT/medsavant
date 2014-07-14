/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.annotation.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.medsavant.api.annotation.AnnotationException;
import org.medsavant.api.annotation.TabixAnnotation;
import org.medsavant.api.annotation.TabixAnnotator;
import org.medsavant.api.annotation.VariantWindow;
import org.medsavant.api.common.InvalidConfigurationException;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.Reference;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;

/**
 *
 * @author jim
 */
public class TabixAnnotatorImpl implements TabixAnnotator{

    private MedSavantServerContext context;
    private static final Map<TabixAnnotation, AnnotationCursor> cursorCache = new HashMap<TabixAnnotation, AnnotationCursor>();
    
    private synchronized AnnotationCursor getAnnotationCursor(MedSavantSession session, TabixAnnotation ta) throws IOException,  MedSavantSecurityException, MedSavantFileDirectoryException{
        AnnotationCursor ac = cursorCache.get(ta);
        if(ac != null){
            return ac;
        }        
        
        ac = new AnnotationCursor(session, ta, context.getMedSavantFileDirectory(), context.getPersistentCacheDirectory());
        cursorCache.put(ta, ac);
        return ac;        
    }
    
    @Override
    public String getComponentID() {
        return TabixAnnotatorImpl.class.getName();
    }

    @Override
    public String getComponentName() {
        return "Tabix Annotator";
    }

    @Override
    public void configure(Dictionary dict) throws InvalidConfigurationException {
        //no configuration options.
    }

    @Override
    public void configure(String key, Object val) throws InvalidConfigurationException {
        //no configuration options.
    }

    @Override
    public void setServerContext(MedSavantServerContext context) throws InvalidConfigurationException {
        this.context = context;
    }

    @Override
    public List<String[]> annotate(MedSavantSession session, JobProgressMonitor jpm, VariantWindow toAnnotate, TabixAnnotation ta, Reference reference) throws AnnotationException{
        try {            
            jpm.setMessage("Annotating "+toAnnotate.getNumVariants()+" variants from "+session.getUser()+" in project "+session.getProject().getProjectName());
            AnnotationCursor ac = getAnnotationCursor(session, ta);                        
            List<String[]> result =  Arrays.asList(ac.annotateVariants(toAnnotate));            
            jpm.setMessage("Completed annotation of "+toAnnotate.getNumVariants()+" variants from "+session.getUser()+" in project "+session.getProject().getProjectName());
            return result;
        } catch (IOException ie) {
            throw new AnnotationException("Unable to annotate variants with "+ta.getProgram(), ie);
        } catch(MedSavantSecurityException mse){
            throw new AnnotationException("Unable to annotate variants with "+ta.getProgram()+" -- this user does not have access to this annotation", mse);
        } catch(MedSavantFileDirectoryException mfde){
            throw new AnnotationException("Unablet o annotate variants with "+ta.getProgram()+" -- problem loading annotation", mfde);
        }
    }

}
