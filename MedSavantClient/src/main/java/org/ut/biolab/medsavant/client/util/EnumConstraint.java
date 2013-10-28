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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

/**
 * A constraint that takes on one of several non-null values. A value constrained by an EnumConstraint cannot be missing
 * (null, blank, or whitespace only), and null cannot be specified as one of the allowed values.
 * 
 * @author jim
 */
public class EnumConstraint extends CustomFieldConstraint {
    private String[][] allowedValues;

    /**
     * @param allowedValues An array of Value/Label pairs. The constraint is applied only on the value, the label is
     *        optional and may be null. e.g. String[][] allowedValues = {{"Red", null}, {"Gre", "Green"}, {"Blu",
     *        "Blue"}};
     * @param maxlength The maximum number of allowed characters in the value.
     * @param errorMessage An error message that can be later retrieved with getErrorMessage()
     */
    public EnumConstraint(String[][] allowedValues, int maxlength, String errorMessage) {
        super(true, maxlength, errorMessage);
        this.allowedValues = allowedValues;
    }

    /**
     * @param allowedValues An array of Value/Label pairs. The constraint is applied only on the value, the label is
     *        optional and may be null. e.g. String[][] allowedValues = {{"Red", null}, {"Gre", "Green"}, {"Blu",
     *        "Blue"}};
     * @param maxlength The maximum number of allowed characters in the value.
     */
    public EnumConstraint(String[][] allowedValues, int maxlength) {
        this(allowedValues, maxlength, null);
    }

    /**
     * @return An array of values allowed by this constraint, with their corresponding labels. i.e. {{Val, Label}, {Val,
     *         Label} ....}
     */
    public String[][] getAllowedValues() {
        return this.allowedValues;
    }

    /**
     * Validates the given input
     * 
     * @param val The input string to validate. Null, zero-length, or white-space only strings are considered missing.
     * @return true if valid, false otherwise.
     */
    @Override
    public boolean isValid(String val) {
        if (super.isValid(val)) {
            for (String[] allowed : this.allowedValues) {
                if (val.equalsIgnoreCase(allowed[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Enum constraints are required by definition, this method has no effect.
     */
    @Override
    public void setRequired(boolean required) {
    }
}
