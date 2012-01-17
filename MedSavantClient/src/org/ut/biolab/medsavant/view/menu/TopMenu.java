/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import com.jidesoft.swing.JideSplitButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JToolBar;
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

/**
 *
 * @author Andrew
 */
public class TopMenu extends JToolBar {
    
    private SubSectionView currentView;
    private final JPanel contentContainer;
    private List<SubSectionView> subSectionViews = new ArrayList<SubSectionView>();
    
    public TopMenu(JPanel panel){
        super();
        this.setFloatable(false);
        
        contentContainer = panel;
        
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
                    currentView = null;
                }
            }
        });
    }
    
    public void addSection(SectionView section) {
        JideSplitButton button = new JideSplitButton(section.getName(), section.getIcon());
        button.setAlwaysDropdown(true);
        button.setButtonSelected(false);
        add(button);      
        
        for (final SubSectionView v : section.getSubSections()) {
            subSectionViews.add(v);
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
    
    public void updateSections(){
        for(int i = 0; i < subSectionViews.size(); i++) {
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
