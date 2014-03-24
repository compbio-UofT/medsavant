/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.component.field.editable;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    // responders to changes field values
    private List<FieldEditedListener> changeListeners;

    /**
     * Constructor
     */
    public EditableField() {
        this.setOpaque(false);
        changeListeners = new ArrayList<FieldEditedListener>();
    }

    /**
     * Set the field's edit state.
     *
     * @param isEditing The field's edit state.
     */
    public void setEditing(boolean isEditing) {
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
     * Whether autonomous editing is enabled for the field.
     *
     * @return Whether autonomous editing is enabled for the field.
     */
    public boolean isAutomousEditingEnabled() {
        return autonomousEditingEnabled;
    }

    /**
     * Get the identifier for this field.
     * @return The identifier for this field.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set the identifier for this field.
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
        if (validate(value)) {
            setValue(value);
            fireFieldEditedEvent();
            return true;
        }
        return false;
    }

    /**
     * Validate values.
     *
     * @param value The value to validate.
     * @return Whether the value is valid or not.
     */
    public boolean validate(T value) {
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
     * Notify listeners that the field was edited
     */
    private void fireFieldEditedEvent() {
        for (FieldEditedListener l : this.changeListeners) {
            l.handleEvent(this);
        }
    }
    
    /**
     * Add a listener that is notified on changes to the field's value.
     * @param l A listener.
     */
    public void addFieldEditedListener(FieldEditedListener l) {
        this.changeListeners.add(l);
    }
    
    /**
     * Remove a listener that is notified on changes to the field's value.
     * @param l A listener.
     */
    public void removeFieldEditedListener(FieldEditedListener l) {
        this.changeListeners.remove(l);
    }

    
    /**
     * Helper methods
     */
    /**
     * Get an edit button
     *
     * @return An edit button.
     */
    protected static JButton generateEditButton(final EditableField field) {
        JButton b = new JButton("Edit");
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                field.setEditing(true);
            }

        });
        b.setFocusable(false);
        return b;
    }

    /**
     * Get a reject button. When clicked, changes made from the editor are not
     * saved and the field is reverted to the original value, with edit mode
     * turned off.
     *
     * @return A reject button.
     */
    protected static JButton generateRejectButton(final EditableField field) {
        JButton b = createSegmentButton("segmentedRoundRect","only");//getIconButton("medsavant/field/editable/icon/cancel.png","OK");
        b.setText("Cancel");
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                field.setEditing(false);
            }

        });
        b.setFocusable(false);
        return b;
    }

    /**
     * Get an accept button. When clicked, changes made from the editor are
     * saved and the field, with edit mode turned off.
     *
     * @return An accept button.
     */
    protected static JButton generateAcceptButton(final EditableField field) {
        JButton b = createSegmentButton("segmentedRoundRect","only");
        b.setText("OK");
        //getIconButton("medsavant/field/editable/icon/ok.png","OK");
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (field.setValueFromEditor()) {
                    field.setEditing(false);
                }
            }

        });
        b.setFocusable(false);
        return b;
    }

    /**
     * Helper method that creates Mac-specific button appearances.
     * @param style The style of button (e.g. segmentedRoundRect)
     * @param position Which position the button is in (e.g. first, middle, last)
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
            ImageIcon i = new ImageIcon(ImageIO.read( ClassLoader.getSystemResource( path ) ));
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


}
