/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;

/**
 * @author Andrew
 */
public class ResultController {

    private Vector filteredVariants;
    
    private static final int DEFAULT_LIMIT = 1000;
    private int limit = -1;
    private int filterSetId = -1;
    
    private int projectId;
    private int referenceId;

    private static ResultController instance;
    
    public ResultController() throws NonFatalDatabaseException {
        updateFilteredVariantDBResults(DEFAULT_LIMIT);
    }

    
    public static ResultController getInstance() throws NonFatalDatabaseException {
        if (instance == null) {
            instance = new ResultController();
        }
        return instance;
    }


    public Vector getAllVariantRecords() {
        return filteredVariants;
    }

    public Vector getFilteredVariantRecords(int limit) {
        if(filterSetId != FilterController.getCurrentFilterSetID() || this.limit < limit ||
                ProjectController.getInstance().getCurrentProjectId() != projectId ||
                ReferenceController.getInstance().getCurrentReferenceId() != referenceId){
            try {
                updateFilteredVariantDBResults(limit);
                this.limit = limit;
            } catch (NonFatalDatabaseException ex) {
                ex.printStackTrace();
            }
            projectId = ProjectController.getInstance().getCurrentProjectId();
            referenceId = ReferenceController.getInstance().getCurrentReferenceId();
        }      
        return filteredVariants;
    }
    
    private void updateFilteredVariantDBResults(int limit) throws NonFatalDatabaseException {
        
        filterSetId = FilterController.getCurrentFilterSetID();
        
        try {
            filteredVariants = VariantQueryUtil.getVariants(
                    ProjectController.getInstance().getCurrentProjectId(), 
                    ReferenceController.getInstance().getCurrentReferenceId(), 
                    FilterController.getQueryFilterConditions(), 
                    limit);
        } catch (SQLException ex) {
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
