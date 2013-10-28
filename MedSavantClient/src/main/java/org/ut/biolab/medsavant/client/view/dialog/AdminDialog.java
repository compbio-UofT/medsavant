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
package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.settings.VersionSettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AdminDialog extends JDialog {
    private PlaceHolderPasswordField passwordAdminField;
    private PlaceHolderTextField userAdminField;
    private PlaceHolderTextField dbnameAdminField;
    private PlaceHolderTextField portAdminField;
    private PlaceHolderTextField addressAdminField;
    private int textFieldAdminColumns = 20;

    public AdminDialog() {
        super(MedSavantFrame.getInstance(), "Database Management", true);
        this.add(getAdminPanel());
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(MedSavantFrame.getInstance());
    }

    private JPanel getAdminPanel() {
        MigLayout ml = new MigLayout();
        final JPanel p = new JPanel(ml);
        p.setBackground(Color.white);

        SettingsController sc = SettingsController.getInstance();

         addressAdminField = new PlaceHolderTextField();
        addressAdminField.setPlaceholder("server address");
        addressAdminField.setText(sc.getServerAddress());
        addressAdminField.setColumns(textFieldAdminColumns);
         portAdminField = new PlaceHolderTextField();
        portAdminField.setPlaceholder("server port number");
        portAdminField.setText(sc.getServerPort());
        portAdminField.setColumns(textFieldAdminColumns);
         dbnameAdminField = new PlaceHolderTextField();
        dbnameAdminField.setPlaceholder("database");
        dbnameAdminField.setColumns(textFieldAdminColumns);
         userAdminField = new PlaceHolderTextField();
        userAdminField.setPlaceholder("admin username");
        userAdminField.setColumns(textFieldAdminColumns);
         passwordAdminField = new PlaceHolderPasswordField();
        passwordAdminField.setPlaceholder("password");
        passwordAdminField.setColumns(textFieldAdminColumns);

        JButton addDB = new JButton("Create");
        JButton removeDB = new JButton("Delete");

        JLabel adminOnly = new JLabel("Administrators only");
        adminOnly.setForeground(Color.red);
        p.add(ViewUtil.makeSmall(adminOnly), "wrap");
        p.add(addressAdminField, "wrap");
        p.add(portAdminField, "wrap");
        p.add(dbnameAdminField, "wrap");
        p.add(userAdminField, "wrap");
        p.add(passwordAdminField, "wrap");
        p.add(addDB, "split 2");
        p.add(removeDB, "wrap");

        addDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (validateAdminParams()) {
                    closeDialogContainingPanel(p);
                    int port = Integer.parseInt(portAdminField.getText());
                    createDatabase(addressAdminField.getText(), port, dbnameAdminField.getText(), userAdminField.getText(), passwordAdminField.getPassword());
                }
            }
        });

        removeDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (validateAdminParams()) {
                    closeDialogContainingPanel(p);
                    int port = Integer.parseInt(portAdminField.getText());
                    removeDatabase(addressAdminField.getText(), port, dbnameAdminField.getText(), userAdminField.getText(), passwordAdminField.getPassword());
                }
            }
        });

        return p;
    }

    private void createDatabase(final String address, final int port, final String database, final String username, final char[] password) {
        new ProgressDialog("Creating Database", String.format("<html>Creating database <i>%s</i>. Please wait.</html>", database)) {
            @Override
            public void run() {
                try {
                    MedSavantClient.initializeRegistry(address, port + "");
                    MedSavantClient.SetupManager.createDatabase(address, port, database, username, password, VersionSettings.getVersionString());
                    DialogUtils.displayMessage("Database Created", String.format("<html>Database <i>%s</i> successfully created.</html>", database));
                } catch (Throwable ex) {
                    setVisible(false);
                    ex.printStackTrace();
                    ClientMiscUtils.reportError("Database could not be created: %s\nPlease check the settings and try again.", ex);
                }
            }
        }.setVisible(true);
    }

    private void removeDatabase(String address, int port, String database, String username, char[] password) {
        if (DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This operation cannot be undone.", database) == DialogUtils.YES) {
            try {
                MedSavantClient.initializeRegistry(address, port + "");
                MedSavantClient.SetupManager.removeDatabase(address, port, database, username, password);
                setVisible(false);
                DialogUtils.displayMessage("Database Removed", String.format("<html>Database <i>%s</i> successfully removed.</html>", database));
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Database could not be removed: %s", ex);
            }
        }
    }

    private void closeDialogContainingPanel(Component c) {
        if (c instanceof JDialog) {
            ((JDialog) c).setVisible(false);
        } else {
            closeDialogContainingPanel(c.getParent());
        }
    }

    private boolean validateAdminParams() {


        if (addressAdminField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Server address required");
            addressAdminField.requestFocus();
            return false;
        }

        if (portAdminField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Server port required");
            portAdminField.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(portAdminField.getText());
        } catch (Exception e) {
            MedSavantFrame.getInstance().notificationMessage("Invalid port number");
            portAdminField.requestFocus();
            return false;
        }

        if (dbnameAdminField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Database name required");
            dbnameAdminField.requestFocus();
            return false;
        }

        if (userAdminField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Username required");
            userAdminField.requestFocus();
            return false;
        }

        if (passwordAdminField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Password required");
            passwordAdminField.requestFocus();
            return false;
        }

        return true;
    }

}
