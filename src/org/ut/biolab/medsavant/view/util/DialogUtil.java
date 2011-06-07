/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.util;

import javax.swing.JOptionPane;

/**
 *
 * @author mfiume
 */
public class DialogUtil {

    public static void displayErrorMessage(String msg, Exception ex) {
        JOptionPane p = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE);
        p.setVisible(true);
    }

}
