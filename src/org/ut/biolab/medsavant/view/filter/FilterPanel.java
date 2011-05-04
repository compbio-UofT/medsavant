/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import fiume.vcf.VariantRecord;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
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

        List<FilterView> fv = getPatientFilterViews();
        addFilterViews(fv);

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
        //cp.setContentAreaFilled(false);
        cp.setCollapsedPercentage(0);
        cp.setContentPane(view.getComponent());
        this.contentPanel.add(cp);
    }

    private List<FilterView> getPatientFilterViews() {
        List<FilterView> views = new ArrayList<FilterView>();

        for (FilterView v : getVariantRecordFilterViews()) {
            views.add(v);
        }
       // views.add(getGenderFilterView());
       // views.add(getAgeFilterView());

        return views;
    }

    /*
    private FilterView getGenderFilterView() {
        String title = "Gender";

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(new JLabel("Patients are:"));
        container.add(new JCheckBox("Male"));
        final JCheckBox buttonFemale = new JCheckBox("Female");
        container.add(buttonFemale);
        listenToComponent(buttonFemale);

        FilterGenerator fg = new FilterGenerator() {

            public Filter generateFilter() {
                QueryFilter qf = new QueryFilter() {

                    @Override
                    public List<Condition> getConditions() {
                        List<Condition> c = new ArrayList<Condition>();
                        String value = "male";
                        if (buttonFemale.isSelected()) {
                            value = "female";
                        }
                        c.add(BinaryCondition.equalTo(DB.getInstance().patientTable.getColumn(PatientTable.COL_GENDER), value));
                        return c;
                    }
                };
                return qf;
            }
        };

        return new FilterView(title, container, fg);
    }

    private FilterView getAgeFilterView() {
        String title = "Age";

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(new JLabel("Patients are:"));
        container.add(new JCheckBox("10-20"));
        container.add(new JCheckBox("20-30"));
        container.add(new JCheckBox("old"));

        return new FilterView(title, container, null);
    }
     * 
     */

    void listenToComponent(final JCheckBox c) {
        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }
        });
    }

    private List<FilterView> getVariantRecordFilterViews() {
        List<FilterView> l = new ArrayList<FilterView>();

        List<String> fieldNames = VariantRecordModel.getFieldNames();
        int numFields = fieldNames.size();

        for (int i = 0; i < numFields; i++) {
            final int fieldNum = i;
            Class c = VariantRecordModel.getFieldClass(i);
            if (Util.isQuantatitiveClass(c)) {
                continue;
            } else {
                final String title = fieldNames.get(i);
               

                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                JPanel bottomContainer = new JPanel();
                bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

                Set<String> uniq = getUniqueValuesOfVariantRecordsAtField(i);

                final JButton apply = new JButton("Apply");
                apply.setEnabled(false);
                final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

                apply.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        apply.setEnabled(false);

                        List<String> acceptableValues = new ArrayList<String>();
                                for (JCheckBox b : boxes) {
                                    if (b.isSelected()) {
                                        acceptableValues.add(b.getText());
                                    }
                                }

                        if (acceptableValues.size() == boxes.size()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new VariantRecordFilter(acceptableValues,fieldNum);
                            System.out.println("Adding filter: " + f);
                            FilterController.addFilter(f);
                        }
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
                            if (pressed) { apply.setEnabled(true); }
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
                            apply.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectAll);

                JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

                selectNone.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (JCheckBox c : boxes) {
                            c.setSelected(false);
                            apply.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectNone);

                bottomContainer.add(Box.createGlue());

                bottomContainer.add(apply);

                bottomContainer.setAlignmentX(0F);
                container.add(bottomContainer);

                final FilterView fv = new FilterView(title, container);
                l.add(fv);
            }
        }

        return l;
    }

    private Set<String> getUniqueValuesOfVariantRecordsAtField(int i) {
        Set<String> result = new TreeSet<String>();

        List<VariantRecord> records = ResultController.getInstance().getAllVariantRecords();
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
}
