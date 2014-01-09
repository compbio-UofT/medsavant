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
package org.ut.biolab.medsavant.client.view.genetics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.MedSavantVariantSectionApp;

import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.variant.ExportVCFWizard;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.app.settings.PluginPage;
import org.ut.biolab.medsavant.client.view.subview.SubSection;
import org.ut.biolab.medsavant.client.view.subview.MultiSection;
import org.ut.biolab.medsavant.client.view.subview.SubSectionViewCollection;
import org.ut.biolab.medsavant.client.view.variants.BrowserPage;

/**
 *
 * @author mfiume
 */
public class GeneticsSection extends MultiSection {

    private static final Log LOG = LogFactory.getLog(GeneticsSection.class);
    public static boolean isInitialized = false;
    private JPanel[] persistencePanels;

    public GeneticsSection() {
        super("Variants");
        getSectionMenuComponents(); // force banner to be active, in turn forcing default reference selection
    }

    @Override
    public SubSection[] getSubSections() {

        SubSectionViewCollection variantCollectionPlugins = new SubSectionViewCollection(this, "Plugins");

        AppController pc = AppController.getInstance();
        //pc.loadPlugins(DirectorySettings.getPluginsDirectory());
        //List<AppDescriptor> knownPlugins = pc.getDescriptorsOfType(AppDescriptor.Category.VARIANT);

        //SubSectionView[] appSections = new SubSectionView[knownPlugins.size()];

        List<SubSection> appSections = new LinkedList<SubSection>();
        //for (int i = 0; i < knownPlugins.size(); i++) {

        List<MedSavantApp> variantSectionApps = AppController.getInstance().getPluginsOfClass(MedSavantVariantSectionApp.class);

        int numApps = variantSectionApps.size();

        for (MedSavantApp app : variantSectionApps) {
            appSections.add(new PluginPage(this, (MedSavantVariantSectionApp)app));
        }

        SubSection[] builtInSections = new SubSection[]{new SpreadsheetPage(this),
            //new BrowserPage(this),
            new GeneticsChartPage(this)};

        return ArrayUtils.addAll(builtInSections, appSections.toArray(new SubSection[numApps]));
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
        return new Component[]{ //createExportVCFButton()
        };
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_SEARCH);
    }
}