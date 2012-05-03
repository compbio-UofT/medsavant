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

package org.ut.biolab.medsavant.view.patients;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class CohortsPage extends SubSectionView {

    public CohortsPage(SectionView parent) { super(parent); }
    private SplitScreenView view;

    @Override
    public String getName() {
        return "Cohorts";
    }

    @Override
    public JPanel getView(boolean update) {
        view =  new SplitScreenView(
                new SimpleDetailedListModel("Cohort") {
                    @Override
                    public List getData() throws Exception {
                        return MedSavantClient.CohortQueryUtilAdapter.getCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId());
                    }
                },
                new CohortDetailedView(),
                new CohortDetailedListEditor());

        return view;
    }

    public Component[] getSubSectionMenuComponents() {
        /*
        Component[] result = new Component[1];
        result[0] = getAddCohortButton();
        return result;
         *
         */
        return null;
    }

    private JButton getAddCohortButton(){
        JButton button = new JButton("Add cohort");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CohortWizard();
                if(view != null) view.refresh();
            }
        });
        return button;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }

}
