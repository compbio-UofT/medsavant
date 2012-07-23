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

package org.ut.biolab.medsavant.region;

import javax.swing.JPanel;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class RegionPage extends SubSectionView {

    int importID = 0;
    SplitScreenView view;

    public RegionPage(SectionView parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "Region Lists";
    }

    @Override
    public JPanel getView(boolean update) {
        view = new SplitScreenView(
                new SimpleDetailedListModel("Region List") {
                    @Override
                    public RegionSet[] getData() throws Exception {
                        return MedSavantClient.RegionSetManager.getRegionSets(LoginController.sessionId);
                    }
                },
                new RegionDetailedView(),
                new RegionDetailedListEditor());

        return view;
    }
}
