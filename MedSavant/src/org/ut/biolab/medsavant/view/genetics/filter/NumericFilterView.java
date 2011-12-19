/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.RangeCondition;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.Table;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class NumericFilterView extends FilterView{
   
    
    /* Convenience Functions */
    
    public static FilterView createVariantFilterView(String tablename, String columnname, int queryId, String alias, boolean isDecimal) throws SQLException, NonFatalDatabaseException {
        return new NumericFilterView(new JPanel(), tablename, columnname, queryId, alias, isDecimal, Table.VARIANT);
    }
    
    public static FilterView createPatientFilterView(String tablename, String columnname, int queryId, String alias, boolean isDecimal) throws SQLException, NonFatalDatabaseException {
        return new NumericFilterView(new JPanel(), tablename, columnname, queryId, alias, isDecimal, Table.PATIENT);
    }
    
    public NumericFilterView(String tablename, String columnname, int queryId, String alias, boolean isDecimal, Table whichTable) throws SQLException{
        this(new JPanel(), tablename, columnname, queryId, alias, isDecimal, whichTable);
    }
    
    public NumericFilterView(FilterState state, int queryId) throws SQLException {
        this(new JPanel(), FilterUtils.getTableName(Table.valueOf(state.getValues().get("table"))), state.getId(), queryId, state.getName(), Boolean.valueOf(state.getValues().get("isDecimal")), Table.valueOf(state.getValues().get("table")));
        String minString = state.getValues().get("min");
        String maxString = state.getValues().get("max");
        if(minString != null && maxString != null){
            applyFilter(Double.parseDouble(minString), Double.parseDouble(maxString));
        }
    }

    
    /* NumericFilterView */
    
    private JTextField frombox;
    private JTextField tobox;
    private DecimalRangeSlider rs;
    private JButton applyButton;
    private String columnname;
    private String alias;
    private Table whichTable;
    private boolean isDecimal;
    private Double[] appliedValues;
            
    public void applyFilter(int low, int high) {
        applyFilter((double)low, (double)high);
    }
    
    /*     
     * Allows filter to be applied without interaction.
     * Assumes values already checked for consistency.
     */
    public void applyFilter(double low, double high){
        frombox.setText(Double.toString(low));
        tobox.setText(Double.toString(high));
        rs.setLow(low);
        rs.setHigh(high);
        applyButton.doClick();
    }
    
    private NumericFilterView(JComponent container, String tablename, final String columnname, int queryId, final String alias, final boolean isDecimal, final Table whichTable) throws SQLException{
        super(alias, container, queryId);
        
        this.columnname = columnname;
        this.alias = alias;
        this.whichTable = whichTable;
        this.isDecimal = isDecimal;
        
        Range extremeValues = null;

        if (columnname.equals("position")) {
            extremeValues = new Range(1,250000000);
        } else if (columnname.equals("sb")) {
            extremeValues = new Range(-100,100);
        } else {
            extremeValues = new Range(VariantQueryUtil.getExtremeValuesForColumn(tablename, columnname));
        }

        if (columnname.equals("dp")) {
            extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
        }

        container.setBorder(ViewUtil.getMediumBorder());
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        final int min = (int) Math.floor(extremeValues.getMin());
        final int max = (int) Math.ceil(extremeValues.getMax());
        
        int precision = 0;
        if(isDecimal && max-min<=1){
            precision = 2;
        } else if (isDecimal && max-min<=10){
            precision = 1;
        }
        //final DecimalRangeSlider rs = new DecimalRangeSlider(precision);
        rs = new DecimalRangeSlider(precision);
        
        rs.setMinimum(min);
        rs.setMaximum(max);

        rs.setMajorTickSpacing(5);
        rs.setMinorTickSpacing(1);

        rs.setLow(min);
        rs.setHigh(max);

        JPanel rangeContainer = new JPanel();
        rangeContainer.setLayout(new BoxLayout(rangeContainer, BoxLayout.X_AXIS));

        frombox = new JTextField(ViewUtil.numToString(min));
        tobox = new JTextField(ViewUtil.numToString(max));
        frombox.setMaximumSize(new Dimension(10000,24));
        tobox.setMaximumSize(new Dimension(10000,24));

        final JLabel fromLabel = new JLabel(ViewUtil.numToString(min));
        final JLabel toLabel = new JLabel(ViewUtil.numToString(max));

        rangeContainer.add(fromLabel);
        rangeContainer.add(rs);
        rangeContainer.add(toLabel);

        container.add(frombox);
        container.add(tobox);
        container.add(rangeContainer);
        container.add(Box.createVerticalBox());

        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        rs.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {
                frombox.setText(ViewUtil.numToString(rs.getLow()));
                tobox.setText(ViewUtil.numToString(rs.getHigh()));
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
                        rs.setLow(acceptableRange.getMin());
                        rs.setHigh(acceptableRange.getMax());           
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
                        rs.setLow(acceptableRange.getMin());
                        rs.setHigh(acceptableRange.getMax());      
                        applyButton.setEnabled(true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        frombox.requestFocus();
                    }
                }   
            }                   
        });

        //
                
        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final Range acceptableRange = new Range(getNumber(frombox.getText().replaceAll(",", "")), getNumber(tobox.getText().replaceAll(",", "")));
                acceptableRange.bound(min, max, true);                     
                frombox.setText(ViewUtil.numToString(acceptableRange.getMin()));
                tobox.setText(ViewUtil.numToString(acceptableRange.getMax()));
                rs.setLow(acceptableRange.getMin());
                rs.setHigh(acceptableRange.getMax());
                appliedValues = new Double[]{acceptableRange.getMin(),acceptableRange.getMax()};

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {
                        if(whichTable == Table.VARIANT){
                            Condition[] results = new Condition[1];
                            double min = acceptableRange.getMin();//getNumber(frombox.getText().replaceAll(",", ""));
                            double max = acceptableRange.getMax();//getNumber(tobox.getText().replaceAll(",", ""));
                            if(isDecimal){
                                results[0] = new RangeCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnname), min, max);
                            } else {
                                results[0] = new RangeCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(columnname), (int)Math.floor(min), (int)Math.ceil(max));
                            }
                            return results;
                        } else if (whichTable == Table.PATIENT){
                            try {
                                List<String> individuals = PatientQueryUtil.getDNAIdsWithValuesInRange(ProjectController.getInstance().getCurrentProjectId(), columnname, acceptableRange);

                                Condition[] results = new Condition[individuals.size()];
                                int i = 0; 
                                for(String ind : individuals){
                                    results[i++] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), ind);
                                }
                                return results;

                            } catch (NonFatalDatabaseException ex) {
                                Logger.getLogger(NumericFilterView.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SQLException ex) {
                                MiscUtils.checkSQLException(ex);
                                Logger.getLogger(NumericFilterView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        return new Condition[0];
                    }

                    @Override
                    public String getName() {
                        return alias;
                    }

                    @Override
                    public String getId() {
                        return columnname;
                    }
                };
                FilterController.addFilter(f, getQueryId());
            }
        };
        applyButton.addActionListener(al);

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
    }
    
    public static double getNumber(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex){
            return 0;
        }
    }

    @Override
    public FilterState saveState() {        
        Map<String, String> map = new HashMap<String, String>();
        map.put("table", whichTable.toString());
        map.put("isDecimal", Boolean.toString(isDecimal));
        if(appliedValues != null){
            map.put("min", Double.toString(appliedValues[0]));
            map.put("max", Double.toString(appliedValues[1]));
        }
        return new FilterState(FilterType.NUMERIC, alias, columnname, map);
    }

}
