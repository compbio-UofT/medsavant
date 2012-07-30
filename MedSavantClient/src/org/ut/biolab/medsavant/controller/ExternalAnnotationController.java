/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Annotation;

/**
 * @author mfiume
 */
public class ExternalAnnotationController {

    private final ArrayList<ExternalAnnotationListener> listeners;

     public void addExternalAnnotation(String program, String version, int referenceid, String path, String format) {

        //TODO: do we need this at all? Adding annotations done in server.

        /*try {
            int annotationid = MedSavantClient.AnnotationQueryUtilAdapter.addAnnotation(program, version, referenceid, path, format);
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

    public Annotation[] getExternalAnnotations() throws SQLException, RemoteException {
        return MedSavantClient.AnnotationManagerAdapter.getAnnotations(LoginController.sessionId);
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
