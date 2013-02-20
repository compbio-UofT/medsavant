/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.util.MiscUtils;


/**
 *
 * @author mfiume
 */
public class WaitPanel extends JPanel {
    private final JLabel statusLabel;
    private final JProgressBar prog;

    public WaitPanel(String message) {

        Color c = getBackground();
        if (c == null) {
            c = Color.WHITE;
        }
        setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 215));

        setOpaque(false);
        setBorder(ViewUtil.getHugeBorder());
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        prog = new JProgressBar();
        if (ClientMiscUtils.MAC) {
            prog.putClientProperty("JProgressBar.style", "circular");
        }
        prog.setIndeterminate(true);
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
        prog.setIndeterminate(false);
        prog.setValue(prog.getMaximum());
    }

    public void setIndeterminate() {
        prog.setIndeterminate(true);
    }
}
