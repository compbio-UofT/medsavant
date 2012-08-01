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

package org.ut.biolab.medsavant.cohort;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author Andrew
 */
public class CohortWizard extends WizardDialog {

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
        page.addText("Choose a name for the cohort.\nThe name cannot already be in use.");

        //setup text field
        final JTextField nameField = new JTextField();
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (nameField.getText() != null && !nameField.getText().equals("")) {
                    cohortName = nameField.getText();
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                } else {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.FINISH);
                }
            }
        });
        page.addComponent(nameField);
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
        finishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });
        bp.addButton(finishButton);

        return bp;
    }

    public void finish() {
        if (cohortName != null && !cohortName.equals("")) {
            try {
                if (validateName()) {
                    MedSavantClient.CohortManager.addCohort(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), cohortName);
                    setVisible(false);
                }
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error adding cohort: %s", ex);
            }
        }
    }

    private boolean validateName() throws SQLException, RemoteException {
        Cohort[] existingCohorts = MedSavantClient.CohortManager.getCohorts(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());
        for (Cohort coh: existingCohorts) {
            if (coh.getName().equals(cohortName)) {
                DialogUtils.displayError("Sorry", "Cohort name already in use.");
                return false;
            }
        }
        return true;
    }
}
