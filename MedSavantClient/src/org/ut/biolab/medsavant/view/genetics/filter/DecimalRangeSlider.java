/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.jidesoft.swing.RangeSlider;

/**
 *
 * @author Andrew
 */
public class DecimalRangeSlider extends RangeSlider {
        
    private int multiplier;

    public DecimalRangeSlider(int precision){
        super();
        if(precision <= 0){
            multiplier = 1;
        } else {
            multiplier = precision * 10;
        }
    }

    public DecimalRangeSlider(){
        this(0);
    }

    @Override
    public void setMinimum(int i){
        super.setMinimum(adjustValue(i));
    }

    @Override
    public void setMaximum(int i){
        super.setMaximum(adjustValue(i));
    }

    public void setLow(double i){
        super.setLowValue(adjustValue(i));
    }

    public void setHigh(double i){
        super.setHighValue(adjustValue(i));
    }

    public double getLow(){
        int value = super.getLowValue();
        return getActualValue(value);
    }

    public double getHigh(){
        int value = super.getHighValue();
        return getActualValue(value);
    }

    private int adjustValue(double i){
        return (int)(i * multiplier);
    }

    private int adjustValue(int i){
        return i * multiplier;
    }

    private double getActualValue(int i){
        return (double)i / (double)multiplier;
    }

}

