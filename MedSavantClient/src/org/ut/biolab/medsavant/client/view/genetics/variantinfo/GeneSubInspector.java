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

package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import javax.swing.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.ComprehensiveInspector;
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
public class GeneSubInspector extends SubInspector implements Listener<Gene> {

    private static String KEY_NAME = "Name";
    private static String KEY_POSITION = "Position";
    private static String KEY_LENGTH = "Length";
    private KeyValuePairPanel panel;
    private Gene selectedGene;

    public GeneSubInspector() {
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
            panel.addKey(KEY_POSITION);
            panel.addKey(KEY_LENGTH);

            JButton genomeBrowserButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BROWSER));
            genomeBrowserButton.setToolTipText("View region in genome browser");
            genomeBrowserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocationController.getInstance().setLocation(selectedGene.getChrom(), new Range((int) (selectedGene.getCodingStart() - 20), (int) (selectedGene.getCodingEnd() + 21)));
                    ViewController.getInstance().getMenu().switchToSubSection(BrowserPage.getInstance());
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
        panel.setValue(KEY_LENGTH, MiscUtils.numToStringWithOrder(g.getEnd()-g.getStart()) + "bp");

        try {
            String s = ClientMiscUtils.breakString(g.getDescription(), "", 45);
            panel.getComponent(KEY_NAME).setToolTipText(s);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            panel.getComponent(KEY_NAME).setToolTipText("");
        }
    }

}