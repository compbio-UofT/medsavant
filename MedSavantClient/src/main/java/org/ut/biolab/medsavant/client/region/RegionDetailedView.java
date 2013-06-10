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
package org.ut.biolab.medsavant.client.region;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsFilterPage;
import org.ut.biolab.medsavant.client.view.list.DetailedTableView;


/**
 *
 * @author mfiume
 */
public class RegionDetailedView extends DetailedTableView<RegionSet> {

    private final RegionController controller;

    public RegionDetailedView(String page) {
        super(page, "", "Multiple lists (%d)", new String[] { "Region", "Chromosome", "Start", "End" });
        controller = RegionController.getInstance();
    }

    @Override
    public MedSavantWorker createWorker() {
        return new MedSavantWorker<List<GenomicRegion>>(getPageName()) {

            @Override
            protected List<GenomicRegion> doInBackground() throws Exception {
                return controller.getRegionsInSet(selected.get(0));
            }

            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(List<GenomicRegion> result) {
                Object[][] list = new Object[result.size()][];
                for (int i = 0; i < result.size(); i++) {
                    GenomicRegion r = result.get(i);
                    list[i] = new Object[] { r.getName(), r.getChrom(), r.getStart(), r.getEnd() };
                }
                setData(list);
            }
        };
    }

    @Override
    public JPopupMenu createPopup() {
        JPopupMenu popupMenu = new JPopupMenu();

        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by patient
            JMenuItem filter1Item = new JMenuItem(String.format("<html>Filter by %s</html>", selected.size() == 1 ? "Region List <i>" + selected.get(0) + "</i>" : "Selected Region Lists"));
            filter1Item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GeneticsFilterPage.getSearchBar().loadFilters(RegionSetFilterView.wrapState(selected));
                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
