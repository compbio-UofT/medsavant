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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genemania.type.CombiningMethod;

import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
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
    private JToggleButton settingsButton;
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

    private int currSizeOfArray;

    public GeneManiaInfoSubPanel(){
        name = "Related Genes";
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public JPanel getInfoPanel() {
         genemania = new GenemaniaInfoRetriever();
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
         p = ViewUtil.getClearPanel();
         p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
         settingsButton = ViewUtil.getTexturedToggleButton("Settings");
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
         p.add(currGenePanel);
         p.add(progressBar);
         p.add(pMessagePanel);
         p.add(kvpPanel);
         p.add(new javax.swing.JSeparator(JSeparator.HORIZONTAL));
         p.add(settingsPanel);
         return p;
     }

     private void setNetworks(){
         networksSelected=getNetworksSelection();
         genemania.setNetworks(networksSelected);
     }

    private void setCombiningMethod(){
        combiningMethod= combiningMethods[getSelectionFromButtonGroup(buttonGroup2)];
        genemania.setCombiningMethod(combiningMethod);
    }

    private void setRankingMethod() {
        if(getSelectionFromButtonGroup(buttonGroup1)==0)
            rankByVarFreq = true;
        else
            rankByVarFreq= false;
    }

     private void initializeSettingsComponents(){
         buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        geneLimit = new javax.swing.JTextField();
        geneLimit.setText(Integer.toString(GENE_LIMIT_DEFAULT));
        currSizeOfArray = GENE_LIMIT_DEFAULT;
        varFreq = new javax.swing.JRadioButton();
        varFreq.setSelected(true);
        genemaniaScore = new javax.swing.JRadioButton();
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

     private void setGeneLimit(){
         glimit= Integer.parseInt(geneLimit.getText());
         genemania.setGeneLimit(glimit +1);
     }

     private int getSelectionFromButtonGroup (ButtonGroup bg){
         int selection = 0;
         Enumeration<AbstractButton> buttonEnum = bg.getElements();
         while(buttonEnum.hasMoreElements()){
             if(buttonEnum.nextElement().isSelected())
                 break;
             selection++;
         }
         return selection;
     }

    private void button_settingsActionPerformed (){
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
        geneLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if(glimit != Integer.parseInt(geneLimit.getText())){
                    if(glimit>Integer.parseInt(geneLimit.getText()))
                        geneLimitDifference= currSizeOfArray - Integer.parseInt(geneLimit.getText());
                    else
                        updateQueryNeeded=true;
                    setGeneLimit();
                }
            }
        });

        relatedGenes.setText("related genes.");

        rankBy.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rankBy.setText("Rank by");

        ActionListener scoringActionPerformed = new ActionListener(){
          public void actionPerformed(ActionEvent evt){
              if((evt.getActionCommand().equals("genemaniaScore") && rankByVarFreq) || (!rankByVarFreq && evt.getActionCommand().equals("varFreq")) ){
                  updateQueryNeeded= true;
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

        ActionListener networksActionPerformed = new ActionListener(){
          public void actionPerformed(ActionEvent evt){
              if (!networksSelected.equals(getNetworksSelection())){
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

        ActionListener combiningMethodActionPerformed = new ActionListener(){
          public void actionPerformed(ActionEvent evt){
              if(!combiningMethod.getCode().equals(combiningMethods[getSelectionFromButtonGroup(buttonGroup1)].getCode())){
                  updateQueryNeeded=true;
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
            equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(equalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(average)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        equalLayout.setVerticalGroup(
            equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(average)
        );

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
            geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(geneOntologyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bp)
                    .addComponent(mf)
                    .addComponent(cc))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        geneOntologyLayout.setVerticalGroup(
            geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(geneOntologyLayout.createSequentialGroup()
                .addComponent(bp, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cc))
        );

        queryDependent.setBorder(javax.swing.BorderFactory.createTitledBorder("Query-dependent weighting"));

        buttonGroup2.add(automatic);
        automatic.setText("Automatically selected weighting method");
        automatic.setActionCommand("automatic");
        automatic.addActionListener(combiningMethodActionPerformed);

        javax.swing.GroupLayout queryDependentLayout = new javax.swing.GroupLayout(queryDependent);
        queryDependent.setLayout(queryDependentLayout);
        queryDependentLayout.setHorizontalGroup(
            queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryDependentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(automatic)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        queryDependentLayout.setVerticalGroup(
            queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(automatic)
        );

        okButton.setText("OK");
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed (ActionEvent ae){
                closeSettingsActionPerformed(ae);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator1)
                            .addComponent(queryDependent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(equal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(networkWeighting)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(limitTo)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(geneLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(rankBy))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(varFreq)
                                        .addComponent(relatedGenes)
                                        .addComponent(genemaniaScore)))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(coexp)
                                        .addComponent(gi)
                                        .addComponent(path)
                                        .addComponent(pi))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(predict))
                                        .addComponent(coloc)
                                        .addComponent(spd)
                                        .addComponent(other)))
                                .addComponent(networks))
                            .addComponent(geneOntology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparator2))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {genemaniaScore, varFreq});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(limitTo)
                    .addComponent(geneLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(relatedGenes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(varFreq)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(genemaniaScore)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rankBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(39, 39, 39)))
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(networks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coexp)
                    .addComponent(spd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(gi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(coloc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(path)
                    .addComponent(predict))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pi)
                    .addComponent(other))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(networkWeighting)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(equal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(geneOntology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryDependent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

     }

    private Set<String> getNetworksSelection(){
        Set<String> networksSelected = new HashSet<String>();
        JCheckBox[] networkButtons = {coexp, spd, gi, coloc, path, predict, pi, other};
        for(int i=0; i<networkButtons.length; i++){
            if (networkButtons[i].isSelected()){
                networksSelected.add(networkButtons[i].getActionCommand());
            }
        }
        return networksSelected;
    }



    private void closeSettingsActionPerformed (ActionEvent evt){
        settingsPanel.removeAll();
        p.invalidate();
        p.updateUI();
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(settingsButton, BorderLayout.EAST);
        p.invalidate();
        p.updateUI();
        if (geneLimitDifference>0){
            System.out.println(geneLimitDifference);
            System.out.println(currSizeOfArray);
             for(int i=currSizeOfArray; i>currSizeOfArray-geneLimitDifference; i--){
                 kvp.removeBottomRow(Integer.toString(i));
             }
             kvpPanel.invalidate();
             kvpPanel.updateUI();
        }
        else if (updateQueryNeeded){
           updateRelatedGenesPanel(gene);
        }
    }

    @Override
    public void geneSelectionChanged(Gene g) {
        if (g == null) {
            l.setText("None");
        } else {
            System.out.println("Received gene " + g.getName());
            l.setText(g.getName());
            if (gene==null || !g.getName().equals(gene.getName())){
                updateRelatedGenesPanel(g);
            }

        }
    }

    private void updateRelatedGenesPanel(Gene g) {
        gene = g;
        kvpPanel.removeAll();
        kvpPanel.invalidate();
        kvpPanel.updateUI();
        kvp = new KeyValuePairPanel(3);
        kvp.setKeysVisible(false);
        kvpPanel.add(kvp);
        progressBar.setVisible(true);
        progressMessage.setVisible(true);
        progressBar.setIndeterminate(true);
        progressMessage.setText("Getting Related Genes");

        Runnable r = new Runnable(){

            @Override
            public void run() {
                boolean setMsgOff = true;
                if (!Thread.interrupted()){
                try {
                    genemania.setGene(gene.getName());
                    GeneSetFetcher geneSetFetcher = new GeneSetFetcher();
                    if (genemania.validGene()) {
                        if(rankByVarFreq){
                            Iterator<org.ut.biolab.medsavant.model.Gene> itr = geneSetFetcher.getGenesByNumVariants(genemania.getRelatedGeneNamesByScore()).iterator();
                            org.ut.biolab.medsavant.model.Gene currGene;
                            itr.next();//skip the first one (it's the name of selected gene already displayed)

                            if (Thread.interrupted()) { throw new InterruptedException(); }

                            int i = 1;
                            while (itr.hasNext()) {
                                currGene = itr.next();
                                kvp.addKey(Integer.toString(i));
                                kvp.setValue(Integer.toString(i), currGene.getName());
                                JButton geneLinkButton = new EntrezButton(currGene.getName());
                                kvp.setAdditionalColumn(Integer.toString(i), 0, geneLinkButton);
                                i++;
                            }
                            currSizeOfArray =i-1;
                        }
                        else {
                           Iterator<String> itr = genemania.getRelatedGeneNamesByScore().iterator();
                           String currGene;
                           itr.next();//skip the first one (it's the name of selected gene already displayed)

                           if (Thread.interrupted()) { throw new InterruptedException(); }

                            int i = 1;
                            while (itr.hasNext()) {
                                currGene = itr.next();
                                kvp.addKey(Integer.toString(i));
                                kvp.setValue(Integer.toString(i), currGene);
                                JButton geneLinkButton = new EntrezButton(currGene);
                                kvp.setAdditionalColumn(Integer.toString(i), 0, geneLinkButton);
                                i++;
                            }
                            currSizeOfArray =i-1;
                        }
                    }
                    else{
                        progressMessage.setText("Gene not found in GeneMANIA");
                        setMsgOff = false;
                    }
                } catch (InterruptedException e){

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
            }
        };
        if(genemaniaAlgorithmThread ==null) {
            genemaniaAlgorithmThread= new Thread(r);
        }else {
            genemaniaAlgorithmThread.interrupt();
            genemaniaAlgorithmThread = new Thread(r);
        }
        //}


        genemaniaAlgorithmThread.start();

    }
}