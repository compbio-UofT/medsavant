package org.ut.biolab.mfiume.query.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

    private final NumberConditionValueGenerator generator;

    public NumberSearchConditionEditorView(SearchConditionItem i, final NumberConditionValueGenerator g) {
        super(i);
        this.generator = g;
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException {

        double[] selectedValues;
        if (encoding == null) {
            selectedValues = null;
        } else {           
            selectedValues = NumericConditionEncoder.unencodeConditions(encoding);        
        }

        final double[] extremeValues = generator.getExtremeNumericValues();       
        this.removeAll();

        if (extremeValues == null || (extremeValues[0] == 0 && extremeValues[1] == 0)) {
            this.add(new JLabel("This field is not populated"));
            return;
        }


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //final DecimalRangeSlider rs = new DecimalRangeSlider(precision);
        final DecimalRangeSlider slider = new DecimalRangeSlider();

        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);

        final JTextField fromBox = new JTextField();
        final JTextField toBox = new JTextField();
        fromBox.setMaximumSize(new Dimension(10000, 24));
        toBox.setMaximumSize(new Dimension(10000, 24));

        final JLabel fromLabel = new JLabel();
        final JLabel toLabel = new JLabel();

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

        slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fromBox.setText(ViewUtil.numToString(slider.getLow()));
                toBox.setText(ViewUtil.numToString(slider.getHigh()));               

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

        fromBox.addKeyListener(keyListener);
        toBox.addKeyListener(keyListener);

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // TODO: encode
                fromBox.setText(ViewUtil.numToString(slider.getLow()));
                toBox.setText(ViewUtil.numToString(slider.getHigh()));
                encodeValue(slider.getLow(), slider.getHigh(), extremeValues[0], extremeValues[1]);
            }
        });

        JButton selectAll = ViewUtil.getSoftButton("Select All");
        selectAll.setFocusable(false);
        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slider.setLowValue((int) Math.floor(extremeValues[0]));
                slider.setHighValue((int) Math.floor(extremeValues[1]));
                fromBox.setText(ViewUtil.numToString(extremeValues[0]));
                toBox.setText(ViewUtil.numToString(extremeValues[1]));
            }
        });

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        bottomContainer.add(selectAll);
        bottomContainer.add(Box.createHorizontalGlue());

        add(bottomContainer);
        setExtremeValues(slider, fromLabel, toLabel, fromBox, toBox, 0, new Range(extremeValues[0], extremeValues[1]));

        if (encoding != null) {
            double[] d = NumericConditionEncoder.unencodeConditions(encoding);
            setSelectedValues(slider, fromBox, toBox, new Range(d[0], d[1]));
        }
    }

    private void encodeValue(double low, double high, double min, double max) {

        String s = NumericConditionEncoder.encodeConditions(low, high);
        
        
              
        saveSearchConditionParameters(s);

        String d = NumericConditionEncoder.getDescription(low, high, min, max);
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
}
