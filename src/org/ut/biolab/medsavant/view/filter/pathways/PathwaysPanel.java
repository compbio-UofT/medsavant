/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter.pathways;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;

/**
 *
 * @author AndrewBrook
 */
public class PathwaysPanel extends JPanel {
    
    private PathwaysTab pathwaysTab;
    private JLabel appliedLabel;
    private JLabel currentLabel;
    private JButton applyButton;
    private JButton removeButton;
    public static String filterName = "WikiPathways";
    
    public PathwaysPanel(){
        pathwaysTab = new PathwaysTab(this);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton chooseButton = new JButton("Choose Pathway");        
        chooseButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                pathwaysTab.setVisible(true);
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(chooseButton);
        p1.add(Box.createHorizontalGlue());
        this.add(p1);
        
        
        this.add(Box.createRigidArea(new Dimension(10,10)));
        
        appliedLabel = new JLabel("Applied Filter: (none)");
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(appliedLabel);
        p2.add(Box.createHorizontalGlue());
        this.add(p2);
        
        currentLabel = new JLabel("Current Filter: (none)");
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        p3.add(currentLabel);
        p3.add(Box.createHorizontalGlue());
        this.add(p3);
        
        this.add(Box.createRigidArea(new Dimension(10,10)));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        removeButton = new JButton("Remove Filter");
        removeButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                removeFilter();
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        removeButton.setEnabled(false);
        buttonPanel.add(removeButton);
        //this.add(removeButton);
        
        applyButton = new JButton("Apply Current Filter");
        applyButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                apply();
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        applyButton.setEnabled(false);
        //this.add(applyButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(Box.createHorizontalGlue());
        
        this.add(buttonPanel);
    }
    
    public void setAppliedFilter(String s){
        appliedLabel.setText("Applied Filter: " + s);
        applyButton.setEnabled(true);
    }
    
    public void setCurrentFilter(String s){
        currentLabel.setText("Current Filter: " + s);
        applyButton.setEnabled(true);
    }
    
    private void removeFilter(){
        FilterController.removeFilter(filterName);
        setAppliedFilter("(none)");
        applyButton.setEnabled(true);
        removeButton.setEnabled(false);
    }
    
    public void enableApply(boolean enable){
        applyButton.setEnabled(enable);
    }
    
    public void apply(){
        pathwaysTab.applyFilter();
        applyButton.setEnabled(false);
        removeButton.setEnabled(true);
    }
    
    
    
    
}
