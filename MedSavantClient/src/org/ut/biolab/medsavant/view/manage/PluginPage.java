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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.api.MedSavantSectionPlugin;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginEvent;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 * Page which a plugin can use to present its UI.
 *
 * @author tarkvara
 */
public class PluginPage extends SubSectionView {
    private static final Log LOG = LogFactory.getLog(PluginPage.class);
    private static PluginController controller = PluginController.getInstance();
    private final PluginDescriptor descriptor;
    private MedSavantSectionPlugin p;
    JPanel panel;

    public PluginPage(SectionView parent, PluginDescriptor desc) {
        super(parent);
        descriptor = desc;
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        p = (MedSavantSectionPlugin)controller.getPlugin(desc.getID());
        if (p != null) {
            p.init(panel);
        } else {
            JLabel placeholder = new JLabel(controller.getPluginStatus(desc.getID()));
            placeholder.setFont(ViewUtil.getBigTitleFont());
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            panel.add(placeholder, gbc);

            // Not yet loaded.  We need to wait for PluginController to fire event.
            controller.addListener(new Listener<PluginEvent>() {
                @Override
                public void handleEvent(PluginEvent event) {
                    LOG.debug(String.format("PluginPage.handleEvent(%s)", event.getType()));
                    switch (event.getType()) {
                        case LOADED:
                            panel.removeAll();
                            MedSavantPlugin plug = event.getPlugin();
                            if (plug instanceof MedSavantSectionPlugin) {
                                p = (MedSavantSectionPlugin)plug;
                                p.init(panel);
                            }
                            break;
                        case ERROR:
                            ((JLabel)panel.getComponent(0)).setText(controller.getPluginStatus(event.getID()));
                            break;
                    }
                    controller.removeListener(this);
                }
            });
        }
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public JPanel getView(boolean update) {
        return panel;
    }

    @Override
    public void viewDidLoad() {
        if (p != null)
            ((MedSavantSectionPlugin)p).viewDidLoad();
    }

    @Override
    public void viewDidUnload() {
        if (p != null)
            ((MedSavantSectionPlugin)p).viewDidUnload();
        ThreadController.getInstance().cancelWorkers(getName());
    }
}
