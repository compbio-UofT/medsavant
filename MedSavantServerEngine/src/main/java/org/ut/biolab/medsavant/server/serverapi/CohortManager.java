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
package org.ut.biolab.medsavant.server.serverapi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.CohortTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.CohortMembershipTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.variants.VariantManager;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.CohortManagerAdapter;


/**
 *
 * @author Andrew
 */
public class CohortManager extends MedSavantServerUnicastRemoteObject implements CohortManagerAdapter, BasicPatientColumns {

    private static CohortManager instance;

    private CohortManager() throws RemoteException, SessionExpiredException {
    }

    public static synchronized CohortManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new CohortManager();
        }
        return instance;
    }

    @Override
    public List<SimplePatient> getIndividualsInCohort(String sid, int projectId, int cohortId) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = PatientManager.getInstance().getPatientTableName(sid,projectId);
        TableSchema cohortTable = MedSavantDatabase.CohortmembershipTableSchema;
        TableSchema patientTable = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(cohortTable.getTable());
        query.addFromTable(patientTable.getTable());
        query.addColumns(
                cohortTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID),
                patientTable.getDBColumn(HOSPITAL_ID),
                patientTable.getDBColumn(DNA_IDS));
        query.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        query.addCondition(BinaryConditionMS.equalTo(cohortTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), patientTable.getDBColumn(PATIENT_ID)));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<SimplePatient> result = new ArrayList<SimplePatient>();
        while (rs.next()) {
            result.add(new SimplePatient(rs.getInt(1), rs.getString(2), PatientManager.getInstance().parseDNAIDs(rs.getString(3))));
        }
        return result;
    }

    @Override
    public List<String> getDNAIDsForCohort(String sessID, int cohortId) throws SQLException, RemoteException, SessionExpiredException {
        List<String> list = getIndividualFieldFromCohort(sessID, cohortId, DNA_IDS.getColumnName());
        List<String> result = new ArrayList<String>();
        for (String s : list) {
            if (s == null) continue;
            String[] dnaIDs = s.split(",");
            for (String id : dnaIDs) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getDNAIDsForCohorts(String sessID, int projID, Collection<String> cohNames) throws SQLException, RemoteException, SessionExpiredException {

        String selQuery = String.format("SELECT %s FROM %s WHERE %s = ANY (SELECT %s FROM %s JOIN %s USING (%s) WHERE %s IN ('%s'))",
                DNA_IDS.getColumnName(),
                PatientManager.getInstance().getPatientTableName(sessID, projID),
                PATIENT_ID.getColumnName(),
                CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID,
                CohortMembershipTableSchema.TABLE_NAME,
                CohortTableSchema.TABLE_NAME,
                CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID,
                CohortTableSchema.COLUMNNAME_OF_NAME,
                StringUtils.join(cohNames, "','"));

        List<String> result = new ArrayList<String>();
        ResultSet rs = ConnectionController.executeQuery(sessID, selQuery);
        while (rs.next()) {
            String s = rs.getString(1);
            if (s != null) {
                String[] dnaIDs = s.split(",");
                for (String id : dnaIDs) {
                    if (!result.contains(id)) {
                        result.add(id);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getIndividualFieldFromCohort(String sessID, int cohortId, String columnName) throws SQLException, RemoteException, SessionExpiredException {

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

        ResultSet rs = ConnectionController.executeQuery(sessID, query1.toString());
        rs.next();
        String patientTablename = rs.getString(1);

        //get field lists
        TableSchema patientTable = CustomTables.getInstance().getCustomTableSchema(sessID, patientTablename);
        SelectQuery query2 = new SelectQuery();
        query2.addFromTable(cohortMembershipTable.getTable());
        query2.addFromTable(patientTable.getTable());
        query2.addColumns(patientTable.getDBColumn(columnName));
        query2.addCondition(BinaryConditionMS.equalTo(cohortMembershipTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortId));
        query2.addCondition(BinaryConditionMS.equalTo(cohortMembershipTable.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), patientTable.getDBColumn(PATIENT_ID)));

        rs = ConnectionController.executeQuery(sessID, query2.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }

    @Override
    public void addPatientsToCohort(String sessID, int[] patientIDs, int cohortID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.CohortmembershipTableSchema;

        Connection c = ConnectionController.connectPooled(sessID);
        c.setAutoCommit(false);

        for (int id : patientIDs) {
            try {
                InsertQuery query = new InsertQuery(table.getTable());
                query.addColumn(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohortID);
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
    public void removePatientsFromCohort(String sessID, int[] patIDs, int cohID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.CohortmembershipTableSchema;

        Connection c = ConnectionController.connectPooled(sessID);
        c.setAutoCommit(false);

        for (int id : patIDs) {
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), cohID));
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), id));
            c.createStatement().executeUpdate(query.toString());
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    @Override
    public Cohort[] getCohorts(String sessID, int projID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.CohortTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Cohort> result = new ArrayList<Cohort>();
        while (rs.next()) {
            result.add(new Cohort(rs.getInt(CohortTableSchema.COLUMNNAME_OF_COHORT_ID), rs.getString(CohortTableSchema.COLUMNNAME_OF_NAME)));
        }
        return result.toArray(new Cohort[0]);
    }

    @Override
    public void addCohort(String sid, int projectId, String name) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.CohortTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
        query.addColumn(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_NAME), name);

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    @Override
    public void removeCohort(String sid, int cohortId) throws SQLException, SessionExpiredException {

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
    public void removeCohorts(String sid, Cohort[] cohorts) throws SQLException, SessionExpiredException {
        for (Cohort c : cohorts) {
            removeCohort(sid,c.getId());
        }
    }

    @Override
    public int[] getCohortIDs(String sid, int projectId) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.CohortTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_COHORT_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Integer> result = new ArrayList<Integer>();
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        return ArrayUtils.toPrimitive(result.toArray(new Integer[0]));
    }

    @Override
    public void removePatientReferences(String sessID, int projID, int patID) throws SQLException, SessionExpiredException {

        int[] cohIDs = getCohortIDs(sessID, projID);

        TableSchema table = MedSavantDatabase.CohortmembershipTableSchema;

        for (int id : cohIDs) {
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_COHORT_ID), id));
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(CohortMembershipTableSchema.COLUMNNAME_OF_PATIENT_ID), patID));
            ConnectionController.executeUpdate(sessID, query.toString());
        }
    }

    @Override
    public int getNumVariantsInCohort(String sessID, int projID, int refID, int cohortID, Condition[][] conditions) throws SQLException, InterruptedException, RemoteException, SessionExpiredException {
        List<String> dnaIDs = getDNAIDsForCohort(sessID, cohortID);
        return VariantManager.getInstance().getVariantCountForDNAIDs(sessID, projID, refID, conditions, dnaIDs);
    }
}
