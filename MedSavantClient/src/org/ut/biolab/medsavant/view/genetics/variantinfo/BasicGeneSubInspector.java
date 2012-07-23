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

import javax.swing.*;

import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class BasicGeneSubInspector extends SubInspector implements GeneSelectionChangedListener, VariantSelectionChangedListener {

    private static String KEY_NAME = "Name";
    private static String KEY_CHROM = "Chrom";
    private static String KEY_START = "Start";
    private static String KEY_END = "End";
    private KeyValuePairPanel panel;

    public BasicGeneSubInspector() {
        //VariantInspector.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Gene Details";
    }
    static String charset = "UTF-8";

    @Override
    public JPanel getInfoPanel() {
        if (panel == null) {
            panel = new KeyValuePairPanel(2);
            panel.addKey(KEY_NAME);

            /*JButton filterButton2 = ViewUtil.getTexturedButton("Card", IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));
            filterButton2.setToolTipText("Lookup Gene Card");
            panel.setAdditionalColumn(KEY_NAME, 1, filterButton2);

            filterButton2.addActionListener(new ActionListener() {

                String baseUrl = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=";

                @Override
                public void actionPerformed(ActionEvent ae) {
                    try {
                        String geneName = panel.getValue(KEY_NAME);
                        URL url = new URL(baseUrl + URLEncoder.encode(geneName, charset));

                        java.awt.Desktop.getDesktop().browse(url.toURI());
                    } catch (Exception ex) {
                        DialogUtils.displayError("Problem launching website.");
                    }
                }
            });*/

            panel.addKey(KEY_CHROM);
            panel.addKey(KEY_START);
            panel.addKey(KEY_END);
        }
        return panel;
    }

    public boolean showHeader() {
        return false;
    }

    @Override
    public void geneSelectionChanged(Gene g) {
        if (panel == null) {
            return;
        }
        if (g == null) {
            // TODO show other card
            return;
        }

        panel.setValue(KEY_NAME, g.getName());
        JButton filterButton2 = new EntrezButton(g.getName());
        panel.setAdditionalColumn(KEY_NAME, 1, filterButton2);
        panel.setValue(KEY_CHROM, g.getChrom());
        panel.setValue(KEY_START, ViewUtil.numToString(g.getStart()));
        panel.setValue(KEY_END, ViewUtil.numToString(g.getEnd()));
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}