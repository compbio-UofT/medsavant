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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.FilterStateAdapter;
import org.ut.biolab.medsavant.filter.Filter;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.*;
import org.ut.biolab.medsavant.filter.FilterState;
import org.ut.biolab.medsavant.filter.TabularFilterView;
import org.ut.biolab.medsavant.view.dialog.CancellableProgressDialog;


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
        new CancellableProgressDialog("Fetching Ontology", String.format("Retrieving list of %s ontology terms.", ontology)) {
            @Override
            public void run() throws InterruptedException, SQLException, RemoteException {
                OntologyTerm[] terms = MedSavantClient.OntologyManager.getAllTerms(LoginController.sessionId, ontology);
                availableValues = Arrays.asList(terms);
            }

            @Override
            public ProgressStatus checkProgress() throws RemoteException {
                return MedSavantClient.OntologyManager.checkProgress(LoginController.sessionId, cancelled);
            }
        }.showDialog();
        initContentPanel();
    }

    public static FilterState wrapState(String title, OntologyType ont, List<OntologyTerm> applied, boolean shortForm) {
        String filterID = OntologyFilter.ontologyToFilterID(ont);

        // Can't use wrapValues directly on applied, because OntologyTerm.toString() includes the description.
        List values = applied;
        if (shortForm) {
            values = new ArrayList();
            for (OntologyTerm t: applied) {
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
