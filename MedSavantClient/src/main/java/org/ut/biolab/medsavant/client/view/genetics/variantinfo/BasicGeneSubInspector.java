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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.ut.biolab.medsavant.client.api.Listener;

import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import savant.controller.LocationController;
import savant.util.Range;

/**
 *
 * @author mfiume
 */
public class BasicGeneSubInspector extends SubInspector implements Listener<Gene> {

    private static String KEY_NAME = "Name";
    private static String KEY_POSITION = "Position";
    private static String KEY_LENGTH = "Length";
    //private static String KEY_START = "Start";
    //private static String KEY_END = "End";
    //private static String KEY_DESCRIPTION = "Description";
    private KeyValuePairPanel panel;
    private Gene selectedGene;

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

            panel.addKey(KEY_POSITION);
            panel.addKey(KEY_LENGTH);

            JButton genomeBrowserButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BROWSER));
            genomeBrowserButton.setToolTipText("View region in genome browser");
            genomeBrowserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocationController.getInstance().setLocation(selectedGene.getChrom(), new Range((int) (selectedGene.getCodingStart() - 50), (int) (selectedGene.getCodingEnd() + 51)));
                    AppDirectory.launchApp(AppDirectory.BuiltInApp.GENOME_BROWSER);
                }
            });

            panel.setAdditionalColumn(KEY_POSITION, 0, KeyValuePairPanel.getCopyButton(KEY_POSITION, panel));
            panel.setAdditionalColumn(KEY_POSITION, 1, genomeBrowserButton);
        }
        return panel;
    }

    public boolean showHeader() {
        return false;
    }

    @Override
    public void handleEvent(Gene g) {
        if (panel == null) {
            return;
        }
        if (g == null) {
            // TODO show other card
            return;
        }

        selectedGene = g;

        panel.setValue(KEY_NAME, g.getName());
        JButton filterButton2 = new EntrezButton(g.getName());
        panel.setAdditionalColumn(KEY_NAME, 1, filterButton2);
        panel.setValue(KEY_POSITION, g.getChrom() + ":" + ViewUtil.numToString(g.getStart()) + "-" + ViewUtil.numToString(g.getEnd()));
        try {
            String s = ClientMiscUtils.breakString(g.getDescription(), "", 45);
            panel.getComponent(KEY_NAME).setToolTipText(s);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            panel.getComponent(KEY_NAME).setToolTipText("");
        }
        panel.setValue(KEY_LENGTH, MiscUtils.numToStringWithOrder(g.getEnd()-g.getStart()) + "bp");
    }


}