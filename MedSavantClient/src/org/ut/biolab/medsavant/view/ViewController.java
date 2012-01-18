/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.menu.TopMenu;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ViewController extends JPanel {

    private SectionHeader sectionHeader;
    private TopMenu menu;
    private JPanel contentContainer;
    private PersistencePanel sectionPanel;
    private PeekingPanel peekPersistence;
    private SectionView currentSection;
    private SubSectionView currentSubsection;

    private static ViewController instance;

    private ViewController() {
        setLayout(new BorderLayout());

        // create the banner
        //banner = new Banner();
        JPanel h1 = new JPanel();
        h1.setLayout(new BorderLayout());

        // create the section header
        sectionHeader = new SectionHeader();
        h1.add(sectionHeader, BorderLayout.NORTH);

        // create the content container
        contentContainer = new JPanel();
        contentContainer.setBackground(Color.white);
        contentContainer.setLayout(new BorderLayout());
        h1.add(contentContainer, BorderLayout.CENTER);

        // create the right panel
        sectionPanel = new PersistencePanel();
        sectionPanel.setPreferredSize(new Dimension(350, 999));
        peekPersistence = new PeekingPanel("", BorderLayout.EAST, sectionPanel, true,400);
        h1.add(peekPersistence, BorderLayout.WEST);

        // add it all to the view
        add(h1, BorderLayout.CENTER);

        peekPersistence.setVisible(false);

        menu = new TopMenu(contentContainer);
        add(menu, BorderLayout.NORTH);


        /*
        JPanel starter = new JPanel();
        ViewUtil.applyVerticalBoxLayout(starter);
        starter.add(new ImagePanel(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOGO).getImage(),128,128), BorderLayout.NORTH);
        starter.add(new JLabel("Choose a menu item from the left"));
        contentContainer.add(starter, BorderLayout.NORTH);
         *
         */
    }

    public static ViewController getInstance() {
        if (instance == null) {
            instance = new ViewController();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public void changeSubSectionTo(SubSectionView view) {

        if (currentSubsection != null) {
            currentSubsection.viewDidUnload();
        }

        currentSubsection = view;

        if (currentSubsection != null) {
            currentSubsection.viewDidLoad();
        }

        sectionHeader.setSubSection(view);

        SectionView parent = view != null ? view.getParent() : null;

        if (parent != currentSection && parent != null) {
            JPanel[] persistentPanels = parent.getPersistentPanels();
            if (persistentPanels != null) {
                peekPersistence.setVisible(true);
                sectionPanel.setSectionPersistencePanels(persistentPanels);
            } else {
                peekPersistence.setVisible(false);
            }
        }
        currentSection = parent;
    }

    void setProject(String projectname) {
         ClientLogger.log(ViewController.class, "Setting project to : " + projectname);
    }

    void clearMenu() {
        menu.removeAll();
    }

    public void addSection(SectionView section) {
        menu.addSection(section);
    }

    public void refreshView() {
        menu.refreshSelection();
    }

    public void addComponent(Component c) {
        menu.addComponent(c);
    }

    public void setPeekRightShown(boolean show){
        peekPersistence.setExpanded(show);
    }

    public boolean isPeekRightShown(){
        return peekPersistence.isExpanded();
    }

    private static class SidePanel extends JPanel {

        public SidePanel() {
            this.setBackground(ViewUtil.getMenuColor());
            this.setBorder(ViewUtil.getSideLineBorder());
            this.setLayout(new BorderLayout());
        }

        public void setContent(JPanel p) {
            this.add(p, BorderLayout.CENTER);
        }
    }

    private static class PersistencePanel extends SidePanel {

        private final JTabbedPane panes;

        public PersistencePanel() {
            panes = new JTabbedPane();
            this.setLayout(new BorderLayout());
        }

        private void setSectionPersistencePanels(JPanel[] persistentPanels) {
            this.removeAll();
            if (persistentPanels.length == 1) {
                JPanel p = ViewUtil.getSecondaryBannerPanel();
                p.add(new JLabel(persistentPanels[0].getName()));
                this.add(
                        p,
                        //ViewUtil.center(new JLabel(persistentPanels[0].getName())),
                        BorderLayout.NORTH);
                this.add(persistentPanels[0],BorderLayout.CENTER);
            } else {
                panes.removeAll();
                for (JPanel p : persistentPanels) {
                    panes.addTab(p.getName(), p);
                }
                this.add(panes,BorderLayout.CENTER);
            }

        }
    }

    private static class SectionHeader extends JPanel {
        private static final String DEFAULT_TITLE = "Welcome to MedSavant";
        private final JLabel title;
        private final JPanel sectionMenuPanel;
        private final JPanel subSectionMenuPanel;
        //private final ImagePanel leftTab;
        //private final ImagePanel rightTab;

        public SectionHeader() {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(null);

            this.setMinimumSize(new Dimension(9999,30));
            this.setPreferredSize(new Dimension(9999,30));
            this.setMaximumSize(new Dimension(9999,30));


            this.setBorder(ViewUtil.getLargeSideBorder());
            title = ViewUtil.getHeaderLabel(DEFAULT_TITLE);
            this.add(title);
            sectionMenuPanel = ViewUtil.getClearPanel();
            subSectionMenuPanel = ViewUtil.getClearPanel();

            sectionMenuPanel.setBackground(new Color(232, 232, 232));

            subSectionMenuPanel.setBackground(new Color(232, 232, 232));

            sectionMenuPanel.setLayout(new BoxLayout(sectionMenuPanel, BoxLayout.X_AXIS));
            subSectionMenuPanel.setLayout(new BoxLayout(subSectionMenuPanel, BoxLayout.X_AXIS));

            this.add(Box.createHorizontalGlue());;

            this.add(subSectionMenuPanel);
            this.add(Box.createHorizontalGlue());
            this.add(sectionMenuPanel);

        }

        @Override
        public void paintComponent(Graphics g) {
            PaintUtil.paintDarkMenu(g, this);
        }

        private void setTitle(String sectionName, String subsectionName) {
            title.setText(sectionName.toUpperCase() + " › " + subsectionName.trim());
        }

        void setSubSection(SubSectionView view) {
            boolean empty = true;

            if (view != null) {
                setTitle(view.getParent().getName(), view.getName());

                subSectionMenuPanel.removeAll();
                sectionMenuPanel.removeAll();

                Component[] subsectionBanner = view.getBanner();
                Component[] sectionBanner = view.getParent().getBanner();

                if (subsectionBanner != null) {
                    for (Component c : subsectionBanner) {
                        subSectionMenuPanel.add(c);
                        empty = false;
                    }
                }

                subSectionMenuPanel.setVisible(!empty);

                subSectionMenuPanel.add(Box.createVerticalGlue());

                empty = true;
                if (sectionBanner != null) {
                    for (Component c : sectionBanner) {
                        sectionMenuPanel.add(c);
                        empty = false;
                    }
                }

                sectionMenuPanel.setVisible(!empty);
            } else {
                title.setText(DEFAULT_TITLE);
            }

            //sectionMenuPanel.add(Box.createVerticalGlue());
        }
    }

    public SectionView getCurrentSectionView(){
        return currentSection;
    }
}
