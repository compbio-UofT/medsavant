/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
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
    
    private int projectId;
    private int referenceId;

    private static ResultController instance;
    private int totalNumVariantsRemaining;
    private boolean updateTotalNumVariantsRemainingIsRequired = true;
    
    public ResultController() throws NonFatalDatabaseException {
        updateFilteredVariantDBResults(0, DEFAULT_LIMIT);
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

    public List<Object[]> getFilteredVariantRecords(int start, int limit) {
        if (filterSetId != FilterController.getCurrentFilterSetID() || this.limit < limit || this.start != start ||
                ProjectController.getInstance().getCurrentProjectId() != projectId ||
                ReferenceController.getInstance().getCurrentReferenceId() != referenceId){
            try {
                updateFilteredVariantDBResults(start, limit);
                this.limit = limit;
                this.start = start;
            } catch (NonFatalDatabaseException ex) {
                ex.printStackTrace();
            }
            projectId = ProjectController.getInstance().getCurrentProjectId();
            referenceId = ReferenceController.getInstance().getCurrentReferenceId();
        }      
        return filteredVariants;
    }
    
    private void updateFilteredVariantDBResults(int start, int limit) throws NonFatalDatabaseException {
        
        filterSetId = FilterController.getCurrentFilterSetID();
        
        try {
            filteredVariants = VariantQueryUtil.getVariants(
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId(), 
                    FilterController.getQueryFilterConditions(),
                    start, 
                    limit);
        } catch (SQLException ex) {
            MiscUtils.checkSQLException(ex);
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int getNumFilteredVariants() {
        try {
            if (updateTotalNumVariantsRemainingIsRequired) {
                totalNumVariantsRemaining =  VariantQueryUtil.getNumFilteredVariants(
                        ProjectController.getInstance().getCurrentProjectId(), 
                        ReferenceController.getInstance().getCurrentReferenceId(), 
                        FilterController.getQueryFilterConditions());
                updateTotalNumVariantsRemainingIsRequired = false;
            }
            return totalNumVariantsRemaining;
        } catch (SQLException ex) {
            MiscUtils.checkSQLException(ex);
            ex.printStackTrace();
            return 0;
        }
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        updateTotalNumVariantsRemainingIsRequired = true;
    }
}
