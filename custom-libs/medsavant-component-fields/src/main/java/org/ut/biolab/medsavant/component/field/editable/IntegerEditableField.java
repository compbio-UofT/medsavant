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
        
        addSaveFocusListener(textField);
        addSaveAndCancelKeyListeners(textField);
        addFieldChangeKeyListener(textField);
        
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
