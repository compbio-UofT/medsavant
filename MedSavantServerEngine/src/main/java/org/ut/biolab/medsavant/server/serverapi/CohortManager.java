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

import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.variants.VariantManager;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.Patient;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.serverapi.CohortManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 *
 * @author Andrew
 */
public class CohortManager extends MedSavantServerUnicastRemoteObject implements CohortManagerAdapter, BasicPatientColumns {

    private static CohortManager instance;
    private static QueryManager queryManager;
    private static EntityManager entityManager;

    private static final Log LOG = LogFactory.getLog(CohortManager.class);

    private CohortManager() throws RemoteException, SessionExpiredException {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
    }

    public static synchronized CohortManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new CohortManager();
        }
        return instance;
    }

    @Override
    public List<SimplePatient> getIndividualsInCohort(String sid, int projectId, int cohortId) throws SQLException, RemoteException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p from Patient p where p.cohort_id := cohortId and p.project_id := projectId");
        query.setParameter("cohortId", cohortId);
        query.setParameter("projectId", projectId);

        List<Patient> patients = query.execute();
        List<SimplePatient> simplePatients = new ArrayList<SimplePatient>();
        for (Patient patient : patients) {
            simplePatients.add(patient.getSimplePatient());
        }

        return simplePatients;
    }

    @Override
    public List<String> getDNAIDsForCohort(String sessID, int cohortId) throws SQLException, RemoteException, SessionExpiredException {
        List<String> list = getIndividualFieldFromCohort(sessID, cohortId, DNA_IDS.getColumnName());

        return list;
    }

    @Override
    public List<String> getDNAIDsForCohorts(String sessID, int projID, Collection<String> cohNames) throws SQLException, RemoteException, SessionExpiredException {
        String statement = String.format("Select c from Cohort c where c.name IN (%s)", StringUtils.join(cohNames, ","));
        Query query = queryManager.createQuery(statement);
        List<ResultRow> cohortResults = query.executeForRows();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cohortResults.size(); i++) {
            int cohortId = (Integer) cohortResults.get(0).getObject("cohort_id");
            sb.append(cohortId);
            if (i != cohortResults.size() - 1) {
                sb.append(",");
            }
        }

        String patientStatement = String.format("Select p.dna_ids from Patient p where p.cohort_ids IN (%s)",sb.toString());
        Query patientQuery = queryManager.createQuery(patientStatement);
        List<ResultRow> resultRows = patientQuery.executeForRows();

        List<String> dnaIds = ListUtils.EMPTY_LIST;
        for (ResultRow resultRow : resultRows) {
            dnaIds = ListUtils.union(dnaIds,(List<String>) resultRow.getObject("dna_ids"));
        }

        return dnaIds;
    }

    @Override
    public List<String> getIndividualFieldFromCohort(String sessID, int cohortId, String columnName) throws SQLException, RemoteException, SessionExpiredException {
        String statement = String.format("Select c.%s from Cohort c where c.cohort_id= :cohortId", columnName);
        Query query = queryManager.createQuery(statement);
        query.setParameter("cohortId", cohortId);
        List<ResultRow> resultRows = query.executeForRows();

        List<String> result = ListUtils.EMPTY_LIST;
        for (ResultRow resultRow : resultRows) {
            result = ListUtils.union(result, (List<String>) resultRow.getObject("cohort_id"));
        }

        return result;

    }

    @Override
    public void addPatientsToCohort(String sessID, int[] patientIDs, int cohortID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select c from Cohort c where c.cohort_id = :cohortId");
        query.setParameter("cohortId", cohortID);
        List<Cohort> cohorts = query.execute();
        if (cohorts.size() > 0) {
            Cohort cohort = cohorts.get(0);
            List<Integer> newPatientIds = ListUtils.union(Arrays.asList(patientIDs), cohort.getPatientIds());
            cohort.setPatientIds(newPatientIds);
            try {
                entityManager.persist(cohort);
            } catch (InitializationException e) {
                LOG.error("Failed to persist cohort");
            }
        }

    }

    @Override
    public void removePatientsFromCohort(String sessID, int[] patIDs, int cohID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select c from Cohort c where c.cohort_id= :cohortId");
        query.setParameter("cohortId", cohID);

        List<Cohort> cohorts = query.execute();
        if (cohorts.size() > 0 ) {
            Cohort c = cohorts.get(0);
            for (int id : patIDs) {
                c.removePatientId(id);
            }
            try {
                entityManager.persist(c);
            } catch (InitializationException e) {
                LOG.error("Error persisting cohort");
            }
        }

    }

    @Override
    public Cohort[] getCohorts(String sessID, int projID) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select c from Cohort c where c.project_id= :projectId");
        query.setParameter("projectId", projID);

        List<Cohort> cohorts = query.execute();
        return cohorts.toArray(new Cohort[]{});
    }

    @Override
    public void addCohort(String sid, int projectId, String name) throws SQLException, SessionExpiredException {
        Cohort cohort = new Cohort(generateId(), projectId, name);
        try {
            entityManager.persist(cohort);
        } catch (InitializationException e) {
            LOG.error("Failed to persist cohort");
        }
    }


    @Override
    public void removeCohort(String sid, int cohortId) throws SQLException, SessionExpiredException {

        //remove patient references
        Query query = queryManager.createQuery("Select p from Patient p where p.cohort_id= :cohortId");
        query.setParameter("cohortId", cohortId);
        List<Patient> patients = query.execute();
        for (Patient patient : patients) {
            patient.removeCohortId(cohortId);
            try {
                entityManager.persist(patient);
            } catch (InitializationException e) {
                LOG.error("Error persisting patient");
            }
        }

        //delete
        Query deleteQuery = queryManager.createQuery("Delete from Cohort c where c.cohort_id= :cohortId");
        deleteQuery.setParameter("cohortId", cohortId);
        deleteQuery.executeDelete();
    }

    @Override
    public void removeCohorts(String sid, Cohort[] cohorts) throws SQLException, SessionExpiredException {
        for (Cohort c : cohorts) {
            removeCohort(sid,c.getId());
        }
    }

    @Override
    public int[] getCohortIDs(String sid, int projectId) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select c.cohort_id from Cohort c where c.project_id= :projectId");
        query.setParameter("projectId",projectId);
        List<ResultRow> resultRowList = query.executeForRows();
        int[] cohortIds = new int[resultRowList.size()];
        for (int i = 0; i < resultRowList.size(); i++) {
            cohortIds[i] = (Integer) resultRowList.get(0).getObject("cohort_id");
        }

        return cohortIds;
    }

    @Override
    public void removePatientReferences(String sessID, int projID, int patID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select c from Cohort c where c.project_id= :projectId");
        query.setParameter("projectId", projID);

        List<Cohort> cohorts = query.execute();
        for (Cohort cohort : cohorts) {
            cohort.removePatientId(patID);
            try {
                entityManager.persist(cohort);
            } catch (InitializationException e) {
                LOG.error("Error persisting cohort");
            }
        }
    }

    @Override
    public int getNumVariantsInCohort(String sessID, int projID, int refID, int cohortID, Condition[][] conditions) throws SQLException, InterruptedException, RemoteException, SessionExpiredException {
        List<String> dnaIDs = getDNAIDsForCohort(sessID, cohortID);
        return VariantManager.getInstance().getVariantCountForDNAIDs(sessID, projID, refID, conditions, dnaIDs);
    }

    private int generateId() {
        Query query = queryManager.createQuery("Select c.cohort_id,max(c.cohort_id) from Cohort c");
        List<ResultRow> results = query.executeForRows();

        int newId;
        if (results.size() == 0 ) {
            newId = 1;
        } else {
            newId = ((Double)(results.get(0).getObject("max"))).intValue() + 1;
        }

        return newId;
    }
}
