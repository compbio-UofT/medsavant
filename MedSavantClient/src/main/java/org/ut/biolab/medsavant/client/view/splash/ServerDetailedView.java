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
package org.ut.biolab.medsavant.client.view.splash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.controller.ServerController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.component.NiceMenu;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.component.field.editable.EditableField;
import org.ut.biolab.medsavant.component.field.editable.EditableFieldValidator;
import org.ut.biolab.medsavant.component.field.editable.EnumEditableField;
import org.ut.biolab.medsavant.component.field.editable.FieldEditedListener;
import org.ut.biolab.medsavant.component.field.editable.IntegerEditableField;
import org.ut.biolab.medsavant.component.field.editable.PasswordEditableField;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;
import org.ut.biolab.medsavant.component.field.validator.NonEmptyStringValidator;

/**
 *
 * @author mfiume
 */
public class ServerDetailedView extends DetailedView implements FieldEditedListener {

    private StringEditableField nameField;
    private StringEditableField hostField;
    private IntegerEditableField portField;
    private StringEditableField databaseField;
    private StringEditableField usernameField;
    private PasswordEditableField passwordField;
    private EnumEditableField rememberPasswordField;

    private final SplashServerManagementComponent serverManagementComponent;
    private MedSavantServerInfo server;

    private JButton chooseButton;
    private KeyValuePairPanel kvp;
    private final SplashFrame splash;

    public ServerDetailedView(SplashFrame splash, SplashServerManagementComponent serverManagementComponent) {
        super("Servers");
        this.serverManagementComponent = serverManagementComponent;
        this.splash = splash;
        setSelectedItem(null);
    }

    @Override
    public void setSelectedItem(Object[] selectedRow) {

        if (selectedRow == null || selectedRow.length == 0) {
            showBlockPanel();
            return;
        }
        MedSavantServerInfo server = (MedSavantServerInfo) selectedRow[1];
        showServerInfo(server);
    }

    @Override
    public void setMultipleSelections(List<Object[]> selectedRows) {
    }

    @Override
    public JPopupMenu createPopup() {
        return new JPopupMenu();
    }

