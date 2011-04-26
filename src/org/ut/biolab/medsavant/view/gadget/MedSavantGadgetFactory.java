/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.gadget;

import com.jidesoft.dashboard.AbstractGadget;
import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetComponent;
import com.jidesoft.dashboard.GadgetManager;
import com.jidesoft.docking.DockableFrame;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.ut.biolab.medsavant.view.gadget.DockableFrameGadget;

/**
 *
 * @author mfiume
 */
public class MedSavantGadgetFactory {

    static Gadget createGadget(String key, final JComponent c) {
        AbstractGadget dashboardElement = new AbstractGadget(key,
                null,
                null) {

            public GadgetComponent createGadgetComponent() {
                final DockableFrameGadget gadget = new DockableFrameGadget(this);
                //gadget.setMaximizable(true);
                //gadget.setAvailableButtons(DockableFrameGadget.BUTTON_ALL);
                gadget.getContentPane().add(c);
                return gadget;
            }

            public void disposeGadgetComponent(GadgetComponent component) {
                // do nothing in this case as we didn't allocate any resource in createGadgetComponent.
            }
        };
        dashboardElement.setDescription("Description is " + key);
        return dashboardElement;
    }

}
