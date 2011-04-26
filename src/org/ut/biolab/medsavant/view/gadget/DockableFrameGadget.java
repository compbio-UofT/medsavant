/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.gadget;

import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetComponent;
import com.jidesoft.docking.DockableFrame;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;

/**
 *
 * @author mfiume
 */
public class DockableFrameGadget extends DockableFrame implements GadgetComponent {

    private Gadget _gadget;
    private Map<String, String> _settings;

    public DockableFrameGadget(Gadget gadget) {
        super(gadget.getName(), gadget.getIcon());
        _gadget = gadget;
        setOpaque(true);
        setCloseAction(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                DockableFrameGadget gadgetComponent = DockableFrameGadget.this;
                gadgetComponent.getGadget().getGadgetManager().hideGadget(gadgetComponent);
            }
        });
    }

    public Gadget getGadget() {
        return _gadget;
    }

    public Map getSettings() {
        return _settings;
    }

    public void setSettings(Map settings) {
        _settings = settings;
    }
}
