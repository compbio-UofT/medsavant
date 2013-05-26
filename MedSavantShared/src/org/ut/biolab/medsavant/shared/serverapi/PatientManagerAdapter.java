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
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public interface PatientManagerAdapter extends Remote {

    public List<Object[]> getBasicPatientInfo(String sessID, int projectId, int limit) throws SQLException, RemoteException, SessionExpiredException;
    public List<Object[]> getPatients(String sessID, int projectId) throws SQLException, RemoteException, SessionExpiredException;
    public Object[] getPatientRecord(String sessID, int projectId, int patientId) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getPatientFieldAliases(String sessID, int projectId) throws SQLException, RemoteException, SessionExpiredException;
    public CustomField[] getPatientFields(String sessID, int projectId) throws SQLException, RemoteException, SessionExpiredException;
    public CustomField[] getCustomPatientFields(String sessID, int projectId) throws SQLException, RemoteException, SessionExpiredException;
    public String getPatientTableName(String sessID, int projectId) throws SQLException, RemoteException, SessionExpiredException;
    public void createPatientTable(String sessID, int projectid, CustomField[] fields) throws SQLException, ParserConfigurationException, SAXException, IOException, RemoteException, SessionExpiredException;
    public void removePatient(String sessID, int projectId, int[] patientIds) throws SQLException, RemoteException, SessionExpiredException;
    public void addPatient(String sessID, int projectId, List<CustomField> cols, List<String> values) throws SQLException, RemoteException, SessionExpiredException;
    public Map<Object, List<String>> getDNAIDsForValues(String sessID, int projectId, String columnName) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsWithValuesInRange(String sessID, int projectId, String columnName, Range r) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsForStringList(String sessID, TableSchema table, List<String> list, String columnname, boolean allowInexactMatch) throws SQLException, RemoteException, SessionExpiredException;
    public void updateFields(String sessID, int projID, CustomField[] fields) throws SQLException, RemoteException, SessionExpiredException;
    public List<Object> getValuesFromField(String sessID, int projectId, String columnNameA, String columnNameB, List<Object> values) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsFromField(String sessID, int projID, String columnNameA, List<Object> values) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getValuesFromDNAIDs(String sessID, int projectId, String columnNameB, List<String> ids) throws SQLException, RemoteException, SessionExpiredException;
    public List<Object[]> getFamily(String sessID, int projectId, String family_id) throws SQLException, RemoteException, SessionExpiredException;
    public List<Object[]> getFamilyOfPatient(String sessID, int projectId, int pid) throws SQLException, RemoteException, SessionExpiredException;
    public String getFamilyIDOfPatient(String sessID, int projectId, int pid) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getFamilyIDs(String sessID, int projectId) throws SQLException, RemoteException, SessionExpiredException;
    public Map<String,String> getDNAIDsForFamily(String sessID, int projectId, String familyId) throws SQLException, RemoteException, SessionExpiredException;
    public void clearPatients(String sessID, int projectId) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> parseDNAIDs(String s) throws RemoteException, SessionExpiredException;
    public List<String> getDNAIDsForHPOID(String sessID, int pid, String id) throws SQLException, RemoteException, SessionExpiredException;
    public boolean hasOptionalField(String sessID, int pid, String fieldName) throws SQLException, RemoteException, SessionExpiredException;
    public String getReadAlignmentPathForDNAID(String sessID, int pid, String dnaID) throws SQLException, RemoteException, SessionExpiredException;
}
