/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.client.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.project.ProjectEvent;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.view.component.HoverButton;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Menu extends JPanel {

    private SubSectionView currentView;
    private final JPanel contentContainer;
    List<SubSectionView> subSectionViews = new ArrayList<SubSectionView>();
    private final JPanel primaryMenu;
    private final JPanel secondaryMenu;
    ButtonGroup sectionButtons;
    private final JPanel sectionDetailedMenu;
    private final JPanel subSectionMenu;
    private JPanel previousSectionPanel;
    Map<SubSectionView, SubSectionButton> map;
    private final JPanel sectionMenu;
    private JLabel loginStatusLabel;

    public Menu(JPanel panel) {

        resetMap();

        sectionButtons = new ButtonGroup();

        primaryMenu = ViewUtil.getPrimaryBannerPanel();
        secondaryMenu = new JPanel();//ViewUtil.getPrimaryBannerPanel();
        secondaryMenu.setBackground(ViewUtil.getSecondaryMenuColor());
        //secondaryMenu.setOpaque(false);

        int padding = 5;


        primaryMenu.setLayout(new BoxLayout(primaryMenu, BoxLayout.X_AXIS));
        primaryMenu.setBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(150, 150, 150)),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)));

        sectionMenu = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(sectionMenu);
        primaryMenu.add(sectionMenu);
        primaryMenu.add(Box.createHorizontalGlue());
        primaryMenu.add(getLoginMenuItem());

        secondaryMenu.setLayout(new BoxLayout(secondaryMenu, BoxLayout.Y_AXIS));
        secondaryMenu.setBorder(ViewUtil.getRightLineBorder());

        secondaryMenu.setPreferredSize(new Dimension(150, 100));

        setLayout(new BorderLayout());
        add(primaryMenu, BorderLayout.NORTH);

        contentContainer = panel;

        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED) {
                    updateSections();
                }
            }
        });

        ProjectController.getInstance().addListener(new Listener<ProjectEvent>() {
            @Override
            public void handleEvent(ProjectEvent evt) {
                if (!GeneticsSection.isInitialized && evt.getType() == ProjectEvent.Type.CHANGED) {
                    //once this section is initialized, referencecombobox fires
                    //referencechanged event on every project change
                    updateSections();
                }
            }
        });

        LoginController.getInstance().addListener(new Listener<LoginEvent>() {
            @Override
            public void handleEvent(LoginEvent evt) {
                if (evt.getType() == LoginEvent.Type.LOGGED_OUT) {
                    contentContainer.removeAll();
                    ViewController.getInstance().changeSubSectionTo(null);
                    currentView = null;
                    resetMap();
                }
            }
        });

        subSectionMenu = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(subSectionMenu);

        sectionDetailedMenu = new JPanel();
        sectionDetailedMenu.setOpaque(false);
        ViewUtil.applyVerticalBoxLayout(sectionDetailedMenu);

        clearMenu();
    }

    public JPanel getSecondaryMenu() {
        return secondaryMenu;
    }

    public void addSection(SectionView section) {

        final JPanel sectionPanel = ViewUtil.getClearPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setVisible(false);

        //HoverButton sectionButton = new SectionButton(section, sectionPanel);
        //sectionButton.setSelectedColor(ViewUtil.getSecondaryMenuColor());

        final JToggleButton sectionButton = ViewUtil.getIconButton(section.getIcon());
        sectionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                sectionButtons.setSelected(sectionButton.getModel(), true);
                if (previousSectionPanel != null) {
                    previousSectionPanel.setVisible(false);
                }
                // Act as if we clicked the first sub-section button.
                ((SubSectionButton) sectionPanel.getComponent(0)).subSectionClicked();
                sectionPanel.setVisible(true);

                previousSectionPanel = sectionPanel;
                primaryMenu.invalidate();
            }
        });
        /*if (sectionButton instanceof HoverButton) {
         ((HoverButton)sectionButton).setSelectedColor(ViewUtil.getSecondaryMenuColor());
         }*/

        ButtonGroup subSectionsGroup = new ButtonGroup();

        for (SubSectionView v : section.getSubSections()) {
            subSectionViews.add(v);

            SubSectionButton subSectionButton = new SubSectionButton(v, subSectionsGroup);
            sectionPanel.add(subSectionButton);
            subSectionsGroup.add(subSectionButton);

            map.put(v, subSectionButton);
        }

        sectionButtons.add(sectionButton);

        sectionPanel.add(Box.createVerticalStrut(50));

        if (section.getPersistentPanels() != null && section.getPersistentPanels().length > 0) {
            JCheckBox box = PeekingPanel.getCheckBoxForPanel(ViewController.getInstance().getPersistencePanel(), section.getPersistentPanels()[0].getName());
            sectionPanel.add(box);
        }

        sectionMenu.add(ViewUtil.subTextComponent(sectionButton, section.getName()));
        sectionMenu.add(Box.createHorizontalStrut(20));

        subSectionMenu.add(sectionPanel);
    }

    public void updateSections() {
        for (int i = 0; i < subSectionViews.size(); i++) {
            subSectionViews.get(i).setUpdateRequired(true);
        }
        if (currentView != null) {
            setContentTo(currentView, true);
        }
    }

    public void switchToSubSection(SubSectionView view) {
        System.out.println("Switching to subsection " + view.getPageName());
        map.get(view).subSectionClicked();
    }

    public void setContentTo(SubSectionView v, boolean update) {
        System.out.println("Setting content to " + v.getPageName());
        if (currentView != v) {
            currentView = v;
            contentContainer.removeAll();
            contentContainer.add(v.getView(), BorderLayout.CENTER);
            contentContainer.updateUI();

            sectionDetailedMenu.removeAll();

            if (v.getSubSectionMenuComponents() != null) {
                for (Component c : v.getSubSectionMenuComponents()) {
                    sectionDetailedMenu.add(c);
                }
            }

            if (v.getParent().getSectionMenuComponents() != null) {
                for (Component c : v.getParent().getSectionMenuComponents()) {
                    sectionDetailedMenu.add(c);
                }
            }

            v.setUpdateRequired(false);
            ViewController.getInstance().changeSubSectionTo(v);
        }
    }

    public void refreshSelection() {
        if (currentView != null) {
            setContentTo(currentView, true);
        }
    }

    public final void clearMenu() {

        while (sectionButtons.getButtonCount() > 0) {
            sectionButtons.remove(sectionButtons.getElements().nextElement());
        }

        sectionMenu.removeAll();
        secondaryMenu.removeAll();

        subSectionMenu.removeAll();
        secondaryMenu.add(subSectionMenu);

        sectionDetailedMenu.removeAll();
        secondaryMenu.add(sectionDetailedMenu);

    }

    private void resetMap() {
        map = new HashMap<SubSectionView, SubSectionButton>();
    }

    private JPanel getLoginMenuItem() {

        loginStatusLabel = new JLabel("");

        final JPanel loginMenu = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(loginMenu);

        loginMenu.add(loginStatusLabel);
        loginMenu.add(ViewUtil.getLargeSeparator());
        JButton logoutButton = ViewUtil.getTexturedButton("Logout", null);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LoginController.getInstance().logout();
            }
        });
        loginMenu.add(logoutButton);
        loginMenu.add(ViewUtil.getSmallSeparator());

        return loginMenu;
    }

    public void updateLoginStatus() {
        if (LoginController.getInstance().isLoggedIn()) {
            loginStatusLabel.setText(LoginController.getInstance().getUserName());
            loginStatusLabel.setToolTipText("Signed in since: " + new SimpleDateFormat().format((new Date())));
        } else {
            loginStatusLabel.setText("Not signed in");
            loginStatusLabel.setToolTipText(null);
        }
    }

    class SectionButton extends HoverButton {

        private final JPanel panel;

        SectionButton(SectionView v, JPanel p) {
            super(v.getName());
            panel = p;

            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    sectionButtonClicked();
                }
            });
        }

        void sectionButtonClicked() {
            sectionButtons.setSelected(getModel(), true);
            if (previousSectionPanel != null) {
                previousSectionPanel.setVisible(false);
            }
            // Act as if we clicked the first sub-section button.
            ((SubSectionButton) panel.getComponent(0)).subSectionClicked();
            panel.setVisible(true);

            previousSectionPanel = panel;
            primaryMenu.invalidate();
        }
    }

    private class SubSectionButton extends HoverButton {

        private final SubSectionView view;
        private final ButtonGroup group;

        SubSectionButton(SubSectionView v, ButtonGroup g) {
            super(v.getPageName());
            view = v;
            group = g;
            setFontSize(12);
            setFontStyle(Font.BOLD);

            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    subSectionClicked();
                }
            });
        }

        public void subSectionClicked() {
            group.setSelected(getModel(), true);
            setContentTo(view, false);
        }
    }
}
