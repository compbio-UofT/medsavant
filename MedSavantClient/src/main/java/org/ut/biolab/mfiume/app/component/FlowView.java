package org.ut.biolab.mfiume.app.component;

import java.awt.FlowLayout;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class FlowView extends JPanel {

    public FlowView() {
        FlowLayout flow = new FlowLayout(FlowLayout.LEFT, 5, 5);
        this.setOpaque(false);
        this.setBorder(null);
        flow.setAlignment(FlowLayout.LEFT);
        this.setLayout(flow);
    }
}
