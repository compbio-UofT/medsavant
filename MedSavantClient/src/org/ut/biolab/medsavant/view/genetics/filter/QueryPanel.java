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

import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import org.ut.biolab.medsavant.api.Listener;

import org.ut.biolab.medsavant.api.MedSavantFilterPlugin;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.ColumnType;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.OntologyType;
import org.ut.biolab.medsavant.model.event.FilterEvent;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.region.RegionSetFilterView;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.WhichTable;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 * Panel which holds together a group of related filters which are ANDed together to form a single query.  Multiple <code>QueryPanel</code>
 * can be ORed together.
 *
 * @author mfiume
 */
public class QueryPanel extends CollapsiblePanes {

    static final Color DEFAULT_KEY_COLOR = Color.GRAY;
    static final Color INACTIVE_KEY_COLOR = Color.RED;

    private int queryID;

    private Map<String, FilterHolder> filterHolders = new TreeMap<String, FilterHolder>();

    public QueryPanel(int queryID) {
        this.queryID = queryID;

        setOpaque(false);
        setBorder(null);

        CollapsiblePane p1 = new CollapsiblePane("Search Conditions");
        p1.setBorder(null);
        p1.setStyle(CollapsiblePane.TREE_STYLE);
        p1.setCollapsible(false);

        ViewUtil.applyVerticalBoxLayout(p1.getContentPane());
        p1.add(populateFilterList());

        add(p1);
        addExpansion();

        p1.repaint();

    }

    public Collection<FilterHolder> getFilterHolders() {
        return filterHolders.values();
    }

    private boolean isFilterable(ColumnType type) {
        switch (type) {
            case INTEGER:
            case FLOAT:
            case DECIMAL:
            case BOOLEAN:
            case VARCHAR:
                return true;
            default:
                return false;
        }
    }

    private JPanel populateFilterList() {
        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);
        ViewUtil.applyVerticalBoxLayout(panes);
        panes.setBorder(null);
        
        try {
            // Cohort filter
            List<FilterHolder> catHolders = new ArrayList<FilterHolder>();
            catHolders.add(new SimpleFilterHolder(CohortFilterView.class, queryID));

            // Add from patient table
            for (CustomField field : ProjectController.getInstance().getCurrentPatientFormat()) {
                if (field.isFilterable() && isFilterable(field.getColumnType())) {
                    catHolders.add(new FieldFilterHolder(field, WhichTable.PATIENT, queryID));
                }
            }
            panes.add(addFilterCategory("Patient Conditions", catHolders, false), BorderLayout.CENTER);

            // Add from variant table
            AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
            for (AnnotationFormat af : afs) {
                for (CustomField field : af.getCustomFields()) {
                    if (field.isFilterable() && isFilterable(field.getColumnType())) {
                        catHolders.add(new FieldFilterHolder(field, WhichTable.VARIANT, queryID));
                    }
                }
            }

            //tag filter
            catHolders.add(new SimpleFilterHolder(TagFilterView.class, queryID));

            //starred variants
//            holders.add(new SimpleFilterPlaceholder(StarredFilterView.class));
            panes.add(addFilterCategory("Variant Conditions", catHolders, false), BorderLayout.CENTER);

            // Ontology filters
            catHolders.add(new OntologyFilterHolder(OntologyType.GO, queryID));
            catHolders.add(new OntologyFilterHolder(OntologyType.HPO, queryID));
            catHolders.add(new OntologyFilterHolder(OntologyType.OMIM, queryID));

            // Region list filter
            catHolders.add(new SimpleFilterHolder(RegionSetFilterView.class, queryID));
            panes.add(addFilterCategory("Ontology and Region Conditions", catHolders, true), BorderLayout.CENTER);

            // Plugin filters
            PluginController pc = PluginController.getInstance();
            for (PluginDescriptor desc : pc.getDescriptors()) {
                MedSavantPlugin plugin = pc.getPlugin(desc.getID());
                if (plugin instanceof MedSavantFilterPlugin) {
                    catHolders.add(new PluginFilterHolder((MedSavantFilterPlugin)plugin, queryID));
                }
            }

            panes.add(addFilterCategory("Plugin Conditions", catHolders, false));
            

        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to load filters: %s", ex);
        }

        return panes;
    }
    
    private CollapsiblePane addFilterCategory(String title, List<FilterHolder> catHolders, boolean longRunning) throws PropertyVetoException {

        CollapsiblePane cPanel = new CollapsiblePane(title);
        cPanel.setCollapsed(true);
        cPanel.setOpaque(false);
        cPanel.setLayout(new BorderLayout());

        cPanel.setStyle(CollapsiblePane.PLAIN_STYLE);

        final KeyValuePairPanel kvp = new KeyValuePairPanel(2);
        cPanel.add(kvp, BorderLayout.CENTER);

        for (FilterHolder f: catHolders) {
            f.addTo(kvp, longRunning);
        }

        FilterController.getInstance().addListener(new Listener<FilterEvent>() {

            @Override
            public void handleEvent(FilterEvent event) {
                Filter changedFilter = event.getFilter();

                if (kvp.containsKey(changedFilter.getName())) {
                    int index = 0;
                    Component c = ((JPanel) kvp.getAdditionalColumn(changedFilter.getName(), index)).getComponent(0);

                    switch (event.getType()) {
                        case ADDED:
                        case MODIFIED:
                            kvp.setKeyColour(changedFilter.getName(), INACTIVE_KEY_COLOR);
                            c.setVisible(true);
                            break;
                        case REMOVED:
                            kvp.setKeyColour(changedFilter.getName(), DEFAULT_KEY_COLOR);
                            c.setVisible(false);
                            break;
                    }
                }
            }
        });
        for (FilterHolder h: catHolders) {
            filterHolders.put(h.getFilterID(), h);
        }
        catHolders.clear();
        
        return cPanel;
    }

    /**
     * Given state loaded in from a saved file, find the correct filter holder and load in the given info.
     */
    public void loadFilterView(FilterState state) throws Exception {
        FilterHolder h = filterHolders.get(state.getFilterID());
        if (h != null) {
            h.loadFilterView(state);
        } else {
            throw new Exception(String.format("Unknown filter ID \"%s\"", state.getFilterID()));
        }
    }
}
