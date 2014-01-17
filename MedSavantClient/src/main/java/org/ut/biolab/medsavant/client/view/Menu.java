/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view;

import org.ut.biolab.medsavant.client.util.notification.NotificationButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import org.ut.biolab.medsavant.client.view.dialog.ChangePasswordDialog;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.MultiSection;
import org.ut.biolab.medsavant.client.view.subview.SubSection;
import org.ut.biolab.medsavant.client.view.subview.SubSection.DockState;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Menu extends JPanel {

    private static final String JOBS_BUTTON_TITLE = "Jobs";
    private static final ImageIcon JOBS_BUTTON_ICON = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_SERVER);
    private SubSection currentView;
    private final JPanel contentContainer;
    List<SubSection> subSectionViews = new ArrayList<SubSection>();
    private final JPanel primaryMenu;
    private final JPanel secondaryMenu;
    private final JPanel tertiaryMenu;
    ButtonGroup primaryMenuButtons;
    private final JPanel primaryMenuSectionButtonContainer;
    private final JPanel tertiaryMenuPanelVisibilityContainer;
    private final JPanel tertiaryMenuPanelAccessoryContainer;
    private JPanel previousSectionPanel;
    private Map<SubSection, SubSectionButton> map;
    private JButton userButton;
    private static final Log LOG = LogFactory.getLog(Menu.class);
    private UpdatesPanel updatesPanel = new UpdatesPanel();
    private NotificationButton notificationButton;

    public void checkForUpdateNotifications() {
        updatesPanel.update();
    }

    public JButton getSubSectionButton(SubSection ssv) {
        return map.get(ssv);
    }

    public NotificationButton getJobNotificationButton() {
        return notificationButton;
    }

    public Menu(JPanel panel) {

        resetMap();

        setLayout(new BorderLayout());
        notificationButton = new NotificationButton(JOBS_BUTTON_TITLE, JOBS_BUTTON_ICON);

        primaryMenuButtons = new ButtonGroup();

        primaryMenu = ViewUtil.getPrimaryBannerPanel();

        secondaryMenu = new JPanel();
        secondaryMenu.setBackground(ViewUtil.getSecondaryMenuColor());

        tertiaryMenu = new JPanel();
        tertiaryMenu.setBorder(ViewUtil.getBottomLineBorder());
        tertiaryMenu.setBackground(ViewUtil.getTertiaryMenuColor());
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

        //NotificationsPanel n = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME);



        JPanel appStorePanel = ViewUtil.getClearPanel();
        final JButton appStoreButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_STORE));
        ViewUtil.applyHorizontalBoxLayout(appStorePanel);
        appStorePanel.add(ViewUtil.subTextComponent(appStoreButton, "App Store"));

        appStoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                MedSavantFrame.getInstance().showAppStore();
            }
        });


        appStoreButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                super.mouseEntered(me);
                appStoreButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                super.mouseExited(me);
                appStoreButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });


        int componentpadding = 10;
        primaryMenu.add(Box.createHorizontalStrut(componentpadding));
        primaryMenu.add(primaryMenuSectionButtonContainer);
        primaryMenu.add(Box.createHorizontalGlue());

        FlowLayout fl = new FlowLayout(FlowLayout.RIGHT, componentpadding, 1);
        JPanel rightSidePanel = ViewUtil.getClearPanel();
        rightSidePanel.setLayout(fl);
        rightSidePanel.add(updatesPanel);
        rightSidePanel.add(notificationButton);
        rightSidePanel.add(appStorePanel);
        rightSidePanel.add(getLoginMenuItem());
        primaryMenu.add(rightSidePanel);

        add(primaryMenu, BorderLayout.NORTH);

        secondaryMenu.setLayout(new BoxLayout(secondaryMenu, BoxLayout.Y_AXIS));

        secondaryMenu.setPreferredSize(new Dimension(140, 100));

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

    public void setPrimaryMenuVisible(boolean isVisible) {
        primaryMenu.setVisible(isVisible);
    }

    public boolean isPrimaryMenuVisible() {
        return primaryMenu.isVisible();
    }

    public void setSecondaryMenuVisible(boolean isVisible) {
        secondaryMenu.setVisible(isVisible);
    }

    public boolean isSecondaryMenuVisible() {
        return secondaryMenu.isVisible();
    }

    public void deactivateSubsection(String subsectionName) {
    }

    public JPanel getSecondaryMenu() {
        return secondaryMenu;
    }

    public JPanel getTertiaryMenu() {
        return tertiaryMenu;
    }

    public void addSection(final MultiSection section) {

        final JPanel sectionPanel = ViewUtil.getClearPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setVisible(false);

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

                // hack to determine if there are more than 1 subsections in this section
                int counter = 0;
                for (Component c : sectionPanel.getComponents()) {
                    if (c instanceof SubSectionButton) {
                        counter++;
                    }
                }
                boolean isSideMenuVisible = counter > 1;

                sectionPanel.setVisible(true);
                secondaryMenu.setVisible(isSideMenuVisible);

                previousSectionPanel = sectionPanel;
                primaryMenu.invalidate();
            }
        });

        ButtonGroup subSectionsGroup = new ButtonGroup();

        for (SubSection v : section.getSubSections()) {
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

    public void switchToSubSection(SubSection view) {
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
    public void refreshSubSection(JPanel contentPanel, SubSection v) {
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

    public void setContentTo(SubSection v) {
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
        map = new HashMap<SubSection, SubSectionButton>();
    }

    private JPanel getLoginMenuItem() {

        //loginStatusLabel = new JLabel("");

        final JPanel loginMenu = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(loginMenu);

        userButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_USER));
        loginMenu.add(ViewUtil.subTextComponent(userButton, LoginController.getInstance().getUserName()));

        final JPopupMenu m = new JPopupMenu();
        JMenuItem changePassItem = new JMenuItem("Change Password");
        JMenuItem logoutItem = new JMenuItem("Log Out");

        m.add(changePassItem);
        m.add(logoutItem);
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                MedSavantFrame.getInstance().requestClose();
            }
        });

        changePassItem.addActionListener(new ActionListener(){
            private final String OLDPASS_LABEL = "Enter Current Password";
            private final String NEWPASS_LABEL1 = "Enter New Password";
            private final String NEWPASS_LABEL2 = "Confirm New Password";
            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog jd = new ChangePasswordDialog();
                jd.setVisible(true);
            }

        });

        userButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                m.show(loginMenu, 0, loginMenu.getHeight());
            }
        });


        userButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                super.mouseEntered(me);
                userButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent me) {
                super.mouseExited(me);
                userButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return loginMenu;
    }

    public void updateLoginStatus() {
        if (LoginController.getInstance().isLoggedIn()) {
            userButton.setToolTipText("Logged in as " + LoginController.getInstance().getUserName() + " since: " + new SimpleDateFormat().format((new Date())));
        } else {
            userButton.setToolTipText(null);
        }
    }

    class SectionButton extends HoverButton {

        private final JPanel panel;

        SectionButton(MultiSection v, JPanel p) {
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

        private final SubSection view;
        private final ButtonGroup group;

        SubSectionButton(SubSection v, ButtonGroup g) {
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
