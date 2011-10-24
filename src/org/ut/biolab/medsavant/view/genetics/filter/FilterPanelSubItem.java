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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class FilterPanelSubItem extends JPanel{
    
    private JComponent contentPanel;
    private FilterView filterView;
    private boolean isRemoved;
    private FilterPanelSub parent;
    private String filterId;
    
    private static Color BAR_COLOUR = Color.darkGray;
    private static Color BUTTON_OVER_COLOUR = Color.gray;
    
    
    
    public FilterPanelSubItem(FilterView filterView, FilterPanelSub parent, String filterId){
        
        this.parent = parent;
        this.filterView = filterView;
        this.filterId = filterId;
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));        
        setBorder(ViewUtil.getTinyLineBorder());
        
        //title bar
        JPanel titlePanel = ViewUtil.getSecondaryBannerPanel();
        //titlePanel.setBackground(BAR_COLOUR);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        JLabel testLabel = new JLabel(filterView.getTitle());
        testLabel.setForeground(Color.black);
        titlePanel.add(Box.createRigidArea(new Dimension(10,16)));
        titlePanel.add(testLabel);  
        titlePanel.add(Box.createHorizontalGlue());     
        
        final JLabel removeLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE));
        removeLabel.setToolTipText("Remove filter");
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

        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                contentPanel.setVisible(!contentPanel.isVisible());
                contentPanel.invalidate();
            }
        });
        titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        contentPanel = filterView.getComponent();
        this.add(contentPanel);
        
    }
    
    public void removeThis(){
        FilterController.removeFilter(filterId, parent.getId());
        isRemoved = true;
        parent.refreshSubItems();
    }
    
    public boolean isRemoved(){
        return isRemoved;
    }
    
    public String getFilterId(){
        return filterId;
    }
    
    
    
}
