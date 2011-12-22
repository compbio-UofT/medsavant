package org.ut.biolab.medsavant.db.util.query.api;

import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.Remote;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.model.SimplePatient;

/**
 *
 * @author mfiume
 */
public interface CohortQueryUtilAdapter extends Remote {

    public List<SimplePatient> getIndividualsInCohort(String sid,int projectId, int cohortId) throws SQLException;
    public List<String> getDNAIdsInCohort(String sid,int cohortId) throws SQLException;
    public List<String> getIndividualFieldFromCohort(String sid,int cohortId, String columnname) throws SQLException;
    public void addPatientsToCohort(String sid,int[] patientIds, int cohortId) throws SQLException;
    public void removePatientsFromCohort(String sid,int[] patientIds, int cohortId) throws SQLException;
    public List<Cohort> getCohorts(String sid,int projectId) throws SQLException;
    public void addCohort(String sid,int projectId, String name) throws SQLException;
    public void removeCohort(String sid,int cohortId) throws SQLException;
    public void removeCohorts(String sid,Cohort[] cohorts) throws SQLException;
    public List<Integer> getCohortIds(String sid,int projectId) throws SQLException;
    public void removePatientReferences(String sid,int projectId, int patientId) throws SQLException;
    public int getNumVariantsInCohort(String sid,int projectId, int referenceId, int cohortId, Condition[][] conditions) throws SQLException, InterruptedException;
}
