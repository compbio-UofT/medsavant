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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.view.manage.ProjectWizard;
import org.ut.biolab.medsavant.view.menu.Menu;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.menu.TopMenu;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ViewController extends JPanel {

    private SectionHeader sectionHeader;
    private Menu menu;
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
        //h1.add(sectionHeader, BorderLayout.NORTH);

        // create the content container
        contentContainer = new JPanel();
        contentContainer.setBackground(ViewUtil.getBGColor());
        //int padding = ViewUtil.getBreathingPadding();
        //contentContainer.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        contentContainer.setLayout(new BorderLayout());
        h1.add(contentContainer, BorderLayout.CENTER);

        // create the right panel
        sectionPanel = new PersistencePanel();
        sectionPanel.setPreferredSize(new Dimension(350, 999));
        peekPersistence = new PeekingPanel("", BorderLayout.EAST, (JComponent) sectionPanel, true, 350);
        peekPersistence.setToggleBarVisible(false);
        h1.add(peekPersistence, BorderLayout.WEST);

        // add it all to the view
        add(h1, BorderLayout.CENTER);

        peekPersistence.setVisible(false);

        menu = new Menu(contentContainer);
        add(menu, BorderLayout.NORTH);
        add(menu.getSecondaryMenu(),BorderLayout.WEST);


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

    public PeekingPanel getPersistencePanel() {
        return peekPersistence;
    }
    /*
    public void setPersistencePanelVisible(boolean b) {
        if (peekPersistence.isVisible()) {
            peekPersistence.setExpanded(b);
        }
    }*/

    //TODO: does this do anything really?
    void setProject(String projectname) {
        ClientLogger.log(ViewController.class, "Setting project to : " + projectname);
    }

    void clearMenu() {
        menu.clearMenu();
    }

    public void addSection(SectionView section) {
        menu.addSection(section);
    }

    public void refreshView() {
        menu.refreshSelection();
    }

    public void addProjectDropDown(Component c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(c);
        p.add(Box.createHorizontalGlue());
        this.add(p, BorderLayout.NORTH);
    }

    public void setPeekRightShown(boolean show) {
        peekPersistence.setExpanded(show);
    }

    public boolean isPeekRightShown() {
        return peekPersistence.isExpanded();
    }

    private static class SidePanel extends JPanel {

        public SidePanel() {
            this.setBackground(ViewUtil.getTertiaryMenuColor());
            this.setBorder(BorderFactory.createCompoundBorder(ViewUtil.getRightLineBorder(), ViewUtil.getBigBorder()));
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
                /*JPanel p = ViewUtil.getClearPanel();
                p.add(new JLabel(persistentPanels[0].getName()));
                this.add(
                        p,
                        //ViewUtil.center(new JLabel(persistentPanels[0].getName())),
                        BorderLayout.NORTH);*/
                this.add(persistentPanels[0], BorderLayout.CENTER);
            } else {
                panes.removeAll();
                for (JPanel p : persistentPanels) {
                    panes.addTab(p.getName(), p);
                }
                this.add(panes, BorderLayout.CENTER);
            }

        }
    }

    private static class SectionHeader extends JPanel implements ProjectListener {

        private static final String DEFAULT_TITLE = "";//Welcome to MedSavant";
        private final JLabel title;
        //private final JPanel sectionMenuPanel;
        //private final JPanel subSectionMenuPanel;
        private final JComboBox projectDropDown;

        public SectionHeader() {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(null);

            this.setMinimumSize(new Dimension(9999, 30));
            this.setPreferredSize(new Dimension(9999, 30));
            this.setMaximumSize(new Dimension(9999, 30));

            this.setBorder(ViewUtil.getLargeSideBorder());

            projectDropDown = new JComboBox();

            projectDropDown.setMinimumSize(new Dimension(210, 23));
            projectDropDown.setPreferredSize(new Dimension(210, 23));
            projectDropDown.setMaximumSize(new Dimension(210, 23));

            refreshProjectDropDown();

            this.add(projectDropDown);

            title = ViewUtil.getHeaderLabel(DEFAULT_TITLE);
            this.add(title);

            ProjectController.getInstance().addProjectListener(this);
        }

        private void refreshProjectDropDown() {
            try {
                projectDropDown.removeAllItems();

                List<String> projects = null;

                while (projects == null || projects.isEmpty()) {
                    projects = ProjectController.getInstance().getProjectNames();

                    if (!projects.isEmpty()) {
                        break;
                    }

                    if (projects.isEmpty() && !LoginController.isAdmin()) {
                        DialogUtils.displayMessage("Welcome to MedSavant. No projects have been started. Please contact your administrator.");
                        LoginController.logout();
                        return;
                    }

                    if (projects.isEmpty()) {
                        while (true) {
                            int result = DialogUtils.askYesNo("Welcome to MedSavant", "To begin using MedSavant, you will need to create a project.");
                            if (result == DialogUtils.NO) {
                                MedSavantFrame.getInstance().requestClose();
                                // don't break, the user chose not to quit
                            } else {
                                ProjectWizard npd = new ProjectWizard();
                                break;
                            }
                        }
                    }
                }

                for (String s : projects) {
                    projectDropDown.addItem(s);
                }
                if (projects.isEmpty()) {
                    projectDropDown.addItem("No Projects");
                    projectDropDown.setEnabled(false);
                } else {
                    projectDropDown.setEnabled(true);
                    projectDropDown.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            String currentName = ProjectController.getInstance().getCurrentProjectName();
                            if (!ProjectController.getInstance().setProject((String) projectDropDown.getSelectedItem())) {
                                projectDropDown.setSelectedItem(currentName);
                            }
                        }
                    });
                    ProjectController.getInstance().setProject((String) projectDropDown.getSelectedItem());
                }
            } catch (SQLException ex) {
            } catch (RemoteException ex) {
            }
        }

        public void projectAdded(String projectName) {
            refreshProjectDropDown();
        }

        public void projectRemoved(String projectName) {
            refreshProjectDropDown();
        }

        public void projectChanged(String projectName) {
        }

        public void projectTableRemoved(int projid, int refid) {
            refreshProjectDropDown();
        }

        @Override
        public void paintComponent(Graphics g) {
            PaintUtil.paintDarkMenu(g, this);
        }
    }

    public SectionView getCurrentSectionView() {
        return currentSection;
    }
}
