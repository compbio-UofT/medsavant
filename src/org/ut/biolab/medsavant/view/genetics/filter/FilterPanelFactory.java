/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class FilterPanelFactory {

    
    public static FilterView getCategoricalFilterView(final String name, List<String> options, final DbColumn column) {
        HashMap<String,String> map = new HashMap<String,String>();
        for(String s : options) {
            map.put(s, s);
        }
        return getCategoricalFilterView(name,options,map,column);
    }
    
    public static FilterView getCategoricalFilterView(final String name, List<String> options, Map<String,String> optionsToDbValueMap, final DbColumn column) {


        long start = System.currentTimeMillis();

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
                    FilterController.removeFilter(name, 0); //TODO
                } else {
                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            Condition[] results = new Condition[acceptableValues.size()];
                            int i = 0;
                            for (String s : acceptableValues) {
                                results[i++] = BinaryCondition.equalTo(column, s);
                            }
                            return results;
                        }

                        @Override
                        public String getName() {
                            return name;
                        }
                        
                        
                        @Override
                        public String getId() {
                            return name;//TODO
                        }
                    };
                    //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                    FilterController.addFilter(f, 0); //TODO
                }

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
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

        long elapsedTimeMillis = System.currentTimeMillis() - start;

        // Get elapsed time in seconds
        float elapsedTimeSec = elapsedTimeMillis / 1000F;

        return new FilterView(name, container);
    }
}