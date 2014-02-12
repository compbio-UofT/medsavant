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
package org.ut.biolab.medsavant.client.view.app.builtin.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.appapi.MedSavantVariantSectionApp;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.plugin.PluginEvent;
import org.ut.biolab.medsavant.shared.appapi.MedSavantApp;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.view.app.MultiSectionApp;
import org.ut.biolab.medsavant.client.view.app.AppSubSection;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 * Page which a plugin can use to present its UI.
 *
 * @author tarkvara
 */
public class PluginPage extends AppSubSection {
    private static final Log LOG = LogFactory.getLog(PluginPage.class);
    private static AppController controller = AppController.getInstance();

    private MedSavantVariantSectionApp plugin;
    private final JPanel view;

    public PluginPage(MultiSectionApp parent, final MedSavantVariantSectionApp plugin) {
        super(parent, plugin.getTitle());
        view = new JPanel();
        view.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        if (plugin != null) {
            plugin.init(view);
        } else {
            JLabel placeholder = new JLabel(plugin.getTitle());
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
                            MedSavantApp plug = event.getPlugin();
                            if (plug instanceof MedSavantVariantSectionApp) {
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
    public void viewWillLoad() {
        super.viewWillLoad();
        if (plugin != null) {
            ((MedSavantVariantSectionApp)plugin).viewDidLoad();
        }
    }

    @Override
    public void viewDidUnload() {
        if (plugin != null) {
            ((MedSavantVariantSectionApp)plugin).viewDidUnload();
        }
        super.viewDidUnload();
    }
}
