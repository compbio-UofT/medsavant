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

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
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

    public String[] getReferenceNamesForProject(String sessID, int projectid) throws SQLException, RemoteException, SessionExpiredException;
    public int[] getReferenceIDsForProject(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
}
