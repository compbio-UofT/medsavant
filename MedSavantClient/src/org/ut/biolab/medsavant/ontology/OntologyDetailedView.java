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
package org.ut.biolab.medsavant.ontology;

import org.ut.biolab.medsavant.model.Ontology;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.list.DetailedTableView;


/**
 *
 * @author tarkvara
 */
public class OntologyDetailedView extends DetailedTableView<Ontology> {

    public OntologyDetailedView(String page) {
        super(page, "", "Multiple ontologies (%d)", new String[] { "Name", "Type", "OBO URL", "Mapping URL" });
    }

    @Override
    public MedSavantWorker createWorker() {
        return new MedSavantWorker<Ontology[]>(getPageName()) {

            @Override
            protected Ontology[] doInBackground() throws Exception {
                return new Ontology[] { selected.get(0) };
            }

            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Ontology[] result) {
                Object[][] list = new Object[result.length][];
                for (int i = 0; i < result.length; i++) {
                    Ontology ont = result[i];
                    list[i] = new Object[] { ont.getName(), ont.getType(), ont.getOBOURL(), ont.getMappingURL() };
                }
                setData(list);
            }
        };
    }
}
