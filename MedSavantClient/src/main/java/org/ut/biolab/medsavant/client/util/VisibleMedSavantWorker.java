/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

public abstract class VisibleMedSavantWorker<T> extends MedSavantWorker<T> implements PropertyChangeListener {

    private static final String THREAD_INTERRUPTED_LABEL = "CANCELLED";
    private static final String THREAD_EXCEPTION_LABEL = "FAILED";
    private static final Log LOG = LogFactory.getLog(VisibleMedSavantWorker.class);

    public static enum JobStatus {

        NOT_STARTED, RUNNING, CANCELLED, FINISHED
    };
    double progress = 0;
    String statusMessage = "";
    String title;
    boolean cancellable;
    boolean indeterminate;
    private final JLabel titleLabel;
    private final JLabel statusLabel;
    private final ProgressWheel progressBar;
    private final JButton cancelButton;
    private final JButton resultsButton;
    private final JButton closeButton;
    private JobStatus status;
    private boolean showResultsOnFinish = true;
    private JPanel view;

    public VisibleMedSavantWorker(String pageName, String title) {
        super(pageName);
        view = ViewUtil.getClearPanel();
        view.setOpaque(false);
        view.setBorder(ViewUtil.getMediumBorder());
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));

        this.title = title;
        titleLabel = new JLabel(title);
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        statusLabel = new JLabel();

        progressBar = ViewUtil.getIndeterminateProgressBar();

        resultsButton = ViewUtil.getSoftButton("View Results");
        cancelButton = ViewUtil.getSoftButton("Cancel");
        closeButton = ViewUtil.getSoftButton("Close");
        resultsButton.setVisible(false);
        closeButton.setVisible(false);
        resultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showResults();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //   setStatus(JobStatus.CANCELLED);
                cancel(true);
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                closeJob();
            }
        });

        JPanel buttonBar = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(buttonBar);

        buttonBar.add(resultsButton);
        buttonBar.add(cancelButton);
        buttonBar.add(closeButton);
        JPanel statusPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(statusPanel);
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(progressBar);
        statusPanel.setPreferredSize(new Dimension(300, 25));
        statusPanel.setMinimumSize(new Dimension(300, 25));

        view.add(ViewUtil.alignLeft(titleLabel));
        view.add(statusPanel);
        view.add(ViewUtil.alignRight(buttonBar));

        setStatus(JobStatus.NOT_STARTED);
    }

    public void showResultsOnFinish(boolean srof) {
        this.showResultsOnFinish = srof;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        this.statusLabel.setText(ViewUtil.ellipsize(statusMessage, 45));
        this.statusLabel.setToolTipText(statusMessage);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.titleLabel.setText(title);
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean isCancellable) {
        this.cancellable = isCancellable;
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

    public void setIndeterminate(boolean indeterminate) {
        //was commented
        this.indeterminate = indeterminate;
        this.progressBar.setIndeterminate(indeterminate);
    }

    public JobStatus getStatus() {
        return status;
    }

    protected void setStatus(JobStatus st) {
        this.status = st;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (status) {
                    case CANCELLED:
                        setStatusMessage("Cancelled");
                        cancelButton.setVisible(false);
                        closeButton.setVisible(true);
                        progressBar.setVisible(false);
                        break;
                    case NOT_STARTED:
                        break;
                    case RUNNING:
                        break;
                    case FINISHED:
                        if (showResultsOnFinish) {
                            resultsButton.setVisible(true);
                        }
                        cancelButton.setVisible(false);
                        closeButton.setVisible(true);
                        progressBar.setIndeterminate(false);
                        progressBar.setComplete();
                        progressBar.setValue(progressBar.getMaximum());
                        break;
                }
            }
        });
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        if (!progressBar.isIndeterminate() && "progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }

    @Override
    protected final T doInBackground() throws Exception {
        try {
            setStatus(JobStatus.RUNNING);
            if (!isIndeterminate()) {
                startProgressTimer();
            }
            return runInBackground();
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    @Override
    public final boolean cancel(boolean cancel) {
        boolean b = super.cancel(cancel);
        if (cancel) {
            setStatus(JobStatus.CANCELLED);
            cancelJob();
        }
        return b;
    }

    @Override
    protected final void showSuccess(T result) {
        //What happens when job is done executing?
        setStatus(JobStatus.FINISHED);
        jobDone();
    }

    /**
     * The function be executed when the job is done, after the job status has
     * been set to finished.
     */
    protected void jobDone() {
        setStatusMessage("Finished.");
    }

    /**
     * Called when cancel button pushed.
     */
    protected void cancelJob() {
        setStatusMessage("Cancelled");
    }

    /**
     * Called if thread throws an exception.
     */
    @Override
    protected void showFailure(Throwable ex) {
        setStatus(JobStatus.CANCELLED);
        if (ex instanceof InterruptedException) {
            setStatusMessage(THREAD_INTERRUPTED_LABEL);
        } else {
            LOG.error(THREAD_EXCEPTION_LABEL, ex);
            setStatusMessage(THREAD_EXCEPTION_LABEL);
        }
    }

    /**
     * The function to be run in the background.
     */
    protected abstract T runInBackground() throws Exception;

    /**
     * Called when results button pushed
     */
    protected void showResults() {
    }

    /**
     * Called when close button pushed.
     */
    protected void closeJob() {
    }
}
