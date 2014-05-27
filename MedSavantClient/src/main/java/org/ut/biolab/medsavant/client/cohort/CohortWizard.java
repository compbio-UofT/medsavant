/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.cohort;

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
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;


import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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
        setResizable(true);
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
                    MedSavantClient.CohortManager.addCohort(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), cohortName);                    
                    setVisible(false);
                }
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error adding cohort: %s", ex);
            }
        }
    }

    private boolean validateName() throws SQLException, RemoteException {
        Cohort[] existingCohorts = null;
        try {
            existingCohorts = MedSavantClient.CohortManager.getCohorts(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return false;
        }
        for (Cohort coh: existingCohorts) {
            if (coh.getName().equals(cohortName)) {
                DialogUtils.displayError("Sorry", "Cohort name already in use.");
                return false;
            }
        }
        return true;
    }
}
