/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import com.jidesoft.swing.JideSplitButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;

/**
 *
 * @author Andrew
 */
public class TopMenu extends JToolBar {
    
    private SubSectionView currentView;
    private final JPanel contentContainer;
    
    public TopMenu(JPanel panel){
        super();
        this.setFloatable(false);
        
        contentContainer = panel;
    }
    
    public void addSection(SectionView section) {
        JideSplitButton button = new JideSplitButton(section.getName(), section.getIcon());
        button.setAlwaysDropdown(true);
        button.setButtonSelected(false);
        add(button);      
        
        for (final SubSectionView v : section.getSubSections()) {
            button.add(new AbstractAction(v.getName()) {
                public void actionPerformed(ActionEvent e) {
                    setContentTo(v, false);
                }
            });
        }
    }
    
    public void addComponent(Component c) {
        add(c);
        add(Box.createHorizontalStrut(30));
    }
    
    private void setContentTo(SubSectionView v, boolean update) {
        currentView = v;
        contentContainer.removeAll();
        contentContainer.add(v.getView(update || v.isUpdateRequired()), BorderLayout.CENTER);
        v.setUpdateRequired(false);
        contentContainer.updateUI();
        ViewController.getInstance().changeSubSectionTo(v);
    }
    
    public void refreshSelection() {
        if(currentView != null){
            setContentTo(currentView, false);
        }
    }

}
