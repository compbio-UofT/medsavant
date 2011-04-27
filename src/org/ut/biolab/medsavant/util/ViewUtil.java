/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.util;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class ViewUtil {

    public static JPanel createClearPanel() {
        return (JPanel) clear(new JPanel());
    }

    public static JComponent clear(JComponent c) {
        c.setOpaque(false);
        return c;
    }

}
