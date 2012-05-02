/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.view.manage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.model.Reference;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.dialog.NewReferenceDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedTableView;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 *
 * @author tarkvara
 */
public class GeneSetPage extends SubSectionView {

    private SplitScreenView panel;
    private boolean updateRequired = false;

    public GeneSetPage(SectionView parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "Standard Gene Sets";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null || updateRequired) {
            setPanel();
        }
        return panel;
    }

    @Override
    public void viewDidLoad() {}

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }

    public void setPanel() {
        panel = new SplitScreenView(
                new SimpleDetailedListModel("Gene Sets") {
                    @Override
                    public List getData() throws Exception {
                        return MedSavantClient.GeneSetAdapter.getGeneSets(LoginController.sessionId);
                    }
                    
                },
                new GenesDetailedView(),
                new GenesDetailedListEditor());
    }

    public void update(){
        panel.refresh();
    }

    /*
     * REFERENCE GENOMES DETAILED VIEW
     */
    private static class GenesDetailedView extends DetailedTableView {
        private GeneSet selectedSet = null;
        
        public GenesDetailedView() {
            super("Gene Sets", "Multiple gene sets (%d)", new String[] { "Name", "Chromosome", "Start", "End", "Coding Start", "Coding End" });
        }

        @Override
        public void setSelectedItem(Object[] item) {
            selectedSet = (GeneSet)item[0];
            super.setSelectedItem(item);
        }

        @Override
        public SwingWorker createWorker() {
            return new SwingWorker<List<Gene>, Void>() {

                @Override
                protected List<Gene> doInBackground() throws Exception {
                    return MedSavantClient.GeneSetAdapter.getGenes(LoginController.sessionId, selectedSet);
                }

                @Override
                protected void done() {
                    //List<Object[]> list = new ArrayList<Object[]>();
                    Object[][] data = null;
                    try {
                        List<Gene> result = get();
                        data = new Object[result.size()][];
                        for (int i = 0; i < result.size(); i++) {
                            Gene g = result.get(i);
                            data[i] = new Object[] { g.getName(), g.getChrom(), g.getStart(), g.getEnd(), g.getCodingStart(), g.getCodingEnd() };
                        }
                    } catch (InterruptedException ex) {
                    } catch (ExecutionException ex) {
                        Logger.getLogger(ReferenceGenomePage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    setData(data);
                }
            };
        }
    }



    /*
     * REFERENCE GENOMES DETAILED LIST EDITOR
     */
    private static class GenesDetailedListEditor extends DetailedListEditor {

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
            NewReferenceDialog npd = new NewReferenceDialog(MedSavantFrame.getInstance(), true);
            npd.setVisible(true);
        }

        @Override
        public void editItems(Object[] results) {
        }

        @Override
        public void deleteItems(List<Object[]> items) {

            int result;

            if (items.size() == 1) {
                String name = ((Reference) items.get(0)[0]).getName();
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
                             "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            } else {
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
                             "Are you sure you want to remove these " + items.size() + " references?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            }

            if (result == JOptionPane.YES_OPTION) {
                int numCouldntRemove = 0;
                for (Object[] v : items) {
                    String refName = ((Reference)v[0]).getName();
                    ReferenceController.getInstance().removeReference(refName);
                }

                if (items.size() != numCouldntRemove) {
                    DialogUtils.displayMessage("Successfully removed " + (items.size()-numCouldntRemove) + " reference(s)");
                }
            }
        }
    }

}
