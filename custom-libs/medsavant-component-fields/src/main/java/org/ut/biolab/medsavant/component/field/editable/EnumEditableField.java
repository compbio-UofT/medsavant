/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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
