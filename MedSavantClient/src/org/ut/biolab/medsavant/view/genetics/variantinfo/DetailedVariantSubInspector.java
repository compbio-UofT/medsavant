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

import org.ut.biolab.medsavant.view.genetics.inspector.SubInspector;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import javax.swing.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.geneset.GeneSetController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.inspector.InspectorController;
import org.ut.biolab.medsavant.view.genetics.inspector.stat.StaticGeneInspector;
import org.ut.biolab.medsavant.view.genetics.inspector.stat.StaticInspectorPanel;
import org.ut.biolab.medsavant.view.genetics.inspector.stat.StaticVariantInspector;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.variants.BrowserPage;
import savant.api.data.DataFormat;
import savant.controller.LocationController;
import savant.util.Range;


/**
 *
 * @author mfiume
 */
public class DetailedVariantSubInspector extends SubInspector {

    private static final String KEY_POSITION = "Position";
    private static final String KEY_GENES = "Genes";
    private static final String KEY_REF = "Reference";
    private static final String KEY_ALT = "Alternate";
    private static final String KEY_TYPE = "Type";
    private static final String URL_CHARSET = "UTF-8";

    private Collection<Gene> genes;
    private KeyValuePairPanel p;
    private JComboBox geneBox;
    private SimpleVariant selectedVariant;

    public DetailedVariantSubInspector(InspectorController c) {
        super(c);
    }

    @Override
    public String getName() {
        return "Detailed Variant Information";
    }

    @Override
    public JPanel getInfoPanel() {
        if (p == null) {
            p = new KeyValuePairPanel(4);
            p.addKey(KEY_POSITION);
            p.addKey(KEY_REF);
            p.addKey(KEY_ALT);
            p.addKey(KEY_TYPE);
            p.addKey(KEY_GENES);

            geneBox = new JComboBox();
            ViewUtil.makeSmall(geneBox);
            p.setValue(KEY_GENES, geneBox);

            JButton genomeBrowserButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BROWSER));
            genomeBrowserButton.setToolTipText("View region in genome browser");
            genomeBrowserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocationController.getInstance().setLocation(selectedVariant.chr, new Range((int) (selectedVariant.pos - 20), (int) (selectedVariant.pos + 21)));
                    ViewController.getInstance().getMenu().switchToSubSection(BrowserPage.getInstance());
                }
            });


            JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
            geneInspectorButton.setToolTipText("Inspect this gene");
            geneInspectorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    StaticGeneInspector.getInstance().setGene((Gene) (geneBox).getSelectedItem());
                    StaticInspectorPanel.getInstance().switchToGeneInspector();
                }
            });

            p.setAdditionalColumn(KEY_GENES, 0, geneInspectorButton);

            JLabel l = new JLabel("This will eventually show a chart");

            int col = 0;

            p.setAdditionalColumn(KEY_POSITION, col, KeyValuePairPanel.getCopyButton(KEY_POSITION,p));

            col++;
            p.setAdditionalColumn(KEY_POSITION, col, genomeBrowserButton);

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

    public void setSimpleVariant(SimpleVariant r) {
        if (p == null) {
            return;
        }
        if (r == null) {
            // TODO show other card
            return;
        }

        selectedVariant = r;

        p.setValue(KEY_POSITION, r.chr + ":"  + ViewUtil.numToString(r.pos));
        p.setValue(KEY_REF, r.ref);
        p.setValue(KEY_ALT, r.alt);

        p.setValue(KEY_TYPE, checkNull(r.type));

        p.ellipsifyValues(StaticInspectorPanel.INSPECTOR_INNER_WIDTH);

        generateGeneIntersections(r);
    }

    private void generateGeneIntersections(SimpleVariant r) {
        try {

            if (genes == null) {
                genes = GeneSetController.getInstance().getCurrentGenes();
            }

            Gene g0 = null;
            JComboBox b = geneBox;
            b.removeAllItems();

            for (Gene g : genes) {
                if (g0 == null) {
                    g0 = g;
                }
                if (g.getChrom().equals(r.chr) && r.pos > g.getStart() && r.pos < g.getEnd()) {
                    b.addItem(g);
                }
            }

            /*if (g0 != null) {
             GeneInspector.getInstance().setGene(g0);
             }
             */
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching genes: %s", ex);
        }
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
                    URL url = new URL(baseUrl + URLEncoder.encode(p.getValue(key), URL_CHARSET));
                    java.awt.Desktop.getDesktop().browse(url.toURI());
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Problem launching NCBI website: %s", ex);
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