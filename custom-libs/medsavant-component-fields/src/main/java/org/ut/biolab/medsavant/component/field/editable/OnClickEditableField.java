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
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.Set;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author mfiume
 */
public abstract class OnClickEditableField<T> extends EditableField<T> {

    private JLabel invalidLabel;

    // a lock to prevent subclass methods from being called until
    // they are initialized
    private boolean extenderShouldBeInitialized = false;

    /**
     * Get a reject button. When clicked, changes made from the editor are not
     * saved and the field is reverted to the original value, with edit mode
     * turned off.
     *
     * @return A reject button.
     */
    protected JButton generateRejectButton(final EditableField field) {
        JButton b = EditableField.createSegmentButton("segmentedRoundRect", "first");
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
    protected JButton generateAcceptButton(final EditableField field) {
        JButton b = EditableField.createSegmentButton("segmentedRoundRect", "last");
        b.setText("OK");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveWithValidationWarning();
                setEditing(false);
            }
        });
        b.setFocusable(false);
        return b;
    }

    /**
     * Helper methods
     */
    /**
     * Get an edit button
     *
     * @return An edit button.
     */
    protected JButton generateEditButton(final EditableField field) {
        JButton b = new JButton("Edit");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                field.setEditing(true);
            }
        });
        b.setFocusable(false);
        return b;
    }

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
        updateUIForAutonomousEditingState(this.isAutonomousEditingEnabled());

        setAcceptButtonVisible(false); // usually, the editable component configures acceptance
        setRejectButtonVisible(true);

        extenderShouldBeInitialized = true; // the extending class should finish its constructor imediately after this call
    }

    @Override
    protected void updateUIForEditingState(boolean isEditing) {
        updateUIForEditingState(isEditing, false);
    }

    protected void updateUIForEditingState(boolean isEditing, boolean requestFocus) {

        // normal state
        setVisibility(new Component[]{valueLabel}, !isEditing);

        // edit state
        setVisibility(new Component[]{editorPlaceholder}, isEditing);

        if (acceptButtonVisible) {
            acceptChangesButton.setVisible(isEditing && this.isAutonomousEditingEnabled());
        }
        if (rejectButtonVisible) {
            rejectChangesButton.setVisible(isEditing && this.isAutonomousEditingEnabled());
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
            //needs a space of padding to prevent cut off (https://bugs.openjdk.java.net/browse/JDK-4262130?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel)
            valueLabel.setText("Not Set ");
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

        if (extenderShouldBeInitialized) {
            this.updateEditorRepresentationForValue(value);
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
        if (this.isEditing()) {
            return this.getValueFromEditor();
        }
        return value;
    }

    private void initUI() {

        this.setLayout(new MigLayout("insets 0, hidemode 3, gapx 0, gapy 1"));

        /**
         * A JLabel that autoellipsizes
         */
        valueLabel = new JLabel() {
            int maxChars = 30;

            @Override
            public void setText(String s) {
                if (s.length() > maxChars) {
                    this.setToolTipText(s);
                    super.setText(s.substring(0, maxChars - 3) + "...");
                } else {
                    super.setText(s);
                    this.setToolTipText(null);
                }
            }
        };
        valueLabel.setFocusable(true);

        editorPlaceholder = new JPanel();
        editorPlaceholder.setOpaque(false);
        editorPlaceholder.setLayout(new MigLayout("insets 0, hidemode 3"));

        this.add(valueLabel);
        this.add(editorPlaceholder);

        rejectChangesButton = generateRejectButton(this);
        acceptChangesButton = generateAcceptButton(this);

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

        invalidLabel = new JLabel();
        invalidLabel.setForeground(Color.red);
        invalidLabel.setFont(invalidLabel.getFont().deriveFont(Font.PLAIN).deriveFont(10f));
        this.add(invalidLabel, "newline");
        //invalidLabel.setVisible(false);
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

    public void addSaveFocusListener(JComponent c) {
        c.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                if (OnClickEditableField.this.isAutonomousEditingEnabled()) {
                    saveWithValidationWarning();
                    setEditing(false);
                }
            }
        });
    }

    /**
     * Validate as usual, but show the invalid label if the value isn't valid
     *
     * @return Whether the current value is valid
     */
    @Override
    public boolean validateCurrentValue() {

        boolean result = super.validateCurrentValue();
        if (this.getValidator() != null) {
            String description = this.getValidator().getDescriptionOfValidValue();
            invalidLabel.setText(description);
            invalidLabel.setVisible(!result);
        } else {
            invalidLabel.setText("");
        }
        return result;
    }

    void saveWithValidationWarning() {
        if (!this.isEditing()) {
            return;
        }
        if (setValueFromEditor()) {
            invalidLabel.setVisible(false);

        } else {
            String description = this.getValidator().getDescriptionOfValidValue();
            invalidLabel.setText(description);
            invalidLabel.setVisible(true);
        }
        return;
    }

    /**
     * Set the field's edit state.
     *
     * @param isEditing The field's edit state.
     */
    @Override
    public void setEditing(boolean isEditing) {
        super.setEditing(isEditing);
        invalidLabel.setVisible(false);
        this.updateUI();
    }

    public void addFieldChangeKeyListener(final JTextField fieldToListenOn) {
        // Listen for changes in the text
        fieldToListenOn.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                OnClickEditableField.this.fireFieldEditedEvent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                OnClickEditableField.this.fireFieldEditedEvent();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                OnClickEditableField.this.fireFieldEditedEvent();
            }
        });
    }

    public void addSaveAndCancelKeyListeners(JComponent c) {
        c.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (OnClickEditableField.this.isAutonomousEditingEnabled()) {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER || e.getKeyChar() == KeyEvent.VK_TAB) {
                        saveWithValidationWarning();
                        setEditing(false);
                        return;
                    }

                    if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        OnClickEditableField.this.setEditing(false);
                    }
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
        updateButtonPositions();
    }

    /**
     * Hide helper buttons. Assumes autonomous editing is turned off (and never
     * turned back on).
     *
     * @param b Whether to permit autonomous editing.
     */
    @Override
    public void setAutonomousEditingEnabled(boolean b) {
        super.setAutonomousEditingEnabled(b);
        if (!b) {
            acceptChangesButton.setVisible(false);
            rejectChangesButton.setVisible(false);
        }
    }

    public void setAcceptButtonVisible(boolean b) {

        acceptButtonVisible = b;
        if (b && editing) {
            acceptChangesButton.setVisible(b);
        }
        updateButtonPositions();
    }

    private void updateButtonPositions() {
        if (rejectChangesButton.isVisible() && acceptChangesButton.isVisible()) {
            rejectChangesButton.putClientProperty("JButton.segmentPosition", "first");
            acceptChangesButton.putClientProperty("JButton.segmentPosition", "last");
        } else {
            rejectChangesButton.putClientProperty("JButton.segmentPosition", "only");
            acceptChangesButton.putClientProperty("JButton.segmentPosition", "only");
        }

    }

}
