/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
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
        
        KeyStroke escapeListeningKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, KeyEvent.META_MASK, true);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Escape");
                if (isSwitching) {
                    selectedIndex = -1;
                     setSwitching(false);
                }
            }
        };
        
        KeyStroke dashBoardListeningKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.META_MASK, true);
        Action dashBoardAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Dash");
                MedSavantFrame.getInstance().getDashboard().goHome();
                if (isSwitching) {
                    selectedIndex = -1;
                     setSwitching(false);
                }
            }
        };

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
        
        componentToListenOn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(dashBoardListeningKeystroke, "Dash");
        componentToListenOn.getActionMap().put("Dash", dashBoardAction);
        
        componentToListenOn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeListeningKeystroke, "Escape");
        componentToListenOn.getActionMap().put("Escape", escapeAction);
        
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
                System.out.println(history);
                if (history.isEmpty()) { 
                    //System.out.println("NO HISTORY, NOT SWITCHING");
                    return; 
                }
                selectedIndex = 0;
                
                apps = history.subList(0, Math.min(history.size(), maxApps));
            
            // switching ended
            } else {
                if (selectedIndex != -1) {
                    MedSavantFrame.getInstance().getDashboard().launchApp(apps.get(selectedIndex));
                }
                //System.out.println("SWITCH ENDED");
            }

            this.isSwitching = isSwitching;
            refresh();
        } else {
            //System.out.println("SORRY, NOT SWITCHING");
        }

    }

    private void refresh() {
        this.removeAll();
        if (this.isSwitching) {
            int shadowSize = 10;
            JPanel p = ViewUtil.getRoundedShadowedPanel(Color.white, new Color(245,245,245), 0.9f, 10, new Color(230,230,230), shadowSize);
            p.setLayout(new MigLayout("insets 30"));
            
            int appNum = 0;
            for (LaunchableApp app : apps) {
                
                p.add(new ImagePanel(ViewUtil.getScaledInstance(app.getIcon().getImage(), 128, 128, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true)),String.format("cell %d 0",appNum));
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
