/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.component.field.editable;

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author mfiume
 */
public class PasswordEditableField extends OnClickEditableField<String> {

    private final JPasswordField textField;

    public PasswordEditableField() {
        super(true);
        
        textField = new JPasswordField();
        textField.setColumns(15);

        addCancelFocusListener(textField);
        addSaveAndCancelKeyListeners(textField);
        this.setRejectButtonVisible(false);
    }

    @Override
    public void updateEditorRepresentationForValue(String value) {
        textField.setText(value);
    }

    @Override
    public JComponent getEditor() {
        return textField;
    }

    @Override
    public String getValueFromEditor() {
        return new String(textField.getPassword());
    }
    
    @Override
    public void didToggleEditMode(boolean editMode) {
    }

    

}
