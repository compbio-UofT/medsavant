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
package org.ut.biolab.medsavant.view.genetics.variantinfo;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.view.CyNetworkView;
import cytoscape.view.NetworkViewManager;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genemania.plugin.cytoscape2.layout.FilteredLayout;
import org.genemania.type.CombiningMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.region.RegionController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.inspector.GeneInspector;
import org.ut.biolab.medsavant.view.genetics.inspector.InspectorPanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.GenemaniaInfoRetriever.NoRelatedGenesInfoException;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author khushi
 */
public class GeneManiaInfoSubPanel extends SubInspector implements GeneSelectionChangedListener {

    private static final Log LOG = LogFactory.getLog(GeneManiaInfoSubPanel.class);
    private final String name;
    private final int GENE_LIMIT_DEFAULT = 10;
    private final CombiningMethod[] combiningMethods = {CombiningMethod.AVERAGE, CombiningMethod.BP, CombiningMethod.MF, CombiningMethod.CC, CombiningMethod.AUTOMATIC};
    private GenemaniaInfoRetriever genemania;
    private JLabel l;
    private JPanel p;
    private javax.swing.JProgressBar progressBar;
    private JLabel progressMessage;
    private KeyValuePairPanel kvp;
    private JPanel kvpPanel;
    private JPanel settingsPanel;
    private boolean updateQueryNeeded;
    private JButton settingsButton;
    private JTextField geneLimit;
    private int glimit;
    private ButtonGroup buttonGroup1;
    private ButtonGroup buttonGroup2;
    private JCheckBox coexp;
    private JCheckBox coloc;
    private JCheckBox gi;
    private JCheckBox other;
    private JCheckBox path;
    private JCheckBox pi;
    private JCheckBox predict;
    private JCheckBox spd;
    private Gene gene;
    private JRadioButton automatic;
    private JRadioButton average;
    private JRadioButton bp;
    private JRadioButton cc;
    private JRadioButton genemaniaScore;
    private JRadioButton mf;
    private JRadioButton varFreq;
    private JButton okButton;
    private boolean rankByVarFreq;
    private CombiningMethod combiningMethod;
    private Set<String> networksSelected;
    private int geneLimitDifference;
    private Thread genemaniaAlgorithmThread;
    private boolean dataPresent;
    private int currSizeOfArray;
    private JPanel graph;

