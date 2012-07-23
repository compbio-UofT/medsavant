/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
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

    public WaitPanel(String message) {

        Color c = getBackground();
        if(c == null) c = Color.white;
        this.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 215));


        this.setOpaque(false);
        this.setBorder(ViewUtil.getHugeBorder());
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        prog = new JProgressBar();
        prog.setIndeterminate(true);
        prog.setMaximumSize(new Dimension(200,25));

        statusLabel = new JLabel("");

        this.add(Box.createVerticalGlue());
        this.add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel(message, Color.BLACK)));
        this.add(ViewUtil.getCenterAlignedComponent(statusLabel));
        this.add(prog);
        this.add(Box.createVerticalGlue());

    }

    public void paintComponent(Graphics g){

        g.setColor(this.getBackground());
        Rectangle r = this.getBounds();
        g.fillRect(r.x, r.y, r.width, r.height);

        super.paintComponent(g);
    }

    public synchronized void setStatus(String status) {
        this.statusLabel.setText(status);
    }

    public void setComplete() {
        prog.setIndeterminate(false);
        prog.setValue(prog.getMaximum());
    }

    public void setIndeterminate() {
        prog.setIndeterminate(true);
    }

}
