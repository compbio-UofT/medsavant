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

package org.ut.biolab.medsavant.ontology;

import javax.swing.JPanel;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Ontology;
import org.ut.biolab.medsavant.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author mfiume
 */
public class OntologyPage extends SubSectionView {

    int importID = 0;
    SplitScreenView view;

    public OntologyPage(SectionView parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "Ontologies";
    }

    @Override
    public JPanel getView(boolean update) {
        view = new SplitScreenView(
                new SimpleDetailedListModel("Ontology") {
                    @Override
                    public Ontology[] getData() throws Exception {
                        return MedSavantClient.OntologyManager.getOntologies(LoginController.sessionId);
                    }
                },
                new OntologyDetailedView(getName()),
                new OntologyDetailedListEditor());

        return view;
    }
}
