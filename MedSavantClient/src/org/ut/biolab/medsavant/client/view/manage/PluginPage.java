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

package org.ut.biolab.medsavant.client.view.manage;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.api.MedSavantSectionPlugin;
import org.ut.biolab.medsavant.client.plugin.PluginController;
import org.ut.biolab.medsavant.client.plugin.PluginEvent;
import org.ut.biolab.medsavant.client.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.client.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 * Page which a plugin can use to present its UI.
 *
 * @author tarkvara
 */
public class PluginPage extends SubSectionView {
    private static final Log LOG = LogFactory.getLog(PluginPage.class);
    private static PluginController controller = PluginController.getInstance();

    private MedSavantSectionPlugin plugin;
    private final JPanel view;

    public PluginPage(SectionView parent, PluginDescriptor desc) {
        super(parent, desc.getName());
        view = new JPanel();
        view.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        plugin = (MedSavantSectionPlugin)controller.getPlugin(desc.getID());
        if (plugin != null) {
            plugin.init(view);
        } else {
            JLabel placeholder = new JLabel(controller.getPluginStatus(desc.getID()));
            placeholder.setFont(ViewUtil.getBigTitleFont());
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            view.add(placeholder, gbc);

            // Not yet loaded.  We need to wait for PluginController to fire event.
            controller.addListener(new Listener<PluginEvent>() {
                @Override
                public void handleEvent(PluginEvent event) {
                    LOG.debug(String.format("PluginPage.handleEvent(%s)", event.getType()));
                    switch (event.getType()) {
                        case LOADED:
                            view.removeAll();
                            MedSavantPlugin plug = event.getPlugin();
                            if (plug instanceof MedSavantSectionPlugin) {
                                plugin = (MedSavantSectionPlugin)plug;
                                plugin.init(view);
                            }
                            break;
                        case ERROR:
                            ((JLabel)view.getComponent(0)).setText(controller.getPluginStatus(event.getID()));
                            break;
                    }
                    controller.removeListener(this);
                }
            });
        }
    }

    @Override
    public JPanel getView() {
        return view;
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        if (plugin != null) {
            ((MedSavantSectionPlugin)plugin).viewDidLoad();
        }
    }

    @Override
    public void viewDidUnload() {
        if (plugin != null) {
            ((MedSavantSectionPlugin)plugin).viewDidUnload();
        }
        super.viewDidUnload();
    }
}
