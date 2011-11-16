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

package org.ut.biolab.medsavant.view.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Menu extends JPanel implements MenuItemSelected {

    public static class MenuItem extends JPanel {

        private boolean isPressed = false;
        private boolean isHovering = false;
        private boolean isSelected = false;
        private final SubSectionView view;
        private final JLabel l;
        private final ArrayList<MenuItemSelected> listeners;

        public boolean isIsSelected() {
            return isSelected;
        }

        public MenuItem(SubSectionView v) {
            subSectionViews.add(v);
            this.view = v;
            listeners = new ArrayList<MenuItemSelected>();
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(ViewUtil.getMenuItemBorder());
            //this.setBackground(Color.red);
            //this.setOpaque(false);
            l = ViewUtil.getMenuSubsectionLabel(v.getName());
            this.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    
                }

                public void mousePressed(MouseEvent e) {
                    setPressed(true);
                }

                public void mouseReleased(MouseEvent e) {
                    setPressed(false);
                    setSelected(true);
                }

                public void mouseEntered(MouseEvent e) {
                    setHovering(true);
                }

                public void mouseExited(MouseEvent e) {
                    setHovering(false);
                }
            });

            setSelected(false);
            this.add(l);
            this.add(Box.createHorizontalGlue());
        }

        private boolean isPressed() {
            return this.isPressed;
        }

        private boolean isHovering() {
            return this.isHovering;
        }

        private void setHovering(boolean h) {
            boolean repaint = h != this.isHovering;
            this.isHovering = h;

            if (repaint) {
                if (isHovering()) {
                    l.setForeground(Color.black);
                } else {
                    if (!isSelected()) {
                        l.setForeground(Color.darkGray);
                    }
                }
                updateUI();
            }
        }

        private void setPressed(boolean h) {
            boolean repaint = h != this.isPressed;
            this.isPressed = h;

            if (repaint) {
                if (isPressed()) {
                    l.setForeground(Color.black);
                } else {
                    if (!isSelected()) {
                        l.setForeground(Color.darkGray);
                    }
                }
                updateUI();
            }
        }

        private void setSelected(boolean b) {
            boolean repaint = b != this.isSelected;
            this.isSelected = b;
            if (repaint) {
                if (isSelected()) {
                    ViewController.getInstance().changeSubSectionTo(this.view);
                    for (MenuItemSelected lis : listeners) {
                        lis.itemSelected(this);
                    }
                    l.setForeground(Color.black);
                } else {
                    l.setForeground(Color.darkGray);
                }
                updateUI();
            }

        }

        private boolean isSelected() {
            return this.isSelected;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            PaintUtil.paintSolid(g, this, ViewUtil.getMenuColor());

            if (isSelected()) {
                PaintUtil.paintSolid(g, this, Color.gray);
                return;
            }

            if (isPressed()) {
                PaintUtil.paintSolid(g, this, Color.gray);
                return;
            }

            if (isHovering()) {
                PaintUtil.paintSolid(g, this, Color.lightGray);
                return;
            }

            PaintUtil.paintSolid(g, this, ViewUtil.getMenuColor());
        }

        private void addSelectionListener(MenuItemSelected l) {
            listeners.add(l);
        }

        private SubSectionView getView() {
            return view;
        }
    }

    private static class MenuItemGroup implements MenuItemSelected {

        private final ArrayList<MenuItem> group;
        private MenuItem lastSelected;

        public MenuItemGroup() {
            group = new ArrayList<MenuItem>();
        }

        private void addMenuItem(MenuItem i) {
            group.add(i);
            i.addSelectionListener(this);
        }

        public void itemSelected(MenuItem mi) {
            if (lastSelected != null) {
                lastSelected.setSelected(false);
            }
            lastSelected = mi;
        }
    }

    private SubSectionView currentView;
    private final JPanel contentContainer;
    private Component glue = Box.createVerticalGlue();
    private final MenuItemGroup itemGroup;
    private static List<SubSectionView> subSectionViews = new ArrayList<SubSectionView>();

    public Menu(JPanel panel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        contentContainer = panel;
        itemGroup = new MenuItemGroup();
        
        ReferenceController.getInstance().addReferenceListener(new ReferenceListener() {
            public void referenceChanged(String referenceName) {
                updateSections();
            }
            public void referenceAdded(String name) {}
            public void referenceRemoved(String name) {}
        });
        
        ProjectController.getInstance().addProjectListener(new ProjectListener() {
            public void projectAdded(String projectName) {}
            public void projectRemoved(String projectName) {}
            public void projectChanged(String projectName) {      
                if(!GeneticsSection.isInitialized){ 
                    //once this section is initialized, referencecombobox fires
                    //referencechanged event on every project change
                    updateSections();
                }
            }
            public void projectTableRemoved(int projid, int refid) {}
        });
        
        LoginController.addLoginListener(new LoginListener() {
            public void loginEvent(LoginEvent evt) {
                if (evt.getType() == LoginEvent.EventType.LOGGED_OUT) {
                    contentContainer.removeAll();
                    ViewController.getInstance().changeSubSectionTo(null);
                }
            }
        });
    }
    
    public void updateSections(){
        for(int i = 0; i < subSectionViews.size(); i++) {
            subSectionViews.get(i).setUpdateRequired(true);
        }
        if (currentView != null) {
            setContentTo(currentView, true);
        }    
    }

    public void addComponent(Component c) {
        remove(glue);
        add(Box.createVerticalStrut(10));
        add(c);
        add(glue);
    }

    public void addSection(SectionView section) {

        remove(glue);
        
        add(Box.createVerticalStrut(10));

        JPanel p = ViewUtil.alignLeft(ViewUtil.getMenuSectionLabel(section.getName()));
        p.setBorder(ViewUtil.getMenuItemBorder());
        add(p);

        for (SubSectionView v : section.getSubSections()) {
            MenuItem subsectionLabel = new MenuItem(v);
            itemGroup.addMenuItem(subsectionLabel);
            subsectionLabel.addSelectionListener(this);
            add(subsectionLabel);
        }

        this.add(glue);
    }
    
    public void itemSelected(MenuItem mi) {
        setContentTo(mi.getView(), false);
    }
    
    private void setContentTo(SubSectionView v, boolean update) {
        currentView = v;
        contentContainer.removeAll();
        contentContainer.add(v.getView(update || v.isUpdateRequired()), BorderLayout.CENTER);
        v.setUpdateRequired(false);
        contentContainer.updateUI();
    }
    
    public void refreshSelection() {
        if (itemGroup.lastSelected != null) {
            itemSelected(itemGroup.lastSelected);
        }
    }
}


