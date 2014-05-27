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
package org.ut.biolab.medsavant.client.cohort;

import java.util.List;
import javax.swing.SwingUtilities;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class CohortDetailedListEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        //return LoginController.getInstance().getUserLevel() == UserLevel.ADMIN;
        return true;
    }

    @Override
    public boolean doesImplementDeleting() {
        //return LoginController.getInstance().getUserLevel() == UserLevel.ADMIN;
        return true;
    }

    @Override
    public void addItems() {
        new CohortWizard().setVisible(true);
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int result;

        if (items.size() == 1) {
            String name = ((Cohort) items.get(0)[0]).getName();
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", name);
        } else {
            result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these %d cohorts?\nThis cannot be undone.", items.size());
        }


        if (result == DialogUtils.YES) {

            new ProgressDialog("Removing Cohort(s)", items.size() + " cohort(s) being removed.  Please wait.") {
                @Override
                public void run() {
                    int numCouldntRemove = 0;
                    for (Object[] v : items) {
                        int id = ((Cohort) v[0]).getId();
                        try {
                            MedSavantClient.CohortManager.removeCohort(LoginController.getSessionID(), id);                            
                        } catch (Exception ex) {
                            numCouldntRemove++;
                            ClientMiscUtils.reportError("Error removing " + ((Cohort) v[0]).getName() + ": %s", ex);
                        }
                    }

                    setVisible(false);
                    final int cohortsRemoved = items.size() - numCouldntRemove;
                    if (numCouldntRemove != items.size()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.displayMessage("Successfully removed " + (cohortsRemoved) + " cohort(s).");
                            }
                        });
                    }
                    this.dispose();
                }
            }.setVisible(true);
        }
    }
}