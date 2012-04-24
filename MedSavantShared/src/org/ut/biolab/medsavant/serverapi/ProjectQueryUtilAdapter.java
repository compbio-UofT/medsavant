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
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.ProjectDetails;


/**
 *
 * @author mfiume
 */
public interface ProjectQueryUtilAdapter extends Remote {

    public List<String> getProjectNames(String sessID) throws SQLException, RemoteException;
    public boolean containsProject(String sessID, String projectName) throws SQLException, RemoteException;
    public int getProjectId(String sessID, String projectName) throws SQLException, RemoteException;
    public void removeReferenceForProject(String sessID, int project_id, int ref_id) throws SQLException, RemoteException;
    public String getProjectName(String sessID, int projectid) throws SQLException, RemoteException;
    //public String createVariantTable(String sessID, int projectid, int referenceid, int updateid) throws SQLException, RemoteException;
    public String createVariantTable(String sessID, int projectid, int referenceid, int updateid, int[] annotationIds, boolean isStaging) throws SQLException, RemoteException;
    public int getNumberOfRecordsInVariantTable(String sessID, int projectid, int refid) throws SQLException, RemoteException;
    public String getVariantTablename(String sessID, int projectid, int refid, boolean published) throws SQLException, RemoteException;
    public String getVariantTablename(String sessID, int projectid, int refid, boolean published, boolean sub) throws SQLException, RemoteException;
    public int addProject(String sessID, String name, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException, RemoteException;
    public void removeProject(String sessID, String projectName) throws SQLException, RemoteException;
    public void removeProject(String sessID, int projectid) throws SQLException, RemoteException;
    public void setAnnotations(String sessID, int projectid, int refid, int updateid, String annotation_ids) throws SQLException, RemoteException;
    public List<ProjectDetails> getProjectDetails(String sessID, int projectId) throws SQLException, RemoteException;
    public void renameProject(String sessID, int projectId, String newName) throws SQLException, RemoteException;
    public void setCustomVariantFields(String sessID, int projectId, int referenceId, int updateId, List<CustomField> fields) throws SQLException, RemoteException;
    public List<CustomField> getCustomVariantFields(String sessID,  int projectId, int referenceId, int updateId) throws SQLException, RemoteException;
    public int getNewestUpdateId(String sessID,  int projectId, int referenceId, boolean published) throws SQLException, RemoteException;
    public boolean existsUnpublishedChanges(String sessID,  int projectId) throws SQLException, RemoteException;
    public boolean existsUnpublishedChanges(String sessID,  int projectId, int referenceId) throws SQLException, RemoteException;
    public void addTableToMap(String sessID,  int projectid, int referenceid, int updateid, boolean published, String tablename, String subTablename) throws SQLException, RemoteException;
}
