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

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * An abstract class that represents a field that can be edited.
 *
 * @author mfiume
 * @param <T> The type of object that this field returns as selected value
 */
public abstract class EditableField<T> extends JPanel {

    // the color to use in normal mode to denote that a field is editable on click
    protected Color editColor = new Color(26, 143, 240);

    // the color to use in normal mode to denote that a field is not set
    protected Color nullColor = new Color(150, 150, 150);

    // is entering edit mode autonomous or externally controlled?
    private boolean autonomousEditingEnabled = true;

    // is this component in edit mode?
    boolean editing = false;

    // identifier 
    private String tag;

    // validates values entered in edit mode
    private EditableFieldValidator<T> validator;

    // responders to commits of field values
    private final List<FieldCommittedListener> commitListeners;

    // responders to edits (event transient ones) of field values
    private final List<FieldEditedListener> editListeners;

    /**
     * Constructor
     */
    public EditableField() {
        this.setOpaque(false);
        commitListeners = new ArrayList<FieldCommittedListener>();
        editListeners = new ArrayList<FieldEditedListener>();

        // Temporarily disabled because, when enabled, the escape
        // key listener causes the field to lose focus 
        // and focus is given to the next field
        // this.setFocusable(true);
        // this.addFocusListener(getEditOnFocusListener());
    }

    /**
     * Set whether the field enters edit mode on focus (useful for tabbing
     * through and editing several fields in a form). To activate this feature,
     * the field must also be autonomously editable.
     *
     * @param b Whether the fields enters edit mode on focus.
     *
     * private void setEditOnFocus(boolean b) { System.out.println("Setting edit
     * on focus: " + b); for (FocusListener l : this.getFocusListeners()) {
     * this.removeFocusListener(l); } if (b) {
     *
     * }
     * }/
     */
    /**
     * Set the field's edit state.
     *
     * @param isEditing The field's edit state.
     */
    public void setEditing(boolean isEditing) {
        //System.out.println("Setting editing to " + isEditing);
        this.editing = isEditing;
        updateUIForEditingState(isEditing);
        updateUI();
    }

    /**
     * Whether the field currently allows editing.
     *
     * @return Whether the field currently allows editing.
     */
    public boolean isEditing() {
        return this.editing;
    }

    /**
     * Turn autonomous editing on or off. Autonomous editing means the component
     * controls it's own editing state.
     *
     * @param b Whether to autonomously edit
     */
    public void setAutonomousEditingEnabled(boolean b) {
        this.autonomousEditingEnabled = b;
        updateUIForAutonomousEditingState(b);
    }

    /**
     * Get the identifier for this field.
     *
     * @return The identifier for this field.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set the identifier for this field.
     *
     * @param tag The identifier for this field.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Update the UI based on the editing state.
     *
     * @param isEditing The field's edit state.
     */
    protected abstract void updateUIForEditingState(boolean isEditing);

    /**
     * Update the UI based on the autonomous editing state.
     *
     * @param isAutonomous The field's autonomous editing state.
     */
    protected abstract void updateUIForAutonomousEditingState(boolean isAutonomous);

    /**
     * Set the value for the field.
     *
     * @param v The value to set.
     */
    public abstract void setValue(T v);

    /**
     * Get the value for this field.
     *
     * @return The value for this field.
     */
    public abstract T getValue();

    /**
     * Get the value for this field from the edit mode.
     *
     * @return The value for this field from the edit mode
     */
    public abstract T getValueFromEditor();

    /**
     * Get whether the field has autonomous editing enabled.
     *
     * @return Whether the field has autonomous editing enabled.
     */
    public boolean isAutonomousEditingEnabled() {
        return autonomousEditingEnabled;
    }

    /**
     * Set the value from the editor, after validating.
     *
     * @return Whether the value was set or not.
     */
    public boolean setValueFromEditor() {
        T value = getValueFromEditor();
        if (validateValue(value)) {
            setValue(value);
            fireFieldCommittedEvent();
            return true;
        }
        return false;
    }

    /**
     * Validate the current value.
     *
     * @return Whether the current value is valid or not.
     */
    public boolean validateCurrentValue() {
        if (validator != null) {
            if (!validator.validate(this.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate values.
     *
     * @param value The value to validate.
     * @return Whether the value is valid or not.
     */
    public boolean validateValue(T value) {
        if (validator != null) {
            if (!validator.validate(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set the validator for this field. The validator checks the validity of
     * the value entered by the user while in edit mode.
     *
     * @param validator
     */
    public void setValidator(EditableFieldValidator<T> validator) {
        this.validator = validator;
    }

    /**
     * Get the validator for this field.
     *
     * @return The validator for this field.
     */
    public EditableFieldValidator<T> getValidator() {
        return validator;
    }

    /**
     * Notify listeners that the field was committed
     */
    private void fireFieldCommittedEvent() {
        for (FieldCommittedListener l : this.commitListeners) {
            l.handleCommitEvent(this);
        }
    }

    /**
     * Notify listeners that the field was edited
     */
    protected void fireFieldEditedEvent() {
        for (FieldEditedListener l : this.editListeners) {
            l.handleEditEvent(this);
        }
    }

    /**
     * Add a listener that is notified on commits to the field's value.
     *
     * @param l A listener.
     */
    public void addFieldComittedListener(FieldCommittedListener l) {
        this.commitListeners.add(l);
    }

    /**
     * Remove a listener that is notified on commits to the field's value.
     *
     * @param l A listener.
     */
    public void removeFieldCommittedListener(FieldCommittedListener l) {
        this.commitListeners.remove(l);
    }

    /**
     * Add a listener that is notified on edits to the field's value.
     *
     * @param l A listener.
     */
    public void addFieldEditedListener(FieldEditedListener l) {
        this.editListeners.add(l);
    }

    /**
     * Remove a listener that is notified on changes to the field's value.
     *
     * @param l A listener.
     */
    public void removeFieldCommittedListener(FieldEditedListener l) {
        this.editListeners.remove(l);
    }

    /**
     * Helper method that creates Mac-specific button appearances.
     *
     * @param style The style of button (e.g. segmentedRoundRect)
     * @param position Which position the button is in (e.g. first, middle,
     * last)
     * @return An accept button.
     */
    public static JButton createSegmentButton(String style, String position) {
        JButton button = new JButton();
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", style);
        button.putClientProperty("JButton.segmentPosition", position);
        return button;
    }

    /**
     * Remove all mouse listeners from a component.
     *
     * @param c The component from which to remove MouseListeners.
     */
    protected static void removeMouseListeners(JComponent c) {
        for (MouseListener ml : c.getMouseListeners()) {
            c.removeMouseListener(ml);
        }
    }

    /**
     * Set a field editing upon a component being clicked.
     *
     * @param field The field to make editable.
     * @param c The component to attach the listener.
     */
    protected static void addEditOnClickListener(final EditableField field, JComponent c) {
        c.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                field.setEditing(true);
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

        });
    }

    public static JButton getIconButton(String path, String failureText) {
        try {
            ImageIcon i = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(path)));
            JButton b = new JButton(i);
            b.setBorder(null);
            b.setFocusable(false);
            b.setBorderPainted(false);
            return b;
        } catch (IOException ex) {
            ex.printStackTrace();
            return new JButton(failureText);
        }
    }

    private FocusListener getEditOnFocusListener() {
        return new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (!EditableField.this.isEditing()) {
                    if (EditableField.this.isAutonomousEditingEnabled()) {
                        EditableField.this.setEditing(true);
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }

        };
    }
}
