/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.ontology;

import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.shared.model.Ontology;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.list.DetailedTableView;


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
