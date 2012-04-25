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
package org.ut.biolab.medsavant.view.menu;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Menu extends JPanel implements ProjectListener {

    private SubSectionView currentView;
    private final JPanel contentContainer;
    private List<SubSectionView> subSectionViews = new ArrayList<SubSectionView>();
    private Component primaryGlue;
    private final JPanel primaryMenu;
    private final JPanel secondaryMenu;
    private final HeaderButton projectButton;
    //private Component secondaryGlue;

    private ButtonGroup bg;
    private final JPanel sectionDetailedMenu;
    private final JPanel sectionMenu;

    public Menu(JPanel panel) {

        bg = new ButtonGroup();

        primaryMenu = ViewUtil.getPrimaryBannerPanel();
        secondaryMenu = new JPanel();//ViewUtil.getPrimaryBannerPanel();
        secondaryMenu.setBackground(Color.darkGray);
        //secondaryMenu.setOpaque(false);

        int padding = 5;


        primaryMenu.setLayout(new BoxLayout(primaryMenu, BoxLayout.X_AXIS));
        primaryMenu.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));


        secondaryMenu.setLayout(new BoxLayout(secondaryMenu, BoxLayout.Y_AXIS));
        secondaryMenu.setBorder(ViewUtil.getRightLineBorder());

        secondaryMenu.setPreferredSize(new Dimension(150,100));

        projectButton = new HeaderButton(ProjectController.getInstance().getCurrentProjectName());

        this.setLayout(new BorderLayout());
        this.add(primaryMenu,BorderLayout.NORTH);
        //this.add(secondaryMenu,BorderLayout.SOUTH);

        contentContainer = panel;

        ReferenceController.getInstance().addReferenceListener(new ReferenceListener() {

            @Override
            public void referenceChanged(String referenceName) {
                updateSections();
            }

            @Override
            public void referenceAdded(String name) {
            }

            @Override
            public void referenceRemoved(String name) {
            }
        });

        ProjectController.getInstance().addProjectListener(new ProjectListener() {

            @Override
            public void projectAdded(String projectName) {
            }

            @Override
            public void projectRemoved(String projectName) {
            }

            @Override
            public void projectChanged(String projectName) {
                if (!GeneticsSection.isInitialized) {
                    //once this section is initialized, referencecombobox fires
                    //referencechanged event on every project change
                    updateSections();
                }
            }

            @Override
            public void projectTableRemoved(int projid, int refid) {
            }
        });

        LoginController.addLoginListener(new LoginListener() {

            @Override
            public void loginEvent(LoginEvent evt) {
                if (evt.getType() == LoginEvent.EventType.LOGGED_OUT) {
                    contentContainer.removeAll();
                    ViewController.getInstance().changeSubSectionTo(null);
                    currentView = null;
                }
            }
        });

        sectionMenu = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(sectionMenu);


        sectionDetailedMenu = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(sectionDetailedMenu);


        primaryGlue = Box.createHorizontalGlue();
        //secondaryGlue = Box.createVerticalGlue();

        clearMenu();
    }

    public JPanel getSecondaryMenu() {
        return this.secondaryMenu;
    }

    private JPanel previousCp;

    public void addSection(final SectionView section) {

        final HeaderButton hb = new HeaderButton(section.getName());

        final JPanel cp = ViewUtil.getClearPanel();
        cp.setLayout(new BoxLayout(cp,BoxLayout.Y_AXIS));
        cp.setVisible(false);

        final ButtonGroup thisBg = new ButtonGroup();

        for (final SubSectionView v : section.getSubSections()) {
            subSectionViews.add(v);
            final HeaderButton tmpBut = new HeaderButton(v.getName());
            tmpBut.setFontSize(12);
            tmpBut.setFontStyle(Font.BOLD);
            tmpBut.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    thisBg.setSelected(tmpBut.getModel(), true);
                    setContentTo(v, false);
                }
            });
            cp.add(tmpBut);
            thisBg.add(tmpBut);

        }

        bg.add(hb);
        hb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                bg.setSelected(hb.getModel(), true);
                showSecondaryPanel(cp);
            }

        });
        //

        cp.add(Box.createVerticalStrut(50));

        if (section.getPersistentPanels() != null && section.getPersistentPanels().length > 0) {
            final JCheckBox box = PeekingPanel.getCheckBoxForPanel(ViewController.getInstance().getPersistencePanel(),section.getPersistentPanels()[0].getName());
            cp.add(box);
        }

        primaryMenu.remove(primaryGlue);
        primaryMenu.add(hb);

        sectionMenu.add(cp);
    }

    private void showSecondaryPanel(final JPanel cp) {
        SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (previousCp != null) {
                            previousCp.setVisible(false);
                        }
                        ((HeaderButton) cp.getComponents()[0]).getActionListeners()[0].actionPerformed(null);
                        cp.setVisible(true);

                        previousCp = cp;
                        primaryMenu.invalidate();
                    }

                });
    }

    public void updateSections() {
        for (int i = 0; i < subSectionViews.size(); i++) {
            subSectionViews.get(i).setUpdateRequired(true);
        }
        if (currentView != null) {
            setContentTo(currentView, true);
        }
    }

    private void setContentTo(SubSectionView v, boolean update) {
        currentView = v;
        contentContainer.removeAll();
        contentContainer.add(v.getView(update || v.isUpdateRequired()), BorderLayout.CENTER);

        sectionDetailedMenu.removeAll();


        if (v.getSubSectionMenuComponents()!= null) {
            for (Component c : v.getSubSectionMenuComponents()) {
                System.out.println("Adding subsection component " + c);
                sectionDetailedMenu.add(c);
            }
        }

        if (v.getParent().getSectionMenuComponents()!= null) {
            for (Component c : v.getParent().getSectionMenuComponents()) {
                sectionDetailedMenu.add(c);
            }
        }

        v.setUpdateRequired(false);
        contentContainer.updateUI();
        ViewController.getInstance().changeSubSectionTo(v);
    }

    public void refreshSelection() {
        if (currentView != null) {
            setContentTo(currentView, true);
        }
    }

    public final void clearMenu() {

        while(bg.getButtonCount() > 0) {
            bg.remove(bg.getElements().nextElement());
        }

        primaryMenu.removeAll();
        secondaryMenu.removeAll();

        sectionMenu.removeAll();
        secondaryMenu.add(sectionMenu);

        sectionDetailedMenu.removeAll();
        secondaryMenu.add(sectionDetailedMenu);

        projectButton.setText(ProjectController.getInstance().getCurrentProjectName());

        primaryMenu.add(projectButton);
        primaryMenu.add(primaryGlue);
    }

    @Override
    public void projectAdded(String projectName) {
    }

    @Override
    public void projectRemoved(String projectName) {
    }

    @Override
    public void projectChanged(String projectName) {
        projectButton.setText(projectName);
        System.out.println("Project Changed to " + projectName);
    }

    @Override
    public void projectTableRemoved(int projid, int refid) {
    }

    public class HeaderButton extends JButton {

        private boolean over;
        private Font f;

        String face = "Arial";
        int size = 15;
        int style = Font.PLAIN;


        public void setForeColor(Color c) {
            this.textColor = c;
        }

        public void setFontSize(int size) {
            this.size = size;
            resetFont();

        }

        public void setFontStyle(int style) {
            this.style = style;
            resetFont();
        }

        public HeaderButton(String s) {
            super(s);

            resetFont();

            this.setOpaque(false);
            final JComponent instance = this;
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {

                    over = true;
                    instance.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    over = false;
                    instance.repaint();
                }
            });
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }


            Color textColorUnselected = Color.gray;
            Color textColor = Color.white;
            Color bgColor = new Color(255, 255, 255, 255);


        @Override
        public void paintComponent(Graphics g) {

            String title = this.getText();

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setFont(f);

            int width = g2d.getFontMetrics().stringWidth(title);
            int height = g2d.getFontMetrics().getAscent();

            if (over || this.isSelected()) {
                g2d.setColor(textColor);
            } else {
                g2d.setColor(textColorUnselected);
            }

            //g2d.drawString(title, (getWidth() - width), (getHeight() + height)-2);

            g2d.drawString(title, (getWidth() - width) / 2, (getHeight() + height) / 2 - 2);

        }

        private void resetFont() {
            f = new Font(face, style, size);
        }
    }
}
