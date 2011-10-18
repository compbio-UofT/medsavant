/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.manage;

import java.awt.Component;
import java.util.List;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;


/**
 * Section class for organising plugins.
 *
 * @author tarkvara
 */
public class PluginsSection extends SectionView {
    private JPanel[] panels;

    public PluginsSection() {
    }

    @Override
    public String getName() {
        return "Plugins";
    }

    @Override
    public SubSectionView[] getSubSections() {
        PluginController pc = PluginController.getInstance();
        pc.loadPlugins(DirectorySettings.getPluginsDirectory());
        List<PluginDescriptor> knownPlugins = pc.getDescriptors();
        
        PluginPage[] pages = new PluginPage[knownPlugins.size()];
        for (int i = 0; i < pages.length; i++) {
            pages[i] = new PluginPage(this, knownPlugins.get(i));
        }
        return pages;
    }

    @Override
    public JPanel[] getPersistentPanels() {
        return panels;
    }

    @Override
    public Component[] getBanner() {
        return null;
    }
}
