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
package org.ut.biolab.medsavant.client.view.manage;

import java.util.List;
import javax.swing.ImageIcon;

import org.ut.biolab.medsavant.client.plugin.PluginController;
import org.ut.biolab.medsavant.client.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;

/**
 * Section class for organising plugins.
 *
 * @author tarkvara
 */
public class PluginsSection extends SectionView {

    public PluginsSection() {
        super("Plugins");
    }

    @Override
    public SubSectionView[] getSubSections() {
        PluginController pc = PluginController.getInstance();
        pc.loadPlugins(DirectorySettings.getPluginsDirectory());
        List<PluginDescriptor> knownPlugins = pc.getDescriptorsOfType(PluginDescriptor.Type.SECTION);

        PluginPage[] pages = new PluginPage[knownPlugins.size()];
        for (int i = 0; i < pages.length; i++) {
            pages[i] = new PluginPage(this, knownPlugins.get(i));
        }
        return pages;
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_OTHER);
    }
}
