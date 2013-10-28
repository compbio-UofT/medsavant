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
package org.ut.biolab.medsavant.client.filter;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.shared.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsFilterPage;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.MasterView;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;

/**
 *
 * @author tarkvara
 */
class SavedFiltersPanel extends MasterView {

    SavedFiltersPanel() {
        super("Filters", new SimpleDetailedListModel("Saved Searches") {
            @Override
            public String[] getData() throws Exception {
                return getFilterList().toArray(new String[0]);
            }
        },
                null,
                new SavedFiltersEditor());
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(20, 300);
    }

    /**
     * Look in our filters directory to get a full list of all available filter
     * sets.
     */
    private static List<String> getFilterList() {
        List<String> list = new ArrayList<String>();
        File[] filterFiles = DirectorySettings.getFiltersDirectory().listFiles(new ExtensionFileFilter("xml"));
        for (File f : filterFiles) {
            list.add(MiscUtils.getBaseName(f.getAbsolutePath()));
        }
        return list;
    }
}

/**
 * Class which allows us to add and delete saved filter sets.
 */
class SavedFiltersEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        return false;
    }

    @Override
    public boolean doesImplementDeleting() {
        return true;
    }

    @Override
    public boolean doesImplementLoading() {
        return true;
    }

    /*@Override
     public void addItems() {
     if (FilterController.getInstance().hasFiltersApplied()) {
     String name = "Untitled";
     do {
     name = DialogUtils.displayInputMessage("Name for Current Filter Set", "Choose a name for saving the current filter set:", name);
     if (name == null) {
     return;     // User cancelled input dialog.
     }
     // If there's no extension, add ".xml".
     if (name.indexOf('.') < 0) {
     name += ".xml";
     }
     } while (!validateName(name));
     try {
     saveFilterSet(name);
     } catch (Exception ex) {
     ClientMiscUtils.reportError("Unable to save filter set: %s", ex);
     }
     } else {
     Toolkit.getDefaultToolkit().beep();
     }
     }*/
    @Override
    public void deleteItems(List<Object[]> items) {

        int result;

        if (items.size() == 1) {
            String name = (String) items.get(0)[0];
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", name);
        } else {
            result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these %d filter sets?\nThis cannot be undone.", items.size());
        }


        if (result == DialogUtils.YES) {
            for (Object[] row : items) {
                new File(DirectorySettings.getFiltersDirectory(), row[0] + ".xml").delete();
            }
        }
    }

    @Override
    public void loadItems(final List<Object[]> items) {
        //warn of overwrite
        if (!FilterController.getInstance().hasFiltersApplied() || DialogUtils.askYesNo("Confirm Load", "<html>Loading filters clears all existing filters. <br>Are you sure you want to continue?</html>") == DialogUtils.YES) {


            final JDialog d = new JDialog(MedSavantFrame.getInstance(), "Search Conditions", true);
            WaitPanel p = new WaitPanel("Applying search conditions...");
            d.add(p);
            d.pack();
            d.setLocationRelativeTo(MedSavantFrame.getInstance());
            d.setResizable(false);
            d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);


            new MedSavantWorker<Boolean>("SavedFiltersPanel") {
                @Override
                protected void showProgress(double fract) {
                }

                @Override
                protected void showSuccess(Boolean success) {
                    d.setVisible(false);
                    if (success) {
                        DialogUtils.displayMessage("Search conditions applied");
                    } else {
                        DialogUtils.displayError("Error loading search conditions");
                    }
                }

                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        // The items in our list are the names of XML files in the Filters directory.
                        List<File> files = new ArrayList<File>();
                        for (Object[] row : items) {
                            files.add(new File(DirectorySettings.getFiltersDirectory(), row[0] + ".xml"));
                        }
                        GeneticsFilterPage.getSearchBar().loadFiltersFromFiles(files);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }

                    return true;
                }
            }.execute();

            d.setVisible(true);
        }
    }

    /*
     private boolean validateName(String name) {
     File[] existingFilterSets = DirectorySettings.getFiltersDirectory().listFiles();
     for (File f: existingFilterSets) {
     if (f.getName().equals(name)) {
     return DialogUtils.askYesNo("Replace Filter Set", "<html>A filter set named <i>%s</i> already exists.<br>Do you want to overwrite it?</html>", name) == DialogUtils.YES;
     }
     }
     return true;
     }

     private void saveFilterSet(String name) throws Exception {

     File file = new File(DirectorySettings.getFiltersDirectory(), name);

     BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
     out.write("<filters>\n");
     for (QueryPanel sub: GeneticsFilterPage.getSearchBar().queryPanels) {
     out.write("\t<set>\n");
     for (FilterHolder item: sub.getFilterHolders()) {
     if (item.hasFilterView()) {
     out.write(item.getFilterView().saveState().generateXML() + "\n");
     }
     }
     out.write("\t</set>\n");
     }
     out.write("</filters>\n");
     out.close();
     }
     */
}
