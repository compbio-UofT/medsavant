/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.swing.RangeSlider;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author AndrewBrook
 */
public class NumericFilterView {
    
    public static FilterView createFilterView(String filterName, String patientDbCol) {
        return new FilterView(filterName, getContentPanel(filterName, patientDbCol));
    }
    
    private static Range getDefaultValues(String filterName, String patientDbCol) throws SQLException, NonFatalDatabaseException {      
        Range extremeValues = FilterCache.getDefaultValuesRange(filterName);
        if(extremeValues == null){
            extremeValues = QueryUtil.getExtremeValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getPatientTableSchema(),
                    MedSavantDatabase.getInstance().getPatientTableSchema().getDBColumn(patientDbCol));
        } 
        FilterCache.addDefaultValues(filterName, extremeValues);
        return extremeValues;
    }
    
    public static double getNumber(String s) {
        return Double.parseDouble(s);
    }
    
    private static JPanel getContentPanel(final String filterName, final String patientDbCol) {

        JPanel container = new JPanel();
        container.setBorder(ViewUtil.getMediumBorder());
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));


        try {
            Range extremeValues = getDefaultValues(filterName, patientDbCol);
            
            final RangeSlider rs = new com.jidesoft.swing.RangeSlider();

            final int min = (int) Math.floor(extremeValues.getMin());
            final int max = (int) Math.ceil(extremeValues.getMax());

            System.out.println("min and max " + min + " : " + max);
            
            rs.setMinimum(min);
            rs.setMaximum(max);

            rs.setMajorTickSpacing(5);
            rs.setMinorTickSpacing(1);

            rs.setLowValue(min);
            rs.setHighValue(max);

            JPanel rangeContainer = new JPanel();
            rangeContainer.setLayout(new BoxLayout(rangeContainer, BoxLayout.X_AXIS));

            final JTextField frombox = new JTextField(ViewUtil.numToString(min));
            final JTextField tobox = new JTextField(ViewUtil.numToString(max));

            final JLabel fromLabel = new JLabel(ViewUtil.numToString(min));
            final JLabel toLabel = new JLabel(ViewUtil.numToString(max));

            rangeContainer.add(fromLabel);
            rangeContainer.add(rs);
            rangeContainer.add(toLabel);

            container.add(frombox);
            container.add(tobox);
            container.add(rangeContainer);
            container.add(Box.createVerticalBox());

            rs.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    frombox.setText(ViewUtil.numToString(rs.getLowValue()));
                    tobox.setText(ViewUtil.numToString(rs.getHighValue()));
                }
            });

            tobox.addKeyListener(new KeyListener() {

                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_ENTER) {
                        try {
                            int num = (int) Math.ceil(getNumber(tobox.getText()));
                            rs.setHighValue(num);
                            tobox.setText(ViewUtil.numToString(num));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            tobox.requestFocus();
                        }
                    }
                }
            });

            frombox.addKeyListener(new KeyListener() {

                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_ENTER) {
                        try {
                            int num = (int) Math.floor(getNumber(frombox.getText()));
                            rs.setLowValue(num);
                            frombox.setText(ViewUtil.numToString(num));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            frombox.requestFocus();
                        }
                    }
                }
            });


            final JButton applyButton = new JButton("Apply");
            applyButton.setEnabled(false);

            applyButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    applyButton.setEnabled(false);

                    Range acceptableRange = new Range(rs.getLowValue(), rs.getHighValue());

                    if (min == acceptableRange.getMin() && max == acceptableRange.getMax()) {
                        FilterController.removeFilter(filterName);
                    } else {
                        Filter f = new QueryFilter() {

                            @Override
                            public Condition[] getConditions() {

                                List<String> individuals = null;
                                try {
                                    individuals = QueryUtil.getPatientsWithValuesInRange(patientDbCol, new Range(rs.getLowValue(),rs.getHighValue()));
                                } catch (NonFatalDatabaseException ex) {
                                    Logger.getLogger(NumericFilterView.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (SQLException ex) {
                                    Logger.getLogger(NumericFilterView.class.getName()).log(Level.SEVERE, null, ex);
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
                                return filterName;
                            }
                        };
                        System.out.println("Adding filter: " + f.getName());
                        FilterController.addFilter(f);
                    }
                }
            });

            rs.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    applyButton.setEnabled(true);
                }
            });

            JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
            selectAll.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    rs.setLowValue(min);
                    rs.setHighValue(max);
                }
            });

            JPanel bottomContainer = new JPanel();
            bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

            bottomContainer.add(selectAll);
            bottomContainer.add(Box.createHorizontalGlue());
            bottomContainer.add(applyButton);

            container.add(bottomContainer);

        } catch (SQLException ex) {
            Logger.getLogger(NumericFilterView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(NumericFilterView.class.getName()).log(Level.SEVERE, null, ex);
        }

        return container;

    }
}
