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

package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.*;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;

import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import savant.util.MiscUtils;


/**
 *
 * @author Andrew
 */
public abstract class ProgressDialog extends JDialog {

    protected static final Log LOG = LogFactory.getLog(ProgressDialog.class);

    protected ProgressWheel bar;
    protected Timer progressTimer;
    private Exception failure;

    /**
     * Constructor for an indeterminate progress dialog with no cancel button.
     * @param title title of the dialog
     * @param message describes the overall process
     */
    public ProgressDialog(String title, String message) {
        this(title, message, false);
        pack();

        setLocationRelativeTo(getParent());
    }

    ProgressDialog(String title, String message, boolean cancellable) {
        super(DialogUtils.getFrontWindow(), title, Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);

        Container p = getContentPane();
        p.setLayout(new GridBagLayout());

        JLabel messageLabel = new JLabel(message);

        bar = ViewUtil.getIndeterminateProgressBar();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 45, 3, 45);
        p.add(messageLabel, gbc);
        gbc.insets = new Insets(3, 45, cancellable ? 3 : 30, 45);
        p.add(bar, gbc);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    @Override
    public void setVisible(boolean flag) {
        if (flag) {
            new MedSavantWorker<Void>("Progress") {
                @Override
                public Void doInBackground() throws Exception {
                    ProgressDialog.this.run();
                    return null;
                }

                @Override
                protected void showProgress(double frac) {
                    if (frac >= 1.0) {
                        setVisible(false);
                    }
                }

                @Override
                protected void showSuccess(Void result) {
                }

                @Override
                protected void showFailure(Throwable x) {
                    if ((x instanceof Exception) && !(x instanceof InterruptedException)) {
                        failure = (Exception)x;
                    } else {
                        super.showFailure(x);
                    }
                }

                /**
                 * Override the basic <c>done()</c> method to stop any associated progress-timer.
                 */
                @Override
                public void done() {
                    if (progressTimer != null) {
                        progressTimer.stop();
                    }
                    super.done();
                }
            }.execute();

            if (progressTimer != null) {
                progressTimer.start();
            }
        }
        super.setVisible(flag);
    }

    /**
     * Performs the actual work whose progress is being represented by this dialog.
     */
    public abstract void run() throws Exception;

    /**
     * Sometimes the caller may want to know why the operation failed.  Classes which don't care about the exception, or which handle
     * everything internally to their <c>run()</c> methods, can just call <c>setVisible(true)</c>.
     */
    public void showDialog() throws Exception {
        setVisible(true);
        if (failure != null) {
            throw failure;
        }
    }
}
