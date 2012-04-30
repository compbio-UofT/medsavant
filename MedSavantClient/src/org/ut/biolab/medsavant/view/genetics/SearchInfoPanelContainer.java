package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.SearchVariantInfoPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class SearchInfoPanelContainer extends JPanel {
    private final JPanel container;
    private final Component glue;

    public SearchInfoPanelContainer() {

        this.setBorder(ViewUtil.getBigBorder());
        this.setBackground(ViewUtil.getTertiaryMenuColor());
        this.setLayout(new BorderLayout());

        container = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(container);

        this.add(ViewUtil.getClearBorderlessJSP(container),BorderLayout.CENTER);

        glue = Box.createVerticalGlue();
        container.add(glue);

        addVariantInfoPanel(new SearchVariantInfoPanel());
    }

    private void addVariantInfoPanel(SearchVariantInfoPanel ipan) {
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
