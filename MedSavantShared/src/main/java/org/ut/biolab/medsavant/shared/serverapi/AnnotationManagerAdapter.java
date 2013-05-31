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
package org.ut.biolab.medsavant.shared.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.AnnotationDownloadInformation;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 *
 * @author mfiume
 */
public interface AnnotationManagerAdapter extends Remote {

    public Annotation getAnnotation(String sid,int annotation_id) throws SQLException, RemoteException, SessionExpiredException;
    public Annotation[] getAnnotations(String sid) throws SQLException, RemoteException, SessionExpiredException;

    public int[] getAnnotationIDs(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException;
    public AnnotationFormat getAnnotationFormat(String sessID, int annotID) throws SQLException, RemoteException, SessionExpiredException;

    public boolean installAnnotationForProject(String sessID, int projectID, AnnotationDownloadInformation info) throws RemoteException, SessionExpiredException;
    public void uninstallAnnotation(String sessID, Annotation an) throws RemoteException, SQLException, SessionExpiredException;

}
