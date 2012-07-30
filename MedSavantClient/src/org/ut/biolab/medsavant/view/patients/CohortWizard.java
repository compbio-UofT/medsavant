/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.view.patients;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTextField;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;

import java.awt.event.KeyAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;

/**
 *
 * @author Andrew
 */
public class CohortWizard extends WizardDialog {

    private static final Log LOG = LogFactory.getLog(CohortWizard.class);

    private String cohortName;

    public CohortWizard() {
        setTitle("Cohort Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getNamePage());
        setPageList(model);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private AbstractWizardPage getNamePage() {
        final CompletionWizardPage page = new CompletionWizardPage("Create Cohort") {
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
        namefield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (namefield.getText() != null && !namefield.getText().equals("")) {
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
    public ButtonPanel createButtonPanel() {
        ButtonPanel bp = super.createButtonPanel();

        //remove finish button
        bp.removeButton((AbstractButton)bp.getButtonByName(ButtonNames.FINISH));

        //add new finish button
        JButton finishButton = new JButton("Finish");
        finishButton.setName(ButtonNames.FINISH);
        finishButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                finish();
            }
        });
        bp.addButton(finishButton);

        return bp;
    }

    public void finish() {
        if (cohortName == null || cohortName.equals("")) return;
        try {
            MedSavantClient.CohortManager.addCohort(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), cohortName);
        } catch (SQLException ex) {
            LOG.error("Error adding cohort.", ex);
        } catch (RemoteException ex) {
            LOG.error("Error adding cohort.", ex);
        }
        setVisible(false);
        dispose();
    }

}
