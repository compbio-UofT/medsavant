/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.subview.MultiSection;
import org.ut.biolab.medsavant.client.view.subview.SubSection;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;

/**
 *
 * @author mfiume
 */
public abstract class MultiSectionDashboardApp implements DashboardApp {

    private MultiSection multiSection;
    private JPanel view;

    public MultiSectionDashboardApp(MultiSection v) {
        this.multiSection = v;
    }

    @Override
    public JPanel getView() {
        if (view == null) {

            view = new JPanel();
            view.setLayout(new BorderLayout());

            JTabbedPane tabs = new JTabbedPane();
            for (SubSection sub : multiSection.getSubSections()) {
                tabs.add(sub.getPageName(), sub.getView());
            }

            if (multiSection.getPersistentPanels() != null 
                    && multiSection.getPersistentPanels().length > 0) {
                PeekingPanel pp = new PeekingPanel("",BorderLayout.EAST,multiSection.getPersistentPanels()[0],true);
                pp.setToggleBarVisible(false);
                
                view.add(pp, BorderLayout.WEST);
            }

            view.add(tabs, BorderLayout.CENTER);

        }
        return view;
    }

}
