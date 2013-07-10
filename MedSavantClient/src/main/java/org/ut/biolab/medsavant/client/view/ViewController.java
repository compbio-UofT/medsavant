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

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.PeekingPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ViewController {

    private Menu menu;
    private SectionView currentSection;
    private SubSectionView currentSubsection;
    private static ViewController instance;
    private JPanel contentContainer;
    private PersistencePanel persistencePanel;
    private PeekingPanel peekingPanel;

    private ViewController(JPanel p) {
        // create the banner
        //banner = new Banner();
        JPanel h1 = new JPanel();
        h1.setLayout(new BorderLayout());

        // create the content container
        contentContainer = new JPanel();
        contentContainer.setBackground(ViewUtil.getBGColor());
        //int padding = ViewUtil.getBreathingPadding();
        //contentContainer.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        contentContainer.setLayout(new BorderLayout());
        h1.add(contentContainer, BorderLayout.CENTER);

        // create the right panel
        persistencePanel = new PersistencePanel();
        persistencePanel.setPreferredSize(new Dimension(350, 999));
        peekingPanel = new PeekingPanel("", BorderLayout.EAST, persistencePanel, true, 330);
        peekingPanel.setToggleBarVisible(false);
        h1.add(peekingPanel, BorderLayout.WEST);


        // add it all to the view
        p.add(h1, BorderLayout.CENTER);

        peekingPanel.setVisible(false);

        menu = new Menu(contentContainer);
        p.add(menu, BorderLayout.NORTH);

        p.add(menu.getSecondaryMenu(), BorderLayout.WEST);

        JPanel h2 = ViewUtil.getClearPanel();
        h2.setLayout(new BorderLayout());
        h2.add(menu.getTertiaryMenu(), BorderLayout.NORTH);
        //h2.add(peekingPanel, BorderLayout.CENTER);


        h1.add(h2, BorderLayout.NORTH);

    }

    public static ViewController getInstance() {
        return instance;
    }

    public static ViewController loggedIn(JPanel p) {
        instance = new ViewController(p);
        return instance;
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
                peekingPanel.setVisible(true);
                persistencePanel.setSectionPersistencePanels(persistentPanels);
            } else {
                peekingPanel.setVisible(false);
            }
        }
        currentSection = parent;
    }

    void selectFirstItem() {
        // Fake a click on the first section button.
        ((JToggleButton) menu.primaryMenuButtons.getElements().nextElement()).doClick();
    }

    public PeekingPanel getPersistencePanel() {
        return peekingPanel;
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

    public void setPeekRightShown(boolean show) {
        peekingPanel.setExpanded(show);
    }

    public boolean isPeekRightShown() {
        return peekingPanel.isExpanded();
    }

    public Menu getMenu() {
        return menu;
    }

    private static class SidePanel extends JPanel {

        public SidePanel() {
            this.setBackground(ViewUtil.getTertiaryMenuColor());
            this.setBorder(
                    BorderFactory.createCompoundBorder(
                    ViewUtil.getRightLineBorder(),
                    ViewUtil.getBigBorder()));
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
            removeAll();
            if (persistentPanels.length == 1) {
                add(persistentPanels[0], BorderLayout.CENTER);
            } else {
                panes.removeAll();
                for (JPanel p : persistentPanels) {
                    panes.addTab(p.getName(), p);
                }
                add(panes, BorderLayout.CENTER);
            }

        }
    }

    public SectionView getCurrentSectionView() {
        return currentSection;
    }
}