    public GeneManiaInfoSubPanel() {
        name = "Related Genes";
        dataPresent = true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getInfoPanel() {
        l = new JLabel("Selected gene: ");
        p = ViewUtil.getClearPanel();
        try {
            genemania = new GenemaniaInfoRetriever();
        } catch (Exception ex) {
            progressMessage = new JLabel("<html><center>" + ex.getMessage() + "<br>Please try again after data has been downloaded.</center></html>", SwingConstants.CENTER);
            p.add(progressMessage);
            dataPresent = false;
            return p;
        }
        initializeSettingsComponents();
        setGeneLimit();
        setRankingMethod();
        setCombiningMethod();
        setNetworks();
        kvp = new KeyValuePairPanel(2);
        kvp.setKeysVisible(false);
        kvpPanel = new JPanel();
        kvpPanel.setLayout(new BorderLayout());
        kvpPanel.add(kvp, BorderLayout.CENTER);
        JPanel currGenePanel = ViewUtil.getClearPanel();
        JPanel pMessagePanel = ViewUtil.getClearPanel();
        settingsPanel = ViewUtil.getClearPanel();
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressMessage = new JLabel();
        progressMessage.setVisible(false);
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_settingsActionPerformed();
            }
        });
        l = new JLabel("Selected gene: ");
        //currGenePanel.add(l);
        pMessagePanel.add(progressMessage);
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(settingsButton, BorderLayout.EAST);
        graph = new JPanel();
        p.add(kvpPanel);
        p.add(currGenePanel);
        p.add(pMessagePanel);
        p.add(progressBar);

        //p.add(new javax.swing.JSeparator(JSeparator.HORIZONTAL));
        p.add(settingsPanel);
        p.add(graph);
        return p;
    }

    private void setNetworks() {
        networksSelected = getNetworksSelection();
        genemania.setNetworks(networksSelected);
    }

    private void setCombiningMethod() {
        combiningMethod = combiningMethods[getSelectionFromButtonGroup(buttonGroup2)];
        genemania.setCombiningMethod(combiningMethod);
    }

    private void setRankingMethod() {
        if (getSelectionFromButtonGroup(buttonGroup1) == 0) {
            rankByVarFreq = true;
        } else {
            rankByVarFreq = false;
        }
    }

    private void initializeSettingsComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        geneLimit = new javax.swing.JTextField();
        geneLimit.setText(Integer.toString(GENE_LIMIT_DEFAULT));
        currSizeOfArray = GENE_LIMIT_DEFAULT;
        varFreq = new javax.swing.JRadioButton();
        genemaniaScore = new javax.swing.JRadioButton();
        genemaniaScore.setSelected(true);
        coexp = new javax.swing.JCheckBox();
        coexp.setActionCommand("Co-expression");
        coexp.setSelected(true);
        spd = new javax.swing.JCheckBox();
        spd.setSelected(true);
        spd.setActionCommand("Shared protein domains");

        gi = new javax.swing.JCheckBox();
        gi.setActionCommand("Genetic interactions");

        gi.setSelected(true);
        coloc = new javax.swing.JCheckBox();
        coloc.setActionCommand("Co-localization");

        coloc.setSelected(true);
        path = new javax.swing.JCheckBox();
        path.setSelected(true);
        path.setActionCommand("Pathway");

        predict = new javax.swing.JCheckBox();
        predict.setSelected(true);
        predict.setActionCommand("Predicted");

        pi = new javax.swing.JCheckBox();
        pi.setSelected(true);
        pi.setActionCommand("Physical interactions");

        other = new javax.swing.JCheckBox();
        other.setSelected(true);
        other.setActionCommand("Other");

        average = new javax.swing.JRadioButton();
        average.setSelected(true);
        bp = new javax.swing.JRadioButton();
        mf = new javax.swing.JRadioButton();
        cc = new javax.swing.JRadioButton();
        automatic = new javax.swing.JRadioButton();
        okButton = new javax.swing.JButton();

    }

    private void setGeneLimit() {
        glimit = Integer.parseInt(geneLimit.getText());
        genemania.setGeneLimit(glimit + 1);
    }

    private int getSelectionFromButtonGroup(ButtonGroup bg) {
        int selection = 0;
        Enumeration<AbstractButton> buttonEnum = bg.getElements();
        while (buttonEnum.hasMoreElements()) {
            if (buttonEnum.nextElement().isSelected()) {
                break;
            }
            selection++;
        }
        return selection;
    }

    private void button_settingsActionPerformed() {
        settingsPanel.removeAll();
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

        updateQueryNeeded = false;
        geneLimitDifference = -1;


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

        geneLimit.setColumns(3);
        geneLimit.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
                
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                if (!KeyEvent.getKeyText(ke.getKeyCode()).equals("Backspace")) {
                    if (glimit != Integer.parseInt(geneLimit.getText())) {
                        if (glimit > Integer.parseInt(geneLimit.getText())) {
                            geneLimitDifference = currSizeOfArray - Integer.parseInt(geneLimit.getText());
                        } else {
                            updateQueryNeeded = true;
                        }
                        setGeneLimit();
                    }
                }
            }
        });
        

        relatedGenes.setText("related genes.");

        rankBy.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rankBy.setText("Rank by");

        ActionListener scoringActionPerformed = new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if ((evt.getActionCommand().equals("genemaniaScore") && rankByVarFreq) || (!rankByVarFreq && evt.getActionCommand().equals("varFreq"))) {
                    updateQueryNeeded = true;
                    setRankingMethod();
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

            public void actionPerformed(ActionEvent evt) {
                if (!networksSelected.equals(getNetworksSelection())) {
                    updateQueryNeeded = true;
                    setNetworks();
                }
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

        path.setText("Pathway interactions");
        path.addActionListener(networksActionPerformed);

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

            public void actionPerformed(ActionEvent evt) {
                if (!combiningMethod.getCode().equals(combiningMethods[getSelectionFromButtonGroup(buttonGroup1)].getCode())) {
                    updateQueryNeeded = true;
                    setCombiningMethod();
                }
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
                equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(average));

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
                geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(geneOntologyLayout.createSequentialGroup().addContainerGap().addGroup(geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(bp).addComponent(mf).addComponent(cc)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        geneOntologyLayout.setVerticalGroup(
                geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(geneOntologyLayout.createSequentialGroup().addComponent(bp, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(mf).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(cc)));

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
                queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(automatic));

        okButton.setText("OK");
        for (ActionListener a:okButton.getActionListeners())
            okButton.removeActionListener(a);
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                closeSettingsActionPerformed();
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(jSeparator1).addComponent(queryDependent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(equal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(networkWeighting).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(limitTo).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(geneLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(rankBy)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(varFreq).addComponent(relatedGenes).addComponent(genemaniaScore))).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(coexp).addComponent(gi).addComponent(path).addComponent(pi)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(predict)).addComponent(coloc).addComponent(spd).addComponent(other))).addComponent(networks)).addComponent(geneOntology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jSeparator2)).addGap(0, 0, Short.MAX_VALUE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{genemaniaScore, varFreq});

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(limitTo).addComponent(geneLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(relatedGenes)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addGroup(layout.createSequentialGroup().addComponent(varFreq).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(genemaniaScore).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)).addGroup(layout.createSequentialGroup().addComponent(rankBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(39, 39, 39))).addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(1, 1, 1).addComponent(networks).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(coexp).addComponent(spd)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(gi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(coloc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(path).addComponent(predict)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(pi).addComponent(other)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(networkWeighting).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(equal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(geneOntology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(queryDependent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(okButton).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

    }

    private Set<String> getNetworksSelection() {
        Set<String> networksSelected = new HashSet<String>();
        JCheckBox[] networkButtons = {coexp, spd, gi, coloc, path, predict, pi, other};
        for (int i = 0; i < networkButtons.length; i++) {
            if (networkButtons[i].isSelected()) {
                networksSelected.add(networkButtons[i].getActionCommand());
            }
        }
        return networksSelected;
    }

    private void closeSettingsActionPerformed() {
        settingsPanel.removeAll();
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(settingsButton, BorderLayout.EAST);
        p.invalidate();
        p.updateUI();
        System.out.println(geneLimitDifference);
        if (geneLimitDifference > 0) {
            System.out.println(geneLimitDifference);
            System.out.println(currSizeOfArray);
            for (int i = currSizeOfArray; i > currSizeOfArray - geneLimitDifference; i--) {
                kvp.removeBottomRow(Integer.toString(i));
            }
            currSizeOfArray -= geneLimitDifference;
            kvpPanel.invalidate();
            kvpPanel.updateUI();
        } else if (updateQueryNeeded) {
            updateRelatedGenesPanel(gene);
        }
    }

    @Override
    public void geneSelectionChanged(Gene g) {
        if (g == null || !dataPresent) {
            l.setText("None");
        } else {
            System.out.println("Received gene " + g.getName());
            l.setText(g.getName());
            if (gene == null || !g.getName().equals(gene.getName())) {
                updateRelatedGenesPanel(g);
            }

        }
    }

    private void updateRelatedGenesPanel(Gene g) {
        gene = g;
        kvpPanel.removeAll();
        kvpPanel.invalidate();
        kvpPanel.updateUI();
        kvp = new KeyValuePairPanel(5);
        kvp.setKeysVisible(false);
        kvpPanel.add(kvp);
        progressBar.setVisible(true);
        progressMessage.setVisible(true);
        progressBar.setIndeterminate(true);
        progressMessage.setText("Querying GeneMANIA for related genes");

        final Object lock = new Object();

        Runnable r = new Runnable() {

            @Override
            public void run() {



                boolean setMsgOff = true;
                if (!Thread.interrupted()) {
                    try {
                        List<String> geneNames = new ArrayList();
                        geneNames.add(gene.getName());
                        List<String> notInGenemania = new ArrayList<String>(geneNames);
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
                            int i = 1;
                            String zero = Integer.toString(0);
                            Font HEADER_FONT = new Font("Arial", Font.BOLD, 10);
                            kvp.addKey(zero);
                            JLabel geneHeader = new JLabel("Gene".toUpperCase());
                            geneHeader.setFont(HEADER_FONT);
                            kvp.setValue(zero, geneHeader);
                            JLabel varFreqHeader = new JLabel("<html>VARIATION<br>FREQUENCY<br>(var/kb)</html>");
                            varFreqHeader.setFont(HEADER_FONT);
                            kvp.setAdditionalColumn(zero, 0, varFreqHeader);
                            JLabel genemaniaHeader = new JLabel("<html>GENEMANIA<br>SCORE</html>");
                            genemaniaHeader.setFont(HEADER_FONT);
                            kvp.setAdditionalColumn(zero, 1, genemaniaHeader);
                            if (rankByVarFreq) {
                                Iterator<org.ut.biolab.medsavant.model.Gene> itr = geneSetFetcher.getGenesByNumVariants(genemania.getRelatedGeneNamesByScore()).iterator();
                                org.ut.biolab.medsavant.model.Gene currGene;
                                itr.next();//skip the first one (it's the name of selected gene already displayed)

                                if (Thread.interrupted()) {
                                    throw new InterruptedException();
                                }


                                while (itr.hasNext()) {
                                    currGene = itr.next();
                                    final org.ut.biolab.medsavant.model.Gene finalGene = currGene;
                                    kvp.addKey(Integer.toString(i));
                                    JLabel geneName = new JLabel(currGene.getName());
                                    EntrezButton geneLinkButton = new EntrezButton(currGene.getName());
                                    kvp.setValue(Integer.toString(i), geneName);
                                    kvp.setAdditionalColumn(Integer.toString(i), 0, new JLabel(Double.toString(GeneSetFetcher.getInstance().getNormalizedVariantCount(currGene))));
                                    JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
                                    geneInspectorButton.setToolTipText("Inspect this gene");
                                    geneInspectorButton.addActionListener(new ActionListener() {

                                        @Override
                                        public void actionPerformed(ActionEvent ae) {
                                            GeneInspector.getInstance().setGene(finalGene);
                                            InspectorPanel.getInstance().switchToGeneInspector();
                                        }
                                    });
                                    final JPopupMenu regionSets = new JPopupMenu();
                                    final RegionController regionController = RegionController.getInstance();
                                    for (RegionSet s : regionController.getRegionSets()) {
                                        final RegionSet finalRegionSet = s;
                                        JMenuItem menuItem = new JMenuItem(s.getName());
                                        menuItem.addActionListener(new ActionListener() {

                                            public void actionPerformed(ActionEvent ae) {
                                                try {
                                                    regionController.addToRegionSet(finalRegionSet, finalGene.getChrom(), finalGene.getStart(), finalGene.getEnd(), finalGene.getName());
                                                    DialogUtils.displayMessage(String.format("Successfully added %s to %s list", finalGene.getName(), finalRegionSet.getName()));
                                                } catch (SQLException ex) {
                                                    Logger.getLogger(GeneManiaInfoSubPanel.class.getName()).log(Level.SEVERE, null, ex);
                                                } catch (RemoteException e) {
                                                    Logger.getLogger(GeneManiaInfoSubPanel.class.getName()).log(Level.SEVERE, null, e);
                                                }
                                            }
                                        });
                                        regionSets.add(menuItem);
                                    }
                                    final JLabel addToRegionListButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
                                    addToRegionListButton.setToolTipText("Add to Region List");
                                    addToRegionListButton.addMouseListener(new MouseAdapter() {

                                        @Override
                                        public void mouseClicked(MouseEvent e) {
                                            regionSets.show(addToRegionListButton, 0, addToRegionListButton.getHeight());
                                        }
                                    });
                                    kvp.setAdditionalColumn(Integer.toString(i), 2, geneInspectorButton);
                                    kvp.setAdditionalColumn(Integer.toString(i), 3, addToRegionListButton);
                                    kvp.setAdditionalColumn(Integer.toString(i), 4, geneLinkButton);
                                    i++;
                                }
                                currSizeOfArray = i - 1;
                            } else {
                                Iterator<String> itr = genemania.getRelatedGeneNamesByScore().iterator();
                                String currGene;
                                itr.next();//skip the first one (it's the name of selected gene already displayed)

                                if (Thread.interrupted()) {
                                    throw new InterruptedException();
                                }

                                System.out.println("start populating table" + System.currentTimeMillis());

                                while (itr.hasNext()) {
                                    currGene = itr.next();
                                    final org.ut.biolab.medsavant.model.Gene finalGene = GeneSetFetcher.getInstance().getGene(currGene);
                                    if (finalGene != null) {
                                        kvp.addKey(Integer.toString(i));
                                        System.err.println("get link out button" + System.currentTimeMillis());
                                        EntrezButton geneLinkButton = new EntrezButton(currGene);
                                        JLabel geneName = new JLabel(currGene);
                                        kvp.setValue(Integer.toString(i), geneName);
                                        System.err.println("get inspector button" + System.currentTimeMillis());
                                        JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
                                        geneInspectorButton.setToolTipText("Inspect this gene");
                                        geneInspectorButton.addActionListener(new ActionListener() {

                                            @Override
                                            public void actionPerformed(ActionEvent ae) {
                                                GeneInspector.getInstance().setGene(finalGene);
                                                InspectorPanel.getInstance().switchToGeneInspector();
                                            }
                                        });
                                        final JPopupMenu regionSets = new JPopupMenu();
                                        System.err.println("get region sets" + System.currentTimeMillis());
                                        final RegionController regionController = RegionController.getInstance();
                                        for (RegionSet s : regionController.getRegionSets()) {
                                            final RegionSet finalRegionSet = s;
                                            JMenuItem menuItem = new JMenuItem(s.getName());
                                            menuItem.addActionListener(new ActionListener() {

                                                public void actionPerformed(ActionEvent ae) {
                                                    try {
                                                        regionController.addToRegionSet(finalRegionSet, finalGene.getChrom(), finalGene.getStart(), finalGene.getEnd(), finalGene.getName());
                                                        DialogUtils.displayMessage(String.format("Successfully added %s to %s list", finalGene.getName(), finalRegionSet.getName()));
                                                    } catch (SQLException ex) {
                                                        Logger.getLogger(GeneManiaInfoSubPanel.class.getName()).log(Level.SEVERE, null, ex);
                                                    } catch (RemoteException e) {
                                                        Logger.getLogger(GeneManiaInfoSubPanel.class.getName()).log(Level.SEVERE, null, e);
                                                    }
                                                }
                                            });
                                            regionSets.add(menuItem);
                                        }
                                        final JLabel addToRegionListButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
                                        addToRegionListButton.setToolTipText("Add to Region List");
                                        addToRegionListButton.addMouseListener(new MouseAdapter() {

                                            @Override
                                            public void mouseClicked(MouseEvent e) {
                                                regionSets.show(addToRegionListButton, 0, addToRegionListButton.getHeight());
                                            }
                                        });
                                        kvp.setAdditionalColumn(Integer.toString(i), 0, geneInspectorButton);
                                        kvp.setAdditionalColumn(Integer.toString(i), 1, addToRegionListButton);
                                        kvp.setAdditionalColumn(Integer.toString(i), 2, geneLinkButton);
                                        i++;
                                    }
                                }
                                System.err.println("done thread" + System.currentTimeMillis());

                                currSizeOfArray = i - 1;
                            }

                        }

                    } catch (InterruptedException e) {
                    } catch (NoRelatedGenesInfoException e) {
                        progressMessage.setText(e.getMessage());
                        setMsgOff = false;
                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Error retrieving data from GeneMANIA: %s", ex);
                    } finally {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);
                        if (setMsgOff) {
                            progressMessage.setVisible(false);
                        }
//                        System.out.println("got here");
//                        graph.removeAll();
//                        graph.add(buildGraph());
//                        System.out.println("got through");
//                        graph.invalidate();
//                        graph.updateUI();
                        System.err.println("done finally" + System.currentTimeMillis());
                    }

                }

                synchronized (lock) {
                    lock.notify();
                }
            }
        };
        if (genemaniaAlgorithmThread == null) {
            genemaniaAlgorithmThread = new Thread(r);
        } else {
            genemaniaAlgorithmThread.interrupt();
            genemaniaAlgorithmThread = new Thread(r);

        }

        final Runnable geneDescriptionFetcher = new Runnable() {

            @Override
            public void run() {
                for (int j = 1; j <= currSizeOfArray; j++) {
                    try {
                        String geneName = kvp.getValue(Integer.toString(j));
                        Gene gene = GeneSetFetcher.getInstance().getGene(geneName);
                        String d = gene.getDescription();
                        kvp.setToolTipForValue(Integer.toString(j), d);
                    } catch (Exception e) {
                        //do nothing (don't set tool tip to anything)
                    }
                }
            }
        };
        //}


        genemaniaAlgorithmThread.start();

        Runnable r2 = new Runnable() {

            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        lock.wait();
                        Thread toolTipGenerator = new Thread(geneDescriptionFetcher);
                        toolTipGenerator.start();
                    }
                } catch (Exception e) {
                }
            }
        };
        
        Thread t2 = new Thread(r2);
        t2.start();



    }

    // final static Object lock;
    public JPanel buildGraph() {
        CyNetwork network = genemania.getGraph();
        //System.out.println("Nodes " + network.getNodeCount());
        // System.out.println("Edges " + network.getEdgeCount());
        for (int i = 0; i < network.getEdgeCount(); i++) {
            //System.out.println(network.getEdge(i));
        }

        CytoscapeUtils cy = new CytoscapeUtils(genemania.getNetworkUtils());
        Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, null);
        Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
        CyNetworkView view = cy.getNetworkView(network);
        CyLayoutAlgorithm layout = CyLayouts.getLayout(FilteredLayout.ID);
        if (layout == null) {
            layout = CyLayouts.getDefaultLayout();
        }
        layout.doLayout(view);
        //NetworkViewManager viewManager = Cytoscape.getDesktop().getNetworkViewManager();
        //JInternalFrame frame = viewManager.getInternalFrame(view);
        JPanel p = new JPanel();
        p.add(view.getComponent());
        return p;
    }
    /*
     * private JComponent getFrameFromView(CyNetworkView view) { final
     * JInternalFrame iframe = new JInternalFrame(view.getTitle(), true, true,
     * true, true);
     *
     *
     * // code added to support layered canvas for each CyNetworkView if (view
     * instanceof DGraphView) { final InternalFrameComponent internalFrameComp =
     * new InternalFrameComponent(iframe.getLayeredPane(), (DGraphView) view);
     *
     * iframe.getContentPane().add(internalFrameComp);
     *
     * } else { logger.info("NetworkViewManager.createContainer() - DGraphView
     * not found!"); iframe.getContentPane().add(view.getComponent()); }
     *
     * iframe.pack();
     *
     * int x = 0; int y = 0; JInternalFrame refFrame = null; JInternalFrame[]
     * allFrames = desktopPane.getAllFrames();
     *
     * if (allFrames.length > 1) { refFrame = allFrames[0]; }
     *
     * if (refFrame != null) { x = refFrame.getLocation().x + 20; y =
     * refFrame.getLocation().y + 20; }
     *
     * if (x > (desktopPane.getWidth() - MINIMUM_WIN_WIDTH)) { x =
     * desktopPane.getWidth() - MINIMUM_WIN_WIDTH; }
     *
     * if (y > (desktopPane.getHeight() - MINIMUM_WIN_HEIGHT)) { y =
     * desktopPane.getHeight() - MINIMUM_WIN_HEIGHT; }
     *
     * if (x < 0) { x = 0; }
     *
     * if (y < 0) { y = 0; }
     *
     * iframe.setBounds(x, y, 400, 400);
     *
     * // maximize the frame if the specified property is set try { String max
     * = CytoscapeInit.getProperties().getProperty("maximizeViewOnCreate");
     *
     * if ((max != null) && Boolean.parseBoolean(max)) iframe.setMaximum(true);
     * } catch (PropertyVetoException pve) { //logger.warn("Unable to maximize
     * internal frame: "+pve.getMessage()); }
     *
     * iframe.setVisible(true); //iframe.addInternalFrameListener(this);
     * iframe.setResizable(true);
     *
     * return iframe; }
     *
     */
}