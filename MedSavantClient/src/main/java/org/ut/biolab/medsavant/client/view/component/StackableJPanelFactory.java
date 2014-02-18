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
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class StackableJPanelFactory {

    
    public static StackableJPanel createModalDialog(String notificationMsg) {
        
        StackableJPanelContainer dialog = new StackableJPanelContainer();
        
        JPanel semiTransparentPanel = ViewUtil.getSemiTransparentPanel(new Color(0,0,0),0.75f);
        ViewUtil.consumeMouseEventsForComponent(semiTransparentPanel);
        semiTransparentPanel.setLayout(new MigLayout("center, wrap, fillx, filly"));
        
        dialog.push(StackableJPanelFactory.convertComponentToStackablePanel(semiTransparentPanel));
        
        JPanel contentContainer = ViewUtil.getClearPanel();
        contentContainer.setLayout(new MigLayout("insets 50, fillx, filly, center"));
        
        JPanel opaquePortion = ViewUtil.getClearPanel();
        contentContainer.add(opaquePortion,"width 500, height 300");
        
        opaquePortion.add(getHugeWhiteLabel(notificationMsg));
        
        JButton close = new JButton("OK");
        opaquePortion.add(close);
        
        dialog.push(StackableJPanelFactory.convertComponentToStackablePanel(contentContainer));
        
        final StackableJPanel result = StackableJPanelFactory.convertComponentToStackablePanel(dialog);
        
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                result.popThis();
            }
            
        });
        
        return result;
    }
    
    public static StackableJPanel createTransientDialog(String notificationMsg, int timeout) {
        final StackableJPanel p = new StackableJPanel();
        p.setBackground(Color.red);
        
        p.add(new JLabel(notificationMsg));
        
        Timer timer = new Timer(timeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                p.popThis();
            }
            
        });
        timer.start(); 
        
        return p;
    }

    static StackableJPanel convertComponentToStackablePanel(Component c) {
         final StackableJPanel p = new StackableJPanel();
         p.setLayout(new BorderLayout());
         p.add(c,BorderLayout.CENTER);
         return p;
    }

    private static JLabel getHugeWhiteLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(ViewUtil.getDefaultFont(30));
        l.setForeground(Color.white);
        return l;
    }
    
}
