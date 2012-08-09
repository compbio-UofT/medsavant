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

package org.ut.biolab.medsavant.serverapi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.ProjectDetails;


/**
 * Class which maintains provides access to a project's state.
 *
 * @author mfiume
 */
public interface ProjectManagerAdapter extends Remote {

    public String[] getProjectNames(String sessID) throws SQLException, RemoteException;
    public boolean containsProject(String sessID, String projName) throws SQLException, RemoteException;
    public int getProjectID(String sessID, String projName) throws SQLException, RemoteException;
    public void removeReferenceForProject(String sessID, int projID, int refID) throws SQLException, RemoteException;
    public String getProjectName(String sessID, int projID) throws SQLException, RemoteException;
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging) throws SQLException, RemoteException;
    public String getVariantTableName(String sessID, int projID, int refID, boolean published) throws SQLException, RemoteException;
    public String getVariantTableName(String sessID, int projID, int refID, boolean published, boolean sub) throws SQLException, RemoteException;
    public int addProject(String sessID, String name, CustomField[] fields) throws SQLException, ParserConfigurationException, SAXException, IOException, RemoteException;
    public void removeProject(String sessID, String projectName) throws SQLException, RemoteException;
    public void removeProject(String sessID, int projID) throws SQLException, RemoteException;
    public void setAnnotations(String sessID, int projID, int refID, int updID, String annIDs) throws SQLException, RemoteException;
    public ProjectDetails[] getProjectDetails(String sessID, int projID) throws SQLException, RemoteException;
    public void renameProject(String sessID, int projID, String newName) throws SQLException, RemoteException;
    public void setCustomVariantFields(String sessID, int projID, int refID, int updateID, CustomField[] fields) throws SQLException, RemoteException;
    public CustomField[] getCustomVariantFields(String sessID,  int projID, int refID, int updateId) throws SQLException, RemoteException;
    public int getNewestUpdateID(String sessID,  int projID, int refID, boolean published) throws SQLException, RemoteException;
    public ProjectDetails[] getUnpublishedChanges(String sessID) throws SQLException, RemoteException;
    public void addTableToMap(String sessID, int projID, int refID, int updID, boolean published, String tableName, int[] annotationIDs, String subTableName) throws SQLException, RemoteException;

    public String[] getReferenceNamesForProject(String sessID, int projectid) throws SQLException, RemoteException;
    public int[] getReferenceIDsForProject(String sessID, int projID) throws SQLException, RemoteException;
}
