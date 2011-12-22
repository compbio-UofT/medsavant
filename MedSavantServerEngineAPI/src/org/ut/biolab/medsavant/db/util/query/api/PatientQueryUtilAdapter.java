package org.ut.biolab.medsavant.db.util.query.api;

import java.io.IOException;
import java.rmi.Remote;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
public interface PatientQueryUtilAdapter extends Remote {

    public List<Object[]> getBasicPatientInfo(String sid,int projectId, int limit) throws SQLException, NonFatalDatabaseException;
    public List<Object[]> getPatients(String sid,int projectId) throws SQLException;
    public Object[] getPatientRecord(String sid,int projectId, int patientId) throws SQLException;
    public List<String> getPatientFieldAliases(String sid,int projectId) throws SQLException;
    public List<CustomField> getPatientFields(String sid,int projectId) throws SQLException;
    public List<CustomField> getCustomPatientFields(String sid,int projectId) throws SQLException;
    public String getPatientTablename(String sid,int projectId) throws SQLException;
    public void createPatientTable(String sid,int projectid, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException;
    public void removePatient(String sid,int projectId, int[] patientIds) throws SQLException;
    public void addPatient(String sid,int projectId, List<CustomField> cols, List<String> values) throws SQLException;
    public Map<Object, List<String>> getDNAIdsForValues(String sid,int projectId, String columnName) throws NonFatalDatabaseException, SQLException;
    public List<String> getDNAIdsWithValuesInRange(String sid,int projectId, String columnName, Range r) throws NonFatalDatabaseException, SQLException;
    public List<String> getDNAIdsForStringList(String sid,TableSchema table, List<String> list, String columnname) throws NonFatalDatabaseException, SQLException;
    public void updateFields(String sid,int projectId, List<CustomField> fields) throws SQLException;
    public List<Object> getValuesFromField(String sid,int projectId, String columnNameA, String columnNameB, List<Object> values) throws SQLException;
    public List<String> getDNAIdsFromField(String sid,int projectId, String columnNameA, List<Object> values) throws SQLException;
    public List<String> getValuesFromDNAIds(String sid,int projectId, String columnNameB, List<String> ids) throws SQLException;
    public List<Object[]> getFamily(String sid,int projectId, String family_id) throws SQLException;
    public List<Object[]> getFamilyOfPatient(String sid,int projectId, int pid) throws SQLException;
    public String getFamilyIdOfPatient(String sid,int projectId, int pid) throws SQLException;
    public List<String> getFamilyIds(String sid,int projectId) throws SQLException;
    public Map<String,String> getDNAIdsForFamily(String sid,int projectId, String familyId) throws SQLException;
    public void clearPatients(String sid,int projectId) throws SQLException;
    public List<String> parseDnaIds(String s);
}
