/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

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
    private int filterSetId = -1;
    
    //last state seen by getFilteredVariantRecords
    private int projectId_records;
    private int referenceId_records;
    private String dbName_records;
    
    //last state seen by getTotalNumVariants    
    private int projectId_total;
    private int referenceId_total;
    private String dbName_total;
    
    //last state seen by getNumFilteredVariants
    private int projectId_remaining;
    private int referenceId_remaining;
    private String dbName_remaining;

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

    public synchronized List<Object[]> getFilteredVariantRecords(int start, int limit) {
        if (filterSetId != FilterController.getCurrentFilterSetID() || this.limit < limit || this.start != start ||
                ProjectController.getInstance().getCurrentProjectId() != projectId_records ||
                ReferenceController.getInstance().getCurrentReferenceId() != referenceId_records || 
                !SettingsController.getInstance().getDBName().equals(dbName_records)){
            try {
                updateFilteredVariantDBResults(start, limit);
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
    
    private synchronized void updateFilteredVariantDBResults(int start, int limit) throws NonFatalDatabaseException {
        
        filterSetId = FilterController.getCurrentFilterSetID();
        
        try {
            filteredVariants = MedSavantClient.VariantQueryUtilAdapter.getVariants(
                    LoginController.sessionId, 
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId(), 
                    FilterController.getQueryFilterConditions(),
                    start, 
                    limit);
        } catch (SQLException ex) {
            MiscUtils.checkSQLException(ex);
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
    
    public synchronized int getNumFilteredVariants() {
        try {
            if (filterSetId != FilterController.getCurrentFilterSetID() ||
                    updateTotalNumVariantsRemainingIsRequired ||
                    ProjectController.getInstance().getCurrentProjectId() != projectId_remaining ||
                    ReferenceController.getInstance().getCurrentReferenceId() != referenceId_remaining || 
                    !SettingsController.getInstance().getDBName().equals(dbName_remaining)) {
                totalNumVariantsRemaining =  MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                        LoginController.sessionId, 
                        ProjectController.getInstance().getCurrentProjectId(), 
                        ReferenceController.getInstance().getCurrentReferenceId(), 
                        FilterController.getQueryFilterConditions());
                updateTotalNumVariantsRemainingIsRequired = false;
                projectId_remaining = ProjectController.getInstance().getCurrentProjectId();
                referenceId_remaining = ReferenceController.getInstance().getCurrentReferenceId();
                dbName_remaining = SettingsController.getInstance().getDBName();
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
    
    public synchronized int getNumTotalVariants() {
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

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        setUpdateTotalNumRequired(true);
    }
    
    public void setUpdateTotalNumRequired(boolean required){
        updateTotalNumVariantsRemainingIsRequired = required;
    }
}
