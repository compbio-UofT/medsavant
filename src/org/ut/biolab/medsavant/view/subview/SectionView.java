/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author mfiume
 */
public abstract class SectionView {
    private JTabbedPane pane;

    public abstract String getName();

    public abstract SubSectionView[] getSubSections();

    public abstract JPanel[] getPersistentPanels();

    public abstract Component getBanner();

    /*
    private void initPages() {
        SubSectionView[] pages = getSubSections();
        pane = new JTabbedPane(JTabbedPane.TOP);
        for (SubSectionView p : pages) {
            pane.addTab(p.getName(), p.getView());
        }

        this.setLayout(new BorderLayout());

        JPanel[] panels = getPersistentPanels();
        if (panels != null) {
            JTabbedPane perpane = new JTabbedPane();
            for (JPanel p : panels) {
                perpane.add(p.getName(), p);
            }
            
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                       pane, perpane);
            //splitPane.setOneTouchExpandable(true);
            splitPane.setResizeWeight(1.0);
            splitPane.setDividerLocation(splitPane.getSize().width
                             - splitPane.getInsets().right
                             - splitPane.getDividerSize()
                             - 300);
            
            this.add(splitPane,BorderLayout.CENTER);
        } else {
            this.add(pane, BorderLayout.CENTER);
        }

        Component banner = getBanner();
        if(banner != null){
            this.add(banner, BorderLayout.NORTH);
        }
    }
     * 
     */

}
