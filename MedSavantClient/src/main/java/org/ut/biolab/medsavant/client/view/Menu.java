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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView.DockState;
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
    private final JPanel tertiaryMenu;
    ButtonGroup primaryMenuButtons;
    private final JPanel primaryMenuSectionButtonContainer;
    private final JPanel tertiaryMenuPanelVisibilityContainer;
    private final JPanel tertiaryMenuPanelAccessoryContainer;
    private JPanel previousSectionPanel;
    private Map<SubSectionView, SubSectionButton> map;
    private JButton userButton;
    private static final Log LOG = LogFactory.getLog(Menu.class);

    public JButton getSubSectionButton(SubSectionView ssv) {
        return map.get(ssv);
    }

    public Menu(JPanel panel) {

        resetMap();

        setLayout(new BorderLayout());

        primaryMenuButtons = new ButtonGroup();

        primaryMenu = ViewUtil.getPrimaryBannerPanel();

        secondaryMenu = new JPanel();
        secondaryMenu.setBackground(ViewUtil.getSecondaryMenuColor());

        tertiaryMenu = new JPanel();
        tertiaryMenu.setBackground(Color.darkGray);
        // tertiaryMenu.setBorder(ViewUtil.getMediumBorder());
        ViewUtil.applyHorizontalBoxLayout(tertiaryMenu);
        //tertiaryMenu.setMinimumSize(new Dimension(9999, 30));
        ViewUtil.applyHorizontalBoxLayout(tertiaryMenu);

        int padding = 5;

        primaryMenu.setLayout(new BoxLayout(primaryMenu, BoxLayout.X_AXIS));
        primaryMenu.setBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(150, 150, 150)),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)));

        primaryMenuSectionButtonContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(primaryMenuSectionButtonContainer);

        UpdatesPanel updatesPanel = new UpdatesPanel();
        NotificationsPanel n = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME);

        primaryMenu.add(primaryMenuSectionButtonContainer);
        primaryMenu.add(Box.createHorizontalGlue());

        primaryMenu.add(updatesPanel);
        primaryMenu.add(ViewUtil.getLargeSeparator());

        primaryMenu.add(n);
        primaryMenu.add(ViewUtil.getLargeSeparator());
        primaryMenu.add(getLoginMenuItem());
        add(primaryMenu, BorderLayout.NORTH);

        secondaryMenu.setLayout(new BoxLayout(secondaryMenu, BoxLayout.Y_AXIS));
        //secondaryMenu.setBorder(ViewUtil.getRightLineBorder());

        secondaryMenu.setPreferredSize(new Dimension(150, 100));

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

        tertiaryMenuPanelVisibilityContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(tertiaryMenuPanelVisibilityContainer);

        tertiaryMenuPanelAccessoryContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(tertiaryMenuPanelAccessoryContainer);

        clearMenu();
    }

    public void deactivateSubsection(String subsectionName) {
    }

    public JPanel getSecondaryMenu() {
        return secondaryMenu;
    }

    public JPanel getTertiaryMenu() {
        return tertiaryMenu;
    }

    public void addSection(SectionView section) {

        final JPanel sectionPanel = ViewUtil.getClearPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setVisible(false);

        //HoverButton sectionButton = new SectionButton(section, sectionPanel);
        //sectionButton.setSelectedColor(ViewUtil.getSecondaryMenuColor());

        final JToggleButton sectionButton = ViewUtil.getTogglableIconButton(section.getIcon());
        sectionButton.setName(section.getName());
        sectionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                primaryMenuButtons.setSelected(sectionButton.getModel(), true);
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

        ButtonGroup subSectionsGroup = new ButtonGroup();

        for (SubSectionView v : section.getSubSections()) {
            subSectionViews.add(v);

            SubSectionButton subSectionButton = new SubSectionButton(v, subSectionsGroup);
            sectionPanel.add(subSectionButton);
            subSectionsGroup.add(subSectionButton);

            map.put(v, subSectionButton);
        }

        primaryMenuButtons.add(sectionButton);

        sectionPanel.add(Box.createVerticalStrut(50));

        secondaryMenu.add(sectionPanel);

        primaryMenuSectionButtonContainer.add(ViewUtil.subTextComponent(sectionButton, section.getName()));
        primaryMenuSectionButtonContainer.add(ViewUtil.getLargeSeparator());

    }

    public void updateSections() {
        for (int i = 0; i < subSectionViews.size(); i++) {
            subSectionViews.get(i).setUpdateRequired(true);
        }
        if (currentView != null) {
            setContentTo(currentView);
        }
    }

    public void switchToSubSection(SubSectionView view) {
        LOG.debug("Switching to subsection " + view.getPageName());
        if (view.getDockState() == DockState.UNDOCKED) {
            view.focusUndockedFrame();
        } else {
            map.get(view).subSectionClicked();
        }
    }

    /**
     * Refreshes the given content pane with content from the subsection view.
     */
    public void refreshSubSection(JPanel contentPanel, SubSectionView v) {
        contentPanel.removeAll();
        contentPanel.add(v.getDockedView(), BorderLayout.CENTER);
        contentPanel.updateUI();
        tertiaryMenuPanelVisibilityContainer.removeAll();
        tertiaryMenuPanelAccessoryContainer.removeAll();

        if (v.getParent().getPersistentPanels() != null && v.getParent().getPersistentPanels().length > 0) {
            JComponent box = PeekingPanel.getToggleButtonForPanel(ViewController.getInstance().getPersistencePanel(), v.getParent().getPersistentPanels()[0].getName());
            tertiaryMenuPanelVisibilityContainer.add(box);
        }

        if (v.getSubSectionMenuComponents() != null) {
            for (Component c : v.getSubSectionMenuComponents()) {
                if (c instanceof JToggleButton) {
                    tertiaryMenuPanelVisibilityContainer.add(c);
                } else {
                    tertiaryMenuPanelAccessoryContainer.add(c);
                }
            }
        }

        if (v.getParent().getSectionMenuComponents() != null) {
            for (Component c : v.getParent().getSectionMenuComponents()) {
                if (c instanceof JToggleButton) {
                    tertiaryMenuPanelVisibilityContainer.add(c);
                } else {
                    tertiaryMenuPanelAccessoryContainer.add(c);
                }
            }
        }

        v.setUpdateRequired(false);
        ViewController.getInstance().changeSubSectionTo(v);
    }

    public void setContentTo(SubSectionView v) {
        if (currentView != v) {
            currentView = v;
            refreshSubSection(contentContainer, currentView);
        }
    }

    public void refreshSelection() {
        if (currentView != null) {
            refreshSubSection(contentContainer, currentView);
        }
    }

    public final void clearMenu() {

        while (primaryMenuButtons.getButtonCount() > 0) {
            primaryMenuButtons.remove(primaryMenuButtons.getElements().nextElement());
        }

        primaryMenuSectionButtonContainer.removeAll();
        secondaryMenu.removeAll();

        tertiaryMenu.removeAll();
        tertiaryMenuPanelVisibilityContainer.removeAll();
        tertiaryMenuPanelAccessoryContainer.removeAll();

        tertiaryMenu.add(tertiaryMenuPanelVisibilityContainer);
        tertiaryMenu.add(Box.createHorizontalGlue());
        tertiaryMenu.add(tertiaryMenuPanelAccessoryContainer);
    }

    private void resetMap() {
        map = new HashMap<SubSectionView, SubSectionButton>();
    }

    private JPanel getLoginMenuItem() {

        //loginStatusLabel = new JLabel("");

        final JPanel loginMenu = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(loginMenu);

        userButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_USER));
        loginMenu.add(ViewUtil.subTextComponent(userButton, LoginController.getInstance().getUserName()));

        final JPopupMenu m = new JPopupMenu();

        /*JMenuItem chpass = new JMenuItem("Change Password");
         chpass.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent ae) {
         LoginController.getInstance().logout();
         }
         });
         m.add(chpass);*/


        JMenuItem logout = new JMenuItem("Log Out");
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LoginController.getInstance().logout();

            }
        });
        m.add(logout);

        userButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                m.show(userButton, 0, userButton.getHeight());
            }
        });

        //loginMenu.add(ViewUtil.getSmallSeparator());

        return loginMenu;
    }

    public void updateLoginStatus() {
        if (LoginController.getInstance().isLoggedIn()) {
            userButton.setToolTipText("Signed in since: " + new SimpleDateFormat().format((new Date())));
        } else {
            userButton.setToolTipText(null);
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
            primaryMenuButtons.setSelected(getModel(), true);
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
            setContentTo(view);
        }
    }
}
