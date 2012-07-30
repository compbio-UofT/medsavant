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
package org.ut.biolab.medsavant.region;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.controller.GeneSetController;
import org.ut.biolab.medsavant.importing.BEDFormat;
import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.importing.ImportFilePanel;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.GeneFetcher;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.ListViewTablePanel;
import org.ut.biolab.medsavant.view.component.PartSelectorPanel;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class RegionWizard extends WizardDialog {
    private static final Log LOG = LogFactory.getLog(RegionWizard.class);
    private static final String PAGENAME_NAME = "List Name";
    private static final String PAGENAME_FILE = "Choose File";
    private static final String PAGENAME_GENES = "Select Genes";
    private static final String PAGENAME_CREATE = "Create";
    private static final String PAGENAME_COMPLETE = "Complete";
    private static final String[] COLUMN_NAMES = new String[] { "Name", "Chromosome", "Start", "End" };
    private static final Class[] COLUMN_CLASSES = new Class[] { String.class, String.class, Integer.class, Integer.class };

    private String listName;
    private String path;
    private char delim;
    private FileFormat fileFormat;
    private int numHeaderLines;
    private final boolean importing;
    private GeneSet standardGenes;
    private final RegionController controller;

    private ListViewTablePanel sourceGenesPanel;
    private ListViewTablePanel selectedGenesPanel;

    public RegionWizard(boolean imp) throws SQLException, RemoteException {
        super(MedSavantFrame.getInstance(), "Region List Wizard", true);
        this.importing = imp;
        controller = RegionController.getInstance();
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
            model.append(getGenesPage());
            standardGenes = GeneSetController.getInstance().getCurrentGeneSet();
            if (standardGenes == null) {
                // That's odd.  We have no standard genes for this genome.
                throw new IllegalArgumentException(String.format("No standard genes to choose from for %s.", ReferenceController.getInstance().getCurrentReferenceName()));
            }

            fetchGenes();
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
                        setCurrentPage(PAGENAME_FILE);
                    } else {
                        setCurrentPage(PAGENAME_GENES);
                    }
                } else if (pageName.equals(PAGENAME_FILE) || pageName.equals(PAGENAME_GENES)) {
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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(960, 600);
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
        return new DefaultWizardPage(PAGENAME_FILE) {
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

    private AbstractWizardPage getGenesPage() {
        return new DefaultWizardPage(PAGENAME_GENES) {
            {
                sourceGenesPanel = new ListViewTablePanel(new Object[0][0], COLUMN_NAMES, COLUMN_CLASSES, new int[0]);
                sourceGenesPanel.setFontSize(10);
                selectedGenesPanel = new ListViewTablePanel(new Object[0][0], COLUMN_NAMES, COLUMN_CLASSES, new int[0]);
                selectedGenesPanel.setFontSize(10);

                PartSelectorPanel selector = new PartSelectorPanel(sourceGenesPanel, selectedGenesPanel);
                selector.setBackground(Color.WHITE);
                addComponent(selector);
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
            }
        };
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
                        new MedSavantWorker<Void>("Region Lists") {
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
        return new CompletionWizardPage(PAGENAME_COMPLETE) {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private boolean validateListName() {
        try {
            boolean dup = ArrayUtils.contains(controller.getRegionSets(), listName);
            if (dup) {
                DialogUtils.displayError("Error", "List name already in use.");
            }
            return !dup;
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching region list: %s", ex);
            return false;
        }
    }

    private void createList() throws SQLException, IOException {
        if (!importing) {
            File tempFile = File.createTempFile("genes", ".bed");
            FileWriter output = new FileWriter(tempFile);
            for (int i = 0; i < selectedGenesPanel.getTable().getRowCount(); i++) {
                Object[] rowData = selectedGenesPanel.getRowData(i);
                output.write(rowData[1] + "\t" + rowData[2] + "\t" + rowData[3] + "\t" + rowData[0] + "\n");
            }
            output.close();
            delim = '\t';
            numHeaderLines = 0;
            fileFormat = new BEDFormat();
            path = tempFile.getAbsolutePath();
        }
        RemoteInputStream stream = new SimpleRemoteInputStream(new FileInputStream(path)).export();
        controller.addRegionSet(listName, stream, delim, fileFormat, numHeaderLines);
    }

    private void fetchGenes() {
        new GeneFetcher(standardGenes, "RegionWizard") {
            @Override
            public void setData(Object[][] data) {
                sourceGenesPanel.updateData(data);
                sourceGenesPanel.updateView();
            }

            /**
             * Don't have progress bar handy, so we don't do anything to show progress.
             */
            @Override
            public void showProgress(double prog) {
            }
        }.execute();
    }
}
