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
