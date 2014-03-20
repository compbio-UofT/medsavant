/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.component.field.editable;

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author mfiume
 */
public class IntegerEditableField extends OnClickEditableField<Integer> {
    private final JTextField textField;

    public IntegerEditableField() {
        textField = new JTextField();
        textField.setColumns(15);
        
        addCancelFocusListener(textField);
        addSaveAndCancelKeyListeners(textField);
        this.setRejectButtonVisible(false);
    }
    
    @Override
    public void updateEditorRepresentationForValue(Integer value) {
        textField.setText(value + "");
    }

    @Override
    public JComponent getEditor() {
        return textField;
    }

    @Override
    public Integer getValueFromEditor() {
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void didToggleEditMode(boolean editMode) {
    }
    
}
