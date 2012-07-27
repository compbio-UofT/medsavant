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

import java.awt.Dimension;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.RangeCondition;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.DecimalRangeSlider;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils.WhichTable;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class NumericFilterView extends FilterView {

    private final String columnName;
    private final String alias;
    private final WhichTable whichTable;
    private final boolean isDecimal;
    private Range appliedRange;
    private int overallMin, overallMax;

    private JTextField fromBox;
    private JTextField toBox;
    private DecimalRangeSlider slider;
    private JButton applyButton;

    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ENTER) {
                Range acceptableRange = new Range(getNumber(fromBox.getText().replaceAll(",", "")), getNumber(toBox.getText().replaceAll(",", "")));
                acceptableRange.bound(overallMin, overallMax, false);
                fromBox.setText(ViewUtil.numToString(acceptableRange.getMin()));
                toBox.setText(ViewUtil.numToString(acceptableRange.getMax()));
                slider.setLow(acceptableRange.getMin());
                slider.setHigh(acceptableRange.getMax());
                applyButton.setEnabled(true);
            }
        }
    };
    private JButton selectAll;

    public NumericFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(WhichTable.valueOf(state.getValues().get("table")), state.getID(), queryID, state.getName(), Boolean.valueOf(state.getValues().get("isDecimal")));
        String minString = state.getValues().get("min");
        String maxString = state.getValues().get("max");
        if (minString != null && maxString != null) {
            applyFilter(Double.parseDouble(minString), Double.parseDouble(maxString));
        }
    }

    public NumericFilterView(WhichTable t, String col, int queryID, String alias, boolean isDecimal) throws SQLException, RemoteException {
        super(alias, queryID);

        System.out.println("Creating filter for " + col);

        this.whichTable = t;
        this.columnName = col;
        this.alias = alias;
        this.isDecimal = isDecimal;

        if (col.equals("position")) {
            initHelper(new Range(1, 250000000));
        } else if (col.equals("sb")) {
            initHelper(new Range(-100, 100));
        } else {
            new IndeterminateProgressDialog("Generating List", "<html>Determining extreme values for field.<br>This may take a few minutes the first time.</html>") {
                @Override
                public void run() {
                    try {
                        initHelper(new Range(MedSavantClient.VariantManager.getExtremeValuesForColumn(LoginController.sessionId, whichTable.getName(), columnName)));
                    } catch (Throwable ex) {
                        ClientMiscUtils.reportError(String.format("Error getting extreme values for %s.%s: %%s", whichTable, columnName), ex);
                    }
                }
            }.setVisible(true);
        }
    }

    private void initHelper(Range extremeValues) {

        if (columnName.equals("dp")) {
            extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
        }

        setBorder(ViewUtil.getMediumBorder());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        overallMin = (int)Math.floor(extremeValues.getMin());
        overallMax = (int)Math.ceil(extremeValues.getMax());

        int precision = 0;
        if (isDecimal && overallMax - overallMin <= 1) {
            precision = 2;
        } else if (isDecimal && overallMax - overallMin <= 10) {
            precision = 1;
        }
        //final DecimalRangeSlider rs = new DecimalRangeSlider(precision);
        slider = new DecimalRangeSlider(precision);

        slider.setMinimum(overallMin);
        slider.setMaximum(overallMax);

        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);

        slider.setLow(overallMin);
        slider.setHigh(overallMax);

        fromBox = new JTextField(ViewUtil.numToString(overallMin));
        toBox = new JTextField(ViewUtil.numToString(overallMax));
        fromBox.setMaximumSize(new Dimension(10000,24));
        toBox.setMaximumSize(new Dimension(10000,24));

        JLabel fromLabel = new JLabel(ViewUtil.numToString(overallMin));
        JLabel toLabel = new JLabel(ViewUtil.numToString(overallMax));

        JPanel fromToContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(fromToContainer);
        fromToContainer.add(fromBox);
        fromToContainer.add(new JLabel(" - "));
        fromToContainer.add(toBox);

        JPanel minMaxContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(minMaxContainer);
        minMaxContainer.add(fromLabel);
        minMaxContainer.add(slider);
        minMaxContainer.add(toLabel);

        add(fromToContainer);
        //container.add(rangeContainer);
        add(minMaxContainer);
        add(Box.createVerticalBox());

        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fromBox.setText(ViewUtil.numToString(slider.getLow()));
                toBox.setText(ViewUtil.numToString(slider.getHigh()));
            }
        });

        fromBox.addKeyListener(keyListener);
        toBox.addKeyListener(keyListener);

        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                appliedRange = new Range(getNumber(fromBox.getText().replaceAll(",", "")), getNumber(toBox.getText().replaceAll(",", "")));
                appliedRange.bound(overallMin, overallMax, true);
                fromBox.setText(ViewUtil.numToString(appliedRange.getMin()));
                toBox.setText(ViewUtil.numToString(appliedRange.getMax()));
                slider.setLow(appliedRange.getMin());
                slider.setHigh(appliedRange.getMax());

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() throws SQLException, RemoteException {
                        TableSchema variantSchema = ProjectController.getInstance().getCurrentVariantTableSchema();
                        if (whichTable == WhichTable.VARIANT) {
                            double min = appliedRange.getMin();
                            double max = appliedRange.getMax();
                            if (isDecimal) {
                                return new Condition[] { new RangeCondition(variantSchema.getDBColumn(columnName), min, max) };
                            } else {
                                return new Condition[] { new RangeCondition(variantSchema.getDBColumn(columnName), (int)Math.floor(min), (int)Math.ceil(max)) };
                            }
                        } else {
                            return getDNAIDCondition(MedSavantClient.PatientManager.getDNAIDsWithValuesInRange(
                                    LoginController.sessionId,
                                    ProjectController.getInstance().getCurrentProjectID(),
                                    columnName,
                                    appliedRange));
                        }
                    }

                    @Override
                    public String getName() {
                        return alias;
                    }

                    @Override
                    public String getID() {
                        return columnName;
                    }
                };
                FilterController.addFilter(f, getQueryID());
            }
        };
        applyButton.addActionListener(al);

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                applyButton.setEnabled(true);
            }
        });

        selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slider.setLowValue(overallMin);
                slider.setHighValue(overallMax);
                fromBox.setText(ViewUtil.numToString(overallMin));
                toBox.setText(ViewUtil.numToString(overallMax));
                applyButton.setEnabled(true);
            }
        });

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(selectAll);
        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        add(bottomContainer);
    }

    public void applyFilter(int low, int high) {
        applyFilter((double)low, (double)high);
    }

    /**
     * Allows filter to be applied without interaction.
     * Assumes values already checked for consistency.
     */
    public final void applyFilter(double low, double high) {
        fromBox.setText(Double.toString(low));
        toBox.setText(Double.toString(high));
        slider.setLow(low);
        slider.setHigh(high);
        applyButton.doClick();
    }

    private static double getNumber(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("table", whichTable.toString());
        map.put("isDecimal", Boolean.toString(isDecimal));
        if (appliedRange != null) {
            map.put("min", Double.toString(appliedRange.getMin()));
            map.put("max", Double.toString(appliedRange.getMax()));
        }
        return new FilterState(Filter.Type.NUMERIC, alias, columnName, map);
    }
}
