/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class WaitPanel extends JPanel {

    public WaitPanel() {
        this.setBackground(Color.lightGray);
        this.setLayout(new BorderLayout());
        JProgressBar prog = new JProgressBar();
        prog.setIndeterminate(true);
        this.add(prog, BorderLayout.NORTH);
    }
    
}
