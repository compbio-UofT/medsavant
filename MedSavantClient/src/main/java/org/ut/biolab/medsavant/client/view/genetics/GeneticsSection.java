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
package org.ut.biolab.medsavant.client.view.genetics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.login.LoginController;

import org.ut.biolab.medsavant.client.plugin.PluginController;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.variant.ExportVCFWizard;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.manage.PluginPage;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionViewCollection;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.variants.BrowserPage;

/**
 *
 * @author mfiume
 */
public class GeneticsSection extends SectionView {

    private static final Log LOG = LogFactory.getLog(GeneticsSection.class);

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
        //pc.loadPlugins(DirectorySettings.getPluginsDirectory());
        List<AppDescriptor> knownPlugins = pc.getDescriptorsOfType(AppDescriptor.Type.VARIANT);

        SubSectionView[] appSections = new SubSectionView[knownPlugins.size()];
        for (int i = 0; i < knownPlugins.size(); i++) {
            try {
                appSections[i] = new PluginPage(this, knownPlugins.get(i));
            } catch (Exception e) {
                LOG.error(e);
            }
        }

        SubSectionView[] builtInSections = new SubSectionView[] { new GeneticsTablePage(this),
                    new BrowserPage(this),
                    new GeneticsChartPage(this)};

        return ArrayUtils.addAll(builtInSections, appSections);
    }

    @Override
    public JPanel[] getPersistentPanels() {
        if (persistencePanels == null) {
            persistencePanels = new JPanel[]{
                new GeneticsFilterPage(this).getView()
            };
        }
        return persistencePanels;
    }

    private JButton createExportVCFButton() {
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
        return new Component[] {
                    //createExportVCFButton()
                };
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_SEARCH);
    }
}