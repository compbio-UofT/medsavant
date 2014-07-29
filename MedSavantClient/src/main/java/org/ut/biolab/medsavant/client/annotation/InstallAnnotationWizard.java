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
package org.ut.biolab.medsavant.client.annotation;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;

import org.ut.biolab.medsavant.shared.model.AnnotationDownloadInformation;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.PathField;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.shared.util.VersionSettings;

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
    private PathField fileChoosePanel;
    private JLabel progressLabel;
    private boolean fromRepository;
    private AnnotationDownloadInformation annotationToInstall;
    private HashMap<String, AnnotationDownloadInformation> annotationKeyToURLMap;
    private boolean hasAnnotations = false;

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

        //setSourceFromRepo(true,null);

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
        setResizable(true);
        setLocationRelativeTo(DialogUtils.getFrontWindow());
    }

    private void setSourceFromRepo(boolean fromRepo, final DefaultWizardPage page) {
        this.fromRepository = fromRepo;

        if (chooseContainer != null) {
            this.chooseContainer.removeAll();

            if (this.fromRepository) {
                chooseTitleLabel.setText("Choose annotation from repository:");
                this.chooseContainer.add(this.repoChoosePanel, BorderLayout.CENTER);

                JButton fromFile = new JButton("Install from file");
                fromFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        chooseContainer.removeAll();
                        chooseTitleLabel.setText("Choose annotation from file:");
                        chooseContainer.add(fileChoosePanel, BorderLayout.CENTER);
                        fromRepository = false;
                        String s = fileChoosePanel.getTextField().getText();
                        if (s == null || s.length() < 1) {
                            page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);

                        }
                        chooseContainer.updateUI();
                    }
                });
                this.chooseContainer.add(ViewUtil.alignLeft(fromFile), BorderLayout.SOUTH);

            } else {
                chooseTitleLabel.setText("Choose annotation from file:");
                chooseContainer.add(fileChoosePanel, BorderLayout.CENTER);
            }
        }

    }

    @Deprecated
    private AbstractWizardPage getAnnotationSourcePage() {
        return new DefaultWizardPage(PAGENAME_SRC) {
            private JRadioButton radioFromRepo = new JRadioButton("MedSavant public repository");
            private JRadioButton radioFromFile = new JRadioButton("file (for custom annotations)");

            {
                ButtonGroup g = new ButtonGroup();
                g.add(radioFromRepo);
                //g.add(radioFromFile);

                final DefaultWizardPage instance = this;
                radioFromRepo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        setSourceFromRepo(radioFromRepo.isSelected(), instance);
                    }
                });

                radioFromFile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        setSourceFromRepo(radioFromRepo.isSelected(), instance);
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
                chooseContainer = ViewUtil.getClearPanel();
                chooseContainer.setLayout(new BorderLayout());
                chooseTitleLabel = new JLabel();
                repoChoosePanel = populateRepositoryPanel(this);
                fileChoosePanel = new PathField(JFileChooser.OPEN_DIALOG);
                fileChoosePanel.setFileFilters(ExtensionFileFilter.createFilters(new String[]{"gz", "zip"}));


                fileChoosePanel.getTextField().addCaretListener(new CaretListener() {
                    @Override
                    public void caretUpdate(CaretEvent ce) {

                        if (!fileChoosePanel.getTextField().getText().isEmpty()) {
                            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        } else {
                            fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                        }
                    }
                });

                addComponent(chooseTitleLabel);
                addComponent(chooseContainer);

                setSourceFromRepo(true, this);
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                //Sstem.out.println("Disabling next button");
                if (!hasAnnotations) {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };
    }

    private static String keyForAnnotationInfo(AnnotationDownloadInformation i) {
        return keyForAnnotationInfo(i.getReference(), i.getProgramName(), i.getProgramVersion());
    }

    private static String keyForAnnotationInfo(String ref, String prog, String ver) {
        return ref + "_" + prog + "_" + ver;
    }

    private JPanel populateRepositoryPanel(DefaultWizardPage page) {
        JPanel p = ViewUtil.getClearPanel();
        p.setLayout(new BorderLayout());

        try {
            List<AnnotationDownloadInformation> annotationsAvailable = AnnotationDownloadInformation.getDownloadableAnnotations(
                    VersionSettings.getVersionString()
            );

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

            LOG.info(annotationsAvailable.size() + " annotations are available");

            StripyTable stp = new StripyTable(data, new String[]{"Reference", "Annotation", "Version", "Description"});
            stp.setBorder(null);
            stp.setShowGrid(false);
            stp.setRowHeight(21);

            SelectionListener listener = new SelectionListener(stp);
            stp.getSelectionModel().addListSelectionListener(listener);

            p.add(stp.getTableHeader(), BorderLayout.NORTH);
            p.add(ViewUtil.getClearBorderedScrollPane(stp), BorderLayout.CENTER);

            stp.addRowSelectionInterval(0, 0);

            if (!annotationsAvailable.isEmpty()) {
                page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                hasAnnotations = true;
            }

        } catch (Exception ex) {
            p.add(ViewUtil.getErrorLabel("Problem fetching available annotations"));
            LOG.error(ex);
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
            private ProgressWheel progressWheel;
            private JButton startButton;

            {
                progressLabel = new JLabel("You are now ready to install this annotation.");
                addComponent(progressLabel);
                //addText("Installing annotation...");

                progressWheel = new ProgressWheel();
                progressWheel.setVisible(false);
                addComponent(progressWheel);

                startButton = new JButton("Install Annotation");
                startButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startButton.setEnabled(false);
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        progressWheel.setIndeterminate(true);
                        progressWheel.setVisible(true);
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
                                progressWheel.setVisible(false);
                                progressLabel.setText("Done");

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

    private boolean create() throws SQLException, IOException, InterruptedException, ExecutionException {
        try {
            if (fromRepository) {
                progressLabel.setText("Installing annotation...");
                return MedSavantClient.AnnotationManagerAdapter.installAnnotationForProject(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), this.annotationToInstall);
                
            } else {
                progressLabel.setText("Uploading file to server...");
                File annotationFile = new File(fileChoosePanel.getPath());
                int transferID = ClientNetworkUtils.copyFileToServer(annotationFile);
                progressLabel.setText("Installing annotation...");
                return MedSavantClient.AnnotationManagerAdapter.installAnnotationForProject(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), transferID);                
            }
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return false; // will not matter
        }
    }
}
