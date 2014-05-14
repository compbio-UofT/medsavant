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

import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.stat.StaticInspectorPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import savant.controller.LocationController;
import savant.util.Range;

/**
 *
 * @author mfiume
 */
public class SimpleVariantSubInspector extends SubInspector {

    private Listener<Object> geneListener;
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

    public SimpleVariantSubInspector() {
    }

    public SimpleVariant getSimpleVariant() {
        return selectedVariant;
    }

    public Gene getSelectedGene() {
        return (Gene) geneBox.getSelectedItem();
    }

    public void setGeneListener(Listener<Object> listener) {
        this.geneListener = listener;
    }

    @Override
    public String getName() {
        return "Basic Variant Information";
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
            Dimension d = new Dimension(150, 22);
            geneBox.setMinimumSize(d);
            geneBox.setPreferredSize(d);
            geneBox.setMaximumSize(d);
            ViewUtil.makeSmall(geneBox);
            p.setValue(KEY_GENES, geneBox);

            JButton genomeBrowserButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BROWSER));
            genomeBrowserButton.setToolTipText("View region in genome browser");
            genomeBrowserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocationController.getInstance().setLocation(selectedVariant.getChromosome(), new Range((int) (selectedVariant.getStartPosition() - 20), (int) (selectedVariant.getEndPosition() + 21)));
                    AppDirectory.launchApp(AppDirectory.BuiltInApp.GENOME_BROWSER);
                }
            });


            JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
            geneInspectorButton.setToolTipText("Inspect this gene");

            geneInspectorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (geneListener != null) {
                        geneListener.handleEvent((Gene) geneBox.getSelectedItem());
                    }
                }
            });

            p.setAdditionalColumn(KEY_GENES, 0, geneInspectorButton);

            JLabel l = new JLabel("This will eventually show a chart");

            int col = 0;

            p.setAdditionalColumn(KEY_POSITION, col, KeyValuePairPanel.getCopyButton(KEY_POSITION, p));

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

    private void generateGeneIntersections(SimpleVariant r) {
        try {

            if (genes == null) {
                genes = GeneSetController.getInstance().getCurrentGenes();
            }

            JComboBox b = geneBox;
            b.removeAllItems();

            b.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus) {

                    if (value != null) {
                        Gene g = (Gene) value;
                        value = g.getName() + "  " + MiscUtils.numToStringWithOrder(g.getEnd() - g.getStart()) + "bp";
                    }
                    return super.getListCellRendererComponent(list, value,
                            index, isSelected, cellHasFocus);
                }
            });

            List<Gene> intersectingGenes = new ArrayList<Gene>();

            for (Gene g : genes) {
                if (MiscUtils.homogenizeSequence(g.getChrom()).equals(MiscUtils.homogenizeSequence(r.getChromosome()))
                        && MiscUtils.doesIntersect((int)r.getStartPosition(), (int)r.getEndPosition(), g.getStart(), g.getEnd())){
                    intersectingGenes.add(g);
                }
            }

            Collections.sort(intersectingGenes, new Comparator<Gene>() {
                @Override
                public int compare(Gene t, Gene t1) {
                    int l = Math.abs(t.getEnd() - t.getStart());
                    int l1 = Math.abs(t1.getEnd() - t1.getStart());
                    return new Integer(l).compareTo(new Integer(l1));
                }
            });

            for (Gene g : intersectingGenes) {
                b.addItem(g);
            }

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

    public void setSimpleVariant(SimpleVariant r) {
        if (p == null) {
            return;
        }

        selectedVariant = r;
        
        String posVal = r.getChromosome() + ":" + ViewUtil.numToString(r.getStartPosition())+" - "+r.getEndPosition();
        p.setValue(KEY_POSITION, posVal);
        p.setValue(KEY_REF, r.getReference());
        p.setValue(KEY_ALT, r.getAlternate());

        p.setValue(KEY_TYPE, checkNull(r.getType()));

        p.simpleEllipsify();
              //     p.ellipsifyValues(StaticInspectorPanel.INSPECTOR_INNER_WIDTH);
        generateGeneIntersections(r);
    }
}