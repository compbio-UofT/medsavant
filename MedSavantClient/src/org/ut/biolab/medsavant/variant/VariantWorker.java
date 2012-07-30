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
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.jidesoft.wizard.WizardDialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.clientapi.ProgressCallbackAdapter;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.util.MedSavantWorker;


/**
 * Class which abstracts out the basic behaviour of a worker for doing one of our lengthy variant-related processes.
 *
 * @author tarkvara
 */
public abstract class VariantWorker extends MedSavantWorker<Void> {

    private static final Log LOG = LogFactory.getLog(PublicationWorker.class);

    private final String activity;

    protected final WizardDialog wizard;
    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    protected final JButton workButton;

    /**
     * Initialise functionality shared by all UpdateWorkers and PublicationWorkers.
     *
     * @param activity "Importing", "Publishing", or "Removing"
     * @param wizard the wizard which is providing the user interface
     * @param progressLabel label which indicates progress activity
     * @param progressBar provides quantitative display of progress
     * @param workButton starts the process and cancels it
     */
    VariantWorker(String activity, WizardDialog wizard, JLabel progressLabel, JProgressBar progressBar, JButton workButton) {
        super(activity + "Variants");
        this.activity = activity;
        this.wizard = wizard;
        this.progressLabel = progressLabel;
        this.progressBar = progressBar;
        this.workButton = workButton;

        progressLabel.setText(String.format("%s variants...", activity));

        try {
            MedSavantClient.VariantManager.registerProgressCallback(LoginController.sessionId, new ProgressCallback(progressLabel, progressBar));
        } catch (RemoteException ex) {
            LOG.error("Unable to register progress callback.", ex);
        }

//        progressBar.setIndeterminate(true);
        // Convert our start button into a cancel button.
        workButton.setText("Cancel");
        if (workButton.getActionListeners().length > 0) {
            workButton.removeActionListener(workButton.getActionListeners()[0]);
        }
        workButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VariantWorker.this.workButton.setText("Cancelling...");
                VariantWorker.this.workButton.setEnabled(false);
                cancel(true);
            }
        });

        workButton.setEnabled(true);
        wizard.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    @Override
    protected void showProgress(double fraction) {
    }

    @Override
    protected void showSuccess(Void result) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        progressLabel.setText(String.format("%s complete.", activity));
        wizard.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    @Override
    protected void showFailure(Throwable ex) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        wizard.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        if (ex instanceof InterruptedException) {
            // Cancellation
            progressLabel.setText(String.format("%s cancelled.", activity));
            workButton.setText("Cancel");
        } else {
            // Failure
            ClientMiscUtils.checkSQLException(ex);
            progressLabel.setForeground(Color.red);
            progressLabel.setText(ex.getMessage());
        }
        LOG.error(activity + " failed.", ex);
    }

    private static class ProgressCallback extends MedSavantServerUnicastRemoteObject implements ProgressCallbackAdapter, Serializable {
        private final JLabel label;
        private final JProgressBar bar;

        ProgressCallback(JLabel label, JProgressBar bar) throws RemoteException {
            this.label = label;
            this.bar = bar;
        }

        @Override
        public void progress(final String activity, final double fraction) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    label.setText(activity);
                    bar.setValue((int)Math.round(fraction * 100.0));
                }
            });
        }
    }
}
