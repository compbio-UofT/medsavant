/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.component.field.editable;

/**
 *
 * @author mfiume
 */
public abstract class EditableFieldValidator<T> {
    
    public abstract boolean validate(T value);

    public abstract String getDescriptionOfValidValue();
}
