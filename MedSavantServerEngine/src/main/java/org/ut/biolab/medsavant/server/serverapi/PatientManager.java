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

package org.ut.biolab.medsavant.server.serverapi;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.db.MedSavantDatabaseExtras;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.*;
import org.ut.biolab.medsavant.shared.persistence.CustomFieldManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.persistence.solr.SolrCustomFieldManager;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.serverapi.PatientManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.Entity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;


/**
 *
 * @author Andrew
 */
public class PatientManager extends MedSavantServerUnicastRemoteObject implements PatientManagerAdapter, BasicPatientColumns {

    private static PatientManager instance;
    private static QueryManager queryManager;
    private static EntityManager entityManager;
    private static CustomFieldManager customFieldManager;
    private static final String DNA_ID_SEPARATOR = ",";

    private static final Log LOG = LogFactory.getLog(PatientManager.class);

    private PatientManager() throws RemoteException, SessionExpiredException {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
        customFieldManager = new SolrCustomFieldManager();
    }

    public static synchronized PatientManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new PatientManager();
        }
        return instance;
    }

    @Override
    public List<Object[]> getBasicPatientInfo(String sid, int projectId, int limit) throws SQLException, RemoteException, SessionExpiredException {

        /*String tablename = getPatientTableName(sid,projectId);*/
        Query q = queryManager.createQuery("Select p from Patient p where p.project_id= :projectId");
        q.setParameter("projectId", projectId);
        List<ResultRow> resultRows = q.executeForRows();
        List<Object[]> result = new ArrayList<Object[]>();
        for (ResultRow resultRow : resultRows) {
            Object[] o = new Object[] {
                    (Integer) resultRow.getObject("patient_id"),
                    resultRow.getObject("family_id"),
                    resultRow.getObject("hospital_id"),
                    resultRow.getObject("mother_id"),
                    resultRow.getObject("father_id"),
                    Integer.parseInt((String) resultRow.getObject("gender")),
                    Integer.parseInt((String) resultRow.getObject("affected")),
                    resultRow.getObject("dna_ids"),
                    resultRow.getObject("bam_url"),
                    resultRow.getObject("phenotypes")
            };
            result.add(o);
        }

        return result;
    }

    @Override
    public List<Object[]> getPatients(String sid, int projectId) throws SQLException, RemoteException, SessionExpiredException {
        Query q = queryManager.createQuery("Select p from Patient p where p.project_id= :projectId");
        q.setParameter("projectId", projectId);
        List<ResultRow> resultRows = q.executeForRows();
        List<Object[]> result = new ArrayList<Object[]>();
        for (ResultRow resultRow : resultRows) {
            Object[] o = new Object[] {
                    resultRow.getObject("project_id"),
                    resultRow.getObject("family_id"),
                    resultRow.getObject("hospital_id"),
                    resultRow.getObject("idbiomom"),
                    resultRow.getObject("idbiodad"),
                    resultRow.getObject("gender"),
                    resultRow.getObject("affected"),
                    resultRow.getObject("dna_ids"),
                    resultRow.getObject("bam_url"),
                    resultRow.getObject("phenotypes")
            };
            result.add(o);
        }

        return result;
    }

    @Override
    public Object[] getPatientRecord(String sid, int projectId, int patientId) throws SQLException, RemoteException, SessionExpiredException {
        Query q = queryManager.createQuery("Select p from Patient p where p.project_id= :projectId and p.patient_id = :patientId");
        q.setParameter("projectId", projectId);
        q.setParameter("patientId", patientId);
        List<ResultRow> resultRows = q.executeForRows();
        Object[] o = null;
        if (resultRows.size() > 0) {
            ResultRow resultRow = resultRows.get(0);
            o = new Object[] {
                    resultRow.getObject("project_id"),
                    resultRow.getObject("family_id"),
                    resultRow.getObject("hospital_id"),
                    resultRow.getObject("idbiomom"),
                    resultRow.getObject("idbiodad"),
                    resultRow.getObject("gender"),
                    resultRow.getObject("affected"),
                    resultRow.getObject("dna_ids"),
                    resultRow.getObject("bam_url"),
                    resultRow.getObject("phenotypes")
            };
        }
        return o;
    }

    @Override
    public List<String> getPatientFieldAliases(String sid, int projectId) throws SQLException, SessionExpiredException {

        //ToDo maybe add ordering to them?
        CustomField[] patientFields = getPatientFields(sid, projectId);
        List<String> result = new ArrayList<String>();

        for (CustomField af: patientFields) {
            result.add(af.getAlias());
        }
        return result;
    }

    @Override
    public CustomField[] getPatientFields(String sessID, int projID) throws SQLException, SessionExpiredException {
        CustomField[] defaultFields = REQUIRED_PATIENT_FIELDS;
        CustomField[] customFields = getCustomPatientFields(sessID, projID);
        CustomField[] result = new CustomField[defaultFields.length + customFields.length];
        System.arraycopy(defaultFields, 0, result, 0, defaultFields.length);
        System.arraycopy(customFields, 0, result, defaultFields.length, customFields.length);
        return result;
    }

    @Override
    public CustomField[] getCustomPatientFields(String sessID, int projID) throws SQLException, SessionExpiredException {
        String entityName = Entity.PATIENT;
        List<CustomField> basicFields = Arrays.asList(BasicPatientColumns.REQUIRED_PATIENT_FIELDS);

        Query query = queryManager.createQuery("Select c from CustomColumn c where c.entity_name = :entityName");
        query.setParameter("entityName", entityName);
        List<CustomField> customFields = query.execute();
        customFields = ListUtils.union(customFields, basicFields);
        return customFields.toArray(new CustomField[0]);
    }

    @Override
    public String getPatientTableName(String sid, int projectId) throws SQLException, SessionExpiredException {
        return "Patient";
    }


    @Override
    public void createPatientTable(String sessID, int projID, CustomField[] fields) throws SQLException, SessionExpiredException, RemoteException {

        List<CustomField> customColumnList = new ArrayList<CustomField>();
        ProjectDetails project =  ProjectManager.getInstance().getProjectDetails(sessID, projID)[0];
        int i = 0;
        for (CustomField customField : fields) {
            customColumnList.add(new CustomColumn(customField, project, CustomColumnType.PATIENT,i++));
        }

        try {
            entityManager.persistAll(customColumnList);
            customFieldManager.addCustomFields(customColumnList);
        } catch (InitializationException e) {
            LOG.error("Error adding custom fields for patient");
        } catch (IOException e) {
            LOG.error("Error adding custom fields for patient");
        } catch (URISyntaxException e) {
            LOG.error("Error adding custom fields for patient");
        }
    }

    @Override
    public void removePatient(String sid, int projectId, int[] patientIds) throws SQLException, RemoteException, SessionExpiredException {

        String patientIdCol = StringUtils.join(ArrayUtils.toObject(patientIds), ",");
        String statement = "Delete from Patient p where p.project_id=:projectId AND p.patient_id in (%s)";
        Query query = queryManager.createQuery(String.format(statement, patientIdCol));
        query.setParameter("projectId",projectId);
        query.executeDelete();
    }

    @Override
    public void addPatient(String sid, int projectId, List<CustomField> cols, List<String> values) throws SQLException, RemoteException, SessionExpiredException {

        Patient patient = mapToPatient(cols, values, projectId);
        patient.setPatientId(generateId());
        try {
            entityManager.persist(patient);
        } catch (InitializationException e) {
            LOG.error("Error persisting patient");
        }
    }

    @Override
    public Map<Object, List<String>> getDNAIDsForValues(String sessID, int projID, String columnName) throws SQLException, RemoteException, SessionExpiredException {

        String statement = String.format("Select p.dna_ids,p.%s from Patient p where p.project_id = :projectId", columnName);
        Query query = queryManager.createQuery(statement);
        query.setParameter("projectId", projID);
        List<ResultRow> resultRowList = query.executeForRows();

        Map<Object, List<String>> map = new HashMap<Object, List<String>>();
        for (ResultRow row : resultRowList) {
            Object columnValue = row.getObject(columnName);
            if (columnValue == null) columnValue = "";
            if (map.get(columnValue) == null) map.put(columnValue, new ArrayList<String>());
            List<String> dnaIds = (List<String>) row.getObject("dna_ids");
            for (String dnaId : dnaIds) {
                if (!map.get(columnValue).contains(dnaId)) {
                    map.get(columnValue).add(dnaId);
                }
            }
        }
        return map;
    }

    @Override
    public List<String> getDNAIDsWithValuesInRange(String sessID, int projID, String columnName, Range r) throws SQLException, RemoteException, SessionExpiredException {

        String statement = String.format("Select p.dna_ids, p.%s from Patient p where p.project_id = :projectId and" +
                "p.%s between :rangeMin and :rangeMax", columnName, columnName);
        Query query = queryManager.createQuery(statement);
        query.setParameter("projectId", projID);
        query.setParameter("rangeMin",r.getMin());
        query.setParameter("rangeMax",r.getMax());

        List<String> dnaIds = new ArrayList<String>();
        List<ResultRow> resultRowList = query.executeForRows();
        for (ResultRow row : resultRowList) {
            List<String> currentDnaIds = (List<String>) row.getObject("dna_ids");
            dnaIds = ListUtils.union(dnaIds, currentDnaIds);
        }

        return dnaIds;
    }

    @Override
    public List<String> getDNAIDsForStringList(String sessID, TableSchema table, List<String> list, String columnName, boolean allowInexactMatch) throws SQLException, SessionExpiredException {

        StringBuffer conditions = new StringBuffer();
        for (int i = 0 ; i < list.size(); i++) {
            if (i > 0) {
                conditions.append(" or ");
            }
            String val = list.get(i);
            if (val.length() == 0) {
                conditions.append("p." + columnName + " IS NULL");
            } else {
                if (allowInexactMatch) {
                    conditions.append("p." + columnName + " LIKE " + val);
                } else {
                    conditions.append("p." + columnName + " = " + val);
                }
            }
        }

        String statement = "Select p.%s, p.dna_ids from Patient p";
        statement = conditions.toString().equals("") ? statement : statement + " where " + conditions ;
        Query query = queryManager.createQuery(statement);


        List<String> result = new ArrayList<String>();
        List<ResultRow> resultRowList = query.executeForRows();
        for (ResultRow row : resultRowList) {
            List<String> current = (List<String>) row.getObject(columnName);
            if (current == null) continue;
            String[] dnaIds = current.toArray(new String[0]);
            for (String id : dnaIds) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    @Override
    public void updateFields(String sessID, int projID, CustomField[] customFields) throws SQLException, RemoteException, SessionExpiredException {
        //Todo add calls to Schema API?
        //get old fields
        List<CustomField> oldFields = Arrays.asList(PatientManager.getInstance().getCustomPatientFields(sessID, projID));
        List<CustomField> newFields = new ArrayList<CustomField>();

        //delete old fields
        Query query = queryManager.createQuery("Delete from CustomColumn c where c.project_id = :projectId");
        query.setParameter("projectId", projID);
        query.executeDelete();

        List<CustomColumn> customColumnList = new ArrayList<CustomColumn>();
        ProjectDetails project =  ProjectManager.getInstance().getProjectDetails(sessID, projID)[0];
        int i = 0;
        for (CustomField customField : customFields) {
            if (!oldFields.contains(customField)) {
                newFields.add(customField);
            }
            customColumnList.add(new CustomColumn(customField, project, CustomColumnType.PATIENT,i++));
        }

        //persist current fields
        try {
            entityManager.persistAll(customColumnList);
            customFieldManager.addCustomFields(newFields);
        } catch (InitializationException e) {
            LOG.error("Error updating patient fields");
        } catch (IOException e) {
            LOG.error("Error adding custom fields to the patient table");
        } catch (URISyntaxException e) {
            LOG.error("Error adding custom fields to the patient table");
        }
    }

    /*
     * Given a list of values for field A, get the corresponding values from field B
     */
    @Override
    public List<Object> getValuesFromField(String sid, int projectId, String columnNameA, String columnNameB, List<Object> values) throws SQLException, RemoteException, SessionExpiredException {

        //build conditions
        StringBuffer conditions = new StringBuffer();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                conditions.append(" or ");
            }
            conditions.append("p." + columnNameA + " = " + values.get(i));
        }

        //build query
        String statement = String.format("Select p.%s from Patient p where p.project_id = :projectId and " + conditions.toString(), columnNameB);
        Query query = queryManager.createQuery(statement);
        query.setParameter("projectId", projectId);

        //result
        List<Object> result = new ArrayList<Object>();
        List<ResultRow> resultRowList = query.executeForRows();
        for (ResultRow row : resultRowList) {
            result.add(row.getObject(columnNameB));
        }

        return result;
    }

    @Override
    public List<String> getDNAIDsFromField(String sessID, int projID, String columnNameA, List<Object> values) throws SQLException, RemoteException, SessionExpiredException {

        List<Object> l1 = getValuesFromField(sessID,projID, columnNameA, DNA_IDS.getColumnName(), values);
        List<String> result = new ArrayList<String>();
        for (Object o : l1) {
            String[] dnaIds = ((String) o).split(",");
            for (String id : dnaIds) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getValuesFromDNAIDs(String sessID, int projID, String columnNameB, List<String> ids) throws SQLException, RemoteException, SessionExpiredException {
        //build conditions
        StringBuffer conditions = new StringBuffer();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                conditions.append(" or ");
            }
            conditions.append("p.dna_ids = " + ids.get(i));
        }

        //build query
        String statement = String.format("Select p.%s from Patient p where p.project_id = :projectId and " + conditions.toString(), columnNameB);
        Query query = queryManager.createQuery(statement);
        query.setParameter("projectId", projID);

        //process result
        List<String> result = new ArrayList<String>();
        List<ResultRow> resultRowList = query.executeForRows();
        for (ResultRow row : resultRowList) {
            result.add(String.valueOf(row.getObject(columnNameB)));
        }

        return result;
    }

    @Override
    public List<Object[]> getFamily(String sessID, int projID, String famID) throws SQLException, RemoteException, SessionExpiredException {
        Query q = queryManager.createQuery("Select p.hospital_id,p.idbiomom, pidbiodad, p.patient_id, p.gender, p.affected" +
                " from Patient p " +
                "where p.project_id= :projectId and p.family_id = :familyId");
        q.setParameter("projectId", projID);
        q.setParameter("familyId", famID);
        List<ResultRow> resultRows = q.executeForRows();
        List<Object[]> result = new ArrayList<Object[]>();
        for (ResultRow  resultRow : resultRows) {
            Object[] o  = new Object[] {
                    resultRow.getObject("patient_id"),
                    resultRow.getObject("family_id"),
                    resultRow.getObject("hospital_id"),
                    resultRow.getObject("idbiomom"),
                    resultRow.getObject("idbiodad"),
                    resultRow.getObject("gender"),
                    resultRow.getObject("affected"),
                    resultRow.getObject("dna_ids"),
                    resultRow.getObject("bam_url"),
                    resultRow.getObject("phenotypes")
            };
            result.add(o);
        }
        return result;
    }


    @Override
    public List<Object[]> getFamilyOfPatient(String sessID, int projID, int patID) throws SQLException, RemoteException, SessionExpiredException {

        String famID = getFamilyIDOfPatient(sessID, projID, patID);
        if (famID == null) {
            return new ArrayList<Object[]>();
        }

        return getFamily(sessID,projID, famID);
    }

    @Override
    public String getFamilyIDOfPatient(String sessID, int projID, int patID) throws SQLException, RemoteException, SessionExpiredException {
        Query query = queryManager.createQuery("Select p from Patient p where p.project_id = :projectId and p.patient_id = :patientId");
        query.setParameter("project_id", projID);
        query.setParameter("patient_id", patID);
        Patient p = query.getFirst();

        return p.getFamilyId();
    }

    @Override
    public List<String> getFamilyIDs(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p.family_id from Patient p where p.project_id = :projectId");
        query.setParameter("projectId", projID);

        List<String> results = new ArrayList<String>();
        List<ResultRow> resultRowList = query.executeForRows();
        for (ResultRow row : resultRowList) {
            results.add(String.valueOf(row.getObject("family_id")));
        }
        return results;
    }


    //SELECT `dna_ids` FROM `z_patient_proj1` WHERE `family_id` = 'AB0001' AND `dna_ids` IS NOT null;
    @Override
    public Map<String,String> getDNAIDsForFamily(String sessID, int projID, String famID) throws SQLException, RemoteException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p.patient_id,p.hospital_id, p.dna_ids from Patient p where p.project_id = :projectId and p.family_id = :familyId");
        query.setParameter("projectId", projID);
        query.setParameter("familyId", famID);

        Map<String,String> patientIDToDNAIDMap = new HashMap<String,String>();
        List<Patient> patients = query.execute();

        for (Patient p : patients) {
            String patientId = p.getHospitalId();
            List<String> dnaIds = p.getDnaIds();

            if (dnaIds != null && !dnaIds.isEmpty()) {
                patientIDToDNAIDMap.put(patientId, StringUtils.join(dnaIds.iterator(),DNA_ID_SEPARATOR));
            }
        }

        return patientIDToDNAIDMap;
    }

    @Override
    public void clearPatients(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException{
        String statement = "Delete from Patient p where p.project_id=:projectId";
        Query query = queryManager.createQuery(statement);
        query.setParameter("projectId",projID);
        query.executeDelete();
    }

    @Override
    public List<String> parseDNAIDs(String s) {
        List<String> result = new ArrayList<String>();
        if (s == null) return result;
        String[] dnaIDs = s.split(",");
        for (String id : dnaIDs) {
            if (!result.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    @Override
    public List<String> getDNAIDsForHPOID(String sessID, int projID, String id) throws SQLException, RemoteException, SessionExpiredException {
        //TODO: make a prepared statement
        String statement = String.format("Select p.dna_ids from Patient p where p.%s = :value", MedSavantDatabaseExtras.OPTIONAL_PATIENT_FIELD_HPO);
        Query query = queryManager.createQuery(statement);
        query.setParameter("value", id);

        List<String> results = new ArrayList<String>();
        List<Patient> patients = query.execute();
        for (Patient patient : patients) {
            results.add(StringUtils.join(patient.getDnaIds().iterator(), DNA_ID_SEPARATOR));  //I'm pretty sure this needs to be joined
        }
        return results;
    }

    @Override
    public boolean hasOptionalField(String sessID, int pid, String fieldName) throws SQLException, SessionExpiredException {
        //Todo
        String tableName = getPatientTableName(sessID, pid);
        return DBUtils.fieldExists(sessID, tableName, MedSavantDatabaseExtras.OPTIONAL_PATIENT_FIELD_HPO);
    }

    @Override
    public String getReadAlignmentPathForDNAID(String sessID, int projID, String dnaID) throws SQLException, RemoteException, SessionExpiredException {
        Query query = queryManager.createQuery("Select p.bam_url from Patient p where p.project_id = :projectId and :dnaId member of p.dna_ids");
        query.setParameter("dnaId", dnaID);
        query.setParameter("projectId", projID);

        List<String> results = new ArrayList<String>();
        Patient patient = query.getFirst();
        return patient.getBamUrl().toString();
    }

    private int generateId() {
        Query query = queryManager.createQuery("Select p.patient_id,max(p.patient_id) from Patient p");
        List<ResultRow> results = query.executeForRows();

        int newId;
        if (results.size() == 0 ) {
            newId = 1;
        } else {
            newId = ((Double)(results.get(0).getObject("max"))).intValue() + 1;
        }

        return newId;
    }

    private Patient mapToPatient(List<CustomField> columns, List<String> columnValues, int projectId) {

        Patient result = new Patient();

        int index = 0;
        if (columns.size() == columnValues.size()) {
            for (CustomField customField : columns) {
                String columnName = customField.getColumnName();
                String columnValue = columnValues.get(index);

                if (columnValue != null) {
                    if ("family_id".equals(columnName)) {
                        result.setFamilyId(columnValue);
                    } else if ("hospital_id".equals(columnName)) {
                        result.setHospitalId(columnValue);
                    } else if ("idbiomom".equals(columnName)) {
                        result.setMotherId(columnValue);
                    } else if ("idbiodad".equals(columnName)) {
                        result.setFatherId(columnValue);
                    } else if ("gender".equals(columnName)) {
                        result.setGender(Integer.parseInt(columnValue));
                    } else if ("affected".equals(columnName)) {
                        result.setAffected(Integer.parseInt(columnValue));
                    } else if ("dna_ids".equals(columnName)) {
                        result.setDnaIds(Arrays.asList(columnValue.split("\n")));
                    } else if ("bam_url".equals(columnName)) {
                        result.setBamUrl(Arrays.asList(columnValue.split("\n")));
                    } else if ("phenotypes".equals(columnName)) {
                        result.setDnaIds(Arrays.asList(columnValue.split("\n")));
                    }
                }

                index++;
            }
        }
        result.setProjectId(projectId);
        return result;
    }
}
