/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.view.app.splash;

import com.explodingpixels.macwidgets.MacIcons;
import com.explodingpixels.macwidgets.SourceList;
import com.explodingpixels.macwidgets.SourceListCategory;
import com.explodingpixels.macwidgets.SourceListControlBar;
import com.explodingpixels.macwidgets.SourceListItem;
import com.explodingpixels.macwidgets.SourceListModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class LoginComponent extends JPanel {

    Log LOG = LogFactory.getLog(LoginComponent.class);

    List<MedSavantServerInfo> servers;
    private String SERVER_FILE_NAME = ".servers";

    private SourceList sourceList;
    private SourceListControlBar controlBar;
    private SourceListModel listModel;
    private SourceListCategory myCategory;

    public LoginComponent() {
        this.setBackground(new Color(222, 222, 222));
        this.setBorder(ViewUtil.getRightLineBorder());

        initComponents();

        loadServers();

    }

    private File getServerFile() {
        return new File(DirectorySettings.getMedSavantDirectory(), SERVER_FILE_NAME);
    }

    private void saveServers() throws FileNotFoundException, IOException {
        FileOutputStream fileout = null;
        ObjectOutputStream out = null;

        try {
            fileout = new FileOutputStream(getServerFile());
            out = new ObjectOutputStream(fileout);
            out.writeObject(servers);
            out.close();
            fileout.close();
        } catch (Exception ex) {
            LOG.error("Problem saving servers", ex);
        } finally {
            try {
                out.close();
            } catch (Exception ex) {
            }
            try {
                fileout.close();
            } catch (Exception ex) {
            }
        }
    }

    private void loadServers() {
        FileInputStream filein = null;
        ObjectInputStream in = null;
        try {
            filein = new FileInputStream(getServerFile());
            in = new ObjectInputStream(filein);
            servers = (List<MedSavantServerInfo>) in.readObject();
            in.close();
            filein.close();
        } catch (Exception ex) {
            LOG.error("Problem loading servers", ex);
            servers = new ArrayList<MedSavantServerInfo>();
        } finally {
            servers.add(new MedSavantServerInfo("medsavant-pro.cs.toronto.edu", 36800, "forge_flt", "MedSavant Pro"));
            refreshSourceList();
            try {
                in.close();
            } catch (Exception ex) {
            }
            try {
                filein.close();
            } catch (Exception ex) {
            }
        }
    }

    private void initComponents() {

        listModel = new SourceListModel();

        sourceList = new SourceList(listModel);

        controlBar = new SourceListControlBar();
        sourceList.installSourceListControlBar(controlBar);
        
        controlBar.createAndAddButton(MacIcons.PLUS, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO: switch to server mode
                }

            });
        
        controlBar.createAndAddButton(MacIcons.MINUS, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO: remove selected server after asking
                }

            });
        
        controlBar.createAndAddButton(MacIcons.GEAR, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO: create a popup
                }

            });
        
        myCategory = new SourceListCategory("My Servers");
        
        listModel.addCategory(myCategory);
        
        this.setLayout(new BorderLayout());
        this.add(sourceList.getComponent(), BorderLayout.CENTER);

    }

    private void refreshSourceList() {
        for (MedSavantServerInfo s : servers) {
            listModel.addItemToCategory(new SourceListItem(s.getHost()), myCategory);
        }
    }

}
