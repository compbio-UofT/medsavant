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

package org.ut.biolab.medsavant.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import com.healthmarketscience.sqlbuilder.dbspec.Column;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;


/**
 * @author Andrew
 */
public class ResultController implements FiltersChangedListener {
    private static final Log LOG = LogFactory.getLog(ResultController.class);
    private List<Object[]> filteredVariants;
    
    private static final int DEFAULT_LIMIT = 1000;
    private int limit = -1;
    private int start = -1;
    
    //last state seen by getFilteredVariantRecords
    private int filterSetId_records = -1;
    private int projectId_records;
    private int referenceId_records;
    private String dbName_records;
    private final Object lock_records = new Object();
    
    //last state seen by getTotalNumVariants    
    private int projectId_total;
    private int referenceId_total;
    private String dbName_total;
    private final Object lock_total = new Object();
    
    //last state seen by getNumFilteredVariants
    private int filterSetId_remaining = -1;
    private int projectId_remaining;
    private int referenceId_remaining;
    private String dbName_remaining;
    private final Object lock_remaining = new Object();
    
    private static ResultController instance;
    private int totalNumVariantsRemaining;
    private int totalNumVariants = -1;
    private boolean updateTotalNumVariantsRemainingIsRequired = true;
    
    private ResultController() {
    }
    
    public static ResultController getInstance() {
        if (instance == null) {
            instance = new ResultController();
            FilterController.addFilterListener(instance);
        }
        return instance;
    }


    public List<Object[]> getAllVariantRecords() {
        return filteredVariants;
    }

    public List<Object[]> getFilteredVariantRecords(int start, int limit, Column[] order) throws RemoteException, SQLException {
        synchronized (lock_records) {
            if (filterSetId_records != FilterController.getCurrentFilterSetID() || this.limit != limit || this.start != start ||
                    ProjectController.getInstance().getCurrentProjectId() != projectId_records ||
                    ReferenceController.getInstance().getCurrentReferenceId() != referenceId_records || 
                    !SettingsController.getInstance().getDBName().equals(dbName_records)) {
                updateFilteredVariantDBResults(start, limit, order);
                this.limit = limit;
                this.start = start;
                projectId_records = ProjectController.getInstance().getCurrentProjectId();
                referenceId_records = ReferenceController.getInstance().getCurrentReferenceId();
                dbName_records = SettingsController.getInstance().getDBName();
            }   
            return filteredVariants;
        }
    }
    
    private void updateFilteredVariantDBResults(int start, int limit, Column[] order) throws RemoteException, SQLException {
        
        filterSetId_records = FilterController.getCurrentFilterSetID();
        
        filteredVariants = MedSavantClient.VariantManager.getVariants(
                LoginController.sessionId, 
                ProjectController.getInstance().getCurrentProjectId(), 
                ReferenceController.getInstance().getCurrentReferenceId(), 
                FilterController.getQueryFilterConditions(),
                start, 
                limit, 
                order);
    }
    
    public int getNumFilteredVariants() throws RemoteException, SQLException {
        synchronized (lock_remaining) {
            if (filterSetId_remaining != FilterController.getCurrentFilterSetID() ||
                    updateTotalNumVariantsRemainingIsRequired ||
                    ProjectController.getInstance().getCurrentProjectId() != projectId_remaining ||
                    ReferenceController.getInstance().getCurrentReferenceId() != referenceId_remaining || 
                    !SettingsController.getInstance().getDBName().equals(dbName_remaining)) {
                updateTotalNumVariantsRemainingIsRequired = false;
                projectId_remaining = ProjectController.getInstance().getCurrentProjectId();
                referenceId_remaining = ReferenceController.getInstance().getCurrentReferenceId();
                dbName_remaining = SettingsController.getInstance().getDBName();
                int tempFilterId = FilterController.getCurrentFilterSetID();
                totalNumVariantsRemaining =  MedSavantClient.VariantManager.getNumFilteredVariants(
                        LoginController.sessionId, 
                        ProjectController.getInstance().getCurrentProjectId(), 
                        ReferenceController.getInstance().getCurrentReferenceId(), 
                        FilterController.getQueryFilterConditions());
                filterSetId_remaining = tempFilterId; //temp not really necessary as this is synched...
            }
            return totalNumVariantsRemaining;
        }
    }
    
    public int getNumTotalVariants() throws RemoteException, SQLException {
        synchronized(lock_total) {
            if (totalNumVariants == -1 ||
                    ProjectController.getInstance().getCurrentProjectId() != projectId_total ||
                    ReferenceController.getInstance().getCurrentReferenceId() != referenceId_total || 
                    !SettingsController.getInstance().getDBName().equals(dbName_total)) {
                totalNumVariants =  MedSavantClient.VariantManager.getNumFilteredVariants(
                        LoginController.sessionId, 
                        ProjectController.getInstance().getCurrentProjectId(), 
                        ReferenceController.getInstance().getCurrentReferenceId());
                projectId_total = ProjectController.getInstance().getCurrentProjectId();
                referenceId_total = ReferenceController.getInstance().getCurrentReferenceId();
                dbName_total = SettingsController.getInstance().getDBName();
            }
            return totalNumVariants;
        }
    }

    @Override
    public void filtersChanged() {
        setUpdateTotalNumRequired(true);
    }
    
    public void setUpdateTotalNumRequired(boolean required) {
        updateTotalNumVariantsRemainingIsRequired = required;
    }
}
