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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.CohortTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.CohortMembershipTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.variants.VariantManager;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.model.SimplePatient;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author Andrew
 */
public class CohortQueryUtil extends MedSavantServerUnicastRemoteObject implements CohortQueryUtilAdapter {

    private static CohortQueryUtil instance;

    public static synchronized CohortQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new CohortQueryUtil();
        }
        return instance;
    }

    public CohortQueryUtil() throws RemoteException {super();}


    /*public List<Integer> getIndividualsInCohort(int cohortId) throws SQLException {

        TableSchema table = MedSavantDatabase.CohortmembershipTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        List<Integer> result = new ArrayList<Integer>();
        while(rs.next()) {
            result.add(rs.getInt(1));
        }
        return result;
    }*/

    @Override
    public List<SimplePatient> getIndividualsInCohort(String sid, int projectId, int cohortId) throws SQLException, RemoteException {

        String tablename = PatientQueryUtil.getInstance().getPatientTablename(sid,projectId);
        TableSchema cohortTable = MedSavantDatabase.CohortmembershipTableSchema;
        TableSchema patientTable = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(cohortTable.getTable());
        query.addFromTable(patientTable.getTable());
        query.addColumns(
                cohortTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID),
                patientTable.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                patientTable.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS));
        query.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        query.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), patientTable.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID)));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<SimplePatient> result = new ArrayList<SimplePatient>();
        while(rs.next()) {
            result.add(new SimplePatient(rs.getInt(1), rs.getString(2), PatientQueryUtil.getInstance().parseDnaIds(rs.getString(3))));
        }
        return result;
    }

    /*public List<String> getDNAIdsInCohort(int cohortId) throws SQLException {

        Connection c = ConnectionController.connectPooled();
        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        TableSchema cohortTable = MedSavantDatabase.CohortTableSchema;
        TableSchema cohortMembershipTable = MedSavantDatabase.CohortmembershipTableSchema;

        //get patient tablename
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(patientMapTable.getTable());
        query1.addFromTable(cohortTable.getTable());
        query1.addColumns(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        query1.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        query1.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID)));

        ResultSet rs = ConnectionController.executeQuery(sid, query1.toString());
        rs.next();
        String patientTablename = rs.getString(1);

        //get dna id lists
        TableSchema patientTable = CustomTables.getCustomTableSchema(patientTablename);
        SelectQuery query2 = new SelectQuery();
        query2.addFromTable(cohortMembershipTable.getTable());
        query2.addFromTable(patientTable.getTable());
        query2.addColumns(patientTable.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS));
        query2.addCondition(BinaryConditionMS.equalTo(cohortMembershipTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        query2.addCondition(BinaryConditionMS.equalTo(cohortMembershipTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID));

        rs = ConnectionController.executeQuery(sid, query2.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()) {
            String[] dnaIds = rs.getString(1).split(",");
            for (String id : dnaIds) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }*/

    @Override
    public List<String> getDNAIdsInCohort(String sid, int cohortId) throws SQLException, RemoteException {
        List<String> list = getIndividualFieldFromCohort(sid,cohortId, DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        List<String> result = new ArrayList<String>();
        for (String s : list) {
            if (s == null) continue;
            String[] dnaIds = s.split(",");
            for (String id : dnaIds) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getIndividualFieldFromCohort(String sid, int cohortId, String columnname) throws SQLException, RemoteException {

        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        TableSchema cohortTable = MedSavantDatabase.CohortTableSchema;
        TableSchema cohortMembershipTable = MedSavantDatabase.CohortmembershipTableSchema;

        //get patient tablename
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(patientMapTable.getTable());
        query1.addFromTable(cohortTable.getTable());
        query1.addColumns(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        query1.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        query1.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID)));

        ResultSet rs = ConnectionController.executeQuery(sid, query1.toString());
        rs.next();
        String patientTablename = rs.getString(1);

        //get field lists
        TableSchema patientTable = CustomTables.getInstance().getCustomTableSchema(sid,patientTablename);
        SelectQuery query2 = new SelectQuery();
        query2.addFromTable(cohortMembershipTable.getTable());
        query2.addFromTable(patientTable.getTable());
        query2.addColumns(patientTable.getDBColumn(columnname));
        query2.addCondition(BinaryConditionMS.equalTo(cohortMembershipTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        query2.addCondition(BinaryConditionMS.equalTo(cohortMembershipTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), patientTable.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID)));

        rs = ConnectionController.executeQuery(sid, query2.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()) {
            /*String[] dnaIds = rs.getString(1).split(",");
            for (String id : dnaIds) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }*/
            result.add(rs.getString(1));
        }
        return result;
    }

    @Override
    public void addPatientsToCohort(String sid, int[] patientIds, int cohortId) throws SQLException {

        TableSchema table = MedSavantDatabase.CohortmembershipTableSchema;

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);

        for (int id : patientIds) {
            try {
                InsertQuery query = new InsertQuery(table.getTable());
                query.addColumn(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId);
                query.addColumn(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), id);
                c.createStatement().executeUpdate(query.toString());
            } catch (MySQLIntegrityConstraintViolationException e) {
                //duplicate entry, ignore
            }
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    @Override
    public void removePatientsFromCohort(String sid, int[] patientIds, int cohortId) throws SQLException {

        TableSchema table = MedSavantDatabase.CohortmembershipTableSchema;

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);

        for (int id : patientIds) {
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), id));
            c.createStatement().executeUpdate(query.toString());
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    @Override
    public List<Cohort> getCohorts(String sid, int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.CohortTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Cohort> result = new ArrayList<Cohort>();
        while(rs.next()) {
            result.add(new Cohort(rs.getInt(CohortTableSchema.COLUMNNAME_OF_COHORT_ID), rs.getString(CohortTableSchema.COLUMNNAME_OF_NAME)));
        }
        return result;
    }

    @Override
    public void addCohort(String sid, int projectId, String name) throws SQLException {

        TableSchema table = MedSavantDatabase.CohortTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
        query.addColumn(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_NAME), name);

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    @Override
    public void removeCohort(String sid, int cohortId) throws SQLException {

        TableSchema cohortMembershipTable = MedSavantDatabase.CohortmembershipTableSchema;
        TableSchema cohortTable = MedSavantDatabase.CohortTableSchema;
        Connection c = ConnectionController.connectPooled(sid);


        //remove all entries from membership
        DeleteQuery query1 = new DeleteQuery(cohortMembershipTable.getTable());
        query1.addCondition(BinaryConditionMS.equalTo(cohortMembershipTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        c.createStatement().execute(query1.toString());

        //remove from cohorts
        DeleteQuery query2 = new DeleteQuery(cohortTable.getTable());
        query2.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        c.createStatement().execute(query2.toString());

        c.close();
    }

    @Override
    public void removeCohorts(String sid, Cohort[] cohorts) throws SQLException {
        for (Cohort c : cohorts) {
            removeCohort(sid,c.getId());
        }
    }

    @Override
    public List<Integer> getCohortIds(String sid, int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.CohortTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_COHORT_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Integer> result = new ArrayList<Integer>();
        while(rs.next()) {
            result.add(rs.getInt(1));
        }
        return result;
    }

    @Override
    public void removePatientReferences(String sid, int projectId, int patientId) throws SQLException {

        List<Integer> cohortIds = getCohortIds(sid, projectId);

        TableSchema table = MedSavantDatabase.CohortmembershipTableSchema;

        for (Integer cohortId : cohortIds) {
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), patientId));
            ConnectionController.executeUpdate(sid, query.toString());
        }
    }

    @Override
    public int getNumVariantsInCohort(String sid, int projectId, int referenceId, int cohortId, Condition[][] conditions) throws SQLException, InterruptedException, RemoteException {
        List<String> dnaIds = getDNAIdsInCohort(sid,cohortId);
        return VariantManager.getInstance().getNumVariantsForDnaIds(sid,projectId, referenceId, conditions, dnaIds);
    }
}
