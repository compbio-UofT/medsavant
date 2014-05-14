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
package org.ut.biolab.medsavant.client.controller;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


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
        try {
            filteredVariants = MedSavantClient.VariantManager.getVariants(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    FilterController.getInstance().getAllFilterConditions(),
                    start,
                    limit,
                    orderByCols);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        }
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
                try {
                    filteredVariantCount =  MedSavantClient.VariantManager.getFilteredVariantCount(
                            LoginController.getSessionID(),
                            ProjectController.getInstance().getCurrentProjectID(),
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            filterController.getAllFilterConditions());
                } catch (SessionExpiredException ex) {
                    MedSavantExceptionHandler.handleSessionExpiredException(ex);
                    return 0;
                }
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
                try {
                    totalVariantCount =  MedSavantClient.VariantManager.getVariantCount(
                            LoginController.getSessionID(),
                            ProjectController.getInstance().getCurrentProjectID(),
                            ReferenceController.getInstance().getCurrentReferenceID());
                } catch (SessionExpiredException ex) {
                    MedSavantExceptionHandler.handleSessionExpiredException(ex);
                    return 0;
                }
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
