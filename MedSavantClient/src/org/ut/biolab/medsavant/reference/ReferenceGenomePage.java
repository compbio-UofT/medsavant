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
package org.ut.biolab.medsavant.reference;

import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.model.Chromosome;
import org.ut.biolab.medsavant.model.Reference;
import org.ut.biolab.medsavant.listener.ReferenceListener;
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
 * @author Andrew
 */
public class ReferenceGenomePage extends SubSectionView implements ReferenceListener {
    private static final Log LOG = LogFactory.getLog(ReferenceGenomePage.class);

    private SplitScreenView panel;
    private boolean updateRequired = false;

    public ReferenceGenomePage(SectionView parent) {
        super(parent);
        ReferenceController.getInstance().addReferenceListener(this);
    }

    @Override
    public String getName() {
        return "Reference Genomes";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null || updateRequired) {
            setPanel();
        }
        return panel;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }

    public void setPanel() {
        panel = new SplitScreenView(
                new SimpleDetailedListModel("Reference") {
                    @Override
                    public List getData() throws Exception {
                        return MedSavantClient.ReferenceQueryUtilAdapter.getReferences(LoginController.sessionId);
                    }
                },
                new ReferenceDetailedView(),
                new ReferenceDetailedListEditor());
    }

    @Override
    public void referenceAdded(String name) {
        if (panel != null) {
            panel.refresh();
        }
    }

    @Override
    public void referenceRemoved(String name) {
        if (panel != null) {
            panel.refresh();
        }
    }

    @Override
    public void referenceChanged(String prnameojectName) {
        if (panel != null) {
            panel.refresh();
        }
    }

    public void update(){
        panel.refresh();
    }


    /*
     * REFERENCE GENOMES DETAILED VIEW
     */
    private static class ReferenceDetailedView extends DetailedTableView {

        private Reference ref;

        public ReferenceDetailedView() {
            super("Reference Information", "Multiple references (%d)", new String[] { "Contig Name", "Contig Length", "Centromere Position" });
        }

        @Override
        public void setSelectedItem(Object[] item) {
            ref = (Reference)item[0];
            super.setSelectedItem(item);
        }

        @Override
        public SwingWorker createWorker() {
            return new SwingWorker<List<Chromosome>, Void>() {

                @Override
                protected List<Chromosome> doInBackground() throws Exception {
                    return MedSavantClient.ChromosomeQueryUtilAdapter.getContigs(LoginController.sessionId, ref.getId());
                }

                @Override
                protected void done() {
                    //List<Object[]> list = new ArrayList<Object[]>();
                    Object[][] list = null;
                    try {
                        List<Chromosome> result = get();
                        list = new Object[result.size()][];
                        for (int i = 0; i < result.size(); i++) {
                            Chromosome c = result.get(i);
                            list[i] = new Object[] { c.getName(), c.getLength(), c.getCentromerepos() };
                        }
                    } catch (InterruptedException ex) {
                    } catch (ExecutionException ex) {
                        LOG.error("Error fetching reference genomes.", ex);
                    }
                    setData(list);
                }
            };
        }
    }



    /*
     * REFERENCE GENOMES DETAILED LIST EDITOR
     */
    private static class ReferenceDetailedListEditor extends DetailedListEditor {

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
            new NewReferenceDialog().setVisible(true);
        }

        @Override
        public void deleteItems(List<Object[]> items) {

            int result;

            if (items.size() == 1) {
                String name = ((Reference) items.get(0)[0]).getName();
                result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove %s?\nThis cannot be undone.", name);
            } else {
                result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these %d references?\nThis cannot be undone.", items.size());
            }

            if (result == DialogUtils.YES) {
                int numCouldntRemove = 0;
                for (Object[] v : items) {
                    String refName = ((Reference)v[0]).getName();
                    ReferenceController.getInstance().removeReference(refName);
                }

                if (items.size() != numCouldntRemove) {
                    DialogUtils.displayMessage(String.format("Successfully removed %d reference(s)", items.size() - numCouldntRemove));
                }
            }
        }
    }

}
