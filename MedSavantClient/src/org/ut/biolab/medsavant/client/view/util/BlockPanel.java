package org.ut.biolab.medsavant.client.view.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author mfiume
 */
public class BlockPanel extends JPanel {
    private final JButton actionButton;

    public BlockPanel(String message, ActionListener a) {

        Color c = getBackground();
        if (c == null) {
            c = Color.WHITE;
        }
        setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 215));

        setOpaque(false);
        setBorder(ViewUtil.getHugeBorder());
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        actionButton = ViewUtil.getSoftButton(message);
        actionButton.addActionListener(a);

        add(Box.createVerticalGlue());
        add(ViewUtil.getCenterAlignedComponent(actionButton));
        add(Box.createVerticalGlue());

        // intercept events so that the underlayers can't access them
        addMouseListener(new MouseListener() {

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

        addMouseWheelListener(new MouseWheelListener() {

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

    @Override
    public void paintComponent(Graphics g){
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public synchronized void setStatus(String status) {
        actionButton.setText(status);
    }
}
