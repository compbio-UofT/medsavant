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
 * @author jim
 */
public class CustomFieldConstraint {
    private boolean required;

    private int maxlength;

    private String errorMessage;

    /**
     * @param required Whether a missing value is acceptable (true) or not (false).
     * @param maxlength The maximum number of characters for the input.
     * @param errorMessage An optional error message that can be stored for later retrieval with getErrorMessage()
     */
    public CustomFieldConstraint(boolean required, int maxlength, String errorMessage) {
        this(required, maxlength);
        this.errorMessage = errorMessage;
    }

    /**
     * @param required Whether a missing value is acceptable (true) or not (false).
     * @param maxlength The maximum number of characters for the input.
     */
    public CustomFieldConstraint(boolean required, int maxlength) {
        this.required = required;
        this.maxlength = maxlength;
    }

    /**
     * @return The errorMessage that was supplied when the constraint was constructed, or null if no message was given.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * @return Whether or not a missing value is acceptable input.
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * @param required true if a missing value is acceptable, false otherwise.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isEmpty(String val) {
        return (val == null || val.trim().isEmpty());
    }

    /**
     * Validates the given input by checking if it is missing or if it contains too many characters.
     * 
     * @param val The input string to validate. Null, zero-length, or white-space only strings are considered missing.
     * @return true if valid, false otherwise.
     */
    public boolean isValid(String val) {
        if (isEmpty(val)) {
            return !this.required;
        }

        if (val.length() > this.maxlength) {
            // val is too long
            return false;
        }

        return true;
    }
}
