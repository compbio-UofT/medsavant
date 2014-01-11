/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.dashboard;

import java.util.Map;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public interface LaunchableApp {
    
    public JPanel getView();

    void viewWillUnload();

    void viewWillLoad();

    void viewDidUnload();

    void viewDidLoad();
    
    String getName();
    
    void didLogout();
    
    void didLogin();

}
