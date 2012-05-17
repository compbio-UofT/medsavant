/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyVetoException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jidesoft.pane.CollapsiblePane;

import javax.swing.JComponent;
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

    public Component addTop(Component ipan) {
        container.add(Box.createVerticalStrut(10));
        container.add(ipan);
        return ipan;
    }

    protected void addSubInfoPanel(InfoSubPanel ipan) {
        container.remove(glue);

        container.add(Box.createVerticalStrut(10));

        if (ipan.showHeader()) {
            container.add(ViewUtil.center(new JLabel(ipan.getName())));
        }
        JPanel p = ipan.getInfoPanel();

        container.add(p);

        container.add(glue);
    }
}
