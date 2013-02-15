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

package org.ut.biolab.medsavant.client.view.component;

import com.jidesoft.swing.RangeSlider;


/**
 *
 * @author Andrew
 */
public class DecimalRangeSlider extends RangeSlider {

    private int multiplier;

    public DecimalRangeSlider(int precision) {
        setPrecision(precision);
    }

    public final void setPrecision(int precision) {
        if (precision <= 0) {
            multiplier = 1;
        } else {
            multiplier = precision * 10;
        }
    }

    public DecimalRangeSlider() {
        this(0);
    }

    @Override
    public void setMinimum(int i) {
        super.setMinimum(adjustValue(i));
    }

    @Override
    public void setMaximum(int i) {
        super.setMaximum(adjustValue(i));
    }

    public void setLow(double i) {
        super.setLowValue(adjustValue(i));
    }

    public void setHigh(double i) {
        super.setHighValue(adjustValue(i));
    }

    public double getLow() {
        int value = super.getLowValue();
        return getActualValue(value);
    }

    public double getHigh() {
        int value = super.getHighValue();
        return getActualValue(value);
    }

    private int adjustValue(double i) {
        return (int)(i * multiplier);
    }

    private int adjustValue(int i) {
        return i * multiplier;
    }

    private double getActualValue(int i) {
        return (double)i / (double)multiplier;
    }
}

