package org.ut.biolab.medsavant.db.util.query.api;

import java.io.IOException;
import java.rmi.Remote;
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

    public List<String> getProjectNames(String sid) throws SQLException;
    public boolean containsProject(String sid,String projectName) throws SQLException;
    public int getProjectId(String sid,String projectName) throws SQLException;
    public void removeReferenceForProject(String sid,int project_id, int ref_id) throws SQLException;
    public String getProjectName(String sid,int projectid) throws SQLException;
    public String createVariantTable(String sid,int projectid, int referenceid, int updateid) throws SQLException;
    public String createVariantTable(String sid,int projectid, int referenceid, int updateid, int[] annotationIds, boolean isStaging) throws SQLException;
    public int getNumberOfRecordsInVariantTable(String sid,int projectid, int refid) throws SQLException;
    public String getVariantTablename(String sid,int projectid, int refid) throws SQLException;
    public int addProject(String sid,String name, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException;
    public void removeProject(String sid,String projectName) throws SQLException;
    public void removeProject(String sid,int projectid) throws SQLException;
    public void setAnnotations(String sid,int projectid, int refid, String annotation_ids, boolean logEntry, String user) throws SQLException;
    public List<ProjectDetails> getProjectDetails(String sid,int projectId) throws SQLException;
    public void renameProject(String sid,int projectId, String newName) throws SQLException;
    public void setCustomVariantFields(String sid,int projectId, List<CustomField> fields, boolean firstSet, String user) throws SQLException;
    public List<CustomField> getCustomVariantFields(String sid,int projectId) throws SQLException;

}
