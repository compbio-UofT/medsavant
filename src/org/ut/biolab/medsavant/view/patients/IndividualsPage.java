/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients;

import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.Page;

/**
 *
 * @author mfiume
 */
public class IndividualsPage implements Page {

    public String getName() {
        return "Individuals";
    }

    public Component getView() {
        return new JPanel();
    }

    public Component getBanner() {
        return new JPanel();
    }
    
}
