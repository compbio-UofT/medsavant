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

package org.ut.biolab.medsavant.region;

import java.util.List;

import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class RegionDetailedListEditor extends DetailedListEditor {

    private final RegionController controller;

    public RegionDetailedListEditor() {
        controller = RegionController.getInstance();
    }

    @Override
    public boolean doesImplementAdding() {
        UserLevel level = LoginController.getInstance().getUserLevel();
        return level == UserLevel.USER || level == UserLevel.ADMIN;
    }

    @Override
    public boolean doesImplementDeleting() {
        UserLevel level = LoginController.getInstance().getUserLevel();
        return level == UserLevel.USER || level == UserLevel.ADMIN;
    }

    @Override
    public boolean doesImplementImporting() {
        UserLevel level = LoginController.getInstance().getUserLevel();
        return level == UserLevel.USER || level == UserLevel.ADMIN;
    }

    @Override
    public void addItems() {
        try {
            new RegionWizard(false).setVisible(true);
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching standard genes: %s", ex);
        }
    }

    @Override
    public void importItems() {
        try {
            new RegionWizard(true).setVisible(true);
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching standard genes: %s", ex);
        }
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int result;

        if (items.size() == 1) {
            String name = ((RegionSet)items.get(0)[0]).getName();
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", name);
        } else {
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove these %d lists?<br>This cannot be undone.</html>", items.size());
        }

        if (result == DialogUtils.YES) {

            new IndeterminateProgressDialog("Removing Region List(s)", "Removing region list(s). Please wait.") {
                @Override
                public void run() {
                    int numCouldntRemove = 0;

                    for (Object[] v : items) {
                        RegionSet set = (RegionSet)v[0];
                        try {
                            controller.removeSet(set.getID());
                        } catch (Throwable ex) {
                            numCouldntRemove++;
                            ClientMiscUtils.reportError("Could not remove " + set.getName() + ": %s", ex);
                        }
                    }
                    if (numCouldntRemove != items.size()) {
                        DialogUtils.displayMessage(String.format("Successfully removed %d list(s)", items.size()));
                    }
                }
            }.setVisible(true);
        }
    }
}
