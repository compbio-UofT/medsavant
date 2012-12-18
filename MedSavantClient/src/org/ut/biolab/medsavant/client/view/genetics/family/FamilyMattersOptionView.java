package org.ut.biolab.medsavant.client.view.genetics.family;

import org.ut.biolab.medsavant.client.view.Notification;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ExportVCF;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.NotificationsPanel;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class FamilyMattersOptionView {

    private List<IncludeExcludeStep> steps;

    private File lastTDFFile;
    private boolean filtersChangedSinceLastDump;

    protected static class IncludeExcludeStep {

        private JPanel view;
        private JLabel datasetLabel;
        private List<IncludeExcludeCriteria> criteria;

        public IncludeExcludeStep() {
            setupView();
        }

        public JPanel getView() {
            return view;
        }

        public void setDatasetName(String name) {
            datasetLabel.setText(name);
        }

        public List<IncludeExcludeCriteria> getCriteria() {
            return criteria;
        }

        private void setupView() {

            view = new RoundedPanel(10);

            ViewUtil.applyVerticalBoxLayout(view);
            view.setBorder(ViewUtil.getMediumBorder());

            JPanel required = ViewUtil.getClearPanel();
            ViewUtil.applyHorizontalBoxLayout(required);

            required.add(new JLabel("Select variants from "));

            datasetLabel = new JLabel("filtered variants");
            datasetLabel.setFont(ViewUtil.getMediumTitleFont());

            required.add(datasetLabel);

            required.add(new JLabel(" where"));

            IncludeExcludeCriteria c = new IncludeExcludeCriteria();

            view.add(required);
            view.add(c.getView());

            criteria = new ArrayList<IncludeExcludeCriteria>();
            criteria.add(c);

            JLabel addButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD_ON_TOOLBAR));
            addButton.setToolTipText("Add criteria to this step");

            view.add(ViewUtil.alignRight(addButton));

            addButton.addMouseListener(new MouseListener() {
                public void actionPerformed(ActionEvent ae) {
                    final IncludeExcludeCriteria c = new IncludeExcludeCriteria();
                    criteria.add(c);

                    JLabel removeButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
                    removeButton.setToolTipText("Remove criteria from this step");

                    final JPanel criteriaPanel = new JPanel();
                    ViewUtil.applyHorizontalBoxLayout(criteriaPanel);

                    criteriaPanel.add(new JLabel(" and "));
                    criteriaPanel.add(c.getView());
                    criteriaPanel.add(removeButton);

                    view.add(criteriaPanel, view.getComponentCount() - 1);

                    removeButton.addMouseListener(new MouseListener() {
                        public void actionPerformed(ActionEvent ae) {
                            criteria.remove(c);
                            view.remove(criteriaPanel);
                            view.updateUI();
                            view.invalidate();
                        }

                        @Override
                        public void mouseClicked(MouseEvent me) {
                        }

                        @Override
                        public void mousePressed(MouseEvent me) {
                        }

                        @Override
                        public void mouseReleased(MouseEvent me) {
                            actionPerformed(null);
                        }

                        @Override
                        public void mouseEntered(MouseEvent me) {
                        }

                        @Override
                        public void mouseExited(MouseEvent me) {
                        }
                    });

                    view.updateUI();
                    view.invalidate();
                }

                @Override
                public void mouseClicked(MouseEvent me) {
                }

                @Override
                public void mousePressed(MouseEvent me) {
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    actionPerformed(null);
                }

                @Override
                public void mouseEntered(MouseEvent me) {
                }

                @Override
                public void mouseExited(MouseEvent me) {
                }
            });
        }
    }

    private enum AggregateBy {

        Position, Variant, Gene
    }
    private AggregateBy aggregateBy = AggregateBy.Variant;
    private JPanel view;

    public FamilyMattersOptionView() {
        setupView();

        FilterController.getInstance().addListener(new Listener<FilterEvent>() {

            @Override
            public void handleEvent(FilterEvent event) {
                filtersChangedSinceLastDump = true;
            }

        });
    }

    public JPanel getView() {
        return view;
    }

    protected static class IncludeExcludeCriteria {

        @Override
        public String toString() {
            return "IncludeExcludeCriteria{" + "freqAmount=" + freqAmount + ", aggregationType=" + aggregationType + ", frequencyType=" + frequencyType + ", frequencyCount=" + frequencyCount + '}';
        }
        private int freqAmount;
        private AggregationType aggregationType;
        private FrequencyType frequencyType;
        private FrequencyCount frequencyCount;

        public enum AggregationType {

            Variant, Gene
        };

        public AggregationType getAggregationType() {
            return aggregationType;
        }

        public enum FrequencyType {

            ALL, NO, AT_LEAST, AT_MOST
        };

        public FrequencyType getFrequencyType() {
            return frequencyType;
        }

        public enum FrequencyCount {

            Count, Percent
        };

        public FrequencyCount getFequencyCount() {
            return frequencyCount;
        }

        public int getFreqAmount() {
            return freqAmount;
        }

        public Cohort getCohort() {
            Object o = cb.getSelectedItem();
            if (o instanceof Cohort) {
                return (Cohort) o;
            }
            return null;
        }
        private JPanel view;
        private JComboBox freqTypeChoice;
        private JTextField freqAmountField;
        private JComboBox freqCountChoice;
        private JComboBox typeBox;
        private static Cohort[] cohorts;
        private JComboBox cb;

        public IncludeExcludeCriteria() {
            setupView();
        }

        public JPanel getView() {
            return view;
        }

        private void setupView() {
            view = ViewUtil.getClearPanel();

            typeBox = new JComboBox();
            typeBox.addItem("variant exists");
            typeBox.addItem("gene has variant");
            //typeBox.addItem("position has variant");


            typeBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (typeBox.getSelectedIndex()) {
                        case 0:
                            aggregationType = AggregationType.Variant;
                            break;
                        case 1:
                            aggregationType = AggregationType.Gene;
                            break;
                    }
                }
            });

            typeBox.setSelectedIndex(0);

            view.add(typeBox);
            view.add(new JLabel("in"));

            freqTypeChoice = new JComboBox();
            freqTypeChoice.addItem("all");
            freqTypeChoice.addItem("no");
            freqTypeChoice.addItem("at most");
            freqTypeChoice.addItem("at least");

            freqTypeChoice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (freqTypeChoice.getSelectedIndex()) {
                        case 0:
                            frequencyType = FrequencyType.ALL;
                            break;
                        case 1:
                            frequencyType = FrequencyType.NO;
                            break;
                        case 2:
                            frequencyType = FrequencyType.AT_MOST;
                            break;
                        case 3:
                            frequencyType = FrequencyType.AT_LEAST;
                            break;
                    }
                }
            });

            freqTypeChoice.setSelectedIndex(0);

            view.add(freqTypeChoice);


            final JPanel freqPanel = ViewUtil.getClearPanel();
            ViewUtil.applyHorizontalBoxLayout(freqPanel);

            freqAmountField = new JTextField();
            freqAmountField.setColumns(3);
            freqPanel.add(freqAmountField);

            freqAmountField.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent ce) {
                    String freq = freqAmountField.getText();
                    int freqNum = 0;
                    try {
                        freqNum = Integer.parseInt(freq);
                    } catch (Exception e) {
                    }
                    freqAmount = freqNum;
                }
            });

            freqCountChoice = new JComboBox();
            freqCountChoice.addItem("");
            freqCountChoice.addItem("%");
            freqPanel.add(freqCountChoice);

            freqCountChoice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (freqCountChoice.getSelectedIndex()) {
                        case 0:
                            frequencyCount = FrequencyCount.Count;
                            break;
                        case 1:
                            frequencyCount = FrequencyCount.Percent;
                            break;
                    }
                }
            });

            freqCountChoice.setSelectedIndex(0);

            view.add(freqPanel);
            freqPanel.setVisible(false);

            freqTypeChoice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selected = (String) freqTypeChoice.getSelectedItem();
                    freqPanel.setVisible(selected.contains("at"));
                }
            });

            view.add(new JLabel("of individuals in the"));

            cb = new JComboBox();
            cb.setName(COHORT_COMBO);
            updateCohorts(cb);
            view.add(cb);

            view.add(new JLabel("cohort"));


        }

        private void updateCohortComboBoxes() {
            updateCohorts(cb);
        }
    }
    private static String COHORT_COMBO = "COHORT_COMBO";

    void viewDidLoad() {
        for (IncludeExcludeStep s : steps) {
            for (IncludeExcludeCriteria c : s.criteria) {
                c.updateCohortComboBoxes();
            }
        }
    }

    private static void updateCohorts(JComboBox b) {

        Object o = b.getSelectedItem();
        Cohort selected = null;
        if (o instanceof Cohort) {
            selected = (Cohort) o;
        }

        b.removeAllItems();
        Cohort[] cohorts = null;

        try {
            cohorts = MedSavantClient.CohortManager.getCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID());
        } catch (Exception ex) {
            DialogUtils.displayErrorMessage("Error loading cohorts", ex);
        }

        if (cohorts == null || cohorts.length == 0) {
            b.addItem("no cohorts");
            b.setEnabled(false);

        } else {
            b.setEnabled(true);
            for (Cohort c : cohorts) {
                b.addItem(c);
                if (selected != null && c.toString().equals(selected.toString())) {
                    b.setSelectedItem(c);
                }
            }
            //if (selected != null) {
            //    b.setSelectedItem(selected);
            //}
        }

    }
    int jobNumber = 0;

    private void setupView() {
        view = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(view);

        IncludeExcludeStep s = new IncludeExcludeStep();
        view.add(s.getView());

        steps = new ArrayList<IncludeExcludeStep>();
        steps.add(s);


        JLabel addButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD_ON_TOOLBAR));
        addButton.setToolTipText("Add step");

        view.add(Box.createVerticalStrut(10));
        view.add(ViewUtil.alignLeft(addButton));

        addButton.addMouseListener(new MouseListener() {
            public void actionPerformed(ActionEvent ae) {
                final IncludeExcludeStep c = new IncludeExcludeStep();
                c.setDatasetName("previous step");

                steps.add(c);

                JLabel removeButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
                removeButton.setToolTipText("Remove step");

                final JPanel criteriaPanel = ViewUtil.getClearPanel();
                ViewUtil.applyHorizontalBoxLayout(criteriaPanel);

                criteriaPanel.add(removeButton);
                criteriaPanel.add(Box.createHorizontalStrut(10));
                criteriaPanel.add(c.getView());

                final Component strut = Box.createVerticalStrut(10);
                view.add(strut, view.getComponentCount() - 3);
                view.add(criteriaPanel, view.getComponentCount() - 3);

                removeButton.addMouseListener(new MouseListener() {
                    public void actionPerformed(ActionEvent ae) {
                        steps.remove(c);
                        view.remove(strut);
                        view.remove(criteriaPanel);
                        view.updateUI();
                        view.invalidate();
                    }

                    @Override
                    public void mouseClicked(MouseEvent me) {
                    }

                    @Override
                    public void mousePressed(MouseEvent me) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent me) {
                        actionPerformed(null);
                    }

                    @Override
                    public void mouseEntered(MouseEvent me) {
                    }

                    @Override
                    public void mouseExited(MouseEvent me) {
                    }
                });

                view.updateUI();
                view.invalidate();
            }

            @Override
            public void mouseClicked(MouseEvent me) {
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                actionPerformed(null);
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });

        view.add(Box.createVerticalStrut(10));

        JButton runButton = new JButton("Run");

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                new MedSavantWorker<Object>(FamilyMattersOptionView.class.getCanonicalName()) {
                    MedSavantWorker currentWorker;

                    @Override
                    protected void showProgress(double fract) {
                    }

                    @Override
                    protected void showSuccess(Object result) {
                    }

                    @Override
                    protected Object doInBackground() throws Exception {

                        jobNumber++;

                        final Locks.FileResultLock fileLock = new Locks.FileResultLock();
                        final Locks.DialogLock dialogLock = new Locks.DialogLock();

                        Notification j = new Notification("Cohort Analysis #" + jobNumber) {
                            @Override
                            public void showResults() {
                                dialogLock.getResultsDialog().setTitle(this.getTitle());
                                dialogLock.getResultsDialog().setVisible(true);
                            }

                            @Override
                            public void cancelJob() {
                                if (currentWorker != null) {
                                    currentWorker.cancel(true);
                                }
                            }
                        };

                        int notificationID = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).addNotification(j.getView());
                        //AnalyticsJobsPanel.getInstance().addJob(j);

                        j.setStatus(Notification.JobStatus.RUNNING);

                        // File retriever
                        currentWorker = getRetrieverWorker(j, fileLock);
                        currentWorker.execute();
                        try {
                            synchronized (fileLock) {
                                fileLock.wait();
                            }
                        } catch (InterruptedException ex) {
                        }

                        System.out.println("Running algorithm on file " + fileLock.getFile().getAbsolutePath());

                        // Algorithm Runner
                        currentWorker = getAlgorithmWorker(fileLock.getFile(), steps, j, dialogLock);
                        currentWorker.execute();

                        try {
                            synchronized (dialogLock) {
                                dialogLock.wait();
                            }
                        } catch (InterruptedException ex) {
                        } finally {
                            NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).markNotificationAsComplete(notificationID);
                        }

                        j.setStatus(Notification.JobStatus.FINISHED);
                        j.setStatusMessage("Complete");

                        int result = DialogUtils.askYesNo("Cohort Analysis Complete", "<html>Would you like to view the<br/>results now?</html>");
                        if (result == DialogUtils.YES) {
                            dialogLock.getResultsDialog().setVisible(true);
                        }

                        return null;
                    }
                }.execute();

                DialogUtils.displayMessage("Cohort Analysis Submitted");
            }

            private MedSavantWorker getAlgorithmWorker(File file, List<IncludeExcludeStep> steps, Notification j, Locks.DialogLock genericLock) {
                FamilyMattersWorker w = new FamilyMattersWorker(steps, file);
                w.setUIComponents(j);
                w.setCompletionLock(genericLock);
                return w;
            }

            private MedSavantWorker getRetrieverWorker(final Notification m, final Locks.FileResultLock fileLock) {
                return new MedSavantWorker<File>(FamilyMattersOptionView.class.getCanonicalName()) {
                    @Override
                    protected void showProgress(double fract) {
                        m.setProgress(fract);
                    }

                    @Override
                    protected void showSuccess(File result) {
                        m.setStatusMessage("Complete");
                        synchronized (fileLock) {
                            fileLock.setFile(result);
                            fileLock.notify();
                        }
                    }

                    @Override
                    protected File doInBackground() throws Exception {

                        m.setStatusMessage("Retrieving Variants");

                        String session = LoginController.getInstance().getSessionID();
                        int refID = ReferenceController.getInstance().getCurrentReferenceID();
                        int projectID = ProjectController.getInstance().getCurrentProjectID();
                        int updateID = MedSavantClient.ProjectManager.getNewestUpdateID(
                                session,
                                projectID,
                                refID,
                                true);

                        File outdir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());

                        // hard code for testing only
                        File tdfFile = new File("/private/var/folders/np/94t7v45x3ll1nls20039ynk00000gn/T/2012-12-18-14-36/Floating Harbour-varexport-1355859238961.tdf");

                        /*
                        File tdfFile;
                        if (filtersChangedSinceLastDump || (lastTDFFile == null)) {

                            if (lastTDFFile == null) {
                                System.out.println("No previous dump");
                            } else {
                                System.out.println("Filters changed since last dump");
                            }

                            File zipFile = new File(outdir, System.currentTimeMillis() + "-variantdump.tdf.zip");

                            System.out.println("Exporting to " + zipFile.getAbsolutePath());
                            tdfFile = ExportVCF.exportTDF(zipFile, this);
                            System.out.println("Finished export");

                            // remove the old tdf file, it's out of date
                            if (lastTDFFile != null) { lastTDFFile.delete(); }

                            // replace with the new one
                            lastTDFFile = tdfFile;
                        } else {
                            System.out.println("Reusing previous dump");
                            tdfFile = lastTDFFile;
                        }
                        */

                        System.out.println("Imported from server " + tdfFile.getAbsolutePath());

                        filtersChangedSinceLastDump = false;


                        int[] columnsToKeep = new int[]{3, 4, 5, 7, 8, 11, 12};
                        m.setStatusMessage("Stripping file");

                        File strippedFile = awkColumnsFromFile(tdfFile, columnsToKeep);
                        //File strippedFile = new File(tdfFile.getAbsolutePath() + ".awk");
                        System.out.println("Stripped file is " + strippedFile.getAbsolutePath());

                        return strippedFile;
                    }

                    private File awkColumnsFromFile(File inFile, int[] columnsToKeep) throws IOException {

                        File outfile = new File(inFile.getAbsolutePath() + ".awk");

                        CSVReader r = new CSVReader(new FileReader(inFile));
                        CSVWriter w = new CSVWriter(new FileWriter(outfile));

                        String[] line = new String[0];
                        String[] subsetLines = new String[columnsToKeep.length];
                        while ((line = readNext(r)) != null) {
                            clear(subsetLines);

                            for (int i = 0; i < columnsToKeep.length; i++) {
                                subsetLines[i] = line[columnsToKeep[i]];
                            }

                            w.writeNext(subsetLines);
                        }

                        r.close();
                        w.close();

                        return outfile;
                    }

                    private String[] readNext(CSVReader recordReader) throws IOException {
                        String[] line = recordReader.readNext();
                        if (line == null) {
                            return null;
                        } else {
                            line[line.length - 1] = removeNewLinesAndCarriageReturns(line[line.length - 1]);
                        }

                        return line;
                    }

                    private String removeNewLinesAndCarriageReturns(String next) {

                        next = next.replaceAll("\n", "");
                        next = next.replaceAll("\r", "");

                        return next;
                    }

                    private void clear(String[] subsetLines) {
                        for (int i = 0; i < subsetLines.length; i++) {
                            subsetLines[i] = null;
                        }
                    }
                };
            }
        });

        view.add(ViewUtil.centerHorizontally(runButton));

    }

}
