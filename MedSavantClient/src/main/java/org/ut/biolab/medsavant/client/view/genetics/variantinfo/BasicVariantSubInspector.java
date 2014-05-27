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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommHandler;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommRegistry;
import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.BAMFileComm;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Set;
import javax.swing.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.stat.StaticGeneInspector;
import org.ut.biolab.medsavant.client.view.genetics.inspector.stat.StaticInspectorPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.stat.StaticVariantInspector;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.variants.BrowserPage;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import savant.api.data.DataFormat;
import savant.controller.LocationController;
import savant.util.Range;

/**
 *
 * @author mfiume
 */
public class BasicVariantSubInspector extends SubInspector implements Listener<VariantRecord> {

    private static final String KEY_DNAID = "DNA ID";
    private static final String KEY_START_POSITION = "Start Position";
    private static final String KEY_GENES = "Genes";
    private static final String KEY_REF = "Reference";
    private static final String KEY_ALT = "Alternate";
    private static final String KEY_QUAL = "Quality";
    private static final String KEY_DBSNP = "dbSNP ID";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_ZYGOSITY = "Zygosity";
    private static final String KEY_INFO = "Info";
    private static final String URL_CHARSET = "UTF-8";
    private Collection<Gene> genes;
    private KeyValuePairPanel p;
    private JComboBox geneBox;
    private VariantRecord selectedVariant;

    @SuppressWarnings("LeakingThisInConstructor")
    public BasicVariantSubInspector() {
        StaticVariantInspector.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Variant Details";
    }

    @Override
    public JPanel getInfoPanel() {
        if (p == null) {
            p = new KeyValuePairPanel(4);
            p.addKey(KEY_DNAID);
            p.addKey(KEY_START_POSITION);
            p.addKey(KEY_REF);
            p.addKey(KEY_ALT);
            p.addKey(KEY_TYPE);
            p.addKey(KEY_ZYGOSITY);
            p.addKey(KEY_QUAL);
            p.addKey(KEY_DBSNP);

            p.addKey(KEY_GENES);
            p.addKey(KEY_INFO);

            geneBox = new JComboBox();
            ViewUtil.makeSmall(geneBox);
            p.setValue(KEY_GENES, geneBox);

            JButton genomeBrowserButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BROWSER));
            genomeBrowserButton.setToolTipText("View region in genome browser");
            genomeBrowserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocationController.getInstance().setLocation(selectedVariant.getChrom(), new Range((int) (selectedVariant.getStartPosition() - 20), (int) (selectedVariant.getEndPosition() + 21)));
                    AppDirectory.launchApp(AppDirectory.BuiltInApp.GENOME_BROWSER);
                }
            });

            JButton bamButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BAMFILE));
            bamButton.setToolTipText("<html>Load read alignments for this<br/> sample in genome browser</html>");
            bamButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {

                    Set<AppCommHandler> handlers = AppCommRegistry.getInstance().getHandlersForEvent(BAMFileComm.class);
                    if (handlers.isEmpty()) {
                        return;
                    }
                    
                    String dnaID = selectedVariant.getDnaID();
                    String bamPath;
                    try {
                        bamPath = MedSavantClient.PatientManager.getReadAlignmentPathForDNAID(
                                LoginController.getSessionID(),
                                ProjectController.getInstance().getCurrentProjectID(),
                                dnaID);
                        if (bamPath != null && !bamPath.equals("")) {
                            /*int response = DialogUtils.askYesNo("Load Read Alignments",
                             "<html>The read alignments for this sample<br>"
                             + "are available. Would you like to load them<br>"
                             + "as a track in the genome browser?</html>");*/
                            int response = DialogUtils.YES;
                            if (response == DialogUtils.YES) {
                                AppCommHandler<BAMFileComm> handler = handlers.iterator().next();
                                handler.handleCommEvent(new BAMFileComm(null, new URL(bamPath)));
                                //BrowserPage.getInstance().addTrackFromURLString(bamPath, DataFormat.ALIGNMENT);
                                //DialogUtils.displayMessage("Read alignments have been loaded into Browser.  Click 'Browser' at left to view.");
                            }
                        }

                    } catch (Exception ex) {
                        ClientMiscUtils.reportError("Unable to load BAM file: %s", ex);
                    }
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

            //JLabel l = new JLabel("This will eventually show a chart");
            //p.setDetailComponent(KEY_QUAL, l);
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

            p.setAdditionalColumn(KEY_DNAID, col, KeyValuePairPanel.getCopyButton(KEY_DNAID, p));
            p.setAdditionalColumn(KEY_DBSNP, col, KeyValuePairPanel.getCopyButton(KEY_DBSNP, p));
            p.setAdditionalColumn(KEY_START_POSITION, col, KeyValuePairPanel.getCopyButton(KEY_START_POSITION, p));
            //p.setAdditionalColumn(KEY_END_POSITION, col, KeyValuePairPanel.getCopyButton(KEY_END_POSITION,p));
            //p.setAdditionalColumn(KEY_QUAL, col, getChartButton(KEY_QUAL));

            col++;
            p.setAdditionalColumn(KEY_DBSNP, col, getNCBIButton(KEY_DBSNP));
            p.setAdditionalColumn(KEY_START_POSITION, col, genomeBrowserButton);
            p.setAdditionalColumn(KEY_DNAID, col, bamButton);

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
    public void handleEvent(VariantRecord r) {
        if (p == null) {
            return;
        }
        if (r == null) {
            // TODO show other card
            return;
        }

        selectedVariant = r;

        p.setValue(KEY_DNAID, r.getDnaID());
        if (r.getStartPosition() == r.getEndPosition()) {
            p.setValue(KEY_START_POSITION, r.getChrom() + ":" + ViewUtil.numToString(r.getStartPosition()));
        } else {
            p.setValue(KEY_START_POSITION, r.getChrom() + ":" + ViewUtil.numToString(r.getStartPosition()));
        }
        p.setValue(KEY_REF, r.getRef());
        p.setValue(KEY_ALT, r.getAlt());

        p.setValue(KEY_TYPE, checkNull(r.getType()));
        p.setValue(KEY_ZYGOSITY, checkNull(r.getZygosity()));

        p.setValue(KEY_QUAL, ViewUtil.numToString(r.getQual()));
        p.setValue(KEY_DBSNP, checkNull(r.getDbSNPID()));
        p.ellipsifyValues(StaticInspectorPanel.INSPECTOR_INNER_WIDTH);

        KeyValuePairPanel infoPanel = getInfoKVPPanel(r.getCustomInfo());
        infoPanel.ellipsifyValues(StaticInspectorPanel.INSPECTOR_INNER_WIDTH);
        p.setDetailComponent(KEY_INFO, infoPanel);

        try {
            String bamPath = MedSavantClient.PatientManager.getReadAlignmentPathForDNAID(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    r.getDnaID());

            JButton bamButton = (JButton) p.getAdditionalColumn(KEY_DNAID, 1);
            if (bamPath != null && !bamPath.equals("")) {
                bamButton.setVisible(true);
            } else {
                bamButton.setVisible(false);
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to get BAM path for DNA ID: %s", ex);
        }

        generateGeneIntersections(r);

    }

    private void generateGeneIntersections(VariantRecord r) {
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
                if (g.getChrom().equals(r.getChrom())
                        && MiscUtils.doesIntersect(r.getStartPosition().intValue(), r.getEndPosition().intValue(), g.getStart(), g.getEnd())) {
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
