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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.genetics.filter.ont.OntologyRetriever;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class GOFilterView extends FilterView {

    public static final String FILTER_NAME = "Gene Ontology";
    public static final String FILTER_ID = "geneontology";
    //private static final String COHORT_ALL = "All Individuals";
    private final Object lock = new Object();

    private Integer appliedID;
    private ActionListener al;


    private JButton applyButton;
    private JLabel labelSelected;


    private GOFilterView(FilterState state, int queryID) throws IOException {
        this(queryID, new JPanel());
        if (state.getValues().get("value") != null) {
            applyFilter(Integer.parseInt(state.getValues().get("value")));
        }
    }
    
    public GOFilterView(int queryID, JPanel container) throws IOException {
        super(FILTER_NAME, container, queryID);
        createContentPanel(container);
    }

    public final void applyFilter(int cohortId) {

        /*
        for (int i = 0; i < b.getItemCount(); i++) {
        if (b.getItemAt(i) instanceof Cohort && ((Cohort) b.getItemAt(i)).getId() == cohortId) {
        b.setSelectedIndex(i);
        al.actionPerformed(new ActionEvent(this, 0, null));
        return;
        }
        }
         *
         */
    }

    private String selectedID = "";
    private String lastAppliedID = "";
    private OntologyRetriever retriever;

    private void setSelectedID(String id) {
        if (id == null) { return; }
        this.selectedID = id;
        if (lastAppliedID.equals(selectedID)) {
            this.applyButton.setEnabled(false);
        } else {
            this.applyButton.setEnabled(true);
        }

        if (id.equals("")) {
            this.labelSelected.setText("Select a row to filter by GO ID");
        } else {
            this.labelSelected.setText(stem + id);
        }
    }

    private String stem = "Filter individuals with GO ID: ";

    private void applyID(String id) {
        if (id == null) { return; }
        lastAppliedID = id;
        setSelectedID(id);
    }

    private String getSelectedID(SearchableTablePanel stp) {
        if (stp.getTable().getSelectedRow() == -1) { return null; }
        return (String)retriever.getTerms().get(stp.getActualRowAt(stp.getTable().getSelectedRow()))[0];
    }

    private void createContentPanel(JPanel p) throws IOException {

        JPanel t = new JPanel();
        t.setBackground(Color.yellow);
        //t.setLayout(new GridLayout(3,0));

        ViewUtil.applyVerticalBoxLayout(t);

        p.add(t);
        p.setBorder(ViewUtil.getMediumBorder());
        p.setMaximumSize(new Dimension(360, 250));

        retriever = new OntologyRetriever("/org/ut/biolab/medsavant/data/hpo/gene_ontology.1_2.obo");

        final SearchableTablePanel stp = new SearchableTablePanel("***GO***",
                                                                  new String[] { "ID", "Name" },
                                                                  new Class[] { String.class, String.class },
                                                                  new int[0], 10000, retriever);
        stp.setBottomBarVisible(false);
        stp.setChooseColumnsButtonVisible(false);
        stp.setExportButtonVisible(false);

        Dimension d = new Dimension(340, 190);
        Dimension d2 = new Dimension(d.width, d.height - 50);

        stp.setMinimumSize(d);
        stp.setPreferredSize(d);
        stp.setMaximumSize(d);

        /**
        stp.getTable().setMinimumSize(d2);
        stp.getTable().setPreferredSize(d2);
        stp.getTable().setMaximumSize(d2);
         */
        stp.forceRefreshData();
        stp.setNumRowsPerPage(99999);

        t.add(stp);

        stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
               setSelectedID(getSelectedID(stp));
              // applyButton.setEnabled(true);
            }

        });

        JPanel topBar = new JPanel();
        labelSelected = new JLabel("");
        topBar.add(ViewUtil.center(labelSelected));

        t.add(topBar);

        JPanel bottomBar = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(bottomBar);

        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

        selectNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSelectedID("");
                stp.getTable().getSelectionModel().clearSelection();
            }
        });
        bottomBar.add(selectNone);

        bottomBar.add(Box.createHorizontalGlue());
        applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                applyID(getSelectedID(stp));
            }

        });

        bottomBar.add(applyButton);

        t.add(bottomBar);

        applyButton.setEnabled(false);

        setSelectedID(this.lastAppliedID);
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        if (appliedID != null) {
            map.put("value", Integer.toString(appliedID));
        }
        return new FilterState(FilterType.STRING, FILTER_NAME, FILTER_ID, map);
    }


    public static void main(String[] argv) throws FileNotFoundException, IOException {
        //loadOntology();
    }
}