    private KeyValuePairPanel getNiceFormForServer(final MedSavantServerInfo server) {

        final String DBNAME_KEY = "Database name";

        final KeyValuePairPanel kvp = new KeyValuePairPanel(1, true);

        final NonEmptyStringValidator nonEmptyStringValidator = new NonEmptyStringValidator("server name");
        EditableFieldValidator nameValidator = new EditableFieldValidator<String>() {

            @Override
            public boolean validate(String value) {
                if (nonEmptyStringValidator.validate(value)) {

                    // check if there's already a different server with this name
                    return !isDifferentServerWithName(value, server);

                }
                return true;
            }

            @Override
            public String getDescriptionOfValidValue() {
                return "Invalid name (must not be blank or already exist)";
            }

        };

        nameField = new StringEditableField();
        nameField.setValidator(nameValidator);
        nameField.setValue(server.getNickname());

        hostField = new StringEditableField();
        hostField.setValidator(new NonEmptyStringValidator("host name"));
        hostField.setValue(server.getHost());

        portField = new IntegerEditableField();
        portField.setValue(server.getPort());

        databaseField = new StringEditableField();
        databaseField.setValidator(new NonEmptyStringValidator("database name"));
        databaseField.setValue(server.getDatabase());

        usernameField = new StringEditableField();
        usernameField.setValidator(new NonEmptyStringValidator("username"));
        usernameField.setValue(server.getUsername());

        passwordField = new PasswordEditableField();
        passwordField.setValidator(new NonEmptyStringValidator("password"));
        passwordField.setValue(server.getPassword());

        rememberPasswordField = new EnumEditableField(new String[]{"No", "Yes"});
        rememberPasswordField.setValue(server.isRememberPassword() ? "Yes" : "No");

        addChangeListenersToFields(nameField, hostField, portField, databaseField, usernameField, passwordField, rememberPasswordField);

        kvp.addKeyWithValue("Server name", nameField);
        kvp.addKeyWithValue("Host name", hostField);
        kvp.addKeyWithValue("Port", portField);

        kvp.addKeyWithValue("Username", usernameField);
        kvp.addKeyWithValue("Password", passwordField);
        kvp.addKeyWithValue("Remember password", rememberPasswordField);
        kvp.addKeyWithValue(DBNAME_KEY, databaseField);

        final JToggleButton adminButton = ViewUtil.getSoftToggleButton("Admin");

        final JLabel adminLabel = ViewUtil.getSettingsHelpLabel("Requires administrative priviledges");
        final JButton createDBButton = ViewUtil.getTexturedButton("Create Database");
        final JButton deleteDBButton = ViewUtil.getTexturedButton("Delete Database");

        createDBButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Creating db");
                createDatabaseSpecifiedByForm();
            }

        });

        deleteDBButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Deleting db");
                deleteDatabaseSpecifiedByForm();
            }
        });

        JPanel adminPanel = ViewUtil.getClearPanel();

        adminPanel.setLayout(new MigLayout("insets 0"));

        //adminPanel.add(admin, "wrap");
        adminPanel.add(adminLabel, "wrap");
        adminPanel.add(createDBButton, "split");
        adminPanel.add(deleteDBButton, "wrap");

        adminButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                kvp.toggleDetailVisibility(DBNAME_KEY, adminButton.isSelected());
            }

        });

        kvp.setAdditionalColumn(DBNAME_KEY, 0, adminButton);
        kvp.setDetailComponent(DBNAME_KEY, adminPanel);
        kvp.toggleDetailVisibility(DBNAME_KEY, adminButton.isSelected());

        return kvp;
    }

    private boolean isDifferentServerWithName(String name, MedSavantServerInfo server) {
        MedSavantServerInfo existingServer = ServerController.getInstance().getServerNamed(name);
        
        // true if the server with this name exists
        return existingServer != null && existingServer != server;
    }

    private void showServerInfo(final MedSavantServerInfo server) {

        this.server = server;

        this.removeAll();
        this.revalidate();

        this.setLayout(new BorderLayout());
        this.setBackground(Color.white);

        JPanel container = ViewUtil.getClearPanel();

        NiceMenu bottomMenu = new NiceMenu(NiceMenu.MenuLocation.BOTTOM);
        bottomMenu.setOpaque(false);
        bottomMenu.setBorder(BorderFactory.createEmptyBorder());

        container.setLayout(new MigLayout("fillx, insets 20, hidemode 3"));

        if (server == null) {
            return;
        }

        kvp = getNiceFormForServer(server);

        container.add(kvp, "wrap, aligny top, growx 1.0, wmax 100%");

        chooseButton = ViewUtil.getTexturedButton("Connect");

        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateServerSettings()) {
                    ServerController.getInstance().setCurrentServer(server);
                    splash.switchToLogin();
                }
            }
        });

        bottomMenu.addRightComponent(chooseButton);

        this.add(container, BorderLayout.CENTER);
        this.add(bottomMenu, BorderLayout.SOUTH);
        this.updateUI();
    }

    private boolean validateServerSettings() {

        EditableField[] fields = new EditableField[]{
            nameField,
            hostField,
            portField,
            usernameField,
            passwordField,
            rememberPasswordField,
            databaseField
        };

        for (EditableField f : fields) {
            if (!f.validateCurrentValue()) {
                return false;
            }
        }

        return true;
    }

    private void createDatabaseSpecifiedByForm() {

        String host = hostField.getValue();
        int port = portField.getValue();
        String database = databaseField.getValue();
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        createDatabase(host, port, database, username, password);
    }

    private void deleteDatabaseSpecifiedByForm() {

        String host = hostField.getValue();
        int port = portField.getValue();
        String database = databaseField.getValue();
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        removeDatabase(host, port, database, username, password);
    }

    private void removeDatabase(final String address, final int port, final String database, final String username, final String password) {

        if (DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This operation cannot be undone.", database) == DialogUtils.YES) {
            new ProgressDialog("Removing Database", String.format("<html>Removing database <i>%s</i>. Please wait.</html>", database)) {
                @Override
                public void run() {
                    try {
                        MedSavantClient.initializeRegistry(address, port + "");
                        MedSavantClient.SetupManager.removeDatabase(address, port, database, username, password.toCharArray());
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                setVisible(false);
                            }

                        });
                        ServerController.getInstance().removeServer(server);
                        DialogUtils.displayMessage("Database Removed", String.format("<html>Database <i>%s</i> successfully removed.</html>", database));
                    } catch (Exception ex) {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {

                                @Override
                                public void run() {
                                    setVisible(false);
                                }

                            });
                        } catch (Exception ex1) {
                            Logger.getLogger(SplashFrame.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                        ClientMiscUtils.reportError("Database could not be removed: %s", ex);
                    }
                }
            }.setVisible(true);
        }
    }

    private void createDatabase(final String address, final int port, final String database, final String username, final String password) {
        if (DialogUtils.askYesNo(
                "Create Database",
                String.format(
                        "<html>Are you sure you want to create the database <i>%s</i>?</html>", database)) == DialogUtils.YES) {

            new ProgressDialog("Creating Database", String.format("<html>Creating database <i>%s</i>. Please wait.</html>", database)) {
                @Override
                public void run() {
                    try {
                        MedSavantClient.initializeRegistry(address, port + "");
                        MedSavantClient.SetupManager.createDatabase(address, port, database, username, password.toCharArray());
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                setVisible(false);
                            }

                        });
                        doSave();
                        DialogUtils.displayMessage("Database Created", String.format("<html>Database <i>%s</i> successfully created.</html>", database));
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {

                                @Override
                                public void run() {
                                    setVisible(false);
                                }

                            });
                        } catch (Exception ex1) {
                        }
                        ClientMiscUtils.reportError("Database could not be created: %s\nPlease check the settings and try again.", ex);
                    }
                }
            }.setVisible(true);
        }
    }

    private void showBlockPanel() {
        this.removeAll();

        this.setLayout(new MigLayout("center, fillx, filly, wrap 1"));

        WaitPanel blockCard = new WaitPanel("No server selected");
        blockCard.setProgressBarVisible(false);
        this.add(blockCard, "width 100%, height 100%");
        this.updateUI();
    }

    private void doSave() {

        System.out.println("Saving...");

        //TODO: validate
        String name = nameField.getValue();
        String host = hostField.getValue();

        Integer portInt = portField.getValue();
        int port = portInt == null ? 0 : portInt;

        String database = databaseField.getValue();
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        boolean rememberPass = rememberPasswordField.getValue().equals("Yes");

        server.setHost(host);
        server.setPort(port);
        server.setDatabase(database);
        server.setNickname(name);
        server.setUsername(username);
        server.setPassword(password);
        server.setRememberPassword(rememberPass);

        // check if there's already a different server with this name
        if (isDifferentServerWithName(server.getNickname(), server)) {
            System.out.println("Not saving with duplicate server name");
            return;
        }

        ServerController.getInstance().saveServers();
        serverManagementComponent.getServerList().selectItemWithKey(name);
    }

    @Override
    public void handleEvent(EditableField f) {
        System.out.println("Field with value " + f.getValue() + "  was edited");
        System.out.println("Saving");
        doSave();
    }

    private void addChangeListenersToFields(EditableField... fields) {
        for (EditableField f : fields) {
            f.addFieldEditedListener(this);
        }
    }

}
