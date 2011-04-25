/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import org.ut.biolab.medsavant.view.View;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class MedSavant extends JFrame {

    public MedSavant() {
        super("MedSavant Prototype 1");

        this.setLayout(new BorderLayout());
        JPanel view = new View();
        this.add(view, BorderLayout.CENTER);

    }

}
