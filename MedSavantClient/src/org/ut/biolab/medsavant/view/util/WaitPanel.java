/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

        // intercept events so that the underlayers can't access them
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }

        });

        this.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
            }

        });

        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }

        });

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
