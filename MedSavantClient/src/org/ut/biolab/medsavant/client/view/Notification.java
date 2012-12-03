package org.ut.biolab.medsavant.client.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

public abstract class Notification {

    public static enum JobStatus { NOT_STARTED, RUNNING, CANCELLED, FINISHED };

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
    private JobStatus status;

    public Notification(String title) {
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

        resultsButton.setVisible(false);

        resultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showResults();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                cancelJob();
            }
        });

        JPanel bottomBar = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(bottomBar);

        bottomBar.add(resultsButton);
        bottomBar.add(cancelButton);

        JPanel statusPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(statusPanel);
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(progressBar);

        view.add(ViewUtil.alignLeft(titleLabel));
        //view.add(ViewUtil.alignLeft());
        view.add(statusPanel);
        view.add(ViewUtil.alignRight(bottomBar));
    }

    public final JPanel getView() {
        return view;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        //this.progressBar.setIndeterminate(false);
        //this.progress = progress;
        //this.progressBar.setValue((int)(progress*100));
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        System.out.println(statusMessage);
        this.statusMessage = statusMessage;
        this.statusLabel.setText(ViewUtil.ellipsize(statusMessage, 15));
        //this.statusLabel.setText(statusMessage);
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
        //this.indeterminate = indeterminate;
        //this.progressBar.setIndeterminate(indeterminate);
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
        switch (status) {
            case NOT_STARTED:
                break;
            case RUNNING:
                break;
            case FINISHED:
                resultsButton.setVisible(true);
                cancelButton.setVisible(false);
                progressBar.setIndeterminate(false);
                progressBar.setValue(progressBar.getMaximum());
                break;
        }
    }

    public abstract void showResults();

    public abstract void cancelJob();


}