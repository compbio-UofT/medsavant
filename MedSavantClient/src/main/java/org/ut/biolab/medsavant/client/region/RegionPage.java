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

import javax.swing.JPanel;

import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class RegionPage extends SubSectionView {

    private final RegionController controller;
    private SplitScreenView view;


    public RegionPage(SectionView parent) {
        super(parent, "Region Lists");
        controller = RegionController.getInstance();
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = new SplitScreenView(
                    new SimpleDetailedListModel("Region List") {
                        @Override
                        public RegionSet[] getData() throws Exception {
                            return controller.getRegionSets().toArray(new RegionSet[0]);
                        }
                    },
                    new RegionDetailedView(pageName),
                    new RegionDetailedListEditor());
        }
        return view;
    }
}
