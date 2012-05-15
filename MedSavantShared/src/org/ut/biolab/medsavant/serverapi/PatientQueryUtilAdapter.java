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
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author mfiume
 */
public interface PatientQueryUtilAdapter extends Remote {

    public List<Object[]> getBasicPatientInfo(String sessID, int projectId, int limit) throws SQLException, RemoteException;
    public List<Object[]> getPatients(String sessID, int projectId) throws SQLException, RemoteException;
    public Object[] getPatientRecord(String sessID, int projectId, int patientId) throws SQLException, RemoteException;
    public List<String> getPatientFieldAliases(String sessID, int projectId) throws SQLException, RemoteException;
    public List<CustomField> getPatientFields(String sessID, int projectId) throws SQLException, RemoteException;
    public List<CustomField> getCustomPatientFields(String sessID, int projectId) throws SQLException, RemoteException;
    public String getPatientTablename(String sessID, int projectId) throws SQLException, RemoteException;
    public void createPatientTable(String sessID, int projectid, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException, RemoteException;
    public void removePatient(String sessID, int projectId, int[] patientIds) throws SQLException, RemoteException;
    public void addPatient(String sessID, int projectId, List<CustomField> cols, List<String> values) throws SQLException, RemoteException;
    public Map<Object, List<String>> getDNAIdsForValues(String sessID, int projectId, String columnName) throws SQLException, RemoteException;
    public List<String> getDNAIdsWithValuesInRange(String sessID, int projectId, String columnName, Range r) throws NonFatalDatabaseException, SQLException, RemoteException;
    public List<String> getDNAIdsForStringList(String sessID, TableSchema table, List<String> list, String columnname) throws NonFatalDatabaseException, SQLException, RemoteException;
    public void updateFields(String sessID, int projectId, List<CustomField> fields) throws SQLException, RemoteException;
    public List<Object> getValuesFromField(String sessID, int projectId, String columnNameA, String columnNameB, List<Object> values) throws SQLException, RemoteException;
    public List<String> getDNAIdsFromField(String sessID, int projectId, String columnNameA, List<Object> values) throws SQLException, RemoteException;
    public List<String> getValuesFromDNAIds(String sessID, int projectId, String columnNameB, List<String> ids) throws SQLException, RemoteException;
    public List<Object[]> getFamily(String sessID, int projectId, String family_id) throws SQLException, RemoteException;
    public List<Object[]> getFamilyOfPatient(String sessID, int projectId, int pid) throws SQLException, RemoteException;
    public String getFamilyIdOfPatient(String sessID, int projectId, int pid) throws SQLException, RemoteException;
    public List<String> getFamilyIds(String sessID, int projectId) throws SQLException, RemoteException;
    public Map<String,String> getDNAIdsForFamily(String sessID, int projectId, String familyId) throws SQLException, RemoteException;
    public void clearPatients(String sessID, int projectId) throws SQLException, RemoteException;
    public List<String> parseDnaIds(String s) throws RemoteException;
    public List<String> getDNAIdsForHPOID(String sessID, int pid, String id) throws SQLException, RemoteException;
    public boolean hasOptionalField(String sessID, int pid, String fieldName) throws SQLException, RemoteException;
}
