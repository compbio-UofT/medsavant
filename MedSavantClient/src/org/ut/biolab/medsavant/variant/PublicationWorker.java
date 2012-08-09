/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.variant;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.WizardDialog;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;


/**
 * Worker class which publishes variants.  Shared by ImportVariantsWizard, RemoveVariantsWizard, and ProjectWizard.
 *
 * @author tarkvara
 */
public class PublicationWorker extends VariantWorker {

    private final int updateID;
    private final String publishText;

    public PublicationWorker(int updateID, WizardDialog wizard, JLabel progressLabel, JProgressBar progressBar, JButton publishButton) {
        super("Publishing variants", wizard, progressLabel, progressBar, publishButton);

        System.out.println("PUBLICATION WORKER HAS UPDATE ID: " + updateID);

        this.updateID = updateID;
        publishText = publishButton.getText();
    }


    @Override
    protected Void doInBackground() throws Exception {
        // Publish.
        MedSavantClient.VariantManager.publishVariants(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), updateID);
        LoginController.getInstance().logout();
        return null;
    }

    @Override
    protected void showSuccess(Void result) {
        super.showSuccess(result);
        wizard.getCurrentPage().fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
    }

    @Override
    protected void showFailure(Throwable ex) {
        super.showFailure(ex);
        if (ex instanceof InterruptedException) {
            workButton.setText(publishText);
        }
    }
}
