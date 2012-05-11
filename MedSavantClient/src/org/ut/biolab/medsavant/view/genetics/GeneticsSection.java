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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.view.dialog.ExportVcfWizard;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.manage.PluginPage;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionViewCollection;


/**
 *
 * @author mfiume
 */
public class GeneticsSection extends SectionView implements ProjectListener {

    private static final Log LOG = LogFactory.getLog(GeneticsSection.class);

    private JPanel[] panels;
    private JComboBox referenceDropDown;
    public static boolean isInitialized = false;

    public GeneticsSection() {
        getSectionMenuComponents(); // force banner to be active, in turn forcing default reference selection
        System.out.println("Done making genetics section");
    }

    @Override
    public String getName() {
        return "Variants";
    }

    @Override
    public Icon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_VARIANTS);
    }

    @Override
    public SubSectionView[] getSubSections() {
        SubSectionView[] pages = new SubSectionView[4];

        SubSectionViewCollection variantCollectionPlugins = new SubSectionViewCollection(this,"Plugins");

        PluginController pc = PluginController.getInstance();
        pc.loadPlugins(DirectorySettings.getPluginsDirectory());
        List<PluginDescriptor> knownPlugins = pc.getDescriptorsOfType(PluginDescriptor.Type.SECTION);
        for (int i = 0; i < knownPlugins.size(); i++) {
            variantCollectionPlugins.addSubSectionView(new PluginPage(this, knownPlugins.get(i)));
        }

        pages[0] = new GeneticsTablePage(this);
        pages[1] = new GeneticsChartPage(this);
        pages[2] = new AggregatePage(this);
        pages[3] = variantCollectionPlugins;

        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        panels = new JPanel[1];
        panels[0] = new GeneticsFilterPage(this).getView(true);

        return panels;
    }

    private JButton exportVcfButton(){
        JButton exportButton = new JButton("Export Variants");
        exportButton.setOpaque(false);
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ExportVcfWizard();
            }
        });
        return exportButton;
    }

    @Override
    public final Component[] getSectionMenuComponents() {

        Component[] result = new Component[1];

        result[0] = exportVcfButton();

        isInitialized = true;
        return result;
    }

    private Component getReferenceDropDown() {
        referenceDropDown = new JComboBox();

        referenceDropDown.setMinimumSize(new Dimension(200, 23));
        referenceDropDown.setPreferredSize(new Dimension(200, 23));
        referenceDropDown.setMaximumSize(new Dimension(200, 23));
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            LOG.error("Error refreshing reference drop-down.", ex);
        }

        ProjectController.getInstance().addProjectListener(this);

        return referenceDropDown;
    }

    private void refreshReferenceDropDown() throws RemoteException {
        try {
            for (ActionListener l : referenceDropDown.getActionListeners()) {
                referenceDropDown.removeActionListener(l);
            }
            referenceDropDown.removeAllItems();

            List<String> references = MedSavantClient.ReferenceQueryUtilAdapter.getReferencesForProject(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId());

            for (String refname : references) {

                int refid = ReferenceController.getInstance().getReferenceId(refname);

                int numVariantsInTable = ProjectController.getInstance().getNumVariantsInTable(ProjectController.getInstance().getCurrentProjectId(), refid);

                referenceDropDown.addItem(refname); // + " (" + numVariantsInTable + " variants)");
            }

            if (references.isEmpty()) {
                referenceDropDown.addItem("No References");
                referenceDropDown.setEnabled(false);
            } else {
                referenceDropDown.setEnabled(true);
                referenceDropDown.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String currentName = ReferenceController.getInstance().getCurrentReferenceName();
                        if (!ReferenceController.getInstance().setReference((String) referenceDropDown.getSelectedItem(), true)) {
                            referenceDropDown.setSelectedItem(currentName);
                        }
                    }
                });
                ReferenceController.getInstance().setReference((String) referenceDropDown.getSelectedItem());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void projectAdded(String projectName) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            LOG.error("Error refreshing reference drop-down.", ex);
        }
    }

    @Override
    public void projectRemoved(String projectName) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            LOG.error("Error refreshing reference drop-down.", ex);
        }
    }

    @Override
    public void projectChanged(String projectName) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            LOG.error("Error refreshing reference drop-down.", ex);
        }
    }

    @Override
    public void projectTableRemoved(int projid, int refid) {
        try {
            refreshReferenceDropDown();
        } catch (RemoteException ex) {
            LOG.error("Error refreshing reference drop-down.", ex);
        }
    }
}
