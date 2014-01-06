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

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


/**
 * Class which maintains provides access to a project's state.
 *
 * @author mfiume
 */
public interface ProjectManagerAdapter extends Remote {

    public String[] getProjectNames(String sessID) throws SQLException, RemoteException, SessionExpiredException;
    public boolean containsProject(String sessID, String projName) throws SQLException, RemoteException, SessionExpiredException;
    public int getProjectID(String sessID, String projName) throws SQLException, RemoteException, SessionExpiredException;
    public void removeReferenceForProject(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException;
    public String getProjectName(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging) throws SQLException, RemoteException, SessionExpiredException;
    public String getVariantTableName(String sessID, int projID, int refID, boolean published) throws SQLException, RemoteException, SessionExpiredException;
    public String getVariantTableName(String sessID, int projID, int refID, boolean published, boolean sub) throws SQLException, RemoteException, SessionExpiredException;
    public int addProject(String sessID, String name, CustomField[] fields) throws SQLException, ParserConfigurationException, SAXException, IOException, RemoteException, SessionExpiredException;
    public void removeProject(String sessID, String projectName) throws SQLException, RemoteException, SessionExpiredException;
    public void removeProject(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
    public void setAnnotations(String sessID, int projID, int refID, int updID, String annIDs) throws SQLException, RemoteException, SessionExpiredException;
    public ProjectDetails[] getProjectDetails(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
    public void renameProject(String sessID, int projID, String newName) throws SQLException, RemoteException, SessionExpiredException;
    public void setCustomVariantFields(String sessID, int projID, int refID, int updateID, CustomField[] fields) throws SQLException, RemoteException, SessionExpiredException;
    public CustomField[] getCustomVariantFields(String sessID,  int projID, int refID, int updateId) throws SQLException, RemoteException, SessionExpiredException;
    public int getNewestUpdateID(String sessID,  int projID, int refID, boolean published) throws SQLException, RemoteException, SessionExpiredException;
    public ProjectDetails[] getUnpublishedChanges(String sessID) throws SQLException, RemoteException, SessionExpiredException;
    public void addTableToMap(String sessID, int projID, int refID, int updID, boolean published, String tableName, int[] annotationIDs, String subTableName) throws SQLException, RemoteException, SessionExpiredException;

    public int[] getDefaultAnnotationIDs(String sessID, int projID, int refID) throws RemoteException, SQLException, SessionExpiredException;
    public String[] getReferenceNamesForProject(String sessID, int projectid) throws SQLException, RemoteException, SessionExpiredException;
    public int[] getReferenceIDsForProject(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
}
