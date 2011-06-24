/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class DualMenu extends JPanel {

    private final JPanel container;
    private JPanel secondLevelMenu;
    private JPanel firstLevelMenu;
    private final Map<String, DualTab> tabMap;
    private JPanel currentPanel;
    private final TreeMap<String, JToggleButton[]> secondLevelComponentMap;
    private ButtonGroup firstLevelButtonGroup;
    private JComboBox sectionDropDown;

    public DualMenu(JPanel container) {
        this.container = container;
        this.tabMap = new TreeMap<String, DualTab>();
        this.secondLevelComponentMap = new TreeMap<String, JToggleButton[]>();

        initGUI();
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());
        firstLevelMenu = ViewUtil.createClearPanel();
        firstLevelMenu.setLayout(new BoxLayout(firstLevelMenu, BoxLayout.X_AXIS));
        
        sectionDropDown = new JComboBox();
        sectionDropDown.setMaximumSize(new Dimension(200,50));
        sectionDropDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeSection((String) ((JComboBox) e.getSource()).getSelectedItem());
            }
            
        });
        firstLevelMenu.add(sectionDropDown);
        
        secondLevelMenu = ViewUtil.createClearPanel();
        secondLevelMenu.setLayout(new BoxLayout(secondLevelMenu, BoxLayout.X_AXIS));
        
        TopMenuPanel top = new TopMenuPanel(); top.add(firstLevelMenu); top.add(secondLevelMenu);
        //BottomMenuPanel bottom = new BottomMenuPanel(); bottom.add(secondLevelMenu);
        
        this.add(top,BorderLayout.NORTH);
        //this.add(Box.createHorizontalStrut(30));
        //this.add(bottom,BorderLayout.SOUTH);
        
        //this.add(Box.createHorizontalGlue());
        firstLevelButtonGroup = new ButtonGroup();
    }

    class TopMenuPanel extends JPanel {
        public TopMenuPanel() { this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS)); }
        public void paintComponent(Graphics g) {
            PaintUtil.paintLightMenu(g, this);
        }
    }
    
    class BottomMenuPanel extends JPanel {
        public BottomMenuPanel() { this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS)); }
        public void paintComponent(Graphics g) {
            PaintUtil.paintDarkMenu(g, this);
        }
    }
    
    /*
    public void paintComponent(Graphics g) {
        PaintUtil.paintLightMenu(g, this);
    }
     * 
     */

    public void addDualTab(DualTab tab) {
        addToFirstLevelMenu(tab);
        tabMap.put(tab.getName(), tab);
        secondLevelComponentMap.put(tab.getName(), getSecondLevelComponents(tab));
    }

    public void changeSection(String sectionName) {
        System.out.println("Changing section to : " + sectionName);
        
        if (secondLevelComponentMap.get(sectionName) == null) { return; }
        secondLevelMenu.removeAll();
        ButtonGroup bg = new ButtonGroup();
        for (JToggleButton button : secondLevelComponentMap.get(sectionName)) {
            if (ViewUtil.isMac()) {
                button.putClientProperty( "JButton.buttonType", "segmentedGradient" );
                button.putClientProperty( "JButton.segmentPosition", "middle" );
                //button.putClientProperty( "JComponent.sizeVariant", "mini" );
            }
            secondLevelMenu.add(button);
            bg.add(button);
        }
        secondLevelMenu.updateUI();
    }

    private void addToFirstLevelMenu(final DualTab tab) {

        /*
        JToggleButton button = ViewUtil.getMenuToggleButton(tab.getName());
        button.setOpaque(false);

        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeSection(tab.getName());
            }
        });

        firstLevelMenu.add(button);
         * 
         */
        sectionDropDown.addItem(tab.getName());

        //firstLevelButtonGroup.add(button);
    }

    private JToggleButton[] getSecondLevelComponents(DualTab dt) {
        JToggleButton[] components = new JToggleButton[dt.getTabs().size()];
        int i = 0;
        for (final Tab t : dt.getTabs()) {
            JToggleButton c = new JToggleButton(t.getName());
            c.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showPanelForTab(t);
                }
            });

            components[i++] = c;
        }
        return components;
    }

    private void showPanel(JPanel panel) {

        if (currentPanel != null) {
            container.remove(currentPanel);
        }

        container.setLayout(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        currentPanel = panel;

        container.updateUI();
    }

    private void showPanelForTab(Tab t) {
        showPanel(t.getPanel());
    }
}
