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
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;


/**
 *
 * @author AndrewBrook
 */
public class GenderFilterView {
 
    private static final String FILTER_NAME = "Gender";
    private static final String FILTER_ALL = "Male and Female";
    
    private static final String FILTER_MALE = "Male";
    private static final String FILTER_FEMALE = "Female";

    static FilterView getGenderFilterView() {
        return new FilterView(FILTER_NAME, getContentPanel());
    }
    
    private static JComponent getContentPanel() {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        final JComboBox b = new JComboBox();

        b.addItem(FILTER_ALL);
        b.addItem(FILTER_MALE);
        b.addItem(FILTER_FEMALE);

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final int gender;
                if (((String) b.getSelectedItem()).equals(FILTER_MALE)){
                    gender = 1;
                } else if (((String) b.getSelectedItem()).equals(FILTER_FEMALE)){
                    gender = 2;
                } else {
                    FilterController.removeFilter(FILTER_NAME);
                    return;
                }
                
                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {

                        try {

                            List<String> individuals = QueryUtil.getDNAIdsForGender(gender);


                            Condition[] results = new Condition[individuals.size()];
                            int i = 0;
                            for (String ind : individuals) {
                                results[i] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(VariantTableSchema.ALIAS_DNAID), ind);
                                i++;
                            }

                            return results;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public String getName() {
                        return FILTER_NAME;
                    }
                };
                System.out.println("Adding filter: " + f.getName());
                FilterController.addFilter(f);

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
            }
        });

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(true);
            }
        });



        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        p.add(b, BorderLayout.CENTER);
        p.add(bottomContainer, BorderLayout.SOUTH);

        return p;
    }
    
}
