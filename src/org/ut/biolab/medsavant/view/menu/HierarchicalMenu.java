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
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class HierarchicalMenu extends JPanel {

    private final JPanel container;
    private JPanel level2MenuContainer;
    private JPanel level1MenuContainer;
    private final Map<String, SectionView> tabMap;
    private JPanel currentPanel;
    private final TreeMap<String, JToggleButton[]> level2ComponentMap;
    private ButtonGroup firstLevelButtonGroup;
    private JComboBox level1Dropdown;

    public HierarchicalMenu(JPanel container) {
        this.container = container;
        this.tabMap = new TreeMap<String, SectionView>();
        this.level2ComponentMap = new TreeMap<String, JToggleButton[]>();

        initGUI();
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());
        level1MenuContainer = ViewUtil.createClearPanel();
        level1MenuContainer.setLayout(new BoxLayout(level1MenuContainer, BoxLayout.X_AXIS));
        
        level1Dropdown = new JComboBox();
        level1Dropdown.setMaximumSize(new Dimension(200,50));
        level1Dropdown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeSection((String) ((JComboBox) e.getSource()).getSelectedItem());
            }
            
        });
        level1MenuContainer.add(level1Dropdown);
        
        level2MenuContainer = ViewUtil.createClearPanel();
        level2MenuContainer.setLayout(new BoxLayout(level2MenuContainer, BoxLayout.X_AXIS));
        
        JPanel menuPanel = ViewUtil.getBannerPanel();
        menuPanel.add(level1MenuContainer);
        menuPanel.add(level2MenuContainer);
        
        this.add(menuPanel,BorderLayout.NORTH);
        firstLevelButtonGroup = new ButtonGroup();
    }

    public void addSection(SectionView section) {
        addToFirstLevelMenu(section);
        tabMap.put(section.getName(), section);
        level2ComponentMap.put(section.getName(), getSecondLevelComponents(section.getSubSections()));
    }

    private JToggleButton[] getSecondLevelComponents(SubSectionView[] subSections) {
        JToggleButton[] components = new JToggleButton[subSections.length];
        int i = 0;
        for (final SubSectionView t : subSections) {
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

    class MenuPanel extends JPanel {
        public MenuPanel() { this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS)); }
        public void paintComponent(Graphics g) {
            PaintUtil.paintLightMenu(g, this);
        }
    }
    
    /*
    class BottomMenuPanel extends JPanel {
        public BottomMenuPanel() { this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS)); }
        public void paintComponent(Graphics g) {
            PaintUtil.paintDarkMenu(g, this);
        }
    }
     */
    
    /*
    public void paintComponent(Graphics g) {
        PaintUtil.paintLightMenu(g, this);
    }
     * 
     */

    /*
    public void addDualTab(Level1Section tab) {
        addToFirstLevelMenu(tab);
        tabMap.put(tab.getName(), tab);
        level2ComponentMap.put(tab.getName(), getSecondLevelComponents(tab));
    }
     * 
     */

    public void changeSection(String sectionName) {
        if (level2ComponentMap.get(sectionName) == null) { return; }
        level2MenuContainer.removeAll();
        ButtonGroup bg = new ButtonGroup();
        for (JToggleButton button : level2ComponentMap.get(sectionName)) {
            if (ViewUtil.isMac()) {
                button.putClientProperty( "JButton.buttonType", "segmentedGradient" );
                button.putClientProperty( "JButton.segmentPosition", "middle" );
                //button.putClientProperty( "JComponent.sizeVariant", "mini" );
            }
            level2MenuContainer.add(button);
            bg.add(button);
        }
        level2MenuContainer.updateUI();
    }

    private void addToFirstLevelMenu(SectionView tab) {

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
        level1Dropdown.addItem(tab.getName());

        //firstLevelButtonGroup.add(button);
    }

    /*
    private JToggleButton[] getSecondLevelComponents(Level1Section dt) {
        JToggleButton[] components = new JToggleButton[dt.getTabs().size()];
        int i = 0;
        for (final Level2Section t : dt.getTabs()) {
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
     * 
     */

    private void showPanel(JPanel panel) {

        if (currentPanel != null) {
            container.remove(currentPanel);
        }

        container.setLayout(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        currentPanel = panel;

        container.updateUI();
    }

    private void showPanelForTab(SubSectionView t) {
        showPanel(t.getView());
    }
}
