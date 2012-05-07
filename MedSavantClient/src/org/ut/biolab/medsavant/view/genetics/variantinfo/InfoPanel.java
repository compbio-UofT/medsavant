package org.ut.biolab.medsavant.view.genetics.variantinfo;

import com.jidesoft.pane.CollapsiblePane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class InfoPanel extends CollapsiblePane {

    private final JPanel container;
    private final Component glue;

    public InfoPanel(String panelName) {
        super(panelName);

        this.setStyle(CollapsiblePane.PLAIN_STYLE);

        try {
            this.setCollapsed(true);
        } catch (PropertyVetoException ex) {
        }

        //this.setPreferredSize(new Dimension(200,10));
        //this.setBorder(ViewUtil.getBigBorder());
        //this.setBackground(ViewUtil.getTertiaryMenuColor());
        this.setLayout(new BorderLayout());

        container = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(container);

        //this.add(ViewUtil.getClearBorderlessJSP(container),BorderLayout.CENTER);
        this.add(Box.createHorizontalGlue());
        this.add(container);

        glue = Box.createVerticalGlue();
        container.add(glue);

    }

    protected void addSubInfoPanel(InfoSubPanel ipan) {
        container.remove(glue);

        container.add(Box.createVerticalStrut(10));

        if (ipan.showHeader()) {
            container.add(ViewUtil.center(ViewUtil.getWhiteLabel(ipan.getName())));
        }
        JPanel p = ipan.getInfoPanel();

        container.add(p);

        container.add(glue);
    }
}
