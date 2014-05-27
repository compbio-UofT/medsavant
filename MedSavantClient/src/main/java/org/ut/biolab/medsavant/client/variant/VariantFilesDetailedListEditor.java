/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.variant;

import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.exception.LockException;

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
                // Get lock.
                String sessionID = LoginController.getSessionID();
                if (!MedSavantClient.SettingsManager.isProjectLockedForChanges(sessionID, ProjectController.getInstance().getCurrentProjectID())) {
                    new ImportVariantsWizardWithAnnotation().setVisible(true);
                } else {
                    DialogUtils.displayMessage("Cannot Modify Project", "This project is currently locked for changes.\nTo unlock, see the Projects page in the Administration section.");
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
            files.add((SimpleVariantFile) f[0]);
        }

        if (!files.isEmpty()) {
            try {
                // Check for existing unpublished changes to this project + reference.
                if (ProjectController.getInstance().promptForUnpublished()) {
                    try {
                        // Get lock.
                        if (!MedSavantClient.SettingsManager.isProjectLockedForChanges(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID())) {
                            new RemoveVariantsWizard(files).setVisible(true);
                        } else {
                            DialogUtils.displayMessage("Cannot Modify Project", "This project is currently locked for changes.\nTo unlock, see the Projects page in the Administration section.");
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
