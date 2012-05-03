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

package org.ut.biolab.medsavant.view.regions;

import java.util.List;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class RegionDetailedListEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        return true;
    }

    @Override
    public boolean doesImplementDeleting() {
        return true;
    }

    @Override
    public boolean doesImplementImporting() {
        return true;
    }

    @Override
    public void addItems() {
        new RegionWizard(false).setVisible(true);
    }

    @Override
    public void importItems() {
        new RegionWizard(true).setVisible(true);
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int result;

        if (items.size() == 1) {
            String name = ((RegionSet)items.get(0)[0]).getName();
            result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove %s?\nThis cannot be undone.", name);
        } else {
            result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these %d lists?\nThis cannot be undone.", items.size());
        }

        if (result == DialogUtils.YES) {

            final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                    "Removing Region List(s)",
                    "Removing region list(s). Please wait.",
                    true);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    int numCouldntRemove = 0;

                    for (Object[] v : items) {
                        String listName = ((RegionSet) v[0]).getName();
                        int listID = ((RegionSet) v[0]).getID();
                        try {
                            MedSavantClient.RegionQueryUtilAdapter.removeRegionList(LoginController.sessionId, listID);
                        } catch (Exception ex) {
                            numCouldntRemove++;
                            DialogUtils.displayErrorMessage("Could not remove " + listName + ".", ex);
                        }
                    }
                    dialog.close();
                    if (numCouldntRemove != items.size()) {
                        DialogUtils.displayMessage("Successfully removed %d list(s)", items.size());
                    }
                }
            };
            thread.start();
            dialog.setVisible(true);
        }
    }
}
