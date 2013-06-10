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

package org.ut.biolab.medsavant.client.variant;

import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author abrook
 */
class VariantFilesDetailedListEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        return LoginController.getInstance().getUserLevel() == UserLevel.ADMIN;
    }

    @Override
    public boolean doesImplementDeleting() {
        return LoginController.getInstance().getUserLevel() == UserLevel.ADMIN;
    }

    @Override
    public void addItems() {
        try {
            // Check for existing unpublished changes to this project + reference.
            if (ProjectController.getInstance().promptForUnpublished()) {
                try {
                    // Get lock.
                    if (MedSavantClient.SettingsManager.getDBLock(LoginController.getInstance().getSessionID())) {
                        try {
                            new ImportVariantsWizard().setVisible(true);
                        } finally {
                            try {
                                MedSavantClient.SettingsManager.releaseDBLock(LoginController.getInstance().getSessionID());
                            } catch (Exception ex1) {
                                VariantFilesPage.LOG.error("Error releasing database lock.", ex1);
                            }
                        }
                    } else {
                        DialogUtils.displayMessage("Cannot Modify Project", "The database is currently locked.\nTo unlock, see the Projects page in the Administration section.");
                    }
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error getting database lock: %s", ex);
                }
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error checking for changes: %s", ex);
        }
    }

    @Override
    public void deleteItems(List<Object[]> results) {
        List<SimpleVariantFile> files = new ArrayList<SimpleVariantFile>();
        for (Object[] f : results) {
            files.add((SimpleVariantFile)f[0]);
        }

        if (!files.isEmpty()) {
            try {
                // Check for existing unpublished changes to this project + reference.
                if (ProjectController.getInstance().promptForUnpublished()) {
                    try {
                        // Get lock.
                        if (MedSavantClient.SettingsManager.getDBLock(LoginController.getInstance().getSessionID())) {
                            try {
                                new RemoveVariantsWizard(files).setVisible(true);
                            } finally {
                                try {
                                    MedSavantClient.SettingsManager.releaseDBLock(LoginController.getInstance().getSessionID());
                                } catch (Exception ex1) {
                                    VariantFilesPage.LOG.error("Error releasing database lock.", ex1);
                                }
                            }
                        } else {
                            DialogUtils.displayMessage("Cannot Modify Project", "The database is currently locked.\nTo unlock, see the Projects page in the Administration section.");
                       }
                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Error getting database lock: %s", ex);
                    }
                }
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error checking for changes: %s", ex);
            }
        }
    }
}
