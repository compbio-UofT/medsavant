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
package org.ut.biolab.medsavant.client.reference;

import java.util.List;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.Reference;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.DetailedTableView;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;


/**
 *
 * @author Andrew
 */
public class ReferenceGenomePage extends SubSectionView {

    private ReferenceController controller;

    private SplitScreenView view;

    public ReferenceGenomePage(SectionView parent) {
        super(parent, "Reference Genomes");
        controller = ReferenceController.getInstance();
        controller.addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (view != null) {
                    view.refresh();
                }
            }
        });
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel<Reference>("Reference") {
                        @Override
                        public Reference[] getData() throws Exception {
                            return controller.getReferences();
                        }
                    },
                    new ReferenceDetailedView(),
                    new ReferenceDetailedListEditor());
        }
        return view;
    }

    public void update(){
        view.refresh();
    }


    /**
     * Reference genome detailed view.
     */
    private class ReferenceDetailedView extends DetailedTableView<Reference> {

        public ReferenceDetailedView() {
            super(pageName, "", "Multiple references (%d)", new String[] { "Contig Name", "Contig Length", "Centromere Position" });
        }

        @Override
        public MedSavantWorker createWorker() {
            return new MedSavantWorker<Chromosome[]>(pageName) {

                @Override
                protected Chromosome[] doInBackground() throws Exception {
                    return controller.getChromosomes(selected.get(0).getID());
                }

                @Override
                protected void showProgress(double fraction) {
                }

                @Override
                protected void showSuccess(Chromosome[] result) {
                    Object[][] list = new Object[result.length][];
                    for (int i = 0; i < result.length; i++) {
                        Chromosome c = result[i];
                        list[i] = new Object[] { c.getName(), c.getLength(), c.getCentromerePos() };
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
