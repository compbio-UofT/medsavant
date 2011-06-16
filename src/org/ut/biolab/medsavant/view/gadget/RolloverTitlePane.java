/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.swing.NullPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RolloverTitlePane extends NullPanel {
    private JLabel _messageLabel;
    private JPanel _buttonPanel;
    private CardLayout _layout;

    public RolloverTitlePane(CollapsiblePane pane) {
        _layout = new CardLayout();

        setLayout(_layout);
        _messageLabel = new JLabel("");
        _messageLabel.setForeground(null);
        _messageLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        add(_messageLabel, "Message");
        _buttonPanel = new NullPanel();
        _buttonPanel.setLayout(new JideBoxLayout(_buttonPanel, JideBoxLayout.X_AXIS));
        _buttonPanel.add(Box.createGlue(), JideBoxLayout.VARY);
        add(_buttonPanel, "Buttons");
        pane.addPropertyChangeListener("rollover", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (Boolean.TRUE.equals(evt.getNewValue())) {
                    showButtons();
                }
                else {
                    showMessage();
                }
            }
        });
        JideSwingUtilities.setOpaqueRecursively(this, false);
    }

    public void setMessage(String message) {
        _messageLabel.setText(message);
    }

    public void showMessage() {
        _layout.show(this, "Message");
    }

    public void showButtons() {
        _layout.show(this, "Buttons");
    }

    public void addButton(AbstractButton button) {
        _buttonPanel.add(button);
    }

    public void addButton(AbstractButton button, int index) {
        _buttonPanel.add(button, index);
    }

    public void removeButton(AbstractButton button) {
        _buttonPanel.remove(button);
    }

    public void removeButton(int index) {
        _buttonPanel.remove(index);
    }
}