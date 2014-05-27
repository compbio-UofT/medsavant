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
package org.ut.biolab.medsavant.client.ontology;

import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
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
                            MedSavantClient.OntologyManager.removeOntology(LoginController.getSessionID(), ontName);
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
