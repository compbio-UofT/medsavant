/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.swing.RangeSlider;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import medsavant.db.BasicQuery;
import medsavant.db.ConnectionController;
import medsavant.db.Database;
import medsavant.db.table.TableSchema;
import medsavant.db.table.VariantTableSchema;
import medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class FilterPanel extends JPanel {

    private final ArrayList<FilterView> filterViews;
    private CollapsiblePanes contentPanel;

    public FilterPanel() {
        this.setLayout(new BorderLayout());
        filterViews = new ArrayList<FilterView>();
        initGUI();
    }
    

    private void initGUI() {

        contentPanel = new CollapsiblePanes();
        contentPanel.setBackground(ViewUtil.getMenuColor());
        this.add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        List<FilterView> fv;
        try {
            fv = getFilterViews();
            addFilterViews(fv);
        } catch (SQLException ex) {
            throw new FatalDatabaseException("Problem getting filters");
        }

        contentPanel.addExpansion();
    }

    public void addFilterViews(List<FilterView> filterViews) {
        for (FilterView view : filterViews) {
            addFilterView(view);
        }
    }

    private void addFilterView(FilterView view) {
        filterViews.add(view);
        CollapsiblePane cp = new CollapsiblePane(view.getTitle());
        try {
            cp.setCollapsed(true);
        } catch (PropertyVetoException ex) {
        }
        cp.setCollapsedPercentage(0);
        cp.setContentPane(view.getComponent());
        this.contentPanel.add(cp);
    }

    private List<FilterView> getFilterViews() throws SQLException {
        List<FilterView> views = new ArrayList<FilterView>();
        views.addAll(getVariantRecordFilterViews());
        views.add(GOFilter.getGOntologyFilterView()); 
        views.add(HPOFilter.getHPOntologyFilterView()); 
        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());
        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());

        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());

        return views;
    }
    
    

    void listenToComponent(final JCheckBox c) {
        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }
        });
    }

    private List<FilterView> getVariantRecordFilterViews() throws SQLException {
        List<FilterView> l = new ArrayList<FilterView>();

        System.out.println("Making filters");

        List<String> fieldNames = VariantRecordModel.getFieldNames();
        int numFields = fieldNames.size();

        for (int i = 0; i < numFields; i++) {

            final int fieldNum = i;
            Class c = VariantRecordModel.getFieldClass(i);

            final String columnAlias = fieldNames.get(i);

            if (columnAlias.equals(VariantTableSchema.ALIAS_ID) || columnAlias.equals(VariantTableSchema.ALIAS_INFORMATION) || columnAlias.equals(VariantTableSchema.ALIAS_FILTER)) {
                continue;
            }

            TableSchema table = Database.getInstance().getVariantTableSchema();
            DbColumn col = table.getDBColumn(columnAlias);
            boolean isNumeric = TableSchema.isNumeric(table.getColumnType(col));

            if (isNumeric) {
                Range extremeValues = BasicQuery.getExtremeValuesForColumn(ConnectionController.connect(), table, col);

                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                RangeSlider rs = new com.jidesoft.swing.RangeSlider();

                rs.setMinimum((int) Math.floor(extremeValues.getMin()));
                rs.setMaximum((int) Math.ceil(extremeValues.getMax()));

                rs.setMajorTickSpacing(5);
                rs.setMinorTickSpacing(1);

                rs.setLowValue((int) Math.floor(extremeValues.getMin()));
                rs.setHighValue((int) Math.ceil(extremeValues.getMax()));

                container.add(rs);
                container.add(Box.createVerticalBox());

                final JButton applyButton = new JButton("Apply");
                applyButton.setEnabled(false);
                final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

                applyButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        applyButton.setEnabled(false);

                        final List<String> acceptableValues = new ArrayList<String>();
                        for (JCheckBox b : boxes) {
                            if (b.isSelected()) {
                                acceptableValues.add(b.getText());
                            }
                        }

                        if (acceptableValues.size() == boxes.size()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[acceptableValues.size()];
                                    int i = 0;
                                    for (String s : acceptableValues) {
                                        results[i++] = BinaryCondition.equalTo(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
                                    }
                                    return results;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
                                }
                            };
                            //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                            System.out.println("Adding filter: " + f.getName());
                            FilterController.addFilter(f);
                        }

                        //TODO: why does this not work? Freezes GUI
                        //apply.setEnabled(false);
                    }
                });

                rs.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        applyButton.setEnabled(true);
                    }

                });

                container.add(applyButton);

                final FilterView fv = new FilterView(columnAlias,container);
                l.add(fv);
            } else {

                List<String> uniq = BasicQuery.getDistinctValuesForColumn(ConnectionController.connect(), table, col);

                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                JPanel bottomContainer = new JPanel();
                bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

                final JButton applyButton = new JButton("Apply");
                applyButton.setEnabled(false);
                final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

                applyButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        applyButton.setEnabled(false);

                        final List<String> acceptableValues = new ArrayList<String>();
                        for (JCheckBox b : boxes) {
                            if (b.isSelected()) {
                                acceptableValues.add(b.getText());
                            }
                        }

                        if (acceptableValues.size() == boxes.size()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[acceptableValues.size()];
                                    int i = 0;
                                    for (String s : acceptableValues) {
                                        results[i++] = BinaryCondition.equalTo(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
                                    }
                                    return results;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
                                }
                            };
                            //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                            System.out.println("Adding filter: " + f.getName());
                            FilterController.addFilter(f);
                        }

                        //TODO: why does this not work? Freezes GUI
                        //apply.setEnabled(false);
                    }
                });

                for (String s : uniq) {
                    JCheckBox b = new JCheckBox(s);
                    b.setSelected(true);
                    b.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            AbstractButton abstractButton =
                                    (AbstractButton) e.getSource();
                            ButtonModel buttonModel = abstractButton.getModel();
                            boolean pressed = buttonModel.isPressed();
                            if (pressed) {
                                applyButton.setEnabled(true);
                            }
                            //System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
                        }
                    });
                    b.setAlignmentX(0F);
                    container.add(b);
                    boxes.add(b);
                }

                JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
                selectAll.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (JCheckBox c : boxes) {
                            c.setSelected(true);
                            applyButton.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectAll);

                JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

                selectNone.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (JCheckBox c : boxes) {
                            c.setSelected(false);
                            applyButton.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectNone);

                bottomContainer.add(Box.createGlue());

                bottomContainer.add(applyButton);

                bottomContainer.setAlignmentX(0F);
                container.add(bottomContainer);

                FilterView fv = new FilterView(columnAlias, container);
                l.add(fv);
            }
        }

        return l;
    }

    /*
    private Set<String> getUniqueValuesOfVariantRecordsAtField(int i) {
    Set<String> result = new TreeSet<String>();

    /**
     * TODO: this should query the database
     *
    List<VariantRecord> records;
    try {
    records = ResultController.getInstance().getFilteredVariantRecords();
    } catch (Exception ex) {
    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
    DialogUtil.displayErrorMessage("Problem getting data.", ex);
    return null;
    }

    for (VariantRecord r : records) {
    Object o = VariantRecordModel.getValueOfFieldAtIndex(i, r);
    if (o == null) {
    result.add("<none>");
    } else {
    result.add(o.toString());
    }
    }

    return result;
    }
     * 
     */
}
