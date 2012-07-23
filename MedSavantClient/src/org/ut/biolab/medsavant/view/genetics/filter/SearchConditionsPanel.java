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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.api.MedSavantFilterPlugin;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.ColumnType;
import org.ut.biolab.medsavant.db.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.format.CustomField.Category;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.OntologyType;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.ontology.OntologyFilter;
import org.ut.biolab.medsavant.ontology.OntologyFilterView;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.region.RegionSetFilterView;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.WhichTable;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SearchConditionsPanel extends JPanel {

    private int id;
    private JPanel contentPanel;
    private List<FilterPanelSubItem> subItems = new ArrayList<FilterPanelSubItem>();
    private SearchBar parent;
    private boolean isRemoved = false;
    private KeyValuePairPanel kvp1;
    private CollapsiblePane p1;

    public SearchConditionsPanel(final SearchBar parent, int id) {
        init(parent, id);
    }

    public final void init(final SearchBar parent, int id) {

        this.setOpaque(false);

        this.id = id;
        this.parent = parent;
        this.setLayout(new BorderLayout());

        ViewUtil.applyVerticalBoxLayout(this);

        contentPanel = ViewUtil.getClearPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        ViewUtil.applyVerticalBoxLayout(contentPanel);

        this.add(contentPanel, BorderLayout.CENTER);

        /*
         * contentPanel.setBorder(BorderFactory.createCompoundBorder(
         * ViewUtil.getMediumBorder(), ViewUtil.getMediumTopHeavyBorder() ));
         */


        JComponent searchConditionComponent = getFilterSet();
        contentPanel.add(searchConditionComponent, BorderLayout.CENTER);

        //p1 = getFilterSet();
        //panes.add(p1);
        //p1.repaint();

        /*
         * filterSetContainer = new JPanel();
         * ViewUtil.applyVerticalBoxLayout(filterSetContainer);
         * filterSetContainer.setBackground(Color.white);
         * filterSetContainer.setBorder(ViewUtil.getMediumBorder());
         * p1.add(filterSetContainer,BorderLayout.CENTER);
         *
         */

        //panes.addExpansion();

        refreshSubItems();
    }

    public List<FilterPanelSubItem> getSubItems() {
        return this.subItems;
    }

    public void refreshSubItems() {

        if (kvp1 != null) {
            p1.remove(kvp1);
        }

        kvp1 = new KeyValuePairPanel(2);
        p1.add(kvp1);

        //check for removed items
        for (int i = subItems.size() - 1; i >= 0; i--) {
            if (subItems.get(i).isRemoved()) {
                subItems.remove(i);
            }
        }

        // this is where we get the individual filter uis
        //refresh panel
        for (int i = 0; i < subItems.size(); i++) {
            String key = subItems.get(i).getName();
            kvp1.addKey(key);
            kvp1.setAdditionalColumn(key, 0, getConfigButton(kvp1, key));
            kvp1.setAdditionalColumn(key, 1, getRemoveButton(kvp1, key));
            kvp1.setDetailComponent(key, ViewUtil.getClearBorderedScrollPane(subItems.get(i).getFilterView()));
        }

        kvp1.repaint();
        p1.repaint();

        this.updateUI();
    }

    private Component getRemoveButton(final KeyValuePairPanel kvp, final String key) {
        final SearchConditionsPanel instance = this;
        final JToggleButton button = ViewUtil.getTexturedToggleButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.DELETE));
        button.setToolTipText("Remove " + key + " conditions");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                instance.removeThis();
            }
        });
        return button;
    }

    private Component getConfigButton(final KeyValuePairPanel kvp, final String key) {
        final JToggleButton button = ViewUtil.getTexturedToggleButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CONFIGURE));
        button.setToolTipText("Edit " + key + " conditions");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                kvp.toggleDetailVisibility(key);
            }
        });
        return button;
    }

    private void removeThis() {
        FilterController.removeFilterSet(id);
        for (int i = subItems.size() - 1; i >= 0; i--) {
            subItems.get(i).removeThisSilent();
        }
        isRemoved = true;
        refreshSubItems();
        parent.refreshSubPanels();
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public FilterPanelSubItem addNewSubItem(FilterView view, String filterID) {
        FilterPanelSubItem result = new FilterPanelSubItem(view, this, filterID);
        subItems.add(result);
        refreshSubItems();
        return result;
    }

    public boolean hasSubItem(String filterId) {
        for (FilterPanelSubItem f : subItems) {
            if (f.getFilterID().equals(filterId)) {
                return true;
            }
        }
        return false;
    }

    public int getID() {
        return id;
    }

    public void removeFiltersByID(String id) {
        //reversed to avoid concurrent mod exception
        for (int i = subItems.size() - 1; i >= 0; i--) {
            FilterPanelSubItem fpsi = subItems.get(i);
            if (fpsi.getFilterID().equals(id)) {
                fpsi.removeThis();
            }
        }
    }

    private Map<Category, List<FilterPlaceholder>> getRemainingFilters() throws RemoteException, SQLException, Exception {

        Map<Category, List<FilterPlaceholder>> map = new EnumMap<Category, List<FilterPlaceholder>>(Category.class);

        map.put(Category.PATIENT, new ArrayList<FilterPlaceholder>());
        map.put(Category.GENOME_COORDS, new ArrayList<FilterPlaceholder>());
        //map.put(Category.GENOTYPE, new ArrayList<FilterPlaceholder>());
        map.put(Category.VARIANT, new ArrayList<FilterPlaceholder>());
        map.put(Category.PLUGIN, new ArrayList<FilterPlaceholder>());

        //cohort filter
        if (!hasSubItem(CohortFilterView.FILTER_ID)) {
            map.get(Category.PATIENT).add(new SimpleFilterPlaceholder(CohortFilterView.class));
        }

        // GO filter
        if (!hasSubItem(OntologyType.GO.toString())) {
            map.get(Category.GENOME_COORDS).add(new OntologyFilterPlaceholder(OntologyType.GO));
        }

        // HPO filter
        if (!hasSubItem(OntologyType.HPO.toString())) {
            map.get(Category.GENOME_COORDS).add(new OntologyFilterPlaceholder(OntologyType.HPO));
        }

        // OMIM filter
        if (!hasSubItem(OntologyType.OMIM.toString())) {
            map.get(Category.GENOME_COORDS).add(new OntologyFilterPlaceholder(OntologyType.OMIM));
        }

        // Region list filter
        if (!hasSubItem(RegionSetFilterView.FILTER_ID)) {
            map.get(Category.GENOME_COORDS).add(new SimpleFilterPlaceholder(RegionSetFilterView.class));
        }

        //plugin filters
        PluginController pc = PluginController.getInstance();
        for (PluginDescriptor desc : pc.getDescriptors()) {
            final MedSavantPlugin p = pc.getPlugin(desc.getID());
            if (p instanceof MedSavantFilterPlugin && !hasSubItem(p.getDescriptor().getID())) {
                map.get(Category.PLUGIN).add(new FilterPlaceholder() {

                    @Override
                    public FilterView getFilterView() {
                        return PluginFilterView.getFilterView((MedSavantFilterPlugin) p, id);
                    }

                    @Override
                    public String getFilterID() {
                        return p.getDescriptor().getID();
                    }

                    @Override
                    public String getFilterName() {
                        return p.getTitle();
                    }
                });
            }
        }

        //add from variant table
        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for (AnnotationFormat af : afs) {
            for (CustomField field : af.getCustomFields()) {
                if (field.isFilterable() && !hasSubItem(field.getColumnName()) && isFilterable(field.getColumnType())) {
                    map.get(field.getCategory()).add(new FieldPlaceholder(field, WhichTable.VARIANT));
                }
            }
        }

        //add from patient table
        for (CustomField field : ProjectController.getInstance().getCurrentPatientFormat()) {
            if (field.isFilterable() && !hasSubItem(field.getColumnName()) && isFilterable(field.getColumnType())) {
                map.get(field.getCategory()).add(new FieldPlaceholder(field, WhichTable.PATIENT));
            }
        }

        //tag filter
        if (!hasSubItem(TagFilterView.FILTER_ID)) {
            System.out.println("Regularly creating Tag filter view");
            map.get(Category.VARIANT).add(new SimpleFilterPlaceholder(TagFilterView.class));
        }

        //starred variants
        /*if (!hasSubItem(StarredFilterView.FILTER_ID)) {
            map.get(Category.VARIANT).add(new SimpleFilterPlaceholder(StarredFilterView.class));
        }*/

        return map;
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


    private JButton getRemoveButton() {
        JButton removeButton = new JButton("Remove filter set");

        removeButton.setToolTipText("Remove all filters in set");
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                removeThis();
            }
        });

        return removeButton;
    }
    List<String> setFilters = new ArrayList<String>();

    Color defaultKeyColor = Color.gray;
    Color activeKeyColor = Color.red;

    private JPanel generateFilterList() {
        JPanel p = ViewUtil.getClearPanel();
        p.setLayout(new BorderLayout());

        CollapsiblePanes panes = new CollapsiblePanes();
        ViewUtil.applyVerticalBoxLayout(panes);
        panes.setBorder(null);

        p.add(panes, BorderLayout.CENTER);

        try {
            Map<Category, List<FilterPlaceholder>> map = getRemainingFilters();

            /*Category[] cats = new Category[map.size()];
            cats = map.keySet().toArray(cats);
            Arrays.sort(cats, new CategoryComparator());
            */

            Category[] cats = new Category[3];
            cats[0] = Category.PATIENT;
            cats[1] = Category.VARIANT;
            cats[2] = Category.GENOME_COORDS;

            for (Category c : cats) {

                if (map.get(c).isEmpty()) {
                    continue;
                }

                final boolean isLongRunningFilter = c == Category.GENOME_COORDS;

                CollapsiblePane cPanel = new CollapsiblePane(c.toString());
                cPanel.setCollapsed(true);
                cPanel.setOpaque(false);
                cPanel.setLayout(new BorderLayout());

                cPanel.setStyle(CollapsiblePane.PLAIN_STYLE);

                final KeyValuePairPanel kvp = new KeyValuePairPanel(2);
                cPanel.add(kvp, BorderLayout.CENTER);
                panes.add(cPanel, BorderLayout.CENTER);

                for (final FilterPlaceholder f : map.get(c)) {
                    kvp.addKey(f.getFilterName());

                    kvp.setKeyColour(f.getFilterName(), defaultKeyColor);

                    JLabel detailString = new JLabel("");
                    detailString.setForeground(Color.orange);

                    kvp.setValue(f.getFilterName(), detailString);

                    final JButton clearButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CLEAR));
                    clearButton.setVisible(false);
                    kvp.setAdditionalColumn(f.getFilterName(), 0, clearButton);



                    final JToggleButton editButton = ViewUtil.getTexturedToggleButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CONFIGURE));
                    editButton.addActionListener(new ActionListener() {

                        boolean acceptedBeingLong = false;

                        @Override
                        public void actionPerformed(ActionEvent ae) {

                            int result = DialogUtils.YES;
                            if (!acceptedBeingLong && editButton.isSelected() && isLongRunningFilter) {
                                result = DialogUtils.askYesNo("Warning", "This is a complex search condition that may take a long time to complete. Would you like to continue anyways?");
                                if (result == DialogUtils.YES) {
                                    acceptedBeingLong = true;
                                }
                            }

                            if (result == DialogUtils.YES) {

                                if (!setFilters.contains(f.getFilterName())) {
                                    try {
                                        kvp.setDetailComponent(f.getFilterName(), f.getFilterView());
                                    } catch (Exception e) {
                                        DialogUtils.displayException("Problem displaying filter", "Problem getting values for filter " + f.getFilterName(), e);
                                    }
                                }
                                setFilters.add(f.getFilterName());

                                kvp.toggleDetailVisibility(f.getFilterName());

                                kvp.invalidate();
                                kvp.repaint();
                            } else {
                                editButton.setSelected(false);
                            }

                        }
                    });
                    kvp.setAdditionalColumn(f.getFilterName(), 1, editButton);

                    clearButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                FilterController.removeFilter(f.getFilterID(), f.getFilterView().queryID);
                                f.getFilterView().resetView();
                                editButton.setSelected(false);
                                kvp.toggleDetailVisibility(f.getFilterName(),false);
                            } catch (Exception ex) {
                            }
                        }

                    });

                }



                FilterController.addFilterListener(new FiltersChangedListener() {

                    @Override
                    public void filtersChanged() throws SQLException, RemoteException {
                        Filter changedFilter = FilterController.getLastFilter();

                        if (kvp.containsKey(changedFilter.getName())) {
                            FilterController.FilterAction act = FilterController.getLastAction();

                            int index = 0;
                            Component c = ((JPanel) kvp.getAdditionalColumn(changedFilter.getName(), index)).getComponent(0);

                            if (act == FilterController.FilterAction.ADDED || act == FilterController.FilterAction.MODIFIED) {
                                kvp.setKeyColour(changedFilter.getName(), activeKeyColor);
                                c.setVisible(true);
                            } else if (act == FilterController.FilterAction.REMOVED) {
                                kvp.setKeyColour(changedFilter.getName(), defaultKeyColor);
                                c.setVisible(false);
                            }
                        }
                    }
                });

            }
        } catch (Exception e) {
        }

        return p;

    }

    private JComponent getFilterSet() {

        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setBorder(null);

        p1 = new CollapsiblePane("Search Conditions");
        p1.setBorder(null);
        p1.setStyle(CollapsiblePane.TREE_STYLE);
        p1.setCollapsible(false);

        ViewUtil.applyVerticalBoxLayout(p1.getContentPane());
        p1.add(generateFilterList());

        panes.add(p1);
        panes.addExpansion();

        p1.repaint();

        return panes;
    }

    /**
     * Use this to avoid creating all filters when generating list.
     */
    abstract class FilterPlaceholder {

        public abstract FilterView getFilterView() throws Exception;

        public abstract String getFilterID() throws NoSuchFieldException, IllegalAccessException;

        public abstract String getFilterName();
    }

    /**
     * Many filters follow the same pattern, with an id called "FILTER_ID", a
     * name called "FILTER_NAME", and a constructor which takes a single
     * <code>int queryID</code> parameter.
     */
    class SimpleFilterPlaceholder extends FilterPlaceholder {

        private final Class wrappedClass;

        SimpleFilterPlaceholder(Class clazz) {
            wrappedClass = clazz;
        }

        @Override
        public FilterView getFilterView() throws Exception {
            return (FilterView) wrappedClass.getDeclaredConstructor(int.class).newInstance(id);
        }

        @Override
        public String getFilterID() throws NoSuchFieldException, IllegalAccessException {
            return (String) wrappedClass.getField("FILTER_ID").get(null);
        }

        @Override
        public String getFilterName() {
            try {
                return (String) wrappedClass.getField("FILTER_NAME").get(null);
            } catch (Exception ex) {
                throw new IllegalArgumentException(ClientMiscUtils.getMessage(ex));
            }
        }
    }

    /**
     * A common placeholder which wraps the filter for a custom field in the
     * patient or variant tables.
     */
    public class FieldPlaceholder extends FilterPlaceholder {

        private FilterView filterView;
        private final CustomField field;
        private final WhichTable whichTable;

        FieldPlaceholder(CustomField field, WhichTable table) {
            this.field = field;
            this.whichTable = table;
        }


        @Override
        public FilterView getFilterView() {
            if (filterView == null) {
                try {
                    String tableName = whichTable == WhichTable.PATIENT ? ProjectController.getInstance().getCurrentPatientTableName() : ProjectController.getInstance().getCurrentVariantTableName();
                    if (field.getColumnName().equals(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER)) {
                        filterView = new StringListFilterView(whichTable, field.getColumnName(), id, field.getAlias());
                    } else {
                        switch (field.getColumnType()) {
                            case INTEGER:
                                filterView = new NumericFilterView(whichTable, field.getColumnName(), id, field.getAlias(), false);
                                break;
                            case FLOAT:
                            case DECIMAL:
                                filterView = new NumericFilterView(whichTable, field.getColumnName(), id, field.getAlias(), true);
                                break;
                            case BOOLEAN:
                                filterView = new BooleanFilterView(whichTable,field.getColumnName(), id, field.getAlias());
                                break;
                            case VARCHAR:
                            default:
                                filterView =  new StringListFilterView(whichTable, field.getColumnName(), id, field.getAlias());
                                break;
                        }
                    }
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error creating filter view: %s", ex);
                }
            }

            return filterView;
        }

        @Override
        public String getFilterID() {
            return field.getColumnName();
        }

        @Override
        public String getFilterName() {
            return field.getAlias();
        }
    }

    class OntologyFilterPlaceholder extends FilterPlaceholder {

        private final OntologyType ontology;

        OntologyFilterPlaceholder(OntologyType ont) {
            this.ontology = ont;
        }

        @Override
        public FilterView getFilterView() throws SQLException, RemoteException {
            return new OntologyFilterView(ontology, id);
        }

        @Override
        public String getFilterID() {
            return OntologyFilter.ontologyToFilterID(ontology);
        }

        @Override
        public String getFilterName() {
            return OntologyFilter.ontologyToTitle(ontology);
        }
    }

    public class CategoryComparator implements Comparator<Category> {

        @Override
        public int compare(Category o1, Category o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }

    class FilterComparator implements Comparator<FilterPlaceholder> {

        @Override
        public int compare(FilterPlaceholder o1, FilterPlaceholder o2) {
            return o1.getFilterName().toLowerCase().compareTo(o2.getFilterName().toLowerCase());
        }
    }
}
