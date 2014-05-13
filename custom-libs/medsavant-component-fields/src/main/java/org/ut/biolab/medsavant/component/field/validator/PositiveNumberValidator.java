/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.component.field.validator;

import org.ut.biolab.medsavant.component.field.editable.EditableFieldValidator;

/**
 *
 * @author mfiume
 */
public class PositiveNumberValidator extends EditableFieldValidator<String> {
    private String name;

    public PositiveNumberValidator() {
        this("value");
    }
    
    public PositiveNumberValidator(String name) {
        this.name = name;
    }
    
    @Override
    public boolean validate(String value) {
        try {
            return value != null && !value.isEmpty() && (Integer.parseInt(value) >= 0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getDescriptionOfValidValue() {
        return "Invalid " + name;
    }

}
