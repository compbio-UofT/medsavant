package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.util.query.Annotation;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;

/**
 * @author mfiume
 */
public class ExternalAnnotationController {
    
    private final ArrayList<ExternalAnnotationListener> listeners;

     public void addExternalAnnotation(String program, String version, int referenceid, String path, String format) {
        
        //TODO: do we need this at all? Adding annotations done in server.
         
        /*try {
            int annotationid = AnnotationQueryUtil.addAnnotation(program, version, referenceid, path, format);
            fireAnnotationAddedEvent("" + annotationid);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }*/
    }

    public static interface ExternalAnnotationListener {
        public void annotationAdded(String name);
        public void annotationRemoved(String name);
        public void annotationChanged(String name);
    }
    
    private static ExternalAnnotationController instance;
    
    private ExternalAnnotationController() {
        listeners = new ArrayList<ExternalAnnotationListener>();
    }
    
    public static ExternalAnnotationController getInstance() {
        if (instance == null) {
            instance = new ExternalAnnotationController();
        }
        return instance;
    }
    
    public List<Annotation> getExternalAnnotations() throws SQLException {
        return AnnotationQueryUtil.getAnnotations();
    }
    
    public void fireAnnotationAddedEvent(String projectName) {
        ExternalAnnotationController pc = getInstance();
        for (ExternalAnnotationListener l : pc.listeners) {
            l.annotationAdded(projectName);
        }
    }
    
    public void addReferenceListener(ExternalAnnotationListener l) {
        this.listeners.add(l);
    }
    
}
