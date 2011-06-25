/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import org.ut.biolab.medsavant.view.menu.Menu;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ViewController extends JPanel {

    private Banner banner;
    private SectionHeader sectionHeader;
    private SidePanel leftPanel;
    private Menu menu;
    private JPanel contentContainer;
    private PersistencePanel sectionPanel;
    private JToggleButton buttonSectionPanelController;
    private SectionView currentSection;

    public void changeSubSectionTo(SubSectionView view) {
        this.sectionHeader.setSubSection(view);
        
        SectionView parent = view.getParent();
        
        if (parent != currentSection) {
            if (parent.getPersistentPanels() != null) {
                sectionPanel.setVisible(true);
                buttonSectionPanelController.setVisible(true);
                buttonSectionPanelController.setEnabled(true);
                sectionPanel.setSectionPersistencePanels(view.getParent().getPersistentPanels());
            } else {
                sectionPanel.setVisible(false);
                buttonSectionPanelController.setVisible(false);
            }
        }
        currentSection = parent;
    }

    private static class SidePanel extends JPanel {

        public SidePanel() {
            this.setBackground(ViewUtil.getMenuColor());
            this.setBorder(ViewUtil.getSideLineBorder());
            
            //this.setPreferredSize(new Dimension(200,200));
            this.setLayout(new BorderLayout());
        }
        
        public void setContent(JPanel p) {
            this.add(p,BorderLayout.CENTER);
        }
    }
    
    private static class PersistencePanel extends SidePanel {
        private final JTabbedPane panes;

        public PersistencePanel() {
            panes = new JTabbedPane();
            this.setLayout(new BorderLayout());
            this.add(panes,BorderLayout.CENTER);
        }

        private void setSectionPersistencePanels(JPanel[] persistentPanels) {
            panes.removeAll();
            for (JPanel p : persistentPanels) {
                panes.addTab(p.getName(), p);
            }
        }
    }

    private static class Banner extends JPanel {

        public Banner() {
            this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
            this.setBorder(ViewUtil.getMediumBorder());
            this.add(ViewUtil.getTitleLabel("MedSavant"));
        }
        
        public void paintComponent(Graphics g) {
            PaintUtil.paintDarkMenu(g, this);
        }
    }

    private static class SectionHeader extends JPanel {

        private final JLabel title;
        private final JPanel sectionMenuPanel;
        private final JPanel subSectionMenuPanel;

        public SectionHeader() {
            this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
            this.setBorder(null);
            
            this.setBorder(ViewUtil.getMediumSideBorder());
            title = ViewUtil.getHeaderLabel(" ");
            this.add(title);
            sectionMenuPanel = ViewUtil.createClearPanel();
            subSectionMenuPanel = ViewUtil.createClearPanel();
            
            sectionMenuPanel.setLayout(new BoxLayout(sectionMenuPanel,BoxLayout.X_AXIS));
            subSectionMenuPanel.setLayout(new BoxLayout(subSectionMenuPanel,BoxLayout.X_AXIS));
            
            this.add(Box.createHorizontalGlue());
            this.add(subSectionMenuPanel);
            this.add(ViewUtil.getMediumSeparator()); 
            this.add(sectionMenuPanel);
          
            
        }
        
        public void paintComponent(Graphics g) {
            PaintUtil.paintLightMenu(g, this);
        }

        private void setTitle(String sectionName, String subsectionName) {
            title.setText(sectionName.toUpperCase() + " › " + subsectionName);
        }

        private void setSubSection(SubSectionView view) {
            setTitle(view.getParent().getName(),view.getName());
            
            subSectionMenuPanel.removeAll();
            sectionMenuPanel.removeAll();
            
            Component[] subsectionBanner = view.getBanner();
            Component[] sectionBanner = view.getParent().getBanner();
            
            if (subsectionBanner != null) {
                for (Component c : subsectionBanner)
                    subSectionMenuPanel.add(c);
            }
            if (sectionBanner != null) {
                for (Component c : sectionBanner)
                    sectionMenuPanel.add(c);
            }
        }
        
        
    }
    
    private static ViewController instance;

    public static ViewController getInstance() {
        if (instance == null) {
            instance = new ViewController();
        }
        return instance;
    }
    
    public ViewController() {
        initUI();
    }
    

    private void initUI() {
        this.setLayout(new BorderLayout());
        
        // create the banner
        banner = new Banner();
        JPanel h1 = new JPanel();
        h1.setLayout(new BorderLayout());
        
        // create the section header
        sectionHeader = new SectionHeader();
        h1.add(sectionHeader, BorderLayout.NORTH);
        
        // create the content container
        contentContainer = new JPanel();
        contentContainer.setBackground(Color.white);
        contentContainer.setLayout(new BorderLayout());
        h1.add(contentContainer,BorderLayout.CENTER);
        
        // create the left menu
        leftPanel = new SidePanel();
        menu = new Menu(contentContainer);
        leftPanel.setContent(menu);
        
        // create the right panel
        sectionPanel = new PersistencePanel();
        sectionPanel.setPreferredSize(new Dimension(350,999));
        h1.add(sectionPanel,BorderLayout.EAST);
        
        buttonSectionPanelController = new JToggleButton();
        buttonSectionPanelController.setFocusPainted(false);
        buttonSectionPanelController.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                sectionPanel.setVisible(!sectionPanel.isVisible());
            }
            
        });
        sectionHeader.add(buttonSectionPanelController);
        
        // add it all to the view
        this.add(banner,BorderLayout.NORTH);
        this.add(h1,BorderLayout.CENTER);
        this.add(leftPanel,BorderLayout.WEST);
        
        sectionPanel.setVisible(false);
        buttonSectionPanelController.setVisible(false);
    }

    public void addSection(SectionView section) {
        menu.addSection(section);
    }
    
}
