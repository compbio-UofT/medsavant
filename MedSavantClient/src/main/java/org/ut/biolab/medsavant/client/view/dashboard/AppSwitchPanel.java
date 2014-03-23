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
package org.ut.biolab.medsavant.client.view.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AppSwitchPanel extends JPanel {

    private boolean isSwitching;
    private int selectedIndex;
    private List<LaunchableApp> apps;

    int maxApps = 5;

    public AppSwitchPanel() { this(null); }
    
    public AppSwitchPanel(JComponent componentToListenOn) {

        this.setOpaque(false);

        KeyStroke appSwitchStopListeningKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_META, 0, true);
        Action appSwitchStopAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Stop");
                setSwitching(false);
            }
        };
        KeyStroke appRightSwitchKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_MASK, true);
        Action appRightSwitchAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Right");
                setSwitching(true);
                shiftRight();
            }
        };
        KeyStroke appLeftSwitchKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.META_MASK, true);
        Action appLeftSwitchAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Left");
                setSwitching(true);
                shiftLeft();
            }
        };
        
        if (componentToListenOn == null) {
            componentToListenOn = this;
        }
        
        componentToListenOn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(appSwitchStopListeningKeystroke, "SwitchAppStop");
        componentToListenOn.getActionMap().put("SwitchAppStop", appSwitchStopAction);
        componentToListenOn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(appLeftSwitchKeystroke, "SwitchAppLeft");
        componentToListenOn.getActionMap().put("SwitchAppLeft", appLeftSwitchAction);
        componentToListenOn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(appRightSwitchKeystroke, "SwitchAppRight");
        componentToListenOn.getActionMap().put("SwitchAppRight", appRightSwitchAction);
    }

    private void shiftLeft() {
        if (!this.isSwitching) { return; }
        if (selectedIndex == 0) {
            selectedIndex = apps.size() - 1;
        } else {
            selectedIndex--;
        }
        refresh();
    }

    private void shiftRight() {
        if (!this.isSwitching) { return; }
        if (selectedIndex == apps.size() - 1) {
            selectedIndex = 0;
        } else {
            selectedIndex++;
        }
        refresh();
    }

    private void setSwitching(boolean isSwitching) {

        if (this.isSwitching != isSwitching) {
           
            // switching started
            if (isSwitching) {
                
                List<LaunchableApp> history = MedSavantFrame.getInstance().getDashboard().getLaunchHistory();
                if (history.isEmpty()) { 
                    return; 
                }
                selectedIndex = 0;
                
                apps = history.subList(0, Math.min(history.size(), maxApps));
            
            // switching ended
            } else {
                MedSavantFrame.getInstance().getDashboard().launchApp(apps.get(selectedIndex));
            }

            this.isSwitching = isSwitching;
            refresh();
        }

    }

    public static void main(String[] arg) {
        JFrame f = new JFrame();
        AppSwitchPanel t = new AppSwitchPanel();
        f.add(t);
        f.pack();
        f.setVisible(true);
    }

    private void refresh() {
        this.removeAll();
        if (this.isSwitching) {
            int shadowSize = 10;
            JPanel p = ViewUtil.getRoundedShadowedPanel(Color.white, new Color(245,245,245), 0.9f, 10, new Color(230,230,230), shadowSize);
            p.setLayout(new MigLayout("insets 30"));
            
            int appNum = 0;
            for (LaunchableApp app : apps) {
                
                p.add(new ImagePanel(app.getIcon().getImage()),String.format("cell %d 0",appNum));
                JLabel l = new JLabel(app.getName());
                if (appNum == selectedIndex) {
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                }
                p.add(l,String.format("cell %d 1, center",appNum));
                
                appNum++;
            }
            
            this.setLayout(new MigLayout("fillx, filly, center"));
            this.add(p,"center, growx 1.0");
            p.repaint();
        }
        this.updateUI();
    }

}
