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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.*;

import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.GeneIntersectionGenerator;
import org.ut.biolab.medsavant.view.genetics.inspector.VariantInspector;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class BasicVariantSubInspector extends SubInspector implements VariantSelectionChangedListener {

    private static String KEY_DNAID = "DNA ID";
    private static String KEY_CHROM = "Chrom";
    private static String KEY_POSITION = "Position";
    private static String KEY_GENES = "Genes";
    private static String KEY_REF = "Reference";
    private static String KEY_ALT = "Alternate";
    private static String KEY_QUAL = "Quality";
    private static String KEY_DBSNP = "dbSNP ID";
    private static String KEY_TYPE = "Type";
    private static String KEY_ZYGOSITY = "Zygosity";


    private static String KEY_INFO = "Info";

    // VCF 4.1 Info fields
    /*
    private static String KEY_AA = "Anc. Allele";
    private static String KEY_AC = "Allele Count";
    private static String KEY_AF = "Allele Freq.";
    private static String KEY_AN = "Num. Alleles";
    private static String KEY_BQ = "Base Quality";
    private static String KEY_CIGAR = "CIGAR";
    private static String KEY_DB = "In dbSNP";
    private static String KEY_DP = "Coverage";
    private static String KEY_END = "End Position";
    private static String KEY_H2 = "In Hapmap2";
    private static String KEY_H3 = "In Hapmap3";
    private static String KEY_MQ = "Mapping Qual.";
    private static String KEY_MQ0 = "Num. MQ0s";
    private static String KEY_NS = "Num. Samples";
    private static String KEY_SB = "Strand Bias";
    private static String KEY_SOMATIC = "Somatic";
    private static String KEY_VALIDATED = "Validated";
    private static String KEY_1000G = "In 1K Genomes";
    *
    */


    private KeyValuePairPanel p;

    public BasicVariantSubInspector() {
        VariantInspector.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Variant Details";
    }
    static String charset = "UTF-8";

    @Override
    public JPanel getInfoPanel() {
        if (p == null) {
            p = new KeyValuePairPanel(4);
            p.addKey(KEY_DNAID);
            p.addKey(KEY_CHROM);
            p.addKey(KEY_POSITION);
            p.addKey(KEY_REF);
            p.addKey(KEY_ALT);
            p.addKey(KEY_TYPE);
            p.addKey(KEY_ZYGOSITY);
            p.addKey(KEY_QUAL);
            p.addKey(KEY_DBSNP);

            p.addKey(KEY_GENES);
            p.addKey(KEY_INFO);

            JComboBox geneBox = GeneIntersectionGenerator.getInstance().getGeneDropDown();
            ViewUtil.makeSmall(geneBox);
            int geneDropdownWidth = 110;
            geneBox.setMinimumSize(new Dimension(geneDropdownWidth, 30));
            geneBox.setPreferredSize(new Dimension(geneDropdownWidth, 30));
            geneBox.setMaximumSize(new Dimension(geneDropdownWidth, 30));
            p.setValue(KEY_GENES, geneBox);

            JLabel l = new JLabel("This will eventually show a chart");
            p.setDetailComponent(KEY_QUAL, l);


            final JToggleButton button = ViewUtil.getTexturedToggleButton("SHOW");
            ViewUtil.makeSmall(button);
            button.setToolTipText("Toggle Info");
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    p.toggleDetailVisibility(KEY_INFO);
                    button.setText(button.isSelected() ? "HIDE" : "SHOW");
                }
            });
            p.setValue(KEY_INFO, button);


            int col = 0;

            p.setAdditionalColumn(KEY_DNAID, col, getCopyButton(KEY_DNAID));
            p.setAdditionalColumn(KEY_DBSNP, col, getCopyButton(KEY_DBSNP));
            p.setAdditionalColumn(KEY_QUAL, col, getChartButton(KEY_QUAL));

            col++;
            p.setAdditionalColumn(KEY_DBSNP, col, getNCBIButton(KEY_DBSNP));

        }
        return p;
    }

    public boolean showHeader() {
        return false;
    }

    private String checkNull(Object o) {
        if (o == null) {
            return KeyValuePairPanel.NULL_VALUE;
        }
        String s = o.toString();
        if (s.equals("")) {
            return KeyValuePairPanel.NULL_VALUE;
        }

        return s;
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        if (p == null) {
            return;
        }
        if (r == null) {
            // TODO show other card
            return;
        }

        p.setValue(KEY_DNAID, r.getDnaID());
        p.setValue(KEY_CHROM, r.getChrom());
        p.setValue(KEY_POSITION, ViewUtil.numToString(r.getPosition()));
        p.setValue(KEY_REF, r.getRef());
        p.setValue(KEY_ALT, r.getAlt());

        p.setValue(KEY_TYPE, checkNull(r.getType()));
        p.setValue(KEY_ZYGOSITY, checkNull(r.getZygosity()));

        p.setValue(KEY_QUAL, ViewUtil.numToString(r.getQual()));
        p.setValue(KEY_DBSNP, checkNull(r.getDbSNPID()));

        p.setDetailComponent(KEY_INFO, getInfoKVPPanel(r.getCustomInfo()));

        /*

        p.setValue(KEY_AA, checkNull(r.getAncestralAllele()));
        p.setValue(KEY_AC, checkNull(r.getAlleleCount()));
        p.setValue(KEY_AF, checkNull(r.getAlleleFrequency()));
        p.setValue(KEY_AN, checkNull(r.getNumberOfAlleles()));
        p.setValue(KEY_BQ, checkNull(r.getBaseQuality()));
        p.setValue(KEY_CIGAR, checkNull(r.getCigar()));
        p.setValue(KEY_DB, checkNull(r.getDbSNPMembership()));
        p.setValue(KEY_DP, checkNull(r.getDepthOfCoverage()));
        p.setValue(KEY_END, checkNull(r.getEndPosition()));
        p.setValue(KEY_H2, checkNull(r.getHapmap2Membership()));
        p.setValue(KEY_H3, checkNull(r.getHapmap3Membership()));
        p.setValue(KEY_MQ, checkNull(r.getMappingQuality()));
        p.setValue(KEY_MQ0, checkNull(r.getNumberOfZeroMQ()));
        p.setValue(KEY_NS, checkNull(r.getNumberOfSamplesWithData()));
        p.setValue(KEY_SB, checkNull(r.getStrandBias()));
        p.setValue(KEY_SOMATIC, checkNull(r.getIsSomatic()));
        p.setValue(KEY_VALIDATED, checkNull(r.getIsValidated()));
        p.setValue(KEY_1000G, checkNull(r.getIsInThousandGenomes()));
        *
        */

    }

    private Component getCopyButton(final String key) {
        JButton button = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COPY));
        button.setToolTipText("Copy " + key);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String selection = p.getValue(key);
                StringSelection data = new StringSelection(selection);
                Clipboard clipboard =
                        Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
                DialogUtils.displayMessage("Copied \"" + selection + "\" to clipboard.");
            }
        });
        return button;
    }

    private Component getFilterButton(final String key) {

        JButton button = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FILTER));
        button.setToolTipText("Filter " + key);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
            }
        });
        return button;
    }

    private Component getChartButton(final String key) {
        final JToggleButton button = ViewUtil.getTexturedToggleButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CHART_SMALL));
        button.setToolTipText("Chart " + key);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                p.toggleDetailVisibility(key);
            }
        });
        return button;
    }

    private Component getNCBIButton(final String key) {
        JButton ncbiButton = ViewUtil.getTexturedButton("", IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));

        //LinkButton ncbiButton = new LinkButton("NCBI");
        ncbiButton.setToolTipText("Lookup " + key + " at NCBI");
        ncbiButton.addActionListener(new ActionListener() {

            String baseUrl = "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URL url = new URL(baseUrl + URLEncoder.encode(p.getValue(key), charset));
                    java.awt.Desktop.getDesktop().browse(url.toURI());
                } catch (Exception ex) {
                    DialogUtils.displayError("Problem launching website.");
                }
            }
        });

        return ncbiButton;
    }

    private KeyValuePairPanel getInfoKVPPanel(String customInfo) {

        String[] pairs = customInfo.split(";");

        KeyValuePairPanel kvp = new KeyValuePairPanel();

        for (String pair : pairs) {
            String[] splitPair = pair.split("=");

            if (splitPair.length == 2) {
                kvp.addKey(splitPair[0]);
                kvp.setValue(splitPair[0], splitPair[1]);
            }

        }

        return kvp;
    }
}