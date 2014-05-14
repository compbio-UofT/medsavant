/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import java.awt.Dimension;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.RangeCondition;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.DecimalRangeSlider;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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
                Range acceptableRange = new Range(getNumber(fromBox.getText()), getNumber(toBox.getText()));
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
    private JLabel toLabel;
    private JLabel fromLabel;

    public NumericFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(WhichTable.valueOf(state.getOneValue(FilterState.TABLE_ELEMENT)), state.getFilterID(), queryID, state.getName(), Boolean.valueOf(state.getOneValue("isDecimal")));
        String minString = state.getOneValue("min");
        String maxString = state.getOneValue("max");
        if (minString != null && maxString != null) {
            applyFilter(Double.parseDouble(minString), Double.parseDouble(maxString));
        }
    }

    public NumericFilterView(WhichTable t, String col, int queryID, String alias, boolean isDecimal) throws SQLException, RemoteException {
        super(alias, queryID);

        this.whichTable = t;
        this.columnName = col;
        this.alias = alias;
        this.isDecimal = isDecimal;

        initUI();

        if (col.equals("position")) {
            setExtremeValues(new Range(1, 250000000));
        } else if (col.equals("sb")) {
            setExtremeValues(new Range(-100, 100));
        } else {

            new MedSavantWorker<Void>("FilterView") {

                @Override
                protected void showProgress(double fract) {
                }

                @Override
                protected void showSuccess(Void result) {
                }

                @Override
                protected Void doInBackground() throws Exception {
                    setExtremeValues(MedSavantClient.DBUtils.getExtremeValuesForColumn(LoginController.getSessionID(), whichTable.getName(), columnName));
                    return null;
                }

            }.execute();
        }
    }

    private void setExtremeValues(Range extremeValues) {
        if (columnName.equals("dp")) {
            extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
        }

        overallMin = (int)Math.floor(extremeValues.getMin());
        overallMax = (int)Math.ceil(extremeValues.getMax());

        int precision = 0;
        if (isDecimal && overallMax - overallMin <= 1) {
            precision = 2;
        } else if (isDecimal && overallMax - overallMin <= 10) {
            precision = 1;
        }

        slider.setPrecision(precision);

        slider.setMinimum(overallMin);
        slider.setMaximum(overallMax);

        slider.setLow(overallMin);
        slider.setHigh(overallMax);

        slider.updateUI();

        fromBox.setText(ViewUtil.numToString(overallMin));
        toBox.setText(ViewUtil.numToString(overallMax));

        fromLabel.setText(ViewUtil.numToString(overallMin));
        toLabel.setText(ViewUtil.numToString(overallMax));
    }

    private void initUI() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //final DecimalRangeSlider rs = new DecimalRangeSlider(precision);
        slider = new DecimalRangeSlider();

        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);

        fromBox = new JTextField();
        toBox = new JTextField();
        fromBox.setMaximumSize(new Dimension(10000,24));
        toBox.setMaximumSize(new Dimension(10000,24));

        fromLabel = new JLabel();
        toLabel = new JLabel();

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

                appliedRange = new Range(getNumber(fromBox.getText()), getNumber(toBox.getText()));
                appliedRange.bound(overallMin, overallMax, true);
                fromBox.setText(ViewUtil.numToString(appliedRange.getMin()));
                toBox.setText(ViewUtil.numToString(appliedRange.getMax()));
                slider.setLow(appliedRange.getMin());
                slider.setHigh(appliedRange.getMax());

                Filter f = new Filter() {

                    @Override
                    public Condition[] getConditions() throws SQLException, RemoteException {
                        TableSchema variantSchema = ProjectController.getInstance().getCurrentVariantTableSchema();
                        if (whichTable == WhichTable.VARIANT) {
                            double min = appliedRange.getMin();
                            double max = appliedRange.getMax();
                            if (isDecimal) {
                                if (overallMin == min) {
                                    return new Condition[] { UnaryCondition.isNull(variantSchema.getDBColumn(columnName)), new RangeCondition(variantSchema.getDBColumn(columnName), min, max) };
                                } else {
                                    return new Condition[] { new RangeCondition(variantSchema.getDBColumn(columnName), min, max) };
                                }
                            } else {
                                if (overallMin == min) {
                                    return new Condition[] { UnaryCondition.isNull(variantSchema.getDBColumn(columnName)), new RangeCondition(variantSchema.getDBColumn(columnName), (int)Math.floor(min), (int)Math.ceil(max)) };
                                } else {
                                    return new Condition[] { new RangeCondition(variantSchema.getDBColumn(columnName), (int)Math.floor(min), (int)Math.ceil(max)) };
                                }
                            }
                        } else {
                            try {
                                return getDNAIDCondition(MedSavantClient.PatientManager.getDNAIDsWithValuesInRange(
                                        LoginController.getSessionID(),
                                        ProjectController.getInstance().getCurrentProjectID(),
                                        columnName,
                                        appliedRange));
                            } catch (SessionExpiredException ex) {
                                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                                return null;
                            }
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
                FilterController.getInstance().addFilter(f, queryID);
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

        JPanel bottomContainer = ViewUtil.getClearPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(selectAll);
        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        add(bottomContainer);

        this.showViewCard();
    }

    public void applyFilter(int low, int high) {
        applyFilter((double)low, (double)high);
    }

    /**
     * Allows filter to be applied without interaction.
     * Assumes values already checked for consistency.
     */
    public final void applyFilter(double low, double high) {
        applyFilterSilently(low,high);
        applyButton.doClick();
    }

    public final void applyFilterSilently(double low, double high) {
        fromBox.setText(Double.toString(low));
        toBox.setText(Double.toString(high));
        slider.setLow(low);
        slider.setHigh(high);
    }

    private static double getNumber(String s) {
        try {
            return Double.parseDouble(s.replaceAll(",", ""));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static FilterState wrapState(WhichTable t, String colName, String alias, Range r, boolean dec) {
        FilterState state = new FilterState(Filter.Type.NUMERIC, alias, colName);
        state.putOneValue("table", t);
        state.putOneValue("isDecimal", dec);
        if (r != null) {
            state.putOneValue("min", r.getMin());
            state.putOneValue("max", r.getMax());
        }
        return state;
    }

    @Override
    public FilterStateAdapter saveState() {
        return wrapState(whichTable, columnName, alias, appliedRange, isDecimal);
    }
}
