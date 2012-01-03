/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.dialog;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;

/**
 *
 * @author Andrew
 */
public class CohortWizard extends WizardDialog {
    
    private String cohortName;
    
    public CohortWizard(){
        setTitle("Cohort Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);
        
        //add pages
        PageList model = new PageList();
        model.append(getNamePage());
        setPageList(model);
        
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private AbstractWizardPage getNamePage(){
        final CompletionWizardPage page = new CompletionWizardPage("Create Cohort"){          
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);              
            }     
        };
        page.addText(
                "Choose a name for the cohort. \n"
                + "The name cannot already be in use. ");
        
        //setup text field
        final JTextField namefield = new JTextField();
        namefield.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                if(namefield.getText() != null && !namefield.getText().equals("")){
                    cohortName = namefield.getText();
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                } else {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.FINISH);
                }
            }
        });
        page.addComponent(namefield);
        return page;
    }
    
    @Override
    public ButtonPanel createButtonPanel(){
        ButtonPanel bp = super.createButtonPanel();
        
        //remove finish button
        bp.removeButton((AbstractButton)bp.getButtonByName(ButtonNames.FINISH));
        
        //add new finish button
        JButton finishButton = new JButton("Finish");
        finishButton.setName(ButtonNames.FINISH);      
        finishButton.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e){
                finish();
            }
        });       
        bp.addButton(finishButton);
        
        return bp;
    }
    
    public void finish(){
        if (cohortName == null || cohortName.equals("")) return;
        try {
            MedSavantClient.CohortQueryUtilAdapter.addCohort(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), cohortName);
        } catch (SQLException ex) {
            Logger.getLogger(CohortWizard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(CohortWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setVisible(false);
        this.dispose();
    }
    
}
