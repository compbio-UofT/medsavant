/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class GenderFilterView2 {

    private static final String FILTER_NAME = "Gender";
    private static final String FILTER_MALE = "Male";
    private static final String FILTER_FEMALE = "Female";

    static FilterView getGenderFilterView() {
        List<String> values = Arrays.asList(new String[]{FILTER_MALE, FILTER_FEMALE});
        
        return new FilterView(FILTER_NAME, getContentPanel(values));
    }

    private static void generateFilerFromAcceptableValues(final List<String> acceptableValues) {
        
        HashMap<String, Integer> optionToDbValueMap = new HashMap<String, Integer>();
        optionToDbValueMap.put(FILTER_MALE, 1);
        optionToDbValueMap.put(FILTER_FEMALE, 2);
        
        final List<Integer> acceptableDbValues = new ArrayList<Integer>();
        for (String accvall : acceptableValues) {
            acceptableDbValues.add(optionToDbValueMap.get(accvall));
        }
        
        Filter f = new QueryFilter() {

            @Override
            public Condition[] getConditions() {
                
                List<String> individuals = new ArrayList<String>();
                for (int gender : acceptableDbValues) {
                    try {
                        individuals.addAll(QueryUtil.getDNAIdsForGender(gender));
                    } catch (NonFatalDatabaseException ex) {
                    } catch (SQLException ex) {
                    }
                }
                
                Condition[] results = new Condition[individuals.size()];
                            int i = 0;
                            for (String ind : individuals) {
                                results[i] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(VariantTableSchema.ALIAS_DNAID), ind);
                                i++;
                            }

                return results;
            }

            @Override
            public String getName() {
                return FILTER_NAME;
            }
        };
        FilterController.addFilter(f);
    }

    private static JComponent getContentPanel(List<String> options) {

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
                    FilterController.removeFilter(FILTER_NAME);
                } else {
                    generateFilerFromAcceptableValues(acceptableValues);
                }
            }
        });

        for (String s : options) {
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

        return container;

    }
}
