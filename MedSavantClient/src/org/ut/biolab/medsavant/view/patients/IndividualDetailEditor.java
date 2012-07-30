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

package org.ut.biolab.medsavant.view.patients;

import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class IndividualDetailEditor extends DetailedListEditor {

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
            new AddPatientsForm().setVisible(true);
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to present Add Patient form: %s", ex);
        }
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int keyIndex = 0;
        int nameIndex = 3;

        int result;

        if (items.size() == 1) {
            String name = (String) items.get(0)[nameIndex];
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", name);
        } else {
            result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these %d individuals?\nThis cannot be undone.", items.size());
        }

        if (result == DialogUtils.YES) {
            final int[] patients = new int[items.size()];
            int index = 0;
            for (Object[] v : items) {
                int id = (Integer) v[keyIndex];
                patients[index++] = id;
            }

            new IndeterminateProgressDialog("Removing Patient(s)", patients.length + " patient(s) being removed. Please wait.") {
                @Override
                public void run() {
                    try {
                        MedSavantClient.PatientManager.removePatient(
                                LoginController.sessionId,
                                ProjectController.getInstance().getCurrentProjectID(),
                                patients);
                        DialogUtils.displayMessage("Successfully removed " + items.size() + " patient(s)");
                    } catch (Exception ex) {
                        setVisible(false);
                        ClientMiscUtils.reportError("Error removing patient(s): %s", ex);
                    }

                }
            }.setVisible(true);
        }
    }
}
