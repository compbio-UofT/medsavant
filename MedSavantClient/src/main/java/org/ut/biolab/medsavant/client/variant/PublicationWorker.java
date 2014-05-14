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
package org.ut.biolab.medsavant.client.variant;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.WizardDialog;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;


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
        MedSavantClient.VariantManager.publishVariants(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), updateID);
        MedSavantFrame.getInstance().forceRestart();
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
