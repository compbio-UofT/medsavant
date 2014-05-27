/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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

    public Annotation[] getExternalAnnotations() throws SQLException, RemoteException, SessionExpiredException {
        return MedSavantClient.AnnotationManagerAdapter.getAnnotations(LoginController.getSessionID());
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
