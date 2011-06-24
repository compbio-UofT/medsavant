/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import java.awt.Component;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public interface Page {

    public String getName();

    public JPanel getView();

    public Component getBanner();

}
