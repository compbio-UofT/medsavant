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

package org.ut.biolab.medsavant.client.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;


/**
 * @author Andrew
 */
public class ResultController {
    private static final Log LOG = LogFactory.getLog(ResultController.class);

    private List<Object[]> filteredVariants;

    private static ResultController instance;

    private int limit = -1;
    private int start = -1;

    //last state seen by getFilteredVariantRecords
    private int filterSetIDForRecords = -1;
    private int projectIDForRecords;
    private int referenceIDForRecords;
    private String dbNameForRecords;
    private final Object recordsLock = new Object();

    // Last state seen by getTotalVariantCount.
    private int projectIDForTotal;
    private int referenceIDForTotal;
    private String dbNameForTotal;
    private final Object totalLock = new Object();

    // Last state seen by getNumFilteredVariants
    private int filterSetIDForFilteredCount = -1;
    private int projectIDForFilteredCount;
    private int referenceIDForFilteredCount;
    private String dbNameForFilteredCount;
    private final Object filteredCountLock = new Object();

    private int filteredVariantCount = -1;
    private int totalVariantCount = -1;

    private FilterController filterController;

    private ResultController() {
        filterController = FilterController.getInstance();
        filterController.addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                filteredVariantCount = -1;
            }
        });
    }

    public static ResultController getInstance() {
        if (instance == null) {
            instance = new ResultController();
        }
        return instance;
    }


    public List<Object[]> getAllVariantRecords() {
        return filteredVariants;
    }

    public List<Object[]> getFilteredVariantRecords(int start, int limit, String[] orderByCols) throws InterruptedException, SQLException, RemoteException {
        synchronized (recordsLock) {
            if (filterSetIDForRecords != filterController.getCurrentFilterSetID() || this.limit != limit || this.start != start ||
                    ProjectController.getInstance().getCurrentProjectID() != projectIDForRecords ||
                    ReferenceController.getInstance().getCurrentReferenceID() != referenceIDForRecords ||
                    !SettingsController.getInstance().getDBName().equals(dbNameForRecords)) {
                long then = System.currentTimeMillis();
                updateFilteredVariantDBResults(start, limit, orderByCols);
                LOG.info("Query for " + start + ", " + limit + " took " + (System.currentTimeMillis() - then) + "ms to return " + filteredVariants.size() + " records.");
                this.limit = limit;
                this.start = start;
                projectIDForRecords = ProjectController.getInstance().getCurrentProjectID();
                referenceIDForRecords = ReferenceController.getInstance().getCurrentReferenceID();
                dbNameForRecords = SettingsController.getInstance().getDBName();
            }
            return filteredVariants;
        }
    }

    private void updateFilteredVariantDBResults(int start, int limit, String[] orderByCols) throws InterruptedException, SQLException, RemoteException {

        filterSetIDForRecords = filterController.getCurrentFilterSetID();

        filteredVariants = MedSavantClient.VariantManager.getVariants(
                LoginController.sessionId,
                ProjectController.getInstance().getCurrentProjectID(),
                ReferenceController.getInstance().getCurrentReferenceID(),
                FilterController.getInstance().getAllFilterConditions(),
                start,
                limit,
                orderByCols);
    }


    public int getFilteredVariantCount() throws InterruptedException, SQLException, RemoteException {
        synchronized (filteredCountLock) {
            if (filteredVariantCount == -1 ||
                    filterSetIDForFilteredCount != filterController.getCurrentFilterSetID() ||
                    ProjectController.getInstance().getCurrentProjectID() != projectIDForFilteredCount ||
                    ReferenceController.getInstance().getCurrentReferenceID() != referenceIDForFilteredCount ||
                    !SettingsController.getInstance().getDBName().equals(dbNameForFilteredCount)) {
                projectIDForFilteredCount = ProjectController.getInstance().getCurrentProjectID();
                referenceIDForFilteredCount = ReferenceController.getInstance().getCurrentReferenceID();
                dbNameForFilteredCount = SettingsController.getInstance().getDBName();
                int tempFilterId = filterController.getCurrentFilterSetID();
                filteredVariantCount =  MedSavantClient.VariantManager.getFilteredVariantCount(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        filterController.getAllFilterConditions());
                filterSetIDForFilteredCount = tempFilterId; //temp not really necessary as this is synched...
            }
            return filteredVariantCount;
        }
    }

    public int getTotalVariantCount() throws RemoteException, SQLException {
        synchronized (totalLock) {
            if (totalVariantCount == -1 ||
                    ProjectController.getInstance().getCurrentProjectID() != projectIDForTotal ||
                    ReferenceController.getInstance().getCurrentReferenceID() != referenceIDForTotal ||
                    !SettingsController.getInstance().getDBName().equals(dbNameForTotal)) {
                totalVariantCount =  MedSavantClient.VariantManager.getVariantCount(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID());
                projectIDForTotal = ProjectController.getInstance().getCurrentProjectID();
                referenceIDForTotal = ReferenceController.getInstance().getCurrentReferenceID();
                dbNameForTotal = SettingsController.getInstance().getDBName();
            }
            return totalVariantCount;
        }
    }

    public void refreshCounts() {
        filteredVariantCount = -1;
        totalVariantCount = -1;
    }
}
