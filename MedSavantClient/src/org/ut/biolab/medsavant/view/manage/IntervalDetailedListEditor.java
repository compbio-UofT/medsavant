package org.ut.biolab.medsavant.view.manage;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.dialog.IntervalWizard;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class IntervalDetailedListEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        return true;
    }

    @Override
    public boolean doesImplementDeleting() {
        return true;
    }

    @Override
    public void addItems() {
        new IntervalWizard();
    }

    @Override
    public void editItems(Object[] results) {
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int result;

        if (items.size() == 1) {
            String name = ((RegionSet) items.get(0)[0]).getName();
            result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                    "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
        } else {
            result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                    "Are you sure you want to remove these " + items.size() + " lists?\nThis cannot be undone.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
        }

        if (result == JOptionPane.YES_OPTION) {

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
                        int listId = ((RegionSet) v[0]).getId();
                        try {
                            MedSavantClient.RegionQueryUtilAdapter.removeRegionList(LoginController.sessionId, listId);
                        } catch (Exception ex) {
                            numCouldntRemove++;
                            DialogUtils.displayErrorMessage("Could remove " + listName + ".", ex);
                        }
                    }
                    dialog.close();
                    if (numCouldntRemove != items.size()) {
                        DialogUtils.displayMessage("Successfully removed " + (items.size()) + " list(s)");
                    }
                }
            };
            thread.start();
            dialog.setVisible(true);
        }
    }

    
}
