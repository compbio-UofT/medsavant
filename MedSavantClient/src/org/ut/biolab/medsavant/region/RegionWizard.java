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
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.nodes.Element;

import org.ut.biolab.medsavant.geneset.GeneSetController;
import org.ut.biolab.medsavant.importing.BEDFormat;
import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.importing.ImportFilePanel;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.GeneFetcher;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.ListViewTablePanel;
import org.ut.biolab.medsavant.view.component.PartSelectorPanel;
import org.ut.biolab.medsavant.view.genetics.inspector.GeneInspector;
import org.ut.biolab.medsavant.view.genetics.inspector.InspectorPanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.EntrezButton;
import org.ut.biolab.medsavant.view.genetics.variantinfo.GeneManiaInfoSubPanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.GeneSetFetcher;
import org.ut.biolab.medsavant.view.genetics.variantinfo.GenemaniaInfoRetriever;
import org.ut.biolab.medsavant.view.images.IconFactory;
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
    private static final String PAGENAME_RECOMMEND = "Recommend Related Genes";
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
    private JTextField relatedGenesLimit;
    private GenemaniaInfoRetriever genemania;
    
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
            model.append(getRecommendPage());
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
                } else if (pageName.equals(PAGENAME_RECOMMEND)){
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
            private JPanel card1 = new JPanel();
            private JButton genemaniaButton;
            private JProgressBar progressBar;
            private JButton settingsButton;
            private JLabel progressMessage = new JLabel();
            private ListViewTablePanel recommendedGenes;
            private boolean rankByVarFreq = false;
            
            private Runnable r = new Runnable() {

                @Override
                public void run() {
                    boolean setMsgOff = true;
                    try {
                    
                    genemania = new GenemaniaInfoRetriever();

                    java.util.List<String> geneNames = new ArrayList();
                    for (int i = 0; i < selectedGenesPanel.getTable().getRowCount(); i++) {
                        Object[] rowData = selectedGenesPanel.getRowData(i);
                        geneNames.add((String) rowData[0]);
                    }
                    java.util.List<String> notInGenemania = new ArrayList<String> (geneNames); 
                    notInGenemania.removeAll(GenemaniaInfoRetriever.getValidGenes(geneNames));
                    geneNames = GenemaniaInfoRetriever.getValidGenes(geneNames);
                    genemania.setGenes(geneNames);
                    if(notInGenemania.size()>0){
                        String message = "<html><center>Following gene(s) not found in GeneMANIA: ";
                        for(String invalidGene: notInGenemania){
                            message+="<br>"+invalidGene;
                        }
                        message+="</center></html>";
                        progressMessage.setText(message);
                        setMsgOff = false;
                    }
                    GeneSetFetcher geneSetFetcher = new GeneSetFetcher();
                    if (genemania.getGenes().size()>0) {
                        if(rankByVarFreq){
                            Iterator<org.ut.biolab.medsavant.model.Gene> itr = geneSetFetcher.getGenesByNumVariants(genemania.getRelatedGeneNamesByScore()).iterator();
                            org.ut.biolab.medsavant.model.Gene currGene;
                            itr.next();//skip the first one (it's the name of selected gene already displayed)

                            if (Thread.interrupted()) { throw new InterruptedException(); }

                            int i = 1;
                            while (itr.hasNext()) {
                                currGene = itr.next();
                                final org.ut.biolab.medsavant.model.Gene finalGene = currGene;
                                JLabel geneName = new JLabel(currGene.getName());
                                recommendedGenes.addRow(new Object[]{geneName, finalGene.getChrom(), finalGene.getStart(), finalGene.getEnd()});
                                i++;
                            }
                            //currSizeOfArray =i-1;
                        }
                        else {
                           Iterator<String> itr = genemania.getRelatedGeneNamesByScore().iterator();
                           String currGene;
                           itr.next();//skip the first one (it's the name of selected gene already displayed)
                            int i = 1;
                            while (itr.hasNext()) {
                                currGene = itr.next();
                                final org.ut.biolab.medsavant.model.Gene finalGene = new GeneSetFetcher().getGene(currGene);
                                recommendedGenes.addRow(new Object[]{finalGene.getName(), finalGene.getChrom(), finalGene.getStart(), finalGene.getEnd()});
                                i++;
                            }
                            //currSizeOfArray =i-1;
                        }
                    }
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error retrieving data from GeneMANIA: %s", ex);
                }
                finally{
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    progressBar.setVisible(false);
                    if (setMsgOff)
                        progressMessage.setVisible(false);
                }
                    }
            };      
            {
                
            
            
                JPanel card2 = new JPanel();
                tabbedPane.addTab("Query genes", card1);
                tabbedPane.addTab("Select genes", card2);
                tabbedPane.setEnabledAt(1, false);
                recommendedGenes = new ListViewTablePanel(new Object[0][0], COLUMN_NAMES, COLUMN_CLASSES, new int[0]);
                recommendedGenes.setFontSize(10);
                card1.setLayout(new BoxLayout(card1, BoxLayout.PAGE_AXIS));
                card1.add(new JLabel("Query GeneMANIA for Related Genes"), Component.LEFT_ALIGNMENT);
                card1.add(selectedGenesPanel);
                progressMessage.setVisible(false);
                card1.add(progressMessage);
                progressBar = new JProgressBar();
                progressBar.setVisible(false);
                card1.add(progressBar);
                
                genemaniaButton = new JButton("Recommend");
                settingsButton = new JButton("Settings");
                genemaniaButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        genemaniaButton.setEnabled(false);
                        settingsButton.setEnabled(false);
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(true);
                        Thread t = new Thread (r);
                        t.start();
                        tabbedPane.setEnabledAt(1, true);
                        tabbedPane.setSelectedIndex(1);
                    }
                });
                settingsButton.addActionListener(new ActionListener(){
                   public void actionPerformed(ActionEvent e){
                       switchToSettingsPanel(card1);
                   } 
                });
                JPanel buttonPanel = new JPanel();

                buttonPanel.add(genemaniaButton);

                buttonPanel.add(ViewUtil.alignRight(settingsButton));
                card1.add(buttonPanel, Component.RIGHT_ALIGNMENT);
                card2.setLayout(new BoxLayout(card2, BoxLayout.PAGE_AXIS));
                card2.add(new JLabel("Select genes from recommended genes list"));
                PartSelectorPanel selector = new PartSelectorPanel(recommendedGenes, selectedGenesPanel);
                selector.setBackground(Color.WHITE);
                card2.add(selector);
                addComponent(tabbedPane);
            }
             

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
            }
        };
    }
    
    
    private void switchToSettingsPanel(JPanel panel){
        panel.invalidate();
        panel.updateUI();
        JPanel equal;

        JPanel geneOntology;


        JSeparator jSeparator1;
        JSeparator jSeparator2;
        JSeparator jSeparator3;
        JLabel limitTo;

        JLabel networkWeighting;
        JLabel networks;


        JPanel queryDependent;
        JLabel rankBy;
        JLabel relatedGenes;

      

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


        limitTo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        limitTo.setText("Limit to");

        relatedGenesLimit.setColumns(3);
        relatedGenesLimit.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genemania.setGeneLimit(Integer.parseInt(relatedGenesLimit.getText()));
                
            }
        });

        relatedGenes.setText("related genes.");

        rankBy.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rankBy.setText("Rank by");


