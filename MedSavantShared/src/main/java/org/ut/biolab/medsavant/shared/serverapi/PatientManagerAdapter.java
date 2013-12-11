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
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.ClientPermission;
import org.ut.biolab.medsavant.shared.util.Modifier;
import static org.ut.biolab.medsavant.shared.util.ModificationType.*;
import static org.ut.biolab.medsavant.shared.util.ClientType.*;
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
    
    @Modifier(type=PATIENT)
    public void createPatientTable(String sessID, int projectid, CustomField[] fields) throws SQLException, ParserConfigurationException, SAXException, IOException, RemoteException, SessionExpiredException;
    
    @Modifier(type=PATIENT)
    public void removePatient(String sessID, int projectId, int[] patientIds) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=PATIENT)
    public void addPatient(String sessID, int projectId, List<CustomField> cols, List<String> values) throws SQLException, RemoteException, SessionExpiredException;   
    public Map<Object, List<String>> getDNAIDsForValues(String sessID, int projectId, String columnName) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsWithValuesInRange(String sessID, int projectId, String columnName, Range r) throws SQLException, RemoteException, SessionExpiredException;
    
    @ClientPermission(deny=WEB)
    public List<String> getDNAIDsForStringList(String sessID, TableSchema table, List<String> list, String columnname, boolean allowInexactMatch) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=PATIENT)
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
    
    public void test(CustomField[] f) throws RemoteException;
}
