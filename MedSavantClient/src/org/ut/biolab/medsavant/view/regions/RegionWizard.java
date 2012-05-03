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
package org.ut.biolab.medsavant.view.regions;

import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.*;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.importing.BEDFormat;
import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.importing.ImportFilePanel;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class RegionWizard extends WizardDialog {
    private static final Log LOG = LogFactory.getLog(RegionWizard.class);
    private static final String PAGENAME_NAME = "List Name";
    private static final String PAGENAME_CHOOSE = "Choose File";
    private static final String PAGENAME_SELECT = "Select Genes";
    private static final String PAGENAME_CREATE = "Create";
    private static final String PAGENAME_COMPLETE = "Complete";
    
    private String listName;
    private String path;
    private char delim;
    private FileFormat fileFormat;
    private int numHeaderLines;
    private final boolean importing;
    
    public RegionWizard(boolean imp) {
        super(MedSavantFrame.getInstance(), "Region List Wizard", true);
        this.importing = imp;
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);
        
        //add pages
        PageList model = new PageList();
        if (imp) {
            model.append(getNamePage());
            model.append(getFilePage());
            model.append(getCreationPage());
            model.append(getCompletionPage());
        } else {
            model.append(getNamePage());
            model.append(getSelectionPage());
            model.append(getCreationPage());
            model.append(getCompletionPage());
        }
        setPageList(model);
        
        //change next action
        setNextAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pageName = getCurrentPage().getTitle();
                if (pageName.equals(PAGENAME_NAME) && validateListName()) {
                    if (importing) {
                        setCurrentPage(PAGENAME_CHOOSE);
                    } else {
                        setCurrentPage(PAGENAME_SELECT);
                    }
                } else if (pageName.equals(PAGENAME_CHOOSE) || pageName.equals(PAGENAME_SELECT)) {
                    setCurrentPage(PAGENAME_CREATE);
                } else if (pageName.equals(PAGENAME_CREATE)) {
                    setCurrentPage(PAGENAME_COMPLETE);
                }
            }
        });
        
        pack();
        setResizable(false);
        setLocationRelativeTo(MedSavantFrame.getInstance());
    }
    
    private AbstractWizardPage getNamePage() {
        return new DefaultWizardPage(PAGENAME_NAME) {

            {
                addText("Choose a name for the region list.\nThe name cannot already be in use. ");
                addComponent(new JTextField() {
                    {
                        addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyReleased(KeyEvent e) {
                                if (getText() != null && !getText().equals("")) {
                                    listName = getText();
                                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                                } else {
                                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                if (listName == null || listName.equals("")) {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };
    }
    
    private AbstractWizardPage getFilePage() {

        //setup page
        return new DefaultWizardPage(PAGENAME_CHOOSE) {
            {
                ImportFilePanel importPanel = new ImportFilePanel() {          
                    @Override
                    public void setReady(boolean ready) {
                        if (ready) {
                            path = getPath();
                            delim = getDelimiter();
                            fileFormat = getFileFormat();
                            numHeaderLines = getNumHeaderLines();
                            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        } else {
                            fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                        }
                    }          
                };
                importPanel.addFileFormat(new BEDFormat());
                addComponent(importPanel);
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                if (path == null || path.equals("")) {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        };
    }
    
    private AbstractWizardPage getSelectionPage() {
        return null;
    }


    private AbstractWizardPage getCreationPage() {

        //setup page
        return new DefaultWizardPage(PAGENAME_CREATE) {
            private JProgressBar progressBar;
            private JButton startButton;

            {
                addText("You are now ready to create this region list.");

                progressBar = new JProgressBar();

                addComponent(progressBar);

                startButton = new JButton("Create List");
                startButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startButton.setEnabled(false);
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        progressBar.setIndeterminate(true);
                        new MedSavantWorker<Void>("RegionPage") {
                            @Override
                            public Void doInBackground() throws Exception {
                                createList();
                                return null;
                            }

                            @Override
                            protected void showProgress(double fraction) {
                            }

                            @Override
                            protected void showSuccess(Void result) {
                                ((CompletionWizardPage)getPageByTitle(PAGENAME_COMPLETE)).addText("List " + listName + " has been successfully created.");
                                setCurrentPage(PAGENAME_COMPLETE);
                            }

                            @Override
                            protected void showFailure(Throwable t) {
                                RegionWizard.this.setVisible(false);
                                LOG.error("Error uploading list.", t);
                                DialogUtils.displayException("Error", "There was an error while trying to create your list. ", t);
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
        CompletionWizardPage page = new CompletionWizardPage(PAGENAME_COMPLETE) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
        return page;
    }
    
    private boolean validateListName() {
        try {
            boolean valid = !MedSavantClient.RegionQueryUtilAdapter.listNameExists(LoginController.sessionId, listName);
            if (!valid) {
                DialogUtils.displayError("Error", "List name already in use.");
            }
            return valid;
        } catch (Exception ex) {
            LOG.error("Error validating region list.", ex);
            DialogUtils.displayException("Error", "Error trying to create region list", ex);
            return false;
        }
    }
    
    private void createList() throws SQLException, NonFatalDatabaseException, IOException {
        RemoteInputStream stream = (new SimpleRemoteInputStream(new FileInputStream(path))).export();
        MedSavantClient.RegionQueryUtilAdapter.addRegionList(LoginController.sessionId, listName, ReferenceController.getInstance().getCurrentReferenceId(), stream, delim, fileFormat, numHeaderLines);
    }
       
}
