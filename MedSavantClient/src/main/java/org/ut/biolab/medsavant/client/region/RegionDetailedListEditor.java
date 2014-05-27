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
package org.ut.biolab.medsavant.client.region;

import java.util.List;

import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class RegionDetailedListEditor extends DetailedListEditor {

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
        if(items == null || items.isEmpty()){
            return;
        }

        if (items.size() == 1) {
            String name = ((RegionSet)items.get(0)[0]).getName();
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", name);
        } else {
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove these %d lists?<br>This cannot be undone.</html>", items.size());
        }

        if (result == DialogUtils.YES) {

            new ProgressDialog("Removing Region List(s)", "Removing region list(s). Please wait.") {
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
                    
					// Ron: commenting this out cause it's annoying me. 
					/*
					if (numCouldntRemove != items.size()) {
                        DialogUtils.displayMessage(String.format("Successfully removed %d list(s)", items.size()));
                    }
					*/
                }
            }.setVisible(true);
        }
    }
}
