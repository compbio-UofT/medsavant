/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.gadget;

/**
 *
 * @author mfiume
 */
import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetComponent;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePaneTitleButton;
import java.awt.BorderLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

public class CollapsibleFrameGadget extends CollapsiblePane implements GadgetComponent {
    private Gadget _gadget;

    private RolloverTitlePane _titlePane;
    private Map<String, String> _settings;

    public CollapsibleFrameGadget(Gadget gadget) {
        super(gadget.getName());
        _gadget = gadget;

        setTitleIcon(gadget.getIcon());
        setFocusable(true);
        setRequestFocusEnabled(true);
        setFocusPainted(false);
        setEmphasized(true);
        this.setLayout(new BorderLayout());

        _titlePane = new RolloverTitlePane(this);
        CollapsiblePaneTitleButton closeButton = new CollapsiblePaneTitleButton(this, null);
        closeButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                CollapsibleFrameGadget gadgetComponent = CollapsibleFrameGadget.this;
                gadgetComponent.getGadget().getGadgetManager().hideGadget(gadgetComponent);
            }
        });
        _titlePane.addButton(closeButton);
        setTitleComponent(_titlePane);
    }

    public Gadget getGadget() {
        return _gadget;
    }

    public void setMessage(String message) {
        _titlePane.setMessage(message);
    }

    public Map getSettings() {
        return _settings;
    }

    public void setSettings(Map settings) {
        _settings = settings;
    }

    public void addButton(AbstractButton button) {
        _titlePane.addButton(button);
    }

    public void addButton(AbstractButton button, int index) {
        _titlePane.addButton(button, index);
    }

    public void removeButton(AbstractButton button) {
        _titlePane.removeButton(button);
    }

    public void removeButton(int index) {
        _titlePane.removeButton(index);
    }
}
