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
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.*;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState;
import org.ut.biolab.medsavant.view.genetics.filter.TabularFilterView;


/**
 *
 * @author tarkvara
 */
public class OntologyFilterView extends TabularFilterView {

    private final OntologyType ontology;

    /**
     * Constructor for loading a saved filter from a file.
     */
    public OntologyFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(OntologyFilter.filterIDToOntology(state.getFilterID()), queryID);
        String values = state.getValues().get("values");
        if (values != null) {
            List<String> l = new ArrayList<String>();
            Collections.addAll(l, values.split(";;;"));
            applyFilter(l);
        }
    }

    /**
     * Constructor for creating a fresh filter from a place-holder.
     */
    public OntologyFilterView(OntologyType ont, int queryID) throws SQLException, RemoteException {
        super(OntologyFilter.ontologyToTitle(ont), queryID);
        ontology = ont;
        new IndeterminateProgressDialog("Fetching Ontology", String.format("Retrieving list of %s ontology terms.", ontology)) {
            @Override
            public void run() {
                try {
                    OntologyTerm[] terms = MedSavantClient.OntologyManager.getAllTerms(LoginController.sessionId, ontology);
                    availableValues = Arrays.asList(terms);
                    initContentPanel();
                } catch (Throwable ex) {
                    setVisible(false);
                    ClientMiscUtils.reportError(String.format("Error getting ontology terms for %s: %%s", OntologyFilter.ontologyToTitle(ontology)), ex);
                }
            }
        }.setVisible(true);
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        String filterID = OntologyFilter.ontologyToFilterID(ontology);
        map.put("ontology", filterID);
        if (appliedValues != null && !appliedValues.isEmpty()) {
            String values = "";
            for (int i = 0; i < appliedValues.size(); i++) {
                values += appliedValues.get(i);
                if (i != appliedValues.size() - 1) {
                    values += ";;;";
                }
            }
            map.put("values", values);
        }
        return new FilterState(Filter.Type.ONTOLOGY, getTitle(), filterID, map);
    }

    @Override
    protected void applyFilter() {
        applyButton.setEnabled(false);

        appliedValues = new ArrayList<OntologyTerm>();

        int[] indices = filterableList.getCheckBoxListSelectedIndices();
        for (int i: indices) {
            appliedValues.add(filterableList.getModel().getElementAt(i));
        }

        FilterController fc = FilterController.getInstance();
        if (appliedValues.size() == availableValues.size()) {
            fc.removeFilter(OntologyFilter.ontologyToFilterID(ontology), queryID);
            return;
        }

        fc.addFilter(new OntologyFilter(appliedValues, ontology), queryID);
    }
}
