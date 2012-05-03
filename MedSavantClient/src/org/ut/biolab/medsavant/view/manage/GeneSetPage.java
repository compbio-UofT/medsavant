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
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedTableView;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;


/**
 *
 * @author tarkvara
 */
public class GeneSetPage extends SubSectionView {
    private static final Log LOG = LogFactory.getLog(GeneSetPage.class);

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
                new DetailedListEditor());
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
                        LOG.error("Error loading gene sets.", ex);
                    }
                    setData(data);
                }
            };
        }
    }
}
