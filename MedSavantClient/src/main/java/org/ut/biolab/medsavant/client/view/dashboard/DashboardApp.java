package org.ut.biolab.medsavant.client.view.dashboard;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public interface DashboardApp {

    public JPanel getView();

    void viewWillUnload();

    void viewWillLoad();

    void viewDidUnload();

    void viewDidLoad();

    ImageIcon getIcon();

    String getName();

}
