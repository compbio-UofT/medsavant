/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.view.genetics;

import org.ut.biolab.medsavant.view.genetics.family.FamilyMattersPage;
import org.ut.biolab.medsavant.aggregate.AggregatePage;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.variant.ExportVCFWizard;
import org.ut.biolab.medsavant.view.manage.PluginPage;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionViewCollection;
import org.ut.biolab.medsavant.view.variants.BrowserPage;


/**
 *
 * @author mfiume
 */
public class GeneticsSection extends SectionView {

    public static boolean isInitialized = false;
    private JPanel[] persistencePanels;

    public GeneticsSection() {
        super("Variants");
        getSectionMenuComponents(); // force banner to be active, in turn forcing default reference selection
    }

    @Override
    public SubSectionView[] getSubSections() {

        SubSectionViewCollection variantCollectionPlugins = new SubSectionViewCollection(this, "Plugins");

        PluginController pc = PluginController.getInstance();
        pc.loadPlugins(DirectorySettings.getPluginsDirectory());
        pc.getGeneManiaData();
        List<PluginDescriptor> knownPlugins = pc.getDescriptorsOfType(PluginDescriptor.Type.SECTION);
        for (int i = 0; i < knownPlugins.size(); i++) {
            variantCollectionPlugins.addSubSectionView(new PluginPage(this, knownPlugins.get(i)));
        }

        return new SubSectionView[] {
            new GeneticsTablePage(this),
            new BrowserPage(this),
            new GeneticsChartPage(this),
            new AggregatePage(this),
            new FamilyMattersPage(this)
                //,variantCollectionPlugins
        };
    }

    @Override
    public JPanel[] getPersistentPanels() {
        if (persistencePanels == null) {
            persistencePanels = new JPanel[] {
                new GeneticsFilterPage(this).getView(true)
            };
        }
        return persistencePanels;
    }

    private JButton createExportVCFButton(){
        JButton exportButton = new JButton("Export Variants");
        exportButton.setOpaque(false);
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new ExportVCFWizard().setVisible(true);
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to launch Variant Export wizard: %s", ex);
                }
            }
        });
        return exportButton;
    }

    @Override
    public final Component[] getSectionMenuComponents() {
        isInitialized = true;
        return new Component[] { createExportVCFButton() };
    }
}