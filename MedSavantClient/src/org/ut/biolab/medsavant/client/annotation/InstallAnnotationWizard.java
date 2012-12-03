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
package org.ut.biolab.medsavant.client.annotation;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.*;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;

import org.ut.biolab.medsavant.shared.model.AnnotationDownloadInformation;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantProgramInformation;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.PathField;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class InstallAnnotationWizard extends WizardDialog {

    private static final Log LOG = LogFactory.getLog(InstallAnnotationWizard.class);
    private static final String PAGENAME_SRC = "Annotation Source";
    private static final String PAGENAME_CHOOSE = "Choose Annotation";
    private static final String PAGENAME_INSTALL = "Install Annotation";
    private static final String PAGENAME_COMPLETE = "Complete";
    private JPanel chooseContainer;
    private JLabel chooseTitleLabel;
    private JPanel repoChoosePanel;
    private JPanel fileChoosePanel;
    private boolean fromRepository;
    private AnnotationDownloadInformation annotationToInstall;
    private HashMap<String, AnnotationDownloadInformation> annotationKeyToURLMap;

    public InstallAnnotationWizard() throws RemoteException {
        super((Frame) DialogUtils.getFrontWindow(), "Install Annotations", true);
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        //model.append(getAnnotationSourcePage());
        model.append(getChoosePage());
        model.append(getInstallPage());
        model.append(getCompletionPage());
        setPageList(model);

        setSourceFromRepo(true);

        //change next action
        setNextAction(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String pageName = getCurrentPage().getTitle();
                if (pageName.equals(PAGENAME_SRC)) {
                    setCurrentPage(PAGENAME_CHOOSE);
                } else if (pageName.equals(PAGENAME_CHOOSE)) {
                    setCurrentPage(PAGENAME_INSTALL);
                } else if (pageName.equals(PAGENAME_INSTALL)) {
                    setCurrentPage(PAGENAME_COMPLETE);
                }
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(DialogUtils.getFrontWindow());
    }

    private void setSourceFromRepo(boolean fromRepo) {
        this.fromRepository = fromRepo;

        if (chooseContainer != null) {
            this.chooseContainer.removeAll();

            if (this.fromRepository) {
                chooseTitleLabel.setText("Choose annotation from repository:");
                this.chooseContainer.add(this.repoChoosePanel, BorderLayout.CENTER);
            } else {
                chooseTitleLabel.setText("Choose annotation from file:");
                this.chooseContainer.add(this.fileChoosePanel, BorderLayout.CENTER);
            }
        }

    }

    private AbstractWizardPage getAnnotationSourcePage() {
        return new DefaultWizardPage(PAGENAME_SRC) {

            private JRadioButton radioFromRepo = new JRadioButton("MedSavant public repository");
            private JRadioButton radioFromFile = new JRadioButton("file (for custom annotations)");

            {
                ButtonGroup g = new ButtonGroup();
                g.add(radioFromRepo);
                //g.add(radioFromFile);

                radioFromRepo.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        setSourceFromRepo(radioFromRepo.isSelected());
                    }
                });

                radioFromFile.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        setSourceFromRepo(radioFromRepo.isSelected());
                    }
                });

                addText("Install annotation from:");
                addComponent(radioFromRepo);
                addComponent(radioFromFile);

                radioFromRepo.setSelected(true);


            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private AbstractWizardPage getChoosePage() {

        return new DefaultWizardPage(PAGENAME_CHOOSE) {

            {
                chooseContainer = new JPanel();
                chooseContainer.setLayout(new BorderLayout());
                chooseTitleLabel = new JLabel();
                repoChoosePanel = populateRepositoryPanel();
                fileChoosePanel = new PathField(JFileChooser.OPEN_DIALOG);

                addComponent(chooseTitleLabel);
                addComponent(chooseContainer);

                setSourceFromRepo(true);
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private static String keyForAnnotationInfo(AnnotationDownloadInformation i) {
        return keyForAnnotationInfo(i.getReference(), i.getProgramName(), i.getProgramVersion());
    }

    private static String keyForAnnotationInfo(String ref, String prog, String ver) {
        return ref + "_" + prog + "_" + ver;
    }

    private JPanel populateRepositoryPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        try {
            List<AnnotationDownloadInformation> annotationsAvailable = AnnotationDownloadInformation.getDownloadableAnnotations(MedSavantProgramInformation.getVersion());

            if (annotationsAvailable == null) {
                p.add(new JLabel("No annotations are available for this version"), BorderLayout.NORTH);
                return p;
            }

            Object[][] data = new Object[annotationsAvailable.size()][4];
            annotationKeyToURLMap = new HashMap<String, AnnotationDownloadInformation>();
            int i = 0;
            for (AnnotationDownloadInformation a : annotationsAvailable) {
                data[i][0] = a.getReference();
                data[i][1] = a.getProgramName();
                data[i][2] = a.getProgramVersion();
                data[i][3] = a.getDescription();
                annotationKeyToURLMap.put(keyForAnnotationInfo(a), a);
                i++;
            }

            System.out.println(annotationsAvailable.size() + " annotations available");

            StripyTable stp = new StripyTable(data, new String[]{"Reference", "Annotation", "Version", "Description"});
            stp.setBorder(null);
            stp.setShowGrid(false);
            stp.setRowHeight(21);

            SelectionListener listener = new SelectionListener(stp);
            stp.getSelectionModel().addListSelectionListener(listener);

            p.add(stp.getTableHeader(), BorderLayout.NORTH);
            p.add(ViewUtil.getClearBorderedScrollPane(stp), BorderLayout.CENTER);

            stp.addRowSelectionInterval(0, 0);

        } catch (Exception ex) {
            Logger.getLogger(InstallAnnotationWizard.class.getName()).log(Level.SEVERE, null, ex);
        }

        return p;
    }

    public class SelectionListener implements ListSelectionListener {

        JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {

            if (!e.getValueIsAdjusting()) {
                if (e.getSource() == table.getSelectionModel()
                        && table.getRowSelectionAllowed()) {

                    int first = table.getSelectedRows()[0];

                    String ref = (String) table.getValueAt(first, 0);
                    String annotation = (String) table.getValueAt(first, 1);
                    String version = (String) table.getValueAt(first, 2);

                    annotationToInstall = annotationKeyToURLMap.get(keyForAnnotationInfo(ref, annotation, version));
                }
            }
        }
    }

    private AbstractWizardPage getInstallPage() {

        //setup page
        return new DefaultWizardPage(PAGENAME_INSTALL) {

            private JProgressBar progressBar;
            private JButton startButton;

            {
                addText("You are now ready to install this annotation.");

                progressBar = new JProgressBar();

                addComponent(progressBar);

                startButton = new JButton("Install Annotation");
                startButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startButton.setEnabled(false);
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        progressBar.setIndeterminate(true);
                        new MedSavantWorker<Void>("Annotations") {
                            private boolean success;

                            @Override
                            public Void doInBackground() throws Exception {
                                success = create();
                                return null;
                            }

                            @Override
                            protected void showProgress(double fraction) {
                            }

                            @Override
                            protected void showSuccess(Void result) {

                                if (success) {
                                    ((CompletionWizardPage) getPageByTitle(PAGENAME_COMPLETE)).addText("Annotation has been successfully installed.\n\nYou can apply this annotation to a project by\nediting project settings.");
                                } else {
                                    ((CompletionWizardPage) getPageByTitle(PAGENAME_COMPLETE)).addText("Annotation could not be installed.\n\nEither this annotation is already installed or the\ninstallation package is invalid.");
                                }

                                setCurrentPage(PAGENAME_COMPLETE);
                            }

                            @Override
                            protected void showFailure(Throwable t) {
                                InstallAnnotationWizard.this.setVisible(false);
                                LOG.error("Error installing annotation.", t);
                                DialogUtils.displayException("Error", "There was an error while trying to install this annotation.", t);
                            }
                        }.execute();
                    }
                });

                addComponent(ViewUtil.alignRight(startButton));
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private AbstractWizardPage getCompletionPage() {
        return new CompletionWizardPage(PAGENAME_COMPLETE) {

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private boolean create() throws SQLException, IOException {
        return MedSavantClient.AnnotationManagerAdapter.installAnnotationForProject(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), this.annotationToInstall);
    }
}
