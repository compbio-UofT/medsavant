/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import org.ut.biolab.medsavant.view.ViewController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
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
            this.view = v;
            listeners = new ArrayList<MenuItemSelected>();
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(ViewUtil.getMenuItemBorder());
            //this.setBackground(Color.red);
            //this.setOpaque(false);
            l = ViewUtil.getMenuSubsectionLabel(v.getName());
            this.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    //l.setForeground(Color.red);
                    setSelected(true);
                }

                public void mousePressed(MouseEvent e) {
                    setPressed(true);
                }

                public void mouseReleased(MouseEvent e) {
                    setPressed(false);
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

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            PaintUtil.paintSolid(g, this, ViewUtil.getMenuColor());

            if (isSelected()) {
                PaintUtil.paintSolid(g, this, Color.lightGray);
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

        /*
        private void addSelectionListener(MenuItemGroup grp) {
        listener = grp;
        }
         * 
         */
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

    
    private final JPanel contentContainer;
    private Component glue = Box.createVerticalGlue();
    private final MenuItemGroup itemGroup;

    public Menu(JPanel contentContainer) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setOpaque(false);
        this.contentContainer = contentContainer;
        itemGroup = new MenuItemGroup();
    }

    public void addSection(SectionView section) {

        //if (this.getComponents().length != 0) {
        this.add(Box.createVerticalStrut(10));
        //}
        this.remove(glue);

        JPanel p = ViewUtil.alignLeft(ViewUtil.getMenuSectionLabel(section.getName()));
        p.setBorder(ViewUtil.getMenuItemBorder());
        this.add(p);

        for (SubSectionView v : section.getSubSections()) {
            //this.add(Box.createVerticalStrut(5));
            MenuItem subsectionLabel = new MenuItem(v);
            itemGroup.addMenuItem(subsectionLabel);
            subsectionLabel.addSelectionListener(this);
            this.add(subsectionLabel);
        }

        this.add(glue);
    }
    
    public void itemSelected(MenuItem mi) {
        setContentTo(mi.getView());
    }
    
    private void setContentTo(SubSectionView v) {
        contentContainer.removeAll();
        contentContainer.add(v.getView(), BorderLayout.CENTER);
        contentContainer.updateUI();
    }
    
    public void refreshSelection(){
        if(itemGroup.lastSelected != null){
            itemSelected(itemGroup.lastSelected);
        }
    }
}


