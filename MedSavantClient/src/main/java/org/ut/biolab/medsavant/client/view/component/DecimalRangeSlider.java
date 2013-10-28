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

