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
package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.PluginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.format.AnnotationField;
import org.ut.biolab.medsavant.db.format.AnnotationField.Category;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.plugin.MedSavantFilterPlugin;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
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
    
    public void showOptions(final FilterPanelSub fps){
        container.removeAll();
        
        
        Map<Category, List<JPanel>> map = new EnumMap<Category, List<JPanel>>(Category.class);
        
        //--add defaults
        map.put(AnnotationField.Category.PATIENT, new ArrayList<JPanel>());
        
        //cohort filter
        map.get(AnnotationField.Category.PATIENT).add(createClickableLabel(fps, new FilterPlaceholder() {
            public FilterView getFilterView() { return CohortFilterView.getCohortFilterView(fps.getId());}
            public String getFilterID() { return CohortFilterView.FILTER_ID;}
            public String getFilterName() { return CohortFilterView.FILTER_NAME;}
        }));  
        
        //gene list filter
        map.get(AnnotationField.Category.PATIENT).add(createClickableLabel(fps, new FilterPlaceholder() {
            public FilterView getFilterView() { return GeneListFilterView.getFilterView(fps.getId());}
            public String getFilterID() { return GeneListFilterView.FILTER_ID;}
            public String getFilterName() { return GeneListFilterView.FILTER_NAME;}
        }));
        
        
        //add from variant table
        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for(AnnotationFormat af : afs){
            for(AnnotationField field : af.getAnnotationFields()){
                if(field.isFilterable() && !FilterController.isFilterActive(fps.getId(), field.getColumnName())){
                    //container.add(createClickableLabel(fps, field));
                    if(!map.containsKey(field.getCategory())){
                        map.put(field.getCategory(), new ArrayList<JPanel>());
                    }
                    map.get(field.getCategory()).add(createClickableLabel(fps, field));
                }
            }
        }
        
        PluginController pc = PluginController.getInstance();
        for (PluginDescriptor desc: pc.getDescriptors()) {
            final MedSavantPlugin p = pc.getPlugin(desc.getID());
            if (p instanceof MedSavantFilterPlugin) {
                if (!map.containsKey(Category.PLUGIN)) {
                    map.put(Category.PLUGIN, new ArrayList<JPanel>());
                }
                map.get(Category.PLUGIN).add(createClickableLabel(fps, new FilterPlaceholder() {
                    public FilterView getFilterView() { return PluginFilterView.getFilterView((MedSavantFilterPlugin)p);}
                    public String getFilterID() { return p.getDescriptor().getID();}
                    public String getFilterName() { return p.getTitle();}
                }));
            }
        }
        
        for(Category c : map.keySet()){
            container.add(createSectionHeader(c.toString()));
            for(JPanel p : map.get(c)){
                container.add(p);
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
    
    private JPanel createClickableLabel(final FilterPanelSub fps, final FilterPlaceholder gen){
        final JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(10000, 22));
        final Color defaultColor = p.getBackground();
        
        JLabel l = new JLabel("     " + gen.getFilterName());
        l.setFont(new Font(ViewUtil.getDefaultFontFamily(),Font.PLAIN,11));
  
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                fps.addNewSubItem(gen.getFilterView(), gen.getFilterID());
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
    
    private JPanel createClickableLabel(final FilterPanelSub fps, final AnnotationField af){
        final JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(10000, 22));
        final Color defaultColor = p.getBackground();
        
        JLabel l = new JLabel("     " + af.getAlias());
        l.setFont(new Font(ViewUtil.getDefaultFontFamily(),Font.PLAIN,11));
        //l.setFont(ViewUtil.getSmallTitleFont());
  
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
    
    /* 
     * Use this to prevent creating all filters when generating list
     */
    interface FilterPlaceholder {
        public FilterView getFilterView();      
        public String getFilterID();
        public String getFilterName();
    }
    
}
