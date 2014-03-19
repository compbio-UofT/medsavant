/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.component.field.editable;

import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 *
 * @author mfiume
 */
public class EnumEditableField extends OnClickEditableField<Object> {
    private final JComboBox comboBox;

    public EnumEditableField() {
        this(null);
    }
    
    public EnumEditableField(Object[] items) {
        super();
        comboBox = new JComboBox();
        comboBox.setFocusable(false);
        
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
        comboBox.setSelectedItem(value);
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
