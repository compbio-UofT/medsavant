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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.shared.util.Modifier;
import static org.ut.biolab.medsavant.shared.util.ModificationType.*;



/**
 *
 * @author mfiume
 */
public interface CohortManagerAdapter extends Remote {

    public List<SimplePatient> getIndividualsInCohort(String sessID, int projID, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsForCohort(String sessID, int cohortID) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getDNAIDsForCohorts(String sessID, int projID, Collection<String> cohNames) throws SQLException, RemoteException, SessionExpiredException;
    public List<String> getIndividualFieldFromCohort(String sessID, int cohID, String columnname) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=COHORT)
    public void addPatientsToCohort(String sessID, int[] patientIds, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=COHORT)
    public void removePatientsFromCohort(String sessID, int[] patientIds, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    
    public Cohort[] getCohorts(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=COHORT)
    public void addCohort(String sessID, int projID, String name) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=COHORT)
    public void removeCohort(String sessID, int cohID) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=COHORT)
    public void removeCohorts(String sessID, Cohort[] cohorts) throws SQLException, RemoteException, SessionExpiredException;
    public int[] getCohortIDs(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException;
    
    @Modifier(type=COHORT)
    public void removePatientReferences(String sessID, int projID, int patientId) throws SQLException, RemoteException, SessionExpiredException;
    public int getNumVariantsInCohort(String sessID, int projID, int referenceId, int cohID, Condition[][] conditions) throws SQLException, InterruptedException, RemoteException, SessionExpiredException;

}
