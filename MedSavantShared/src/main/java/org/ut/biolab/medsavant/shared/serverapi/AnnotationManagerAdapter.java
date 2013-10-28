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

    public boolean installAnnotationForProject(String sessionID, int currentProjectID, int transferID) throws RemoteException, SessionExpiredException, SQLException;
    public boolean installAnnotationForProject(String sessID, int projectID, AnnotationDownloadInformation info) throws RemoteException, SessionExpiredException, SQLException;
    public void uninstallAnnotation(String sessID, int annotationID) throws RemoteException, SQLException, SessionExpiredException;


}
