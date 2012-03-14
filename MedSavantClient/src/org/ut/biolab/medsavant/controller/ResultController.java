/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import com.healthmarketscience.sqlbuilder.dbspec.Column;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MiscUtils;

/**
 * @author Andrew
 */
public class ResultController implements FiltersChangedListener {

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
    
    public ResultController() throws NonFatalDatabaseException {
        //updateFilteredVariantDBResults(0, DEFAULT_LIMIT);
        FilterController.addFilterListener(this);
    }

    
    public static ResultController getInstance() throws NonFatalDatabaseException {
        if (instance == null) {
            instance = new ResultController();
        }
        return instance;
    }


    public List<Object[]> getAllVariantRecords() {
        return filteredVariants;
    }

    public List<Object[]> getFilteredVariantRecords(int start, int limit, Column[] order) {
        synchronized(lock_records){
            if (filterSetId_records != FilterController.getCurrentFilterSetID() || this.limit != limit || this.start != start ||
                    ProjectController.getInstance().getCurrentProjectId() != projectId_records ||
                    ReferenceController.getInstance().getCurrentReferenceId() != referenceId_records || 
                    !SettingsController.getInstance().getDBName().equals(dbName_records)){
                try {
                    updateFilteredVariantDBResults(start, limit, order);
                    this.limit = limit;
                    this.start = start;
                } catch (NonFatalDatabaseException ex) {
                    ex.printStackTrace();
                }
                projectId_records = ProjectController.getInstance().getCurrentProjectId();
                referenceId_records = ReferenceController.getInstance().getCurrentReferenceId();
                dbName_records = SettingsController.getInstance().getDBName();
            }   
            return filteredVariants;
        }
    }
    
    private void updateFilteredVariantDBResults(int start, int limit, Column[] order) throws NonFatalDatabaseException {
        
        filterSetId_records = FilterController.getCurrentFilterSetID();
        
        try {
            filteredVariants = MedSavantClient.VariantQueryUtilAdapter.getVariants(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId(), 
                    FilterController.getQueryFilterConditions(),
                    start, 
                    limit, 
                    order);
        } catch (SQLException ex) {
            MiscUtils.checkSQLException(ex);
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
    
    public int getNumFilteredVariants() {
        synchronized(lock_remaining){
            try {
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
                    totalNumVariantsRemaining =  MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                            LoginController.sessionId, 
                            ProjectController.getInstance().getCurrentProjectId(), 
                            ReferenceController.getInstance().getCurrentReferenceId(), 
                            FilterController.getQueryFilterConditions());
                    filterSetId_remaining = tempFilterId; //temp not really necessary as this is synched...
                }
                return totalNumVariantsRemaining;
            } catch (SQLException ex) {
                MiscUtils.checkSQLException(ex);
                ex.printStackTrace();
                return 0;
            } catch (RemoteException ex) {
                ex.printStackTrace();
                return 0;
            }
        }
    }
    
    public int getNumTotalVariants() {
        synchronized(lock_total){
            try {
                if (totalNumVariants == -1 ||
                        ProjectController.getInstance().getCurrentProjectId() != projectId_total ||
                        ReferenceController.getInstance().getCurrentReferenceId() != referenceId_total || 
                        !SettingsController.getInstance().getDBName().equals(dbName_total)) {
                    totalNumVariants =  MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                            LoginController.sessionId, 
                            ProjectController.getInstance().getCurrentProjectId(), 
                            ReferenceController.getInstance().getCurrentReferenceId());
                    projectId_total = ProjectController.getInstance().getCurrentProjectId();
                    referenceId_total = ReferenceController.getInstance().getCurrentReferenceId();
                    dbName_total = SettingsController.getInstance().getDBName();
                }
                return totalNumVariants;
            } catch (SQLException ex) {
                MiscUtils.checkSQLException(ex);
                ex.printStackTrace();
                return 0;
            } catch (RemoteException ex) {
                ex.printStackTrace();
                return 0;
            }
        }
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            setUpdateTotalNumRequired(true);
    }
    
    public void setUpdateTotalNumRequired(boolean required){
        updateTotalNumVariantsRemainingIsRequired = required;
    }
}
