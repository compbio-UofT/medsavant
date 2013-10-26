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

package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.ut.biolab.medsavant.shared.model.ProgressStatus;


/**
 * Progress-dialog variant which provides a cancel button and text describing the current stage of a long process.
 *
 * @author tarkvara
 */
public abstract class CancellableProgressDialog extends ProgressDialog {

    protected boolean cancelled = false;
    private JLabel stageLabel;
    private JButton cancelButton;

    /**
     * Last status returned by checkProgress.  Alternatively, derived classes can just
     * set this directly instead of overriding checkProgress.
     */
    protected ProgressStatus lastStatus;
    private boolean waitForCancelConfirmation;

    public CancellableProgressDialog(String title, String message) {
        this(title,message,true);
    }

    public CancellableProgressDialog(String title, String message, boolean waitForConfirmationOnCancel) {
        super(title, message, true);
        lastStatus = new ProgressStatus("Preparing...", 0.0);
        stageLabel = new JLabel("Preparing...");
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                cancelled = true;
                try {
                    progressTimer.stop();
                    stageLabel.setText("Cancelling...");
                    cancelButton.setEnabled(false);

                    // Call checkProgress() to tell the server that we've cancelled.
                    LOG.info("Calling checkProgress to tell server that we've cancelled.");
                    checkProgress();

                    if (!waitForCancelConfirmation) {
                        setVisible(false);
                    }

                } catch (Exception ex) {
                    LOG.info("Ignoring exception thrown while checking for progress.", ex);
                }
            }
        });

        Container p = getContentPane(); // Layout already set to grid-bag in base-class constructor.

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 45, 3, 45);

        p.add(stageLabel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(3, 45, 15, 45);
        p.add(cancelButton, gbc);

        progressTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    ProgressStatus status = checkProgress();
                    if (status != null) {
                        if (status.fractionCompleted == -1.0) {
                            bar.setIndeterminate(true);
                        } else {
                            bar.setValue((int)Math.round(status.fractionCompleted * 100.0));
                            bar.setIndeterminate(false);
                        }
                        stageLabel.setText(status.message);
                    }
                } catch (Exception ex) {
                    LOG.info("Ignoring exception thrown while checking for progress.", ex);
                }
            }
        });

        setWaitForCancelConfirmation(waitForConfirmationOnCancel);

        pack();
        setLocationRelativeTo(getParent());
    }


    public final void setWaitForCancelConfirmation(boolean b) {
        waitForCancelConfirmation = b;
    }

    /**
     * Derived classes should override this to poll the server for progress.  Alternatively,
     * if there is no server-polling involved, derived classes can just set lastStatus directly.
     */
    public ProgressStatus checkProgress() throws Exception {
        return lastStatus;
    }
}
