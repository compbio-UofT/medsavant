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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.WizardDialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;

/**
 * Worker class which publishes variants.  Shared by ImportVariantsWizard, RemoveVariantsWizard, and ProjectWizard.
 * 
 * @author tarkvara
 */
public class PublicationWorker extends MedSavantWorker<Void> {

    private static final Log LOG = LogFactory.getLog(PublicationWorker.class);

    private final int updateID;
    private final WizardDialog wizard;
    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    private final JButton cancelButton;
    private final JButton startButton;

    public PublicationWorker(int updateID, WizardDialog wizard, JLabel progressLabel, JProgressBar progressBar, JButton cancelButton, JButton startButton) {
        super("PublishVariants");
        this.updateID = updateID;
        this.wizard = wizard;
        this.progressLabel = progressLabel;
        this.progressBar = progressBar;
        this.cancelButton = cancelButton;
        this.startButton = startButton;

        if (cancelButton.getActionListeners().length == 0) {
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PublicationWorker.this.cancelButton.setText("Cancelling...");
                    PublicationWorker.this.cancelButton.setEnabled(false);
                    cancel(true);
                }
            });
        }
    }

    @Override
    protected void showProgress(double fraction) {
    }

    @Override
    protected void showSuccess(Void result) {
        cancelButton.setVisible(false);
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        progressLabel.setText("Publish complete.");
        wizard.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        wizard.getCurrentPage().fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
    }

    @Override
    protected void showFailure(Throwable ex) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        wizard.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //cancellation
        if (ex instanceof InterruptedException) {
            progressLabel.setText("Publish cancelled.");
            startButton.setVisible(true);
            startButton.setEnabled(true);
            cancelButton.setText("Cancel");
            cancelButton.setEnabled(true);
            cancelButton.setVisible(false);

            //failure
        } else {
            ClientMiscUtils.checkSQLException(ex);
            progressLabel.setForeground(Color.red);
            progressLabel.setText(ex.getMessage());
            startButton.setVisible(false);
            cancelButton.setVisible(false);
        }
        LOG.error("Error publishing variants.", ex);
    }

    @Override
    protected Void doInBackground() throws Exception {
        progressLabel.setText("Publishing variants...");
        progressBar.setIndeterminate(true);
        wizard.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // publish
        MedSavantClient.VariantManager.publishVariants(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), updateID);

        LoginController.logout();
        return null;
    }
}
