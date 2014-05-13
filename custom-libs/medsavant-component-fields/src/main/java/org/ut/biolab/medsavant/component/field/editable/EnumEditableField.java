/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.component.field.editable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 *
 * @author mfiume
 */
public class EnumEditableField extends OnClickEditableField<Object> {

    private final JComboBox comboBox;
    private boolean saveOnSelection = true;

    public EnumEditableField() {
        this(null);
    }

    public EnumEditableField(Object[] items) {
        super();
        comboBox = new JComboBox();

        addSaveFocusListener(comboBox);
        addSaveAndCancelKeyListeners(comboBox);

        comboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (saveOnSelection && e.getStateChange() == ItemEvent.SELECTED) {
                    if (EnumEditableField.this.isAutonomousEditingEnabled()) {
                        saveWithValidationWarning();
                        setEditing(false);
                    }
                    EnumEditableField.this.fireFieldEditedEvent();
                }
            }
        });

        if (items != null) {
            for (Object o : items) {
                this.addItem(o);
            }
        }
    }

    public void addItem(Object item) {
        comboBox.addItem(item);
    }

    @Override
    public void updateEditorRepresentationForValue(Object value) {
        saveOnSelection = false;
        comboBox.setSelectedItem(value);
        saveOnSelection = true;
    }

    @Override
    public JComponent getEditor() {
        return comboBox;
    }

    @Override
    public Object getValueFromEditor() {
        return comboBox.getSelectedItem();
    }

    @Override
    public void didToggleEditMode(boolean editMode) {
    }

}
