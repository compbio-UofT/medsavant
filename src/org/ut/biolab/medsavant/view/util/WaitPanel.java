/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.Color;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author mfiume
 */
public class WaitPanel extends JPanel {
    private final JLabel statusLabel;
    private final JProgressBar prog;
    
    public WaitPanel(String message){
        this(message, null);
    }

    public WaitPanel(String message, Color foreground) {
        //this.setBackground(Color.lightGray);       
        this.setOpaque(false);
        this.setBorder(ViewUtil.getHugeBorder());
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        prog = new JProgressBar();
        prog.setIndeterminate(true);
        
        statusLabel = new JLabel("");
        
        this.add(Box.createVerticalGlue());
        this.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel(message, foreground)));
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
