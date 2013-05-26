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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimplePatient;


/**
 *
 * @author mfiume
 */
public interface CohortManagerAdapter extends Remote {

    public List<SimplePatient> getIndividualsInCohort(String sessID, int projID, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsForCohort(String sessID, int cohortID) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsForCohorts(String sessID, int projID, Collection<String> cohNames) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getIndividualFieldFromCohort(String sessID, int cohID, String columnname) throws SQLException, RemoteException, SessionExpiredException;
    public void addPatientsToCohort(String sessID, int[] patientIds, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    public void removePatientsFromCohort(String sessID, int[] patientIds, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    public Cohort[] getCohorts(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
    public void addCohort(String sessID, int projID, String name) throws SQLException, RemoteException, SessionExpiredException;
    public void removeCohort(String sessID, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    public void removeCohorts(String sessID, Cohort[] cohorts) throws SQLException, RemoteException, SessionExpiredException;
    public int[] getCohortIDs(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
    public void removePatientReferences(String sessID, int projID, int patientId) throws SQLException, RemoteException, SessionExpiredException;
    public int getNumVariantsInCohort(String sessID, int projID, int referenceId, int cohID, Condition[][] conditions) throws SQLException, InterruptedException, RemoteException, SessionExpiredException;
}
