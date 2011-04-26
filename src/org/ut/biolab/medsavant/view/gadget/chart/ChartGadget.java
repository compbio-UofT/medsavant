/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget.chart;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.dashboard.AbstractGadget;
import com.jidesoft.dashboard.GadgetComponent;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.ut.biolab.medsavant.model.VariantRecordModel;
import org.ut.biolab.medsavant.view.gadget.DockableFrameGadget;

/**
 *
 * @author mfiume
 */
public class ChartGadget extends AbstractGadget {

    public ChartGadget() {
        super("Chart");
    }

    public GadgetComponent createGadgetComponent() {
        final DockableFrameGadget gadget = new DockableFrameGadget(this);
        gadget.getContentPane().add(new ChartPanel());
        return gadget;
    }

    public void disposeGadgetComponent(GadgetComponent gc) {
        return;
    }
}
