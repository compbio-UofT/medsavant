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
package org.ut.biolab.medsavant.client.ontology;

import java.awt.Frame;
import java.awt.event.*;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Ontology;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.serverapi.OntologyManagerAdapter;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.WebResources;

/**
 *
 * @author Andrew
 */
public class OntologyWizard extends WizardDialog {
    private static final Log LOG = LogFactory.getLog(OntologyWizard.class);
    private static final String PAGENAME_NAME = "Ontology Name";
    private static final String PAGENAME_SOURCE = "Choose Source";
    private static final String PAGENAME_CREATE = "Create";
    private static final String PAGENAME_COMPLETE = "Complete";

    private String name;
    private OntologyType type;

    private JTextField oboField = new JTextField();
    private JTextField mappingField = new JTextField();

    public OntologyWizard() throws SQLException, RemoteException {
        super((Frame)DialogUtils.getFrontWindow(), "Ontology Wizard", true);
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        model.append(getNamePage());
        model.append(getSourcePage());
        model.append(getCreationPage());
        model.append(getCompletionPage());
        setPageList(model);

        //change next action
        setNextAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pageName = getCurrentPage().getTitle();
                if (pageName.equals(PAGENAME_NAME) && validateName()) {
                    setCurrentPage(PAGENAME_SOURCE);
                } else if (pageName.equals(PAGENAME_SOURCE)) {
                    setCurrentPage(PAGENAME_CREATE);
                } else if (pageName.equals(PAGENAME_CREATE)) {
                    setCurrentPage(PAGENAME_COMPLETE);
                }
            }
        });

        pack();
        setResizable(true);
        setLocationRelativeTo(DialogUtils.getFrontWindow());
    }

    private AbstractWizardPage getNamePage() {
        return new DefaultWizardPage(PAGENAME_NAME) {
            private JTextField nameField = new JTextField();
            private JComboBox typeCombo = new JComboBox(OntologyListItem.DEFAULT_ITEMS);

            {
                addText("Choose a name for the ontology.\nThe name cannot already be in use. ");
                addComponent(nameField);
                nameField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (StringUtils.isNotEmpty(nameField.getText())) {
                            name = nameField.getText();
                            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        } else {
                            fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                        }
                    }
                });

                addText("Indicate the type of ontology data:");
                addComponent(typeCombo);
                typeCombo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        type = ((OntologyListItem)typeCombo.getSelectedItem()).getType();
                        switch (type) {
                            case GO:
                                oboField.setText(WebResources.GO_OBO_URL.toString());
                                mappingField.setText(WebResources.GO_TO_GENES_URL.toString());
                                break;
                            case HPO:
                                oboField.setText(WebResources.HPO_OBO_URL.toString());
                                mappingField.setText(WebResources.HPO_TO_GENES_URL.toString());
                                break;
                            case OMIM:
                                oboField.setText(WebResources.OMIM_OBO_URL.toString());
                                mappingField.setText(WebResources.OMIM_TO_HPO_URL.toString());
                                break;
                        }
                    }
                });
                typeCombo.setSelectedIndex(0);
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                if (name == null || name.equals("")) {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };
    }

    private AbstractWizardPage getSourcePage() {

        return new DefaultWizardPage(PAGENAME_SOURCE) {
            {
                KeyListener listener = new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (StringUtils.isNotEmpty(oboField.getText()) && StringUtils.isNotEmpty(mappingField.getText())) {
                            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        } else {
                            fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                        }
                    }
                };

                addText("URL for the OBO file containing the ontology data:");
                addComponent(oboField);
                oboField.addKeyListener(listener);

                addText("URL for the file definining the mapping between genes\nand ontology terms:");
                addComponent(mappingField);
                mappingField.addKeyListener(listener);
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

    private AbstractWizardPage getCreationPage() {

        //setup page
        return new DefaultWizardPage(PAGENAME_CREATE) {
            private JProgressBar progressBar;
            private JButton startButton;

            {
                addText("You are now ready to create this ontology.");

                progressBar = new JProgressBar();

                addComponent(progressBar);

                startButton = new JButton("Create Ontology");
                startButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startButton.setEnabled(false);
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        progressBar.setIndeterminate(true);
                        new MedSavantWorker<Void>("Ontologies") {
                            @Override
                            public Void doInBackground() throws Exception {
                                create();
                                return null;
                            }

                            @Override
                            protected void showProgress(double fraction) {
                            }

                            @Override
                            protected void showSuccess(Void result) {
                                ((CompletionWizardPage)getPageByTitle(PAGENAME_COMPLETE)).addText("Ontology " + name + " has been successfully created.");
                                setCurrentPage(PAGENAME_COMPLETE);
                            }

                            @Override
                            protected void showFailure(Throwable t) {
                                OntologyWizard.this.setVisible(false);
                                LOG.error("Error creating ontology.", t);
                                DialogUtils.displayException("Error", "There was an error while trying to create your ontology. ", t);
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

    private boolean validateName() {
        try {
            for (Ontology ont: MedSavantClient.OntologyManager.getOntologies(LoginController.getSessionID())) {
                if (ont.getName().equals(name)) {
                    DialogUtils.displayError("Error", "Ontology name already in use.");
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching ontology list: %s", ex);
            return false;
        }
    }

    private void create() throws InterruptedException, SQLException, IOException {
        try {
            MedSavantClient.OntologyManager.addOntology(LoginController.getSessionID(), name, type, new URL(oboField.getText()), new URL(mappingField.getText()));
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
        }
    }
}
