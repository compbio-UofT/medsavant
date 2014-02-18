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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JComponent;
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
public class StackableJPanelContainer extends JPanel {

    private final Stack<JPanel> panels;
    private final JLayeredPane layeredPane;

    public StackableJPanelContainer() {

        this.setOpaque(false);
        
        layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);

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
    
    public static int counter;

    
    @Override
    public Component add(Component c) {
        throw new UnsupportedOperationException("Cannot add to StackableJPanelContainer, use push instead");
    }  
    
     public void push(JPanel p) {
        push(StackableJPanelFactory.convertComponentToStackablePanel(p));
    }
    
    public void push(StackableJPanel p) {
        
        p.setParentContainer(this);
        layeredPane.add(p, new Integer(counter++));
        panels.push(p);
        setBounds(p);
        this.updateUI();
    }

    public void pop() {
        remove(panels.peek());
    }
    
    public void remove(StackableJPanel p) {
        layeredPane.remove(p);
        panels.remove(p);
        this.updateUI();
    }

    public static void main(String[] argv) {

        StackableJPanelContainer pstack = new StackableJPanelContainer();

        StackableJPanel p = new StackableJPanel();
        p.setBackground(Color.red);
        p.add(new JButton("I'm on the first layer"));

        pstack.push(p);
        p = new StackableJPanel();
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
