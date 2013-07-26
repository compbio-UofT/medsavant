package org.ut.biolab.medsavant.client.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

public abstract class Notification implements PropertyChangeListener{

    public static enum JobStatus {

        NOT_STARTED, RUNNING, CANCELLED, FINISHED
    };
    double progress = 0;
    String statusMessage = "";
    String title;
    boolean cancellable;
    boolean indeterminate;
    private final JPanel view;
    private final JLabel titleLabel;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    private final JButton cancelButton;
    private final JButton resultsButton;
    private final JButton closeButton;
    private JobStatus status;
    private boolean showResultsOnFinish = true;

    public Notification(String title) {
        this.title = title;
        view = ViewUtil.getClearPanel();

        view.setBorder(ViewUtil.getMediumBorder());
        ViewUtil.applyVerticalBoxLayout(view);

        titleLabel = new JLabel(title);
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));

        ///new Font(titleLabel.getFont().getFamily(),Font.BOLD,13)

        statusLabel = new JLabel();

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setIndeterminate(true);

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
                setStatus(JobStatus.CANCELLED);
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
        statusPanel.setPreferredSize(new Dimension(300,25));
        statusPanel.setMinimumSize(new Dimension(300,25));

        if (ClientMiscUtils.MAC) {
            this.progressBar.putClientProperty("JProgressBar.style", "circular");
        }

        view.add(ViewUtil.alignLeft(titleLabel));
        //view.add(ViewUtil.alignLeft());
        view.add(statusPanel);
        //view.add(progressBar);
        view.add(ViewUtil.alignRight(buttonBar));
    }

    public final JPanel getView() {
        return view;
    }

    public double getProgress() {
        return progress;
    }

    public void showResultsOnFinish(boolean srof) {
        this.showResultsOnFinish = srof;
    }

    public void setProgress(double progress) {
        //was commented
        //setIndeterminate(false);
        this.progress = progress;
        //this.progressBar.setValue((int) (progress * 100));
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

    public void setStatus(JobStatus st) {
        this.status = st;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (status) {
                    case CANCELLED:
                        setStatusMessage("Cancelled");
                        cancelButton.setVisible(false);
                        closeButton.setVisible(true);
                        progressBar.setVisible(false);
                        cancelJob();
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
                        progressBar.setValue(progressBar.getMaximum());
                        break;
                }
            }
        });
    }

    public void monitorWorker(SwingWorker worker){
        worker.addPropertyChangeListener(this);
        worker.execute();
    }

    //handle progress bar update.
    public void propertyChange(PropertyChangeEvent evt) {
        if (!progressBar.isIndeterminate() && "progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }

    //Called when results button pushed
    protected abstract void showResults();

    //Called when cancel button pushed.
    protected abstract void cancelJob();

    //Called when close button pushed.
    protected abstract void closeJob();
}