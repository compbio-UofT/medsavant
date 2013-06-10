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

package org.ut.biolab.medsavant.client.ontology;

import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author tarkvara
 */
class OntologyDetailedListEditor extends DetailedListEditor {

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
            new OntologyWizard().setVisible(true);
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching standard genes: %s", ex);
        }
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int result;

        String caption, message;
        if (items.size() == 1) {
            caption = "Removing Ontology";
            message = "Removing ontology.  Please wait.";
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", items.get(0)[0]);
        } else {
            caption = "Removing Ontologies";
            message = "Removing ontologies.  Please wait.";
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove these %d ontologies?<br>This cannot be undone.</html>", items.size());
        }

        if (result == DialogUtils.YES) {

            new ProgressDialog(caption, message) {
                @Override
                public void run() {
                    int numCouldntRemove = 0;

                    for (int i = 0; i < items.size(); i++) {
                        String ontName = items.get(i)[0].toString();
                        try {
                            MedSavantClient.OntologyManager.removeOntology(LoginController.getInstance().getSessionID(), ontName);
                        } catch (Throwable ex) {
                            numCouldntRemove++;
                            setVisible(false);
                            ClientMiscUtils.reportError("Could not remove " + ontName + ": %s", ex);
                        }
                    }
                    setVisible(false);
                    if (numCouldntRemove != items.size()) {
                        DialogUtils.displayMessage(String.format("Successfully removed %d %s.", items.size(), items.size() > 1 ? "ontologies" : "ontology"));
                    }
                }
            }.setVisible(true);
        }
    }
}
