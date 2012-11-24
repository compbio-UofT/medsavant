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

package org.ut.biolab.medsavant.geneset;

import javax.swing.JPanel;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.serverapi.GeneSetManagerAdapter;
import org.ut.biolab.medsavant.util.GeneFetcher;
import org.ut.biolab.medsavant.util.MedSavantWorker;
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

    private SplitScreenView view;
    private final GeneSetManagerAdapter manager;

    public GeneSetPage(SectionView parent) {
        super(parent, "Standard Gene Sets");
        manager = MedSavantClient.GeneSetManager;
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel<GeneSet>("Gene Sets") {
                        @Override
                        public GeneSet[] getData() throws Exception {
                            return manager.getGeneSets(LoginController.sessionId);
                        }
                    },
                    new GenesDetailedView(),
                    new DetailedListEditor());
        }
        return view;
    }

    public void update(){
        view.refresh();
    }

    private class GenesDetailedView extends DetailedTableView<GeneSet> {

        public GenesDetailedView() {
            super(pageName, "", "Multiple gene sets (%d)", new String[] { "Name", "Chromosome", "Start", "End", "Coding Start", "Coding End" });
        }

        @Override
        public MedSavantWorker createWorker() {
            return new GeneFetcher(selected.get(0), pageName) {
                @Override
                public void setData(Object[][] data) {
                    GenesDetailedView.this.setData(data);
                }

                /**
                 * Don't have progress bar handy, so we don't do anything to show progress.
                 */
                @Override
                public void showProgress(double prog) {
                }
            };
        }
    }
}
