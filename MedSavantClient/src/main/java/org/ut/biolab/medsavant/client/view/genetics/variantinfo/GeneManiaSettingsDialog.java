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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import org.genemania.type.CombiningMethod;

/**
 *
 * @author khushi
 */
public class GeneManiaSettingsDialog extends JDialog{
    private GenemaniaInfoRetriever genemania;
    private boolean rankByVarFreq;
    private JFrame frame;
    private static final int DEFAULT_RELATED_GENES_LIMIT = 50;
    private final CombiningMethod[] GENEMANIA_COMBINING_METHODS = {CombiningMethod.AVERAGE, CombiningMethod.BP, CombiningMethod.MF, CombiningMethod.CC, CombiningMethod.AUTOMATIC};
    private boolean updateQueryNeeded;
    private JButton okButton;
    public GeneManiaSettingsDialog(GenemaniaInfoRetriever g){
        super();
        genemania = g;
        initialize();
    }

    private void initialize(){

        final JPanel panel = new JPanel();

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
        updateQueryNeeded= false;

        limitTo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        limitTo.setText("Limit to");
        genemaniaScore.setSelected(true);
        rankByVarFreq=false;
        coexp.setSelected(true);
        coloc.setSelected(true);
        spd.setSelected(true);
        gi.setSelected(true);
        pi.setSelected(true);
        pathCheck.setSelected(true);
        other.setSelected(true);
        predict.setSelected(true);
        average.setSelected(true);
        okButton.setEnabled(false);
        relatedGenesLimit.setColumns(3);
        relatedGenesLimit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButton.setEnabled(true);
                genemania.setGeneLimit(Integer.parseInt(relatedGenesLimit.getText()));

            }
        });

        relatedGenes.setText("related genes.");

        rankBy.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rankBy.setText("Rank by");

        ActionListener scoringActionPerformed = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okButton.setEnabled(true);
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
                okButton.setEnabled(true);
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
                okButton.setEnabled(true);
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
                updateQueryNeeded= true;
                setVisible(false);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(28, 28, 28).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(gi).addComponent(coexp)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(pathCheck).addComponent(coloc)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(spd).addComponent(pi)).addGap(10, 10, 10).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(other).addComponent(predict))).addGroup(layout.createSequentialGroup().addGap(20, 20, 20).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(networkWeighting).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(equal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(geneOntology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(queryDependent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(networks).addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(rankBy).addGap(44, 44, 44).addComponent(varFreq).addGap(18, 18, 18).addComponent(genemaniaScore)).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(limitTo).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(relatedGenesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(relatedGenes)))).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(limitTo).addComponent(relatedGenesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(relatedGenes)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(rankBy).addComponent(varFreq).addComponent(genemaniaScore)).addGap(26, 26, 26).addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(networks).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(coexp).addComponent(coloc).addComponent(spd).addComponent(predict)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(gi).addComponent(pathCheck).addComponent(pi).addComponent(other)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(networkWeighting).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(equal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(geneOntology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(queryDependent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(okButton).addContainerGap()));
        add(panel);

    }

    public void showSettings(){
        okButton.setEnabled(false);
        this.setSize(960, 600);
        pack();
        setTitle("GeneMANIA Settings");
        this.setLocationRelativeTo(null);
        setModal(true);
        this.setVisible(true);
    }

    public boolean getRankByVarFreq(){
        return rankByVarFreq;
    }

    public boolean getUpdateQueryNeeded(){
        return updateQueryNeeded;
    }

}