//        buttonGroup1.add(varFreq);
//        varFreq.setText("Variation Frequency");
//        varFreq.setActionCommand("varFreq");
//        varFreq.addActionListener(scoringActionPerformed);
//
//        buttonGroup1.add(genemaniaScore);
//        genemaniaScore.setText("GeneMANIA Score");
//        genemaniaScore.setActionCommand("genemaniaScore");
//        genemaniaScore.addActionListener(scoringActionPerformed);
//
//        networks.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
//        networks.setText("Networks");
//
//        ActionListener networksActionPerformed = new ActionListener() {
//
//            public void actionPerformed(ActionEvent evt) {
//                if (!networksSelected.equals(getNetworksSelection())) {
//                    updateQueryNeeded = true;
//                    setNetworks();
//                }
//            }
//        };
//
//        coexp.setText("Co-expression");
//        coexp.addActionListener(networksActionPerformed);
//
//        spd.setText("Shared Protein Domains");
//        spd.addActionListener(networksActionPerformed);
//
//        gi.setText("Genetic interactions");
//        gi.addActionListener(networksActionPerformed);
//
//        coloc.setText("Co-localization");
//        coloc.addActionListener(networksActionPerformed);
//
//        path.setText("Pathway interactions");
//        path.addActionListener(networksActionPerformed);
//
//        predict.setText("Predicted");
//        predict.addActionListener(networksActionPerformed);
//
//        pi.setText("Physical interactions");
//        pi.addActionListener(networksActionPerformed);
//
//        other.setText("Other");
//        other.addActionListener(networksActionPerformed);
//
//        networkWeighting.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
//        networkWeighting.setText("Network weighting");
//
//        equal.setBorder(javax.swing.BorderFactory.createTitledBorder("Equal weighting"));
//
//        ActionListener combiningMethodActionPerformed = new ActionListener() {
//
//            public void actionPerformed(ActionEvent evt) {
//                if (!combiningMethod.getCode().equals(combiningMethods[getSelectionFromButtonGroup(buttonGroup1)].getCode())) {
//                    updateQueryNeeded = true;
//                    setCombiningMethod();
//                }
//            }
//        };
//
//        buttonGroup2.add(average);
//        average.setText("Equal by network");
//        average.setActionCommand("average");
//        average.addActionListener(combiningMethodActionPerformed);
//
//        javax.swing.GroupLayout equalLayout = new javax.swing.GroupLayout(equal);
//        equal.setLayout(equalLayout);
//
//        equalLayout.setHorizontalGroup(
//                equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(equalLayout.createSequentialGroup().addContainerGap().addComponent(average).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
//        equalLayout.setVerticalGroup(
//                equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(average));
//
//        geneOntology.setBorder(javax.swing.BorderFactory.createTitledBorder("Gene Ontology (GO)- based weighting"));
//
//        buttonGroup2.add(bp);
//        bp.setText("Biological process based");
//        bp.setActionCommand("bp");
//        bp.addActionListener(combiningMethodActionPerformed);
//
//        buttonGroup2.add(mf);
//        mf.setText("Molecular function based");
//        mf.setActionCommand("mf");
//        mf.addActionListener(combiningMethodActionPerformed);
//
//        buttonGroup2.add(cc);
//        cc.setText("Cellular component based");
//        cc.setActionCommand("cc");
//        cc.addActionListener(combiningMethodActionPerformed);
//
//        javax.swing.GroupLayout geneOntologyLayout = new javax.swing.GroupLayout(geneOntology);
//        geneOntology.setLayout(geneOntologyLayout);
//        geneOntologyLayout.setHorizontalGroup(
//                geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(geneOntologyLayout.createSequentialGroup().addContainerGap().addGroup(geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(bp).addComponent(mf).addComponent(cc)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
//        geneOntologyLayout.setVerticalGroup(
//                geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(geneOntologyLayout.createSequentialGroup().addComponent(bp, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(mf).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(cc)));
//
//        queryDependent.setBorder(javax.swing.BorderFactory.createTitledBorder("Query-dependent weighting"));
//
//        buttonGroup2.add(automatic);
//        automatic.setText("Automatically selected weighting method");
//        automatic.setActionCommand("automatic");
//        automatic.addActionListener(combiningMethodActionPerformed);
//
//        javax.swing.GroupLayout queryDependentLayout = new javax.swing.GroupLayout(queryDependent);
//        queryDependent.setLayout(queryDependentLayout);
//        queryDependentLayout.setHorizontalGroup(
//                queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(queryDependentLayout.createSequentialGroup().addContainerGap().addComponent(automatic).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
//        queryDependentLayout.setVerticalGroup(
//                queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(automatic));
//
//        okButton.setText("OK");
//        okButton.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent ae) {
//                closeSettingsActionPerformed(ae);
//            }
//        });
//
//        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(settingsPanel);
//        settingsPanel.setLayout(layout);
//        layout.setHorizontalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(jSeparator1).addComponent(queryDependent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(equal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(networkWeighting).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(limitTo).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(relatedGenesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(rankBy)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(varFreq).addComponent(relatedGenes).addComponent(genemaniaScore))).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(coexp).addComponent(gi).addComponent(path).addComponent(pi)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(predict)).addComponent(coloc).addComponent(spd).addComponent(other))).addComponent(networks)).addComponent(geneOntology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jSeparator2)).addGap(0, 0, Short.MAX_VALUE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
//
//        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{genemaniaScore, varFreq});
//
//        layout.setVerticalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(limitTo).addComponent(relatedGenesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(relatedGenes)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addGroup(layout.createSequentialGroup().addComponent(varFreq).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(genemaniaScore).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)).addGroup(layout.createSequentialGroup().addComponent(rankBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(39, 39, 39))).addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(1, 1, 1).addComponent(networks).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(coexp).addComponent(spd)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(gi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(coloc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(path).addComponent(predict)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(pi).addComponent(other)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(networkWeighting).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(equal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(geneOntology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(queryDependent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(okButton).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
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
            for (RegionSet r: controller.getRegionSets()) {
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
