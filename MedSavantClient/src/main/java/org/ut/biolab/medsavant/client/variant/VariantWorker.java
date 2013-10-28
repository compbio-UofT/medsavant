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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.jidesoft.wizard.WizardDialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;


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

        progressLabel.setText(String.format("%s ...", activity));

        // Convert our start button into a cancel button.
        workButton.setText("Cancel");
        if (workButton.getActionListeners().length > 0) {
            workButton.removeActionListener(workButton.getActionListeners()[0]);
        }
        workButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VariantWorker.this.progressBar.setIndeterminate(true);
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
        workButton.setEnabled(false);
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
            progressLabel.setForeground(Color.RED);
            progressLabel.setText(ex.getMessage());
            LOG.error(activity + " failed.", ex);
        }
    }
}
