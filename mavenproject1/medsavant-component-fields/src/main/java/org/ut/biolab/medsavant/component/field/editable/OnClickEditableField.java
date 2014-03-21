/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.component.field.editable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author mfiume
 */
public abstract class OnClickEditableField<T> extends EditableField<T> {
    
    // whether the field is a password field
    private boolean passwordField;

    /**
     * Provide the opportunity to reflect the current value of the field in the
     * editor.
     *
     * @param value The current value of the field.
     */
    public abstract void updateEditorRepresentationForValue(T value);

    /**
     * Get the UI component for the editor.
     *
     * @return The UI component for the editor.
     */
    public abstract JComponent getEditor();

    /**
     * Get the chosen value for the editor.
     *
     * @return The chosen value for the editor.
     */
    public abstract T getValueFromEditor();

    /**
     * Provide the opportunity to respond to edit mode being turned on or off.
     *
     * @param editMode The current edit mode.
     */
    public abstract void didToggleEditMode(boolean editMode);

    // whether these buttons are shown or not
    private boolean acceptButtonVisible;
    private boolean rejectButtonVisible;

    private T value;
    private JPanel editorPlaceholder;
    private JLabel valueLabel;
    private JButton rejectChangesButton;
    private JButton acceptChangesButton;

    public OnClickEditableField() {
        this(false);
    }
    
    public OnClickEditableField(boolean passwordField) {
        super();
        
        this.passwordField = passwordField;
        
        initUI();

        setValue(null);
        updateUIForEditingState(this.isEditing());
        updateUIForAutonomousEditingState(this.isAutomousEditingEnabled());

        setAcceptButtonVisible(false); // usually, the editable component configures acceptance
        setRejectButtonVisible(true);
    }

    @Override
    protected void updateUIForEditingState(boolean isEditing) {

        // normal state
        setVisibility(new Component[]{valueLabel}, !isEditing);

        // edit state
        setVisibility(new Component[]{editorPlaceholder}, isEditing);
        if (acceptButtonVisible) {
            acceptChangesButton.setVisible(isEditing);
        }
        if (rejectButtonVisible) {
            rejectChangesButton.setVisible(isEditing);
        }

        if (isEditing) {
            updateEditorRepresentationForValue(value);

            editorPlaceholder.removeAll();
            JComponent c = getEditor();
            editorPlaceholder.add(c);
            c.requestFocus();
            editorPlaceholder.updateUI();
        }

        didToggleEditMode(isEditing);
    }

    @Override
    protected void updateUIForAutonomousEditingState(boolean isAutonomous) {
        if (isAutonomous) {
            //valueLabel.setForeground(editColor);
            addEditOnClickListener(this, valueLabel);
        } else {
            removeClickListeners(valueLabel);
            //valueLabel.setForeground(Color.black);
        }
    }

    @Override
    public void setValue(T v) {
        value = v;
        if (v == null || v.toString().isEmpty()) {
            valueLabel.setForeground(nullColor);
            valueLabel.setText("Not Set");
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.ITALIC));
        } else {
            valueLabel.setForeground(Color.black);
            String strRepresentation = v.toString();
            if (passwordField) {
                strRepresentation = passwordStringOfLength(strRepresentation.length());
            }
            valueLabel.setText(strRepresentation);
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN));
        }

    }

    /**
     * Create a password string of a given length.
     *
     * @param length The length of the string to produce.
     * @return A password string of a given length.
     */
    private String passwordStringOfLength(int length) {
        String s = "";
        while (length-- > 0) {
            s += "•";
        }
        return s;
    }

    @Override
    public T getValue() {
        return value;
    }

    private void initUI() {

        this.setLayout(new MigLayout("insets 0, hidemode 3, gapx 0"));

        valueLabel = new JLabel();

        editorPlaceholder = new JPanel();
        editorPlaceholder.setOpaque(false);
        editorPlaceholder.setLayout(new MigLayout("insets 0, hidemode 3"));

        this.add(valueLabel);
        this.add(editorPlaceholder);

        rejectChangesButton = EditableField.generateRejectButton(this);
        acceptChangesButton = EditableField.generateAcceptButton(this);

        this.add(rejectChangesButton);
        this.add(acceptChangesButton);

        rejectChangesButton.setVisible(false);
        acceptChangesButton.setVisible(false);

        final Font font = valueLabel.getFont();
        final Map attributes = font.getAttributes();

        valueLabel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                if (isAutonomousEditingEnabled()) {
                    valueLabel.setForeground(editColor);
                    //attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    //valueLabel.setFont(font.deriveFont(attributes));
                }
            }

            public void mouseExited(MouseEvent e) {
                if (isAutonomousEditingEnabled()) {
                    if (value == null || value.toString().isEmpty()) {
                        valueLabel.setForeground(nullColor);
                    } else {
                        valueLabel.setForeground(Color.black);
                    }
                    //attributes.remove(TextAttribute.UNDERLINE);
                    //valueLabel.setFont(font.deriveFont(attributes));
                }
            }

        });

    }
    
    private void setVisibility(Component[] components, boolean isVisible) {
        for (Component c : components) {
            c.setVisible(isVisible);
        }
    }

    private void removeClickListeners(JLabel valueLabel) {
        for (MouseListener ml : valueLabel.getMouseListeners()) {
            valueLabel.removeMouseListener(ml);
        }
    }

    public void addCancelFocusListener(JComponent c) {
        c.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                OnClickEditableField.this.setEditing(false);
                return;
            }

        });
    }

    public void addSaveAndCancelKeyListeners(JComponent c) {
        c.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER || e.getKeyChar() == KeyEvent.VK_TAB) {
                    if (OnClickEditableField.this.setValueFromEditor()) {
                        OnClickEditableField.this.setEditing(false);
                    }
                    return;
                }

                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    OnClickEditableField.this.setEditing(false);
                    return;
                }
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }

        });
    }

    public void setRejectButtonVisible(boolean b) {
        rejectButtonVisible = b;
        if (b && editing) {
            rejectChangesButton.setVisible(b);
        }
    }

    public void setAcceptButtonVisible(boolean b) {
        acceptButtonVisible = b;
        if (b && editing) {
            acceptChangesButton.setVisible(b);
        }
    }

}
