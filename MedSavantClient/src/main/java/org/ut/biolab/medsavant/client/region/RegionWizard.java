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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genemania.type.CombiningMethod;

import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.shared.importing.BEDFormat;
import org.ut.biolab.medsavant.shared.importing.FileFormat;
import org.ut.biolab.medsavant.client.importing.ImportFilePanel;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.GeneFetcher;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.ListViewTablePanel;
import org.ut.biolab.medsavant.client.view.component.PartSelectorPanel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GeneSetFetcher;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GenemaniaInfoRetriever;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class RegionWizard extends WizardDialog {

    private static final Log LOG = LogFactory.getLog(RegionWizard.class);
    private static final String PAGENAME_NAME = "List Name";
    private static final String PAGENAME_FILE = "Choose File";
    private static final String PAGENAME_GENES = "Select Genes";
    private static final String PAGENAME_RECOMMEND = "Recommend Related Genes";
    private static final String PAGENAME_CREATE = "Create";
    private static final String PAGENAME_COMPLETE = "Complete";
    private static final String[] COLUMN_NAMES = new String[]{"Name", "Chromosome", "Start", "End"};
    private static final Class[] COLUMN_CLASSES = new Class[]{String.class, String.class, Integer.class, Integer.class};
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
    private ListViewTablePanel sourceGenesPanel;
    private ListViewTablePanel selectedGenesPanel;
    private ListViewTablePanel selectedGenesPanelForGeneMania;
    private ListViewTablePanel finalSelectedGenes;
    private GenemaniaInfoRetriever genemania;
    private boolean rankByVarFreq;

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
            model.append(getRecommendPage());
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
                    setCurrentPage(PAGENAME_RECOMMEND);
                } else if (pageName.equals(PAGENAME_RECOMMEND)) {
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

    private AbstractWizardPage getRecommendPage() {
        return new DefaultWizardPage(PAGENAME_RECOMMEND) {

            private JTabbedPane tabbedPane = new JTabbedPane();
            private JPanel card1 = new JPanel(new CardLayout());
            private JPanel card1a = new JPanel();
            private JPanel card1b;
            private JButton genemaniaButton;
            private JProgressBar progressBar;
            private JButton settingsButton;
            private JLabel progressMessage = new JLabel();
            private ListViewTablePanel recommendedGenes;
            private JPanel card2 = new JPanel();

            private Runnable r = new Runnable() {

                @Override
                public void run() {
                    boolean setMsgOff = true;
                    try {

                        genemania = new GenemaniaInfoRetriever();
                        recommendedGenes = new ListViewTablePanel(new Object[0][0], COLUMN_NAMES, COLUMN_CLASSES, new int[0]);
                        recommendedGenes.setFontSize(10);
                        java.util.List<String> geneNames = new ArrayList();
                        int[] selectedRows = selectedGenesPanelForGeneMania.getSelectedRows();
                        if (selectedRows.length == 0) {
                            //getRowData doesn't work for the cloned table so use the original
                            for (int i = 0; i < selectedGenesPanel.getTable().getRowCount(); i++){
                                Object[] rowData = selectedGenesPanel.getRowData(i);
                                geneNames.add((String) rowData[0]);
                            }
                        }
                        for (int row : selectedRows) {
                            //getRowData doesn't work for the cloned table so use the original
                            Object[] rowData = selectedGenesPanel.getRowData(row);
                            geneNames.add((String) rowData[0]);
                        }
                        java.util.List<String> notInGenemania = new ArrayList<String>(geneNames);
                        notInGenemania.removeAll(GenemaniaInfoRetriever.getValidGenes(geneNames));
                        geneNames = GenemaniaInfoRetriever.getValidGenes(geneNames);
                        genemania.setGenes(geneNames);
                        if (notInGenemania.size() > 0) {
                            String message = "<html><center>Following gene(s) not found in GeneMANIA: ";
                            for (String invalidGene : notInGenemania) {
                                message += "<br>" + invalidGene;
                            }
                            message += "</center></html>";
                            progressMessage.setText(message);
                            setMsgOff = false;
                        }
                        GeneSetFetcher geneSetFetcher = GeneSetFetcher.getInstance();
                        if (genemania.getGenes().size() > 0) {
                            if (rankByVarFreq) {
                                Iterator<org.ut.biolab.medsavant.shared.model.Gene> itr = geneSetFetcher.getGenesByNumVariants(genemania.getRelatedGeneNamesByScore()).iterator();
                                org.ut.biolab.medsavant.shared.model.Gene currGene;
                                itr.next();//skip the first one (it's the name of selected gene already displayed)

                                if (Thread.interrupted()) {
                                    throw new InterruptedException();
                                }

                                int i = 1;
                                while (itr.hasNext()) {
                                    currGene = itr.next();
                                    final org.ut.biolab.medsavant.shared.model.Gene finalGene = currGene;
                                    recommendedGenes.addRow(new Object[]{currGene.getName(), finalGene.getChrom(), finalGene.getStart(), finalGene.getEnd()});
                                    i++;
                                }

                            } else {
                                Iterator<String> itr = genemania.getRelatedGeneNamesByScore().iterator();
                                String currGene;
                                itr.next();//skip the first one (it's the name of selected gene already displayed)
                                int i = 1;
                                while (itr.hasNext()) {
                                    currGene = itr.next();
                                    final org.ut.biolab.medsavant.shared.model.Gene finalGene = GeneSetFetcher.getInstance().getGene(currGene);
                                    recommendedGenes.addRow(new Object[]{finalGene.getName(), finalGene.getChrom(), finalGene.getStart(), finalGene.getEnd()});
                                    i++;
                                }

                            }
                        }


                        PartSelectorPanel selector = new PartSelectorPanel(recommendedGenes, finalSelectedGenes);
                        selector.setBackground(Color.WHITE);
                        card2.removeAll();
                        card2.add(selector);
                        card2.invalidate();
                        card2.updateUI();

                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Error retrieving data from GeneMANIA: %s", ex);
                    } finally{
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);
                        genemaniaButton.setEnabled(true);
                        settingsButton.setEnabled(true);
                        fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
                        tabbedPane.setEnabledAt(1, true);
                        tabbedPane.setSelectedIndex(1);
                        if (finalSelectedGenes.getTable().getRowCount() == 0) {
                              for (int i = 0; i < selectedGenesPanel.getTable().getRowCount(); i++){
                                finalSelectedGenes.addRow(selectedGenesPanel.getRowData(i));
                              }
                        }
                        if (setMsgOff) {
                            progressMessage.setVisible(false);
                        }
                    }
                }
            };
            {

                card1b = setUpSettingsPanel(card1);
                card1.add(card1a, "Query");
                card1.add(card1b, "Settings");
                tabbedPane.addTab("Query genes", card1);
                tabbedPane.addTab("Select genes", card2);

                tabbedPane.setPreferredSize(new Dimension(400,460));
                tabbedPane.setMinimumSize(new Dimension(400,460));
                tabbedPane.setMaximumSize(new Dimension(400,460));

                tabbedPane.setEnabledAt(1, false);
                card1a.setLayout(new BoxLayout(card1a, BoxLayout.PAGE_AXIS));
                card1a.add(new JLabel("Query GeneMANIA for Related Genes"), Component.LEFT_ALIGNMENT);
                selectedGenesPanelForGeneMania = new ListViewTablePanel(new Object[0][0], COLUMN_NAMES, COLUMN_CLASSES, new int[0]);
                selectedGenesPanelForGeneMania.getTable().setModel(selectedGenesPanel.getTable().getModel());
                selectedGenesPanelForGeneMania.setFontSize(10);
                card1a.add(selectedGenesPanelForGeneMania);
                progressMessage.setVisible(false);
                card1a.add(progressMessage);
                progressBar = new JProgressBar();
                progressBar.setVisible(false);
                card1a.add(progressBar);

                genemaniaButton = new JButton("Recommend");
                settingsButton = new JButton("Settings");
                genemaniaButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        genemaniaButton.setEnabled(false);
                        settingsButton.setEnabled(false);
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(true);
                        progressMessage.setText("Querying GeneMANIA for related genes");
                        progressMessage.setVisible(true);
                        new Thread(r).start();
                    }
                });
                settingsButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ((CardLayout) card1.getLayout()).show(card1, "Settings");
                    }
                });
                JPanel buttonPanel = new JPanel();

                buttonPanel.add(genemaniaButton);

                buttonPanel.add(ViewUtil.alignRight(settingsButton));
                card1a.add(buttonPanel, Component.RIGHT_ALIGNMENT);
                card2.setLayout(new BoxLayout(card2, BoxLayout.PAGE_AXIS));
                card2.add(new JLabel("Select genes from recommended genes list"));
                finalSelectedGenes = new ListViewTablePanel(new Object[0][0], COLUMN_NAMES, COLUMN_CLASSES, new int[0]);
                finalSelectedGenes.setFontSize(10);
                addComponent(ViewUtil.getClearBorderedScrollPane(tabbedPane));
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private JPanel setUpSettingsPanel(JPanel p1) {
        final JPanel parent = p1;
        final JPanel panel = new JPanel();

        JPanel equal;

        JPanel geneOntology;


        JSeparator jSeparator1;
        JSeparator jSeparator2;
        JSeparator jSeparator3;
        JLabel limitTo;

        JLabel networkWeighting;
        JLabel networks;

        JButton okButton;
        JPanel queryDependent;
        JLabel rankBy;
        JLabel relatedGenes;
        final JTextField relatedGenesLimit;
        ButtonGroup buttonGroup1;
        final ButtonGroup buttonGroup2;
        JRadioButton varFreq;
        JRadioButton genemaniaScore;
        final JCheckBox coexp;
        final JCheckBox coloc;
        final JCheckBox gi;
        final JCheckBox other;
        final JCheckBox pathCheck;
        final JCheckBox pi;
        final JCheckBox predict;
        final JCheckBox spd;
        JRadioButton automatic;
        JRadioButton average;
        JRadioButton bp;
        JRadioButton cc;
        JRadioButton mf;

        limitTo = new javax.swing.JLabel();
        relatedGenes = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        rankBy = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        networks = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        networkWeighting = new javax.swing.JLabel();
        equal = new javax.swing.JPanel();
        geneOntology = new javax.swing.JPanel();
        queryDependent = new javax.swing.JPanel();
        relatedGenesLimit = new JTextField();
        relatedGenesLimit.setText(Integer.toString(DEFAULT_RELATED_GENES_LIMIT));
        buttonGroup1 = new ButtonGroup();
        buttonGroup2 = new ButtonGroup();
        varFreq = new JRadioButton();
        genemaniaScore = new JRadioButton();
        coexp = new JCheckBox();
        spd = new JCheckBox();
        gi = new JCheckBox();
        coloc = new JCheckBox();
        pathCheck = new JCheckBox();
        predict = new JCheckBox();
        pi = new JCheckBox();
        other = new JCheckBox();
        automatic = new JRadioButton();
        bp = new JRadioButton();
        mf = new JRadioButton();
        average = new JRadioButton();
        cc = new JRadioButton();
        okButton = new JButton();

        limitTo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        limitTo.setText("Limit to");
        varFreq.setSelected(true);
        rankByVarFreq=true;
        coexp.setSelected(true);
        coloc.setSelected(true);
        spd.setSelected(true);
        gi.setSelected(true);
        pi.setSelected(true);
        pathCheck.setSelected(true);
        other.setSelected(true);
        predict.setSelected(true);
        average.setSelected(true);
        relatedGenesLimit.setColumns(3);
        relatedGenesLimit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genemania.setGeneLimit(Integer.parseInt(relatedGenesLimit.getText()));

            }
        });

        relatedGenes.setText("related genes.");

        rankBy.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rankBy.setText("Rank by");

        ActionListener scoringActionPerformed = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (evt.getActionCommand().equals("genemaniaScore")) {
                    rankByVarFreq = false;
                } else {
                    rankByVarFreq = true;
                }
            }
        };

        buttonGroup1.add(varFreq);
        varFreq.setText("Variation Frequency");
        varFreq.setActionCommand("varFreq");
        varFreq.addActionListener(scoringActionPerformed);

        buttonGroup1.add(genemaniaScore);
        genemaniaScore.setText("GeneMANIA Score");
        genemaniaScore.setActionCommand("genemaniaScore");
        genemaniaScore.addActionListener(scoringActionPerformed);

        networks.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        networks.setText("Networks");

        ActionListener networksActionPerformed = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Set<String> networksSelected = new HashSet<String>();
                JCheckBox[] networkButtons = {coexp, spd, gi, coloc, pathCheck, predict, pi, other};
                for (int i = 0; i < networkButtons.length; i++) {
                    if (networkButtons[i].isSelected()) {
                        networksSelected.add(networkButtons[i].getActionCommand());
                    }
                }
                genemania.setNetworks(networksSelected);
            }
        };

        coexp.setText("Co-expression");
        coexp.addActionListener(networksActionPerformed);

        spd.setText("Shared Protein Domains");
        spd.addActionListener(networksActionPerformed);

        gi.setText("Genetic interactions");
        gi.addActionListener(networksActionPerformed);

        coloc.setText("Co-localization");
        coloc.addActionListener(networksActionPerformed);

        pathCheck.setText("Pathway interactions");
        pathCheck.addActionListener(networksActionPerformed);

        predict.setText("Predicted");
        predict.addActionListener(networksActionPerformed);

        pi.setText("Physical interactions");
        pi.addActionListener(networksActionPerformed);

        other.setText("Other");
        other.addActionListener(networksActionPerformed);

        networkWeighting.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        networkWeighting.setText("Network weighting");

        equal.setBorder(javax.swing.BorderFactory.createTitledBorder("Equal weighting"));

        ActionListener combiningMethodActionPerformed = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int indexOfCombiningMethod = 0;
                Enumeration<AbstractButton> buttonEnum = buttonGroup2.getElements();
                while (buttonEnum.hasMoreElements()) {
                    if (buttonEnum.nextElement().isSelected()) {
                        break;
                    }
                    indexOfCombiningMethod++;
                }
                genemania.setCombiningMethod(GENEMANIA_COMBINING_METHODS[indexOfCombiningMethod]);
            }
        };

        buttonGroup2.add(average);
        average.setText("Equal by network");
        average.setActionCommand("average");
        average.addActionListener(combiningMethodActionPerformed);

        javax.swing.GroupLayout equalLayout = new javax.swing.GroupLayout(equal);
        equal.setLayout(equalLayout);
        equalLayout.setHorizontalGroup(
                equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(equalLayout.createSequentialGroup().addContainerGap().addComponent(average).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        equalLayout.setVerticalGroup(
                equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(equalLayout.createSequentialGroup().addComponent(average).addGap(0, 2, Short.MAX_VALUE)));
        geneOntology.setBorder(javax.swing.BorderFactory.createTitledBorder("Gene Ontology (GO)- based weighting"));

        buttonGroup2.add(bp);
        bp.setText("Biological process based");
        bp.setActionCommand("bp");
        bp.addActionListener(combiningMethodActionPerformed);

        buttonGroup2.add(mf);
        mf.setText("Molecular function based");
        mf.setActionCommand("mf");
        mf.addActionListener(combiningMethodActionPerformed);

        buttonGroup2.add(cc);
        cc.setText("Cellular component based");
        cc.setActionCommand("cc");
        cc.addActionListener(combiningMethodActionPerformed);

        javax.swing.GroupLayout geneOntologyLayout = new javax.swing.GroupLayout(geneOntology);
        geneOntology.setLayout(geneOntologyLayout);
        geneOntologyLayout.setHorizontalGroup(
                geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(geneOntologyLayout.createSequentialGroup().addContainerGap().addComponent(bp).addGap(32, 32, 32).addComponent(mf).addGap(18, 18, 18).addComponent(cc).addContainerGap(129, Short.MAX_VALUE)));
        geneOntologyLayout.setVerticalGroup(
                geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(geneOntologyLayout.createSequentialGroup().addGroup(geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(bp).addComponent(mf).addComponent(cc)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        queryDependent.setBorder(javax.swing.BorderFactory.createTitledBorder("Query-dependent weighting"));

        buttonGroup2.add(automatic);
        automatic.setText("Automatically selected weighting method");
        automatic.setActionCommand("automatic");
        automatic.addActionListener(combiningMethodActionPerformed);

        javax.swing.GroupLayout queryDependentLayout = new javax.swing.GroupLayout(queryDependent);
        queryDependent.setLayout(queryDependentLayout);
        queryDependentLayout.setHorizontalGroup(
                queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(queryDependentLayout.createSequentialGroup().addContainerGap().addComponent(automatic).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        queryDependentLayout.setVerticalGroup(
                queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(queryDependentLayout.createSequentialGroup().addComponent(automatic).addContainerGap(8, Short.MAX_VALUE)));

        okButton.setText("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ((CardLayout) parent.getLayout()).show(parent, "Query");
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(28, 28, 28).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(gi).addComponent(coexp)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(pathCheck).addComponent(coloc)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(spd).addComponent(pi)).addGap(10, 10, 10).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(other).addComponent(predict))).addGroup(layout.createSequentialGroup().addGap(20, 20, 20).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(networkWeighting).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(equal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(geneOntology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(queryDependent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(networks).addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(rankBy).addGap(44, 44, 44).addComponent(varFreq).addGap(18, 18, 18).addComponent(genemaniaScore)).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(limitTo).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(relatedGenesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(relatedGenes)))).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(limitTo).addComponent(relatedGenesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(relatedGenes)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(rankBy).addComponent(varFreq).addComponent(genemaniaScore)).addGap(26, 26, 26).addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(networks).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(coexp).addComponent(coloc).addComponent(spd).addComponent(predict)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(gi).addComponent(pathCheck).addComponent(pi).addComponent(other)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(networkWeighting).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(equal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(geneOntology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(queryDependent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(okButton).addContainerGap()));
        return panel;
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

    private void createList() throws SQLException, IOException, InterruptedException {
        if (!importing) {
            File tempFile = File.createTempFile("genes", ".bed");
            FileWriter output = new FileWriter(tempFile);
            ListViewTablePanel list = finalSelectedGenes.getTable().getRowCount() > 0 ? finalSelectedGenes : selectedGenesPanel;
            for (int i = 0; i < list.getTable().getRowCount(); i++) {
                Object[] rowData = list.getRowData(i);
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
