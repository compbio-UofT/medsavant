/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.format.AnnotationField;

/**
 *
 * @author Andrew
 */
public final class FilterPanelSub extends JPanel{
    
    private int id;
    private JPanel contentPanel;
    private List<FilterPanelSubItem> subItems = new ArrayList<FilterPanelSubItem>();   
    private FilterPanel parent;
    private boolean isRemoved = false;
    
    private static Color BAR_COLOUR = Color.black;
    private static Color BUTTON_OVER_COLOUR = Color.gray;
    
    public FilterPanelSub(final FilterPanel parent, int id){
        
        this.id = id;
        this.parent = parent;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));        
        setBorder(BorderFactory.createLineBorder(BAR_COLOUR, 1));
        
        
        //title bar
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BAR_COLOUR);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                contentPanel.setVisible(!contentPanel.isVisible());
                contentPanel.invalidate();
            }
        });
        titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel("Sub Query " + id);
        titleLabel.setForeground(Color.white);
        titlePanel.add(Box.createRigidArea(new Dimension(10,20)));
        titlePanel.add(titleLabel);  
        
        titlePanel.add(Box.createHorizontalGlue());     
        
        final JLabel addLabel = new JLabel(" + ");
        addLabel.setToolTipText("Add new filter");
        addLabel.setForeground(Color.white);
        addLabel.setBackground(BAR_COLOUR);
        addLabel.setOpaque(true);
        final FilterPanelSub instance = this;
        addLabel.addMouseListener(new MouseListener() {
            
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {
                parent.showOptions(instance);
                addLabel.setBackground(BAR_COLOUR);
            }

            public void mouseEntered(MouseEvent e) {
                addLabel.setBackground(BUTTON_OVER_COLOUR);
            }

            public void mouseExited(MouseEvent e) {
                addLabel.setBackground(BAR_COLOUR);
            }
        });
        titlePanel.add(addLabel);
                
        final JLabel removeLabel = new JLabel(" X ");
        removeLabel.setToolTipText("Remove sub query and all contained filters");
        removeLabel.setForeground(Color.white);
        removeLabel.setBackground(BAR_COLOUR);
        removeLabel.setOpaque(true);
        removeLabel.addMouseListener(new MouseListener() {
            
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {
                removeThis();
            }

            public void mouseEntered(MouseEvent e) {
                removeLabel.setBackground(BUTTON_OVER_COLOUR);
            }

            public void mouseExited(MouseEvent e) {
                removeLabel.setBackground(BAR_COLOUR);
            }
        });
        titlePanel.add(removeLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(10,20)));
        
        this.add(titlePanel);

        
        //content pane
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createLineBorder(contentPanel.getBackground(), 6));
        //contentPanel.add(Box.createRigidArea(new Dimension(100,100)));
        this.add(contentPanel);
        
        refreshSubItems();

    }
    
    public void refreshSubItems(){
        contentPanel.removeAll();
        
        //check for removed items
        for(int i = subItems.size()-1; i >= 0; i--){
            if(subItems.get(i).isRemoved()){
                subItems.remove(i);
            }
        }
        
        //refresh panel
        for(int i = 0; i < subItems.size(); i++){
            this.contentPanel.add(subItems.get(i));
            if(i != subItems.size()-1){
                this.contentPanel.add(Box.createRigidArea(new Dimension(5,5)));
            }
        }
        
        this.updateUI();
    }
    
    private void removeThis(){
        for(int i = subItems.size()-1; i >= 0; i--){
            subItems.get(i).removeThis();
        }
        isRemoved = true;
        parent.refreshSubPanels();
    }
    
    public boolean isRemoved(){
        return isRemoved;
    }
    
    public void addNewSubItem(FilterView view, String filterId){
        subItems.add(new FilterPanelSubItem(view, this, filterId));
        refreshSubItems();
    }
    
    public void addNewSubItem(AnnotationField af){
        
        String tablename = ProjectController.getInstance().getCurrentTableName();
        try {
            switch(af.getFieldType()){
                case INT:
                case FLOAT:
                case DECIMAL:
                    subItems.add(new FilterPanelSubItem(VariantNumericFilterView.createFilterView(tablename, af.getColumnName(), id, af.getAlias()), this, af.getColumnName()));
                    break;
                case VARCHAR:
                    subItems.add(new FilterPanelSubItem(VariantStringListFilterView.createFilterView(tablename, af.getColumnName(), id, af.getAlias()), this, af.getColumnName()));
                    break;
                case BOOLEAN:
                    subItems.add(new FilterPanelSubItem(VariantBooleanFilterView.createFilterView(tablename, af.getColumnName(), id, af.getAlias()), this, af.getColumnName()));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshSubItems();
    }
    
    public int getId(){
        return id;
    }
    
    
    
}
