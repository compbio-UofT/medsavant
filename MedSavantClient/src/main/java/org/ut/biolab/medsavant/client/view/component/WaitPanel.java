/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class WaitPanel extends JPanel {
    private final JLabel statusLabel;
    private final ProgressWheel prog;

    public WaitPanel(String message) {

        Color c = getBackground();
        if (c == null) {
            c = Color.WHITE;
        }
        setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 215));

        setOpaque(false);
        setBorder(ViewUtil.getHugeBorder());
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        prog = ViewUtil.getIndeterminateProgressBar();
        prog.setMaximumSize(new Dimension(200,25));

        statusLabel = new JLabel("");

        add(Box.createVerticalGlue());
        add(ViewUtil.getCenterAlignedComponent(ViewUtil.getDialogLabel(message, Color.BLACK)));
        add(ViewUtil.getCenterAlignedComponent(statusLabel));
        add(Box.createVerticalStrut(5));
        add(ViewUtil.getCenterAlignedComponent(prog));
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

    public void setProgressBarVisible(boolean b) {
        prog.setVisible(b);
    }

    public void setTextColor(Color c) {
        statusLabel.setForeground(c);
    }

    @Override
    public void paintComponent(Graphics g){
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public synchronized void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void setComplete() {
        prog.setComplete();
    }

    public void setIndeterminate() {
    }
}