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

import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.client.filter.Filter;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.filter.FilterState;
import org.ut.biolab.medsavant.client.filter.TabularFilterView;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;

/**
 *
 * @author tarkvara
 */
public class OntologyFilterView extends TabularFilterView<OntologyTerm> {

    private final OntologyType ontology;

    /**
     * Constructor for loading a saved filter from a file.
     */
    public OntologyFilterView(FilterState state, int queryID) throws Exception {
        this(OntologyFilter.filterIDToOntology(state.getFilterID()), queryID);
        List<String> values = state.getValues("value");
        if (values != null) {
            setFilterValues(values);
        }
    }

    /**
     * Constructor for creating a fresh filter from a place-holder.
     */
    public OntologyFilterView(OntologyType ont, int queryID) throws Exception {
        super(OntologyFilter.ontologyToTitle(ont), queryID);
        ontology = ont;

        new MedSavantWorker<Void>("FilterView") {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Void result) {
                initContentPanel();
            }

            @Override
            protected Void doInBackground() throws Exception {
                OntologyTerm[] terms = MedSavantClient.OntologyManager.getAllTerms(LoginController.getSessionID(), ontology);
                setAvailableValues(Arrays.asList(terms));
                return null;
            }
        }.execute();

        /*new CancellableProgressDialog("Fetching Ontology", String.format("Retrieving list of %s ontology terms.", ontology,true)) {
         @Override
         public void run() throws InterruptedException, SQLException, RemoteException {
         OntologyTerm[] terms = MedSavantClient.OntologyManager.getAllTerms(LoginController.getSessionID(), ontology);
         availableValues = Arrays.asList(terms);
         }

         @Override
         public ProgressStatus checkProgress() throws RemoteException {
         return MedSavantClient.OntologyManager.checkProgress(LoginController.getSessionID(), cancelled);
         }
         }.showDialog();
         */


    }

    public static FilterState wrapState(String title, OntologyType ont, List<OntologyTerm> applied, boolean shortForm) {
        String filterID = OntologyFilter.ontologyToFilterID(ont);

        // Can't use wrapValues directly on applied, because OntologyTerm.toString() includes the description.
        List values = applied;
        if (shortForm) {
            values = new ArrayList();
            for (OntologyTerm t : applied) {
                values.add(t.getID());
            }
        }

        FilterState state = new FilterState(Filter.Type.ONTOLOGY, title, filterID);
        state.putValues(FilterState.VALUE_ELEMENT, wrapValues(values));
        return state;
    }

    @Override
    public FilterStateAdapter saveState() {
        return wrapState(getTitle(), ontology, appliedValues, true);
    }

    @Override
    protected void applyFilter() {
        preapplyFilter();
        FilterController.getInstance().addFilter(new OntologyFilter(appliedValues, ontology), queryID);
    }
}
