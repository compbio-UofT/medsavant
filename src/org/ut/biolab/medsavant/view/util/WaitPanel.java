/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class WaitPanel extends JPanel {
    private final JLabel statusLabel;
    private final JProgressBar prog;

    public WaitPanel(String message) {
        this.setBackground(Color.lightGray);
        this.setBorder(ViewUtil.getHugeBorder());
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        prog = new JProgressBar();
        prog.setIndeterminate(true);
        
        statusLabel = new JLabel("");
        
        this.add(Box.createVerticalGlue());
        this.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel(message)));
        this.add(ViewUtil.getCenterAlignedComponent(statusLabel));
        this.add(prog);
        this.add(Box.createVerticalGlue());
    }

    
    public synchronized void setStatus(String status) {
        this.statusLabel.setText(status);
    }

    public void setComplete() {
        prog.setIndeterminate(false);
        prog.setValue(prog.getMaximum());
    }
    
}
