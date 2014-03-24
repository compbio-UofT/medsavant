/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.component.field.editable;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author mfiume
 */
public class StringEditableField extends OnClickEditableField<String> {

    private final JTextField textField;

    public StringEditableField() {
        textField = new JTextField();
        textField.setColumns(15);

        addSaveFocusListener(textField);
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
        return textField.getText();
    }

    @Override
    public void didToggleEditMode(boolean editMode) {
    }

}
