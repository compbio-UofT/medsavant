/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * A helper class for layering panels. Overcomes the limitation
 * of JLayeredPane not having a default Layout Manager. All panels
 * added to the stack are laid out in full width and height within
 * the panel.
 * @author mfiume
 */
public class JPanelStack extends JPanel {

    private final Stack<JPanel> panels;
    private final JLayeredPane layeredPane;

    public JPanelStack() {

        layeredPane = new JLayeredPane();
        layeredPane.setOpaque(true);

        this.setLayout(new BorderLayout());
        this.add(layeredPane, BorderLayout.CENTER);

        layeredPane.setPreferredSize(new Dimension(300, 300));

        panels = new Stack<JPanel>();

        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                resetBounds();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
                resetBounds();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }

        });
    }

    private void resetBounds() {
        for (JPanel p : panels) {
            setBounds(p);
        }
    }

    public void push(JPanel p) {

        layeredPane.add(p, new Integer(panels.size()));
        panels.push(p);
        setBounds(p);
        System.out.println("Added panel to the stack, total = " + panels.size());
    }

    public void pop() {
        layeredPane.remove(panels.pop());
    }

    public static void main(String[] argv) {

        JPanelStack pstack = new JPanelStack();

        JPanel p = new JPanel();
        p.setBackground(Color.red);
        p.add(new JButton("I'm on the first layer"));

        pstack.push(p);
        p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new MigLayout("fillx, filly, center"));
        p.add(new JButton("I'm on the second layer"));

        pstack.push(p);
        JFrame f = new JFrame();
        f.setContentPane(pstack);
        f.pack();
        f.show();
        System.out.println("Frame shown");
    }

    private void setBounds(JPanel p) {
        p.setBounds(0, 0, this.getWidth(), this.getHeight());
        p.updateUI();
    }

}
