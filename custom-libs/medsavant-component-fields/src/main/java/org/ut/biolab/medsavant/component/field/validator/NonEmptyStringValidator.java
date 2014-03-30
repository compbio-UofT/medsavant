/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.component.field.validator;

import org.apache.commons.validator.UrlValidator;
import org.ut.biolab.medsavant.component.field.editable.EditableFieldValidator;

/**
 *
 * @author mfiume
 */
public class NonEmptyStringValidator extends EditableFieldValidator<String> {
    private String name;

    public NonEmptyStringValidator() {
        this("value");
    }
    
    public NonEmptyStringValidator(String name) {
        this.name = name;
    }
    
    @Override
    public boolean validate(String value) {
        return !value.isEmpty();
    }

    @Override
    public String getDescriptionOfValidValue() {
        return "Invalid " + name;
    }

}
