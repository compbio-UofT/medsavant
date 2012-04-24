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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.MedSavantFilterPlugin;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.ColumnType;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabaseExtras;
import org.ut.biolab.medsavant.format.CustomField.Category;
import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class FilterPanelSub extends JPanel {

    private int id;
    private JPanel contentPanel;
    private List<FilterPanelSubItem> subItems = new ArrayList<FilterPanelSubItem>();
    private FilterPanel parent;
    private boolean isRemoved = false;
    private static Color BAR_COLOUR = Color.black;
    private static Color BUTTON_OVER_COLOUR = Color.gray;

    public FilterPanelSub(final FilterPanel parent, int id) {
        init(parent, id);
    }

    public FilterPanelSub(boolean isCollapsible, final FilterPanel parent, int id) {
        init(parent, id);
    }

    public final void init(final FilterPanel parent, int id) {

        this.setOpaque(false);

        this.id = id;
        this.parent = parent;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        contentPanel = ViewUtil.getClearPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                ViewUtil.getMediumBorder(),
                ViewUtil.getMediumTopHeavyBorder()
                ));

        this.add(contentPanel);

        refreshSubItems();

    }

    public List<FilterPanelSubItem> getSubItems() {
        return this.subItems;
    }

    public void refreshSubItems() {
        contentPanel.removeAll();

        //check for removed items
        for (int i = subItems.size() - 1; i >= 0; i--) {
            if (subItems.get(i).isRemoved()) {
                subItems.remove(i);
            }
        }

        //refresh panel
        for (int i = 0; i < subItems.size(); i++) {
            this.contentPanel.add(subItems.get(i));
            contentPanel.add(Box.createRigidArea(new Dimension(5, 5)));
        }

        final JButton addButton = ViewUtil.getSoftButton("Add filter condition");

        addButton.setToolTipText("Add a filter");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                Map<Category, List<FilterPlaceholder>> map = getRemainingFilters();

                final JPopupMenu p = new JPopupMenu();
                p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.lightGray), BorderFactory.createLineBorder(Color.white, 5)));
                //p.setBackground(Color.white);

                Category[] cats = new Category[map.size()];
                cats = map.keySet().toArray(cats);
                Arrays.sort(cats, new CategoryComparator());

                final Map<JPanel, List<Component>> menuMap = new HashMap<JPanel, List<Component>>();

                for (Category c : cats) {

                    final JPanel header = new JPanel();
                    header.setBackground(Color.white);
                    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
                    JLabel label = new JLabel(" " + CustomField.categoryToString(c));
                    header.add(label);
                    header.add(Box.createHorizontalGlue());
                    header.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    label.setFont(ViewUtil.getMediumTitleFont());
                    header.setPreferredSize(new Dimension(260, 20));
                    header.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            for (Object key : menuMap.keySet()) {
                                for (Component comp : menuMap.get(key)) {
                                    comp.setVisible(false);

                                }
                            }
                            for (Component comp : menuMap.get(header)) {
                                comp.setVisible(true);
                            }
                            p.validate();
                            p.pack();
                            p.repaint();
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            header.setBackground(new Color(0.9f, 0.9f, 0.9f));
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            header.setBackground(Color.white);
                        }
                    });
                    menuMap.put(header, new ArrayList<Component>());
                    p.add(header);

                    FilterPlaceholder[] filters = new FilterPlaceholder[map.get(c).size()];
                    filters = map.get(c).toArray(filters);
                    Arrays.sort(filters, new FilterComparator());

                    for (final FilterPlaceholder filter : filters) {

                        final JPanel item = new JPanel();
                        item.setPreferredSize(new Dimension(260, 20));
                        item.setBackground(Color.white);
                        item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
                        JLabel itemLabel = new JLabel("     " + filter.getFilterName());
                        item.add(itemLabel);
                        item.add(Box.createHorizontalGlue());
                        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        item.addMouseListener(new MouseAdapter() {

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                subItems.add(new FilterPanelSubItem(filter.getFilterView(), FilterPanelSub.this, filter.getFilterID()));
                                refreshSubItems();
                                p.setVisible(false);
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                item.setBackground(new Color(0.9f, 0.9f, 0.9f));
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                item.setBackground(Color.white);
                            }
                        });
                        menuMap.get(header).add(item);
                        item.setVisible(false);
                        p.add(item);
                    }

                    if (filters.length == 0) {
                        JPanel item = new JPanel();
                        item.setPreferredSize(new Dimension(150, 20));
                        item.setBackground(Color.white);
                        item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
                        JLabel empty = new JLabel("     (No filters)");
                        empty.setFont(ViewUtil.getSmallTitleFont());
                        item.setVisible(false);
                        item.add(empty);
                        item.add(Box.createHorizontalGlue());
                        p.add(item);
                        menuMap.get(header).add(item);
                    }
                }

                JPanel ppp = new JPanel();
                //ppp.setBackground(Color.white);
                ppp.setPreferredSize(new Dimension(1, 1));
                p.add(ppp);

                p.show(addButton, 0, 25);

            }

        });

        JButton removeButton = new JButton("Remove filter set");

        removeButton.setToolTipText("Remove all filters in set");
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                removeThis();
            }
        });

        JPanel tmp1 = ViewUtil.getClearPanel();//ViewUtil.getSecondaryBannerPanel();//ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(tmp1);

        tmp1.add(addButton);
        tmp1.add(Box.createHorizontalGlue());

        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(tmp1);

        this.updateUI();
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

    public FilterPanelSubItem addNewSubItem(FilterView view, String filterId) {
        FilterPanelSubItem result = new FilterPanelSubItem(view, this, filterId);
        subItems.add(result);
        refreshSubItems();
        return result;
    }

    public boolean hasSubItem(String filterId) {
        for (FilterPanelSubItem f : subItems) {
            if (f.getFilterId().equals(filterId)) {
                return true;
            }
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void removeFiltersById(String id) {
        //reversed to avoid concurrent mod exception
        for (int i = subItems.size() - 1; i >= 0; i--) {
            FilterPanelSubItem fpsi = subItems.get(i);
            if (fpsi.getFilterId().equals(id)) {
                fpsi.removeThis();
            }
        }
    }

    private Map<Category, List<FilterPlaceholder>> getRemainingFilters() {

        Map<Category, List<FilterPlaceholder>> map = new EnumMap<Category, List<FilterPlaceholder>>(Category.class);

        map.put(Category.PATIENT, new ArrayList<FilterPlaceholder>());
        map.put(Category.GENOME_COORDS, new ArrayList<FilterPlaceholder>());
        //map.put(Category.GENOTYPE, new ArrayList<FilterPlaceholder>());
        map.put(Category.VARIANT, new ArrayList<FilterPlaceholder>());
        map.put(Category.PLUGIN, new ArrayList<FilterPlaceholder>());

        //cohort filter
        if (!hasSubItem(CohortFilterView.FILTER_ID)) {
            map.get(Category.PATIENT).add(new FilterPlaceholder() {

                @Override
                public FilterView getFilterView() {
                    return CohortFilterView.getCohortFilterView(id);
                }

                @Override
                public String getFilterID() {
                    return CohortFilterView.FILTER_ID;
                }

                @Override
                public String getFilterName() {
                    return CohortFilterView.FILTER_NAME;
                }
            });
        }

        //GO filter
        if (!hasSubItem(GOFilterView.FILTER_ID)) {
            map.get(Category.GENOME_COORDS).add(new FilterPlaceholder() {

                @Override
                public FilterView getFilterView() {
                    return GOFilterView.getGOFilterView(id);
                }

                @Override
                public String getFilterID() {
                    return GOFilterView.FILTER_ID;
                }

                @Override
                public String getFilterName() {
                    return GOFilterView.FILTER_NAME;
                }
            });
        }

        //HPO filter
        try {
            if (MedSavantClient.PatientQueryUtilAdapter.hasOptionalField(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), MedSavantDatabaseExtras.OPTIONAL_PATIENT_FIELD_HPO)) {
                if (!hasSubItem(HPOFilterView.FILTER_ID)) {
                    map.get(Category.PATIENT).add(new FilterPlaceholder() {

                        @Override
                        public FilterView getFilterView() {
                            return HPOFilterView.getHPOFilterView(id);
                        }

                        @Override
                        public String getFilterID() {
                            return HPOFilterView.FILTER_ID;
                        }

                        @Override
                        public String getFilterName() {
                            return HPOFilterView.FILTER_NAME;
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //gene list filter
        if (!hasSubItem(GeneListFilterView.FILTER_ID)) {
            map.get(Category.GENOME_COORDS).add(new FilterPlaceholder() {

                @Override
                public FilterView getFilterView() {
                    return GeneListFilterView.getFilterView(id);
                }

                @Override
                public String getFilterID() {
                    return GeneListFilterView.FILTER_ID;
                }

                @Override
                public String getFilterName() {
                    return GeneListFilterView.FILTER_NAME;
                }
            });
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
            for (final CustomField field : af.getCustomFields()) {
                if (field.isFilterable() && !hasSubItem(field.getColumnName()) && isFilterable(field.getColumnType())) {

                    map.get(field.getCategory()).add(new FilterPlaceholder() {

                        @Override
                        public FilterView getFilterView() {
                            try {
                                switch (field.getColumnType()) {
                                    case INTEGER:
                                        return NumericFilterView.createVariantFilterView(ProjectController.getInstance().getCurrentVariantTableName(), field.getColumnName(), id, field.getAlias(), false);
                                    case FLOAT:
                                    case DECIMAL:
                                        return NumericFilterView.createVariantFilterView(ProjectController.getInstance().getCurrentVariantTableName(), field.getColumnName(), id, field.getAlias(), true);
                                    case BOOLEAN:
                                        return BooleanFilterView.createVariantFilterView(field.getColumnName(), id, field.getAlias());
                                    case VARCHAR:
                                    default:
                                        return StringListFilterView.createVariantFilterView(ProjectController.getInstance().getCurrentVariantTableName(), field.getColumnName(), id, field.getAlias());
                                }
                            } catch (SQLException e) {
                                ClientMiscUtils.checkSQLException(e);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public String getFilterID() {
                            return field.getColumnName();
                        }

                        @Override
                        public String getFilterName() {
                            return field.getAlias();
                        }
                    });
                }
            }
        }

        //add from patient table
        for (final CustomField field : ProjectController.getInstance().getCurrentPatientFormat()) {
            if (field.isFilterable() && !hasSubItem(field.getColumnName()) && isFilterable(field.getColumnType())) {
                map.get(field.getCategory()).add(new FilterPlaceholder() {

                    @Override
                    public FilterView getFilterView() {
                        try {

                            //special cases:
                            if (field.getColumnName().equals(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER)) {
                                return StringListFilterView.createPatientFilterView(ProjectController.getInstance().getCurrentPatientTableName(), field.getColumnName(), id, field.getAlias());
                            }

                            switch (field.getColumnType()) {
                                case INTEGER:
                                    return NumericFilterView.createPatientFilterView(ProjectController.getInstance().getCurrentPatientTableName(), field.getColumnName(), id, field.getAlias(), false);
                                case FLOAT:
                                case DECIMAL:
                                    return NumericFilterView.createPatientFilterView(ProjectController.getInstance().getCurrentPatientTableName(), field.getColumnName(), id, field.getAlias(), true);
                                case BOOLEAN:
                                    return BooleanFilterView.createPatientFilterView(field.getColumnName(), id, field.getAlias());
                                case VARCHAR:
                                default:
                                    return StringListFilterView.createPatientFilterView(ProjectController.getInstance().getCurrentPatientTableName(), field.getColumnName(), id, field.getAlias());
                            }
                        } catch (SQLException e) {
                            ClientMiscUtils.checkSQLException(e);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public String getFilterID() {
                        return field.getColumnName();
                    }

                    @Override
                    public String getFilterName() {
                        return field.getAlias();
                    }
                });
            }
        }

        //tag filter
        if (!hasSubItem(TagFilterView.FILTER_ID)) {
            map.get(Category.VARIANT).add(new FilterPlaceholder() {

                @Override
                public FilterView getFilterView() {
                    return TagFilterView.getTagFilterView(id);
                }

                @Override
                public String getFilterID() {
                    return TagFilterView.FILTER_ID;
                }

                @Override
                public String getFilterName() {
                    return TagFilterView.FILTER_NAME;
                }
            });
        }

        //starred variants
        if (!hasSubItem(StarredFilterView.FILTER_ID)) {
            map.get(Category.VARIANT).add(new FilterPlaceholder() {

                @Override
                public FilterView getFilterView() {
                    return StarredFilterView.getStarredFilterView(id);
                }

                @Override
                public String getFilterID() {
                    return StarredFilterView.FILTER_ID;
                }

                @Override
                public String getFilterName() {
                    return StarredFilterView.FILTER_NAME;
                }
            });
        }

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

    /*
     * Use this to prevent creating all filters when generating list
     */
    abstract class FilterPlaceholder {

        public abstract FilterView getFilterView();

        public abstract String getFilterID();

        public abstract String getFilterName();
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
