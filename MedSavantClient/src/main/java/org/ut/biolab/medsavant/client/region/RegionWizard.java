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
package org.ut.biolab.medsavant.client.region;

import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.CompletionWizardPage;
import com.jidesoft.wizard.DefaultWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.jidesoft.wizard.WizardStyle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.type.CombiningMethod;

import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.shared.importing.BEDFormat;
import org.ut.biolab.medsavant.shared.importing.FileFormat;
import org.ut.biolab.medsavant.client.importing.ImportFilePanel;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.GeneFetcher;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.GeneSelectionPanel;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.DownloadTask;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GeneSetFetcher;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GenemaniaInfoRetriever;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GenemaniaInfoRetriever.NoRelatedGenesInfoException;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.Gene;

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
    private static final int DEFAULT_RELATED_GENES_LIMIT = 50;
    private final CombiningMethod[] GENEMANIA_COMBINING_METHODS = {CombiningMethod.AVERAGE, CombiningMethod.BP, CombiningMethod.MF, CombiningMethod.CC, CombiningMethod.AUTOMATIC};
    private String listName;
    private String path;
    private char delim;
    private FileFormat fileFormat;
    private int numHeaderLines;
    private final boolean importing;
    private GeneSet standardGenes;
    private final RegionController controller;
    private GeneSelectionPanel sourceGenesPanel;
    private JButton runGeneManiaButton;
    private GeneSelectionPanel selectedGenesPanel;

    private class GeneManiaDownloadCompleteListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("downloadState")) {
                DownloadTask.DownloadState ds = (DownloadTask.DownloadState) evt.getNewValue();
                if (ds == DownloadTask.DownloadState.CANCELLED
                        || ds == DownloadTask.DownloadState.FINISHED) {

                    if (ds == DownloadTask.DownloadState.FINISHED) {
                        runGeneManiaButton.setText("Run GeneMANIA");
                    } else {
                        runGeneManiaButton.setText("Download GeneMANIA");
                    }
                    if (runGeneManiaButton != null) {
                        runGeneManiaButton.setEnabled(
                                (selectedGenesPanel.getNumSelected() + sourceGenesPanel.getNumSelected()) > 0);
                    }
                }
            }
        }
    }
    private static GeneManiaDownloadCompleteListener geneManiaDownloadCompleteListener;

    private void registerDownloadListener() {
        try {
            if (geneManiaDownloadCompleteListener == null) {
                geneManiaDownloadCompleteListener = new GeneManiaDownloadCompleteListener();
                DownloadTask dt = GenemaniaInfoRetriever.getGeneManiaDownloadTask();
                dt.addPropertyChangeListener(geneManiaDownloadCompleteListener);
            }
        } catch (IOException e) {
            DialogUtils.displayMessage("Error downloading GeneMANIA files");
            LOG.error(e);
        }
    }

    public RegionWizard(boolean doImportProcess) throws SQLException, RemoteException {
        super(MedSavantFrame.getInstance(), "Region List Wizard", true);
        this.importing = doImportProcess;
        controller = RegionController.getInstance();
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        //add pages
        PageList model = new PageList();
        if (doImportProcess) {
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
            //model.append(getRecommendPage());
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
                    //setCurrentPage(PAGENAME_RECOMMEND);
                    setCurrentPage(PAGENAME_CREATE);
                    //} else if (pageName.equals(PAGENAME_RECOMMEND)) {
                    //  setCurrentPage(PAGENAME_CREATE);
                } else if (pageName.equals(PAGENAME_CREATE)) {
                    setCurrentPage(PAGENAME_COMPLETE);
                }
            }
        });

        pack();
        // Changed resizableness: No good reason to restrict window size.
        setResizable(true);
        setLocationRelativeTo(MedSavantFrame.getInstance());
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension parentDimension = MedSavantFrame.getInstance().getSize();

        int xdim = Math.min(1520, Math.max(960, parentDimension.width - 100));
        int ydim = Math.min(950, Math.max(600, parentDimension.height - 100));

        return new Dimension(xdim, ydim);
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
                ImportFilePanel importPanel = new ImportFilePanel(1) {
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
            private static final int GENE_SELECTION_PANE_WIDTH = 350;
            private JPanel leftSide;
            private GeneSelectionPanel geneManiaResultsPanel;
            private Set<String> geneManiaGeneNames = null;

            {
                selectedGenesPanel = new GeneSelectionPanel(true, true);
                sourceGenesPanel = new GeneSelectionPanel(true, true);
                geneManiaResultsPanel = new GeneSelectionPanel(true, true) {
                    @Override
                    protected void dragAndDropAddGenes(Set<Gene> geneSet) {
                        Set<Object> genesToMoveToGeneManiaPanel = new HashSet<Object>(geneManiaGeneNames);
                        genesToMoveToGeneManiaPanel.retainAll(selectedGenesPanel.getSelectedKeys());
                        selectedGenesPanel.copyItems(geneManiaResultsPanel, genesToMoveToGeneManiaPanel);
                        selectedGenesPanel.moveSelectedItems(sourceGenesPanel);
                    }

                    @Override
                    protected void dragAndDropRemoveKeys(Set<Object> keySet) {
                        Set<Object> keys = geneManiaResultsPanel.getSelectedKeys();
                        geneManiaResultsPanel.removeRows(keys);
                        sourceGenesPanel.removeRows(keys);
                    }
                };
                geneManiaResultsPanel.setOddRowColor(new Color(242, 249, 245));


                runGeneManiaButton = new JButton("Run GeneMANIA");
                runGeneManiaButton.setEnabled(!DirectorySettings.isGeneManiaInstalled());

                ListSelectionListener selectionListener = new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent lse) {
                        int numSel = sourceGenesPanel.getNumSelected() + selectedGenesPanel.getNumSelected();
                        if (geneManiaGeneNames != null) {
                            numSel += geneManiaResultsPanel.getNumSelected();
                        }
                        if (GenemaniaInfoRetriever.isGeneManiaDownloading()) {
                            runGeneManiaButton.setEnabled(false);
                        } else {
                            runGeneManiaButton.setEnabled(numSel > 0 || !DirectorySettings.isGeneManiaInstalled());
                        }
                    }
                };

                sourceGenesPanel.getTable().getSelectionModel().addListSelectionListener(selectionListener);
                selectedGenesPanel.getTable().getSelectionModel().addListSelectionListener(selectionListener);
                selectedGenesPanel.getTable().getModel().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent tme) {
                        if (selectedGenesPanel.getData().length > 0) {
                            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                        } else {
                            fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                        }
                    }
                });

                selectedGenesPanel.setPreferredSize(new Dimension(GENE_SELECTION_PANE_WIDTH, selectedGenesPanel.getPreferredSize().height));


                final JPanel outerLeftSide = new JPanel();
                outerLeftSide.setLayout(new BoxLayout(outerLeftSide, BoxLayout.X_AXIS));

                leftSide = new JPanel();
                leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
                leftSide.add(sourceGenesPanel);
                outerLeftSide.add(leftSide);
                final JPanel bg = new JPanel();
                bg.setLayout(new BoxLayout(bg, BoxLayout.Y_AXIS));

                JButton addButton = new JButton("Add →");
                JButton removeButton = new JButton("← Remove");

                sourceGenesPanel.getTable().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        if (me.getClickCount() == 2) {
                            sourceGenesPanel.moveSelectedItems(selectedGenesPanel);
                        }
                    }
                });

                selectedGenesPanel.getTable().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        if (me.getClickCount() == 2) {
                            if (geneManiaGeneNames != null) {
                                Set<Object> genesToMoveToGeneManiaPanel = new HashSet<Object>(geneManiaGeneNames);
                                genesToMoveToGeneManiaPanel.retainAll(selectedGenesPanel.getSelectedKeys());
                                selectedGenesPanel.copyItems(geneManiaResultsPanel, genesToMoveToGeneManiaPanel);
                            }
                            selectedGenesPanel.moveSelectedItems(sourceGenesPanel);
                        }
                    }
                });

                geneManiaResultsPanel.getTable().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        if (me.getClickCount() == 2) {
                            Set<Object> keys = geneManiaResultsPanel.getSelectedKeys();
                            geneManiaResultsPanel.moveSelectedItems(selectedGenesPanel);
                            sourceGenesPanel.moveItems(selectedGenesPanel, keys);
                        }
                    }
                });

                addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (geneManiaGeneNames != null) {
                            Set<Object> keys = geneManiaResultsPanel.getSelectedKeys();
                            geneManiaResultsPanel.moveSelectedItems(selectedGenesPanel);
                            sourceGenesPanel.moveItems(selectedGenesPanel, keys);
                        } else {
                            sourceGenesPanel.moveSelectedItems(selectedGenesPanel);
                        }
                    }
                });

                removeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (geneManiaGeneNames != null) {
                            Set<Object> genesToMoveToGeneManiaPanel = new HashSet<Object>(geneManiaGeneNames);
                            genesToMoveToGeneManiaPanel.retainAll(selectedGenesPanel.getSelectedKeys());
                            selectedGenesPanel.copyItems(geneManiaResultsPanel, genesToMoveToGeneManiaPanel);
                        }
                        selectedGenesPanel.moveSelectedItems(sourceGenesPanel);
                    }
                });

                bg.add(Box.createVerticalGlue());
                bg.add(addButton);
                bg.add(removeButton);
                bg.add(Box.createVerticalGlue());
                outerLeftSide.add(bg);

                JPanel rightSide = new JPanel();
                rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
                rightSide.add(selectedGenesPanel);
                rightSide.add(runGeneManiaButton);

                final JSplitPane hsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, outerLeftSide, rightSide);
                hsplitPane.setResizeWeight(1);
                addComponent(hsplitPane, true);

                if (!DirectorySettings.isGeneManiaInstalled()) {
                    runGeneManiaButton.setText("Download GeneMANIA");
                    if (GenemaniaInfoRetriever.isGeneManiaDownloading()) {
                        runGeneManiaButton.setEnabled(false);
                        registerDownloadListener();
                    }
                }

                runGeneManiaButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {

                        if (!DirectorySettings.isGeneManiaInstalled()) {
                            int response = DialogUtils.askYesNo("Download GeneMANIA?",
                                    "GeneMANIA is not yet installed.  Do you want to download and install it now?");
                            try {
                                if (response == DialogUtils.OK) {
                                    runGeneManiaButton.setText("Run GeneMANIA");
                                    runGeneManiaButton.setEnabled(false);
                                    registerDownloadListener();

                                    /*
                                     DownloadTask dt = GenemaniaInfoRetriever.getGeneManiaDownloadTask();
                                     dt.addPropertyChangeListener(new PropertyChangeListener() {
                                     @Override
                                     public void propertyChange(PropertyChangeEvent evt) {
                                     if (evt.getPropertyName().equals("downloadState")) {
                                     DownloadTask.DownloadState ds = (DownloadTask.DownloadState) evt.getNewValue();
                                     if (ds == DownloadTask.DownloadState.CANCELLED
                                     || ds == DownloadTask.DownloadState.FINISHED) {

                                     runGeneManiaButton.setEnabled(
                                     (selectedGenesPanel.getNumSelected() + sourceGenesPanel.getNumSelected()) > 0);
                                     }
                                     }
                                     }
                                     });
                                     */
                                    GenemaniaInfoRetriever.getGeneManiaDownloadTask().execute();
                                }
                            } catch (IOException e) {
                                DialogUtils.displayMessage("Error downloading GeneMANIA files");
                                LOG.error(e);
                            }
                        } else {

                            final List<String> selectedGenes = new LinkedList<String>();
                            for (Gene g : selectedGenesPanel.getSelectedGenes()) {
                                selectedGenes.add(g.getName());
                            }
                            for (Gene g : sourceGenesPanel.getSelectedGenes()) {
                                selectedGenes.add(g.getName());
                            }
                            if (geneManiaGeneNames != null) {
                                for (Gene g : geneManiaResultsPanel.getSelectedGenes()) {
                                    selectedGenes.add(g.getName());
                                }
                            }
                            final JButton closeGeneManiaButton = new JButton("← Close GeneMANIA results");
                            closeGeneManiaButton.setEnabled(false);
                            final JPanel geneManiaContainingPanel = new JPanel();
                            geneManiaContainingPanel.setLayout(new BoxLayout(geneManiaContainingPanel, BoxLayout.Y_AXIS));

                            final SwingWorker geneManiaWorker = new SwingWorker() {
                                private List<Object[]> results;

                                @Override
                                public void done() {
                                    Object[][] newdata = new Object[results.size()][4];
                                    results.toArray(newdata);
                                    geneManiaResultsPanel.updateData(newdata);
                                    geneManiaResultsPanel.updateView();
                                    geneManiaContainingPanel.removeAll();
                                    geneManiaContainingPanel.add(geneManiaResultsPanel);
                                    geneManiaContainingPanel.revalidate();
                                    geneManiaContainingPanel.repaint();
                                    closeGeneManiaButton.setEnabled(true);
                                }

                                @Override
                                public Object doInBackground() {
                                    try {
                                        GenemaniaInfoRetriever genemania = new GenemaniaInfoRetriever();
                                        genemania.setGenes(selectedGenes);
                                        List<String> geneNameList = genemania.getRelatedGeneNamesByScore();
                                        geneManiaGeneNames = new HashSet<String>();
                                        geneManiaGeneNames.addAll(geneNameList);
                                        LOG.debug("Found " + geneNameList.size() + " related genes");

                                        results = new ArrayList<Object[]>(geneNameList.size());

                                        int i = 0;
                                        for (String gene : geneNameList) {
                                            if (isCancelled()) {
                                                return null;
                                            }
                                            Gene g = GeneSetFetcher.getInstance().getGeneDictionary().get(gene);
                                            if (g == null) {
                                                LOG.warn("No gene found for " + gene);
                                            } else if (!selectedGenesPanel.hasKey(g.getName())) {
                                                results.add(new Object[]{g.getName(), g.getChrom(), g.getStart(), g.getEnd()});
                                            }
                                        }
                                    } catch (IOException e) {
                                        LOG.error(e);
                                    } catch (ApplicationException e) {
                                        LOG.error(e);
                                    } catch (DataStoreException e) {
                                        LOG.error(e);
                                    } catch (NoRelatedGenesInfoException e) {
                                        LOG.error(e);
                                    }
                                    return null;
                                }
                            };

                            leftSide.removeAll();

                            closeGeneManiaButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    try {
                                        geneManiaWorker.cancel(true);
                                    } catch (Exception e) {
                                        //genemania throws exceptions when cancelled
                                    }
                                    leftSide.removeAll();
                                    leftSide.add(sourceGenesPanel);
                                    leftSide.validate();
                                    leftSide.repaint();
                                    geneManiaGeneNames = null;
                                }
                            });

                            JPanel closeButtonPanel = new JPanel();
                            closeButtonPanel.setLayout(new BoxLayout(closeButtonPanel, BoxLayout.X_AXIS));
                            closeButtonPanel.add(closeGeneManiaButton);
                            closeButtonPanel.add(Box.createHorizontalGlue());

                            leftSide.add(closeButtonPanel);

                            geneManiaContainingPanel.add(new WaitPanel("Querying GeneMANIA for related genes"));

                            leftSide.add(geneManiaContainingPanel);
                            leftSide.validate();
                            leftSide.repaint();
                            geneManiaWorker.execute();


                        }//end else
                    }//end actionPerformed
                });//end ActionListener
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);

                if (selectedGenesPanel.getNumSelected() > 0) {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
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
                                ((CompletionWizardPage) getPageByTitle(PAGENAME_COMPLETE)).addText("List " + listName + " has been successfully created.");
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
            for (RegionSet r : controller.getRegionSets()) {
                if (r.getName().equals(listName)) {
                    DialogUtils.displayError("Error", "List name already in use.");
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching region list: %s", ex);
            return false;
        }
    }

    private void createList() throws SQLException, IOException, InterruptedException, ExecutionException {
        if (!importing) {
            File tempFile = File.createTempFile("genes", ".bed");
            FileWriter output = new FileWriter(tempFile);

            for (Object[] rowData : selectedGenesPanel.getData()) {
                output.write(rowData[1] + "\t" + rowData[2] + "\t" + rowData[3] + "\t" + rowData[0] + "\n");
            }

            output.close();
            delim = '\t';
            numHeaderLines = 0;
            fileFormat = new BEDFormat();
            path = tempFile.getAbsolutePath();
        }

        int transferID = ClientNetworkUtils.copyFileToServer(new File(path));

        controller.addRegionSet(listName, delim, fileFormat, numHeaderLines, transferID);
    }

    private void fetchGenes() {
        new GeneFetcher(standardGenes, "RegionWizard") {
            @Override
            public void setData(Object[][] data) {
                sourceGenesPanel.updateData(data);
                sourceGenesPanel.updateView();
            }

            /**
             * Don't have progress bar handy, so we don't do anything to show
             * progress.
             */
            @Override
            public void showProgress(double prog) {
            }
        }.execute();
    }
}
