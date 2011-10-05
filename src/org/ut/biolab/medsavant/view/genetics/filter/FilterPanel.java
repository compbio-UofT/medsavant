/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.LineBorder;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.query.AnnotationField;
import org.ut.biolab.medsavant.db.util.query.AnnotationFormat;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class FilterPanel extends JScrollPane {
    
    private JPanel container;
    private List<FilterPanelSub> subs = new ArrayList<FilterPanelSub>();
    private int subNum = 1;
    
    public FilterPanel(){
        container = new JPanel();
        this.getViewport().add(container);
        
        container.setBorder(BorderFactory.createLineBorder(container.getBackground(), 10));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        createNewSubPanel();
    }

    private JPanel createOrLabel(){
        JPanel p = new JPanel();
        p.setMaximumSize(new Dimension(10000,40));
        p.setLayout(new BorderLayout());
        
        JLabel label = new JLabel("OR");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createLineBorder(label.getBackground(), 8));
        label.setFont(ViewUtil.getMediumTitleFont());
        
        p.add(label, BorderLayout.CENTER);
        return p;
    }
 
    private JPanel createNewOrButton(){
        JPanel p = new JPanel();
        p.setMaximumSize(new Dimension(10000,40));
        p.setLayout(new BorderLayout());
        
        JLabel label = new JLabel("Create new sub query");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createLineBorder(label.getBackground(), 8));
        label.setFont(ViewUtil.getMediumTitleFont());
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                createNewSubPanel();
            }
        });
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        p.add(label, BorderLayout.CENTER);
        return p;
    }
    
    private void createNewSubPanel(){
        subs.add(new FilterPanelSub(this, subNum++));
        refreshSubPanels();
    }
    
    public void refreshSubPanels(){
        container.removeAll();
        
        //check for removed items
        for(int i = subs.size()-1; i >= 0; i--){
            if(subs.get(i).isRemoved()){
                subs.remove(i);
            }
        }
        
        //refresh panel
        for(int i = 0; i < subs.size(); i++){
            container.add(subs.get(i));
            if(i != subs.size()-1){
                container.add(createOrLabel());
            } 
        }
        container.add(createNewOrButton());
        container.add(Box.createVerticalGlue());
        
        this.updateUI();
    }
    
    public void showOptions(FilterPanelSub fps){
        container.removeAll();
        
        container.add(createSectionHeader("Default"));
        
        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for(AnnotationFormat af : afs){
            for(AnnotationField field : af.getAnnotationFields()){
                if(field.isFilterable()){
                    container.add(createClickableLabel(fps, field));
                }
            }
        }
        
        this.updateUI();        
    }
    
    private JPanel createSectionHeader(String title){
        final JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(10000, 30));
        
        JLabel l = new JLabel(title);
        l.setFont(ViewUtil.getMediumTitleFont());
        p.add(l, BorderLayout.WEST);
        
        return p;
    }
    
    private JPanel createClickableLabel(final FilterPanelSub fps, final AnnotationField af){
        final JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(10000, 22));
        final Color defaultColor = p.getBackground();
        
        JLabel l = new JLabel(af.getAlias());
        l.setFont(ViewUtil.getSmallTitleFont());
  
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fps.addNewSubItem(af);
                refreshSubPanels();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                p.setBackground(Color.white);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                p.setBackground(defaultColor);
            }
        });
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.add(l, BorderLayout.WEST);
        
        return p;
    }
    
    
}
