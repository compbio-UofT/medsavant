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

package org.ut.biolab.medsavant.variant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.WizardDialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Code shared by VariantWorkers which update the variant tables, either by uploading variants or by removing them.
 * Unlike PublicationWorkers, these 1) grab a database lock, and 2) enable the publishing UI upon success.
 *
 * @author tarvara
 */
public abstract class UpdateWorker extends VariantWorker {
    
    private static final Log LOG = LogFactory.getLog(UpdateWorker.class);

    protected int updateID;
    private final String publishText;

    private final JCheckBox autoPublishCheck;
    private final JLabel publishProgressLabel;
    private final JProgressBar publishProgressBar;
    private final JButton publishButton;
    
    protected UpdateWorker(String activity, WizardDialog wizard, JLabel progressLabel, JProgressBar progressBar, JButton workButton, JCheckBox autoPublishCheck, JLabel publishProgressLabel, JProgressBar publishProgressBar, JButton publishButton) {
        super(activity, wizard, progressLabel, progressBar, workButton);
        this.autoPublishCheck = autoPublishCheck;
        this.publishProgressLabel = publishProgressLabel;
        this.publishProgressBar = publishProgressBar;
        this.publishButton = publishButton;
        this.publishText = publishButton.getText();
        
        wizard.getCurrentPage().fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
    }

    @Override
    protected void showSuccess(Void result) {
        super.showSuccess(result);

        publishProgressLabel.setVisible(true);
        publishProgressBar.setVisible(true);

        autoPublishCheck.setVisible(false);

        if (autoPublishCheck.isSelected()) {
            new PublicationWorker(updateID, wizard, publishProgressLabel, publishProgressBar, publishButton).execute();
        } else {
            publishButton.setVisible(true);

            if (publishButton.getActionListeners().length == 0) {
                publishButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        new PublicationWorker(updateID, wizard, publishProgressLabel, publishProgressBar, publishButton).execute();
                        publishButton.setText("Cancel");
                    }
                });
            }
            wizard.getCurrentPage().fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
        }
    }

    @Override
    protected void showFailure(Throwable ex) {
        super.showFailure(ex);
        if (ex instanceof InterruptedException) {
            publishButton.setText(publishText);
        }
    }
}
