/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.mfiume.query.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.component.DecimalRangeSlider;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.value.NumberConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.encode.NumericConditionEncoder;

/**
 *
 * @author mfiume
 */
public class NumberSearchConditionEditorView extends SearchConditionEditorView {

    private static final int FROM_TO_WIDTH = 150; //width of the 'from' and 'to' textboxes.
    private static final Log LOG = LogFactory.getLog(NumberSearchConditionEditorView.class);
    private final NumberConditionValueGenerator generator;
    boolean isAdjustingSlider = false;

    public NumberSearchConditionEditorView(SearchConditionItem i, final NumberConditionValueGenerator g) {
        super(i);
        this.generator = g;
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException {

        System.out.println("Loading number search confition editor with encoding " + encoding);

        double[] selectedValues;
        if (encoding == null) {
            selectedValues = null;
        } else {
            selectedValues = NumericConditionEncoder.unencodeConditions(encoding);
        }

        final double[] extremeValues = generator.getExtremeNumericValues();
        this.removeAll();

        if (extremeValues == null || (extremeValues[0] == 0 && extremeValues[1] == 0)) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(Box.createHorizontalGlue());
            p.add(new JLabel("This field is not populated"));
            p.add(Box.createHorizontalGlue());
            this.add(p);
            return;
        }

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        JPanel labelPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(labelPanel);
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(new JLabel("Filtering variants where " + item.getName() + ": "));
        labelPanel.add(Box.createHorizontalGlue());
        ButtonGroup group = new ButtonGroup();
        //JRadioButton isButton = new JRadioButton("is within the following range:");
        //JRadioButton nullButton = new JRadioButton("is missing");
        //group.add(isButton);
        //group.add(nullButton);

        final JCheckBox nullButton = new JCheckBox("include missing values");

        JPanel bp = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(bp);
        p.add(labelPanel);
        p.add(bp);
        add(p);
        final DecimalRangeSlider slider = new DecimalRangeSlider();

        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);

        final JTextField fromBox = new JTextField();
        final JTextField toBox = new JTextField();

        nullButton.setSelected(NumericConditionEncoder.encodesNull(encoding));

        nullButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                encodeValue(ViewUtil.parseDoubleFromFormattedString(fromBox.getText()), ViewUtil.parseDoubleFromFormattedString(toBox.getText()), extremeValues[0], extremeValues[1], nullButton.isSelected());
            }
        });

        fromBox.setMaximumSize(new Dimension(10000, 24));
        toBox.setMaximumSize(new Dimension(10000, 24));
        fromBox.setPreferredSize(new Dimension(FROM_TO_WIDTH, 24));
        toBox.setPreferredSize(new Dimension(FROM_TO_WIDTH, 24));
        fromBox.setMinimumSize(new Dimension(FROM_TO_WIDTH, 24));
        toBox.setMinimumSize(new Dimension(FROM_TO_WIDTH, 24));
        fromBox.setHorizontalAlignment(JTextField.RIGHT);
        toBox.setHorizontalAlignment(JTextField.RIGHT);

        final JLabel fromLabel = new JLabel();
        final JLabel toLabel = new JLabel();

        ViewUtil.makeMini(fromLabel);
        ViewUtil.makeMini(toLabel);

        JPanel fromToContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(fromToContainer);
        fromToContainer.add(Box.createHorizontalGlue());
        fromToContainer.add(fromBox);
        fromToContainer.add(new JLabel(" - "));
        fromToContainer.add(toBox);
        fromToContainer.add(Box.createHorizontalGlue());

        JPanel minMaxContainer = ViewUtil.getClearPanel();
        minMaxContainer.setLayout(new BoxLayout(minMaxContainer, BoxLayout.X_AXIS));

        JPanel sliderContainer = ViewUtil.getClearPanel();
        sliderContainer.setLayout(new BoxLayout(sliderContainer, BoxLayout.Y_AXIS));
        sliderContainer.add(slider);

        JPanel nullValueContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(nullValueContainer);
        nullValueContainer.add(Box.createHorizontalGlue());
        nullValueContainer.add(nullButton);
        nullValueContainer.add(Box.createHorizontalGlue());

        JPanel labelContainer = ViewUtil.getClearPanel();
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.X_AXIS));
        labelContainer.add(fromLabel);
        labelContainer.add(Box.createHorizontalGlue());
        labelContainer.add(toLabel);
        sliderContainer.add(labelContainer);
        minMaxContainer.add(Box.createHorizontalGlue());
        minMaxContainer.add(sliderContainer);
        minMaxContainer.add(Box.createHorizontalGlue());

        add(fromToContainer);
        add(minMaxContainer);
        add(nullValueContainer);
        add(Box.createVerticalBox());

        slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (slider.isEnabled()) {
                    fromBox.setText(ViewUtil.numToString(slider.getLow()));
                    toBox.setText(ViewUtil.numToString(slider.getHigh()));
                    encodeValue(ViewUtil.parseDoubleFromFormattedString(fromBox.getText()), ViewUtil.parseDoubleFromFormattedString(toBox.getText()), extremeValues[0], extremeValues[1], nullButton.isSelected());
                }
            }
        });

        final KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    Range selectedRage = new Range(getNumber(fromBox.getText()), getNumber(toBox.getText()));
                    setSelectedValues(slider, fromBox, toBox, selectedRage);
                }
            }

            private double getNumber(String s) {
                try {
                    return Double.parseDouble(s.replaceAll(",", ""));
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        };

        CaretListener caretListener = new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent ce) {
                if (!isAdjustingSlider) {
                    try {
                        encodeValue(ViewUtil.parseDoubleFromFormattedString(fromBox.getText()), ViewUtil.parseDoubleFromFormattedString(toBox.getText()), extremeValues[0], extremeValues[1], nullButton.isSelected());
                    } catch (Exception e) {
                    }
                }
            }
        };

        fromBox.addKeyListener(keyListener);

        toBox.addKeyListener(keyListener);

        fromBox.addCaretListener(caretListener);

        toBox.addCaretListener(caretListener);

        slider.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        isAdjustingSlider = true;
                        fromBox.setText(ViewUtil.numToString(slider.getLow()));
                        toBox.setText(ViewUtil.numToString(slider.getHigh()));
                        isAdjustingSlider = false;
                    }
                });

        JPanel bottomContainer = new JPanel();

        bottomContainer.setLayout(
                new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(Box.createHorizontalGlue());

        add(bottomContainer);

        setExtremeValues(slider, fromLabel, toLabel, fromBox, toBox,
                0, new Range(extremeValues[0], extremeValues[1]));

        if (encoding != null) {
            double[] d = NumericConditionEncoder.unencodeConditions(encoding);
            setSelectedValues(slider, fromBox, toBox, new Range(d[0], d[1]));
        }
    }

    private void encodeValue(double low, double high, double min, double max, boolean includeNull) {

        //LOG.debug("Encoding " + low + " - " + high);
        String s = NumericConditionEncoder.encodeConditions(low, high, includeNull);

        saveSearchConditionParameters(s);

        String d = NumericConditionEncoder.getDescription(low, high, min, max, includeNull);
        item.setDescription(d);
    }

    private void setExtremeValues(DecimalRangeSlider slider, JLabel fromLabel, JLabel toLabel, JTextField fromBox, JTextField toBox, int precision, Range extremeValues) {
        //if (columnName.equals("dp")) {
        //    extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
        //}

        int overallMin = (int) Math.floor(extremeValues.getMin());
        int overallMax = (int) Math.ceil(extremeValues.getMax());

        if (overallMax - overallMin <= 1) {
            precision = 2;
        } else if (overallMax - overallMin <= 10) {
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

    private void setSelectedValues(DecimalRangeSlider slider, JTextField fromBox, JTextField toBox, Range selectedValues) {

        slider.setLow(selectedValues.getMin());
        slider.setHigh(selectedValues.getMax());

        fromBox.setText(ViewUtil.numToString(selectedValues.getMin()));
        toBox.setText(ViewUtil.numToString(selectedValues.getMax()));

        slider.updateUI();
    }

    public static void main(String[] args) {
        Double.parseDouble("1,456,094");
    }
}
