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
import org.medsavant.api.common.MedSavantServerContext;

/**
 *
 * @author jim
 */
public class TabixAnnotatorImpl implements TabixAnnotator{

    private MedSavantServerContext context;
    private static final Map<TabixAnnotation, AnnotationCursor> cursorCache = new HashMap<TabixAnnotation, AnnotationCursor>();
    
    private synchronized AnnotationCursor getAnnotationCursor(TabixAnnotation ta) throws IOException{
        AnnotationCursor ac = cursorCache.get(ta);
        if(ac != null){
            return ac;
        }        
        ac = new AnnotationCursor(ta);
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
    public List<String[]> annotate(String username, JobProgressMonitor jpm, VariantWindow toAnnotate, TabixAnnotation ta) throws AnnotationException{
        try {            
            AnnotationCursor ac = getAnnotationCursor(ta);            
            return Arrays.asList(ac.annotateVariants(toAnnotate));            
        } catch (IOException ie) {
            throw new AnnotationException("Unable to annotate variants with "+ta.getProgram(), ie);
        }        
    }

}
