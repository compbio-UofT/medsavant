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
import java.util.Map;
import java.util.Set;

import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
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
    public AnnotationFormat[] getAnnotationFormats(String sessID, int projectID, int refID) throws SQLException, RemoteException, SessionExpiredException;
    /**
     * Returns a mapping of defined tags to the customfields with that tag.  
     * 
     * @param sessionId - the user's session
     * @param includeUndefined - if true, custom fields without any tag will be included in the map under the key 'UNDEFINED'.  
     * If false, these fields would not be returned in the map.
     * @return A mapping from tags to all custom fields assigned that tag. If a custom field is assigned multiple tags,
     * it will appear multiple times in the map with correspondingly different keys.
     * @throws SQLException
     * @throws RemoteException
     * @throws SessionExpiredException
     */
    public Map<String, Set<CustomField>> getAnnotationFieldsByTag(String sessionId, boolean includeUndefined) throws SQLException, RemoteException,  SessionExpiredException;

    
    /**          
     * @param sessID - The user's session ID.
     * @return the set of all tags used to tag installed annotation customfields.
     * @throws SQLException
     * @throws RemoteException
     * @throws SessionExpiredException 
     */
    public Set<String> getAnnotationTags(String sessID) throws SQLException, RemoteException, SessionExpiredException;
    public boolean installAnnotationForProject(String sessionID, int currentProjectID, int transferID) throws RemoteException, SessionExpiredException, SQLException;
    public boolean installAnnotationForProject(String sessID, int projectID, AnnotationDownloadInformation info) throws RemoteException, SessionExpiredException, SQLException;
    public void uninstallAnnotation(String sessID, int annotationID) throws RemoteException, SQLException, SessionExpiredException;
}
