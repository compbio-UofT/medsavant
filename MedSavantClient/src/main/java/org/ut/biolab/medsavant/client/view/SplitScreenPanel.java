/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view;

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
