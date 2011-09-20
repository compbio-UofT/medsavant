/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import com.jidesoft.swing.RangeSlider;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author AndrewBrook
 */
public class VariantNumericFilterView {
    
    public static FilterView createFilterView(final TableSchema table, final String columnAlias) throws SQLException, NonFatalDatabaseException {
        
        DbColumn col = table.getDBColumn(columnAlias);
        
        boolean isVariantTableSchema = table.getTable().getTableNameSQL().equals(VariantTableSchema.TABLE_NAME);
        
        Range extremeValues = null;

        if (isVariantTableSchema && columnAlias.equals(VariantTableSchema.ALIAS_POSITION)) {
            extremeValues = new Range(1,250000000);
        } else if (isVariantTableSchema && columnAlias.equals(VariantTableSchema.ALIAS_SB)) {
            extremeValues = new Range(-100,100);
        } else {
            extremeValues = FilterCache.getDefaultValuesRange(columnAlias);
            if(extremeValues == null){
                //System.out.println(columnAlias + " - retrieving");
                extremeValues = QueryUtil.getExtremeValuesForColumn(ConnectionController.connect(), table, col);
            } else {
                //System.out.println(columnAlias + " - found cache");
            }
            FilterCache.addDefaultValues(table.getTable().getTableNameSQL(), columnAlias, extremeValues);
        }

        if (isVariantTableSchema && columnAlias.equals(VariantTableSchema.ALIAS_DP)) {
            extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
        }

        JPanel container = new JPanel();
        container.setBorder(ViewUtil.getMediumBorder());
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        final RangeSlider rs = new com.jidesoft.swing.RangeSlider();

        final int min = (int) Math.floor(extremeValues.getMin());
        final int max = (int) Math.ceil(extremeValues.getMax());

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

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        rs.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {
                frombox.setText(ViewUtil.numToString(rs.getLowValue()));
                tobox.setText(ViewUtil.numToString(rs.getHighValue()));
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        frombox.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    try {
                        Range acceptableRange = new Range(getNumber(frombox.getText().replaceAll(",", "")), getNumber(tobox.getText().replaceAll(",", "")));
                        acceptableRange.bound(min, max, true);                     
                        frombox.setText(ViewUtil.numToString(acceptableRange.getMin()));
                        tobox.setText(ViewUtil.numToString(acceptableRange.getMax()));
                        rs.setLowValue((int)acceptableRange.getMin());
                        rs.setHighValue((int)acceptableRange.getMax());           
                        applyButton.setEnabled(true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        frombox.requestFocus();
                    }
                }
            }                
        });

        tobox.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    try {
                        Range acceptableRange = new Range(getNumber(frombox.getText().replaceAll(",", "")), getNumber(tobox.getText().replaceAll(",", "")));
                        acceptableRange.bound(min, max, false);                     
                        frombox.setText(ViewUtil.numToString(acceptableRange.getMin()));
                        tobox.setText(ViewUtil.numToString(acceptableRange.getMax()));
                        rs.setLowValue((int)acceptableRange.getMin());
                        rs.setHighValue((int)acceptableRange.getMax());      
                        applyButton.setEnabled(true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        frombox.requestFocus();
                    }
                }   
            }                   
        });

        applyButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {

            applyButton.setEnabled(false);

            Range acceptableRange = new Range(getNumber(frombox.getText().replaceAll(",", "")), getNumber(tobox.getText().replaceAll(",", "")));
            acceptableRange.bound(min, max, true);                     
            frombox.setText(ViewUtil.numToString(acceptableRange.getMin()));
            tobox.setText(ViewUtil.numToString(acceptableRange.getMax()));
            rs.setLowValue((int)acceptableRange.getMin());
            rs.setHighValue((int)acceptableRange.getMax());

            if (min == acceptableRange.getMin() && max == acceptableRange.getMax()) {
                FilterController.removeFilter(columnAlias);
            } else {
                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {
                        Condition[] results = new Condition[2];
                        //results[0] = BinaryCondition.greaterThan(table.getDBColumn(columnAlias), getNumber(frombox.getText().replaceAll(",", "")), true);
                        //results[1] = BinaryCondition.lessThan(table.getDBColumn(columnAlias), getNumber(tobox.getText().replaceAll(",", "")), true);
                        DbColumn tempCol = MedSavantDatabase.getInstance().getVariantTableSchema().createTempColumn(table.getDBColumn(columnAlias));
                        results[0] = BinaryCondition.greaterThan(tempCol, getNumber(frombox.getText().replaceAll(",", "")), true);
                        results[1] = BinaryCondition.lessThan(tempCol, getNumber(tobox.getText().replaceAll(",", "")), true);
                        
                        Condition[] resultsCombined = new Condition[1];
                        resultsCombined[0] = ComboCondition.and(results);

                        return resultsCombined;
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

        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rs.setLowValue(min);
                rs.setHighValue(max);
                frombox.setText(ViewUtil.numToString(min));
                tobox.setText(ViewUtil.numToString(max));
                applyButton.setEnabled(true);
            }
        });

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(selectAll);
        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        container.add(bottomContainer);

        return new FilterView(columnAlias, container);
    }
    
    public static double getNumber(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex){
            return 0;
        }
    }
    
}
