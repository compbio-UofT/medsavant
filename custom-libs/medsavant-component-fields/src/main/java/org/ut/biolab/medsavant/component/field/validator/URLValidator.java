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
public class URLValidator extends EditableFieldValidator<String> {

    @Override
    public boolean validate(String value) {
        System.out.println("Validating " + value);
        String[] schemes = {"http", "https", "ftp"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        return value.isEmpty() || urlValidator.isValid(value);
    }

    @Override
    public String getDescriptionOfValidValue() {
        return "Invalid URL";
    }

}
