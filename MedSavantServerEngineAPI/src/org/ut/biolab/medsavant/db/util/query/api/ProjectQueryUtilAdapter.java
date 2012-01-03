package org.ut.biolab.medsavant.db.util.query.api;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.ProjectDetails;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
public interface ProjectQueryUtilAdapter extends Remote {

    public List<String> getProjectNames(String sid) throws SQLException, RemoteException;
    public boolean containsProject(String sid,String projectName) throws SQLException, RemoteException;
    public int getProjectId(String sid,String projectName) throws SQLException, RemoteException;
    public void removeReferenceForProject(String sid,int project_id, int ref_id) throws SQLException, RemoteException;
    public String getProjectName(String sid,int projectid) throws SQLException, RemoteException;
    public String createVariantTable(String sid,int projectid, int referenceid, int updateid) throws SQLException, RemoteException;
    public String createVariantTable(String sid,int projectid, int referenceid, int updateid, int[] annotationIds, boolean isStaging) throws SQLException, RemoteException;
    public int getNumberOfRecordsInVariantTable(String sid,int projectid, int refid) throws SQLException, RemoteException;
    public String getVariantTablename(String sid,int projectid, int refid) throws SQLException, RemoteException;
    public int addProject(String sid,String name, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException, RemoteException;
    public void removeProject(String sid,String projectName) throws SQLException, RemoteException;
    public void removeProject(String sid,int projectid) throws SQLException, RemoteException;
    public void setAnnotations(String sid,int projectid, int refid, String annotation_ids, boolean logEntry, String user) throws SQLException, RemoteException;
    public List<ProjectDetails> getProjectDetails(String sid,int projectId) throws SQLException, RemoteException;
    public void renameProject(String sid,int projectId, String newName) throws SQLException, RemoteException;
    public void setCustomVariantFields(String sid,int projectId, List<CustomField> fields, boolean firstSet, String user) throws SQLException, RemoteException;
    public List<CustomField> getCustomVariantFields(String sid,int projectId) throws SQLException, RemoteException;

}
