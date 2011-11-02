/*
 *    Copyright 2011 University of Toronto
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

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;


/**
 * @author Andrew
 */
public class ResultController {

    private List<Object[]> filteredVariants;
    
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


    public List<Object[]> getAllVariantRecords() {
        return filteredVariants;
    }

    public List<Object[]> getFilteredVariantRecords(int limit) {
        if (filterSetId != FilterController.getCurrentFilterSetID() || this.limit < limit ||
                ProjectController.getInstance().getCurrentProjectId() != projectId ||
                ReferenceController.getInstance().getCurrentReferenceId() != referenceId) {
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
