/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget;

import com.jidesoft.dashboard.AbstractGadget;
import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetComponent;
import javax.swing.JComponent;
import org.ut.biolab.medsavant.view.gadget.filter.FilterPanel;
import org.ut.biolab.medsavant.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class GenericGadget extends AbstractGadget {

    private final GadgetContentGenerator generator;

    public GenericGadget(String title, GadgetContentGenerator generator) {
        super(title);
        this.generator = generator;
    }

    public GadgetComponent createGadgetComponent() {
        final CollapsibleFrameGadget gadget = new CollapsibleFrameGadget(this);
        gadget.getContentPane().add(generator.generateGadgetContent());
        return gadget;
    }

    public void disposeGadgetComponent(GadgetComponent gc) {
        return;
    }
}
