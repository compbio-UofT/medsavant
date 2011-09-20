/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.Insets;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.oldcontroller.SettingsController;

/**
 *
 * @author mfiume
 */
public class ComponentUtil {

    
    public static JTabbedPane getH1TabbedPane() {
        if (ViewUtil.isMac()) {
            JTabbedPane p = new JTabbedPane(JTabbedPane.TOP);
            p.putClientProperty(
                "Quaqua.Component.visualMargin", new Insets(3,-3,-4,-3)
            );
            return p;
        } else {
            return new JTabbedPane(JTabbedPane.TOP);
        }
    }
    
}
