/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 *
 * @author jim
 */
public class SplitScreenPanel extends JPanel {

    
    private Component mainPanel;
    private boolean split = false;

    public SplitScreenPanel(Component mainPanel) {        
        this.mainPanel = mainPanel;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(mainPanel);
    }
    
    public void splitScreen(JPanel p) {
        split = true;
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, p);
        splitPane.setResizeWeight(1);
        this.removeAll();
        this.add(splitPane);
        this.revalidate();
        this.repaint();
    }

    public void unsplitScreen() {
        split = false;
        this.removeAll();
        this.add(mainPanel);
        this.revalidate();
        this.repaint();
    }

    public boolean isSplit() {
        return split;
    }
}
