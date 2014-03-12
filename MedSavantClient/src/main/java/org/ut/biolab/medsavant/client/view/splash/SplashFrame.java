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

import com.explodingpixels.macwidgets.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.ServerController;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.component.NiceMenu;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.DetailedListModel;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.form.NiceForm;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormField;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormFieldGroup;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormModel;
import org.ut.biolab.medsavant.client.app.jAppStore;

/**
 *
 * @author mfiume
 */
public class SplashFrame extends JFrame {

    private static Log LOG = LogFactory.getLog(SplashFrame.class);

    private final JPanel primaryPanel;
    private jAppStore appStore;

    private final LoginComponent loginComponent;
    private final ServerManagementComponent serverManager;

    public SplashFrame() {

        if (ClientMiscUtils.MAC) {
            MacUtils.makeWindowLeopardStyle(this.getRootPane());
        }

        this.setTitle("MedSavant");
        this.setResizable(false);
        this.setBackground(Color.white);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setLayout(new MigLayout("filly, insets 0, gapx 0, height 530, width 750, hidemode 3"));

        primaryPanel = new JPanel();

        loginComponent = new LoginComponent();
        serverManager = new ServerManagementComponent();

        setPrimaryPanel(loginComponent);

        this.add(primaryPanel, "width 100%, growy 1.0, growx 1.0");

        this.pack();
        this.setLocationRelativeTo(null);
    }

    private void closeSplashFrame() {
        this.setVisible(false);
    }

    private void setPrimaryPanel(JPanel p) {

        if (p instanceof LoginComponent) {
            loginComponent.setPageAppropriately();
        }

        primaryPanel.removeAll();
        primaryPanel.setLayout(new BorderLayout());
        primaryPanel.add(p, BorderLayout.CENTER);
        primaryPanel.updateUI();
    }

    private class ServerDetailedView extends DetailedView {

        // fields in this form
        private NiceFormField nameField;
        private NiceFormField hostField;
        private NiceFormField portField;
        private NiceFormField databaseField;
        private NiceFormField usernameField;
        private NiceFormField passwordField;
        private NiceFormField rememberPasswordField;

        private final ServerManagementComponent serverManagementComponent;
        private NiceForm form;
        private MedSavantServerInfo server;

        private JPanel adminPanel;
        private JButton chooseButton;
        private JButton cancelButton;
        private JButton saveButton;
        private boolean isEditing;

        public ServerDetailedView(ServerManagementComponent parent) {
            super("Servers");
            this.serverManagementComponent = parent;
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

        private NiceForm getNiceFormForServer(MedSavantServerInfo server) {
            NiceFormModel model = new NiceFormModel();

            NiceFormFieldGroup settingsGroup = new NiceFormFieldGroup("Server Settings", true);

            settingsGroup.addField(nameField = new NiceFormField(true, "Name", NiceForm.FieldType.STRING, server.getNickname()));
            settingsGroup.addField(hostField = new NiceFormField(true, "Hostname", NiceForm.FieldType.STRING, server.getHost()));
            settingsGroup.addField(portField = new NiceFormField(true, "Port", NiceForm.FieldType.NUMBER, server.getPort()));
            settingsGroup.addField(databaseField = new NiceFormField(true, "Database", NiceForm.FieldType.STRING, server.getDatabase()));

            model.addGroup(settingsGroup);

            NiceFormFieldGroup loginGroup = new NiceFormFieldGroup("Login", true);

            loginGroup.addField(usernameField = new NiceFormField(false, "Username", NiceForm.FieldType.STRING, server.getUsername()));
            loginGroup.addField(passwordField = new NiceFormField(false, "Password", NiceForm.FieldType.PASSWORD, server.getPassword()));
            loginGroup.addField(rememberPasswordField = new NiceFormField(false, "Remember password", NiceForm.FieldType.BOOLEAN, server.isRememberPassword()));

            model.addGroup(loginGroup);

            NiceForm f = new NiceForm(model);

            return f;
        }

        private void setEditing(boolean b) {
            isEditing = b;
            if (server != null) { serverManager.setSelectedServer(server); }
            updateStateOfForm();
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

            form = getNiceFormForServer(server);

            form.addListener(new Listener<NiceForm.FormEvent>() {

                @Override
                public void handleEvent(NiceForm.FormEvent event) {

                    if (form.isEditModeOn()) {
                        serverManagementComponent.setMode(ServerManagementComponent.EDIT_MODE);
                    } else {
                        serverManagementComponent.setMode(ServerManagementComponent.NORMAL_MODE);
                    }
                }

            });

            container.add(form, "wrap, aligny top");

            chooseButton = ViewUtil.getTexturedButton("Connect");

            cancelButton = ViewUtil.getTexturedButton("Cancel");
            saveButton = ViewUtil.getTexturedButton("Save");

            saveButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    doSave();
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setEditing(false);
                    serverManagementComponent.setMode(ServerManagementComponent.NORMAL_MODE);
                }
            });

            chooseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ServerController.getInstance().setCurrentServer(server);
                    setPrimaryPanel(loginComponent);
                }
            });

            bottomMenu.addRightComponent(cancelButton);
            bottomMenu.addRightComponent(saveButton);
            bottomMenu.addRightComponent(chooseButton);

            cancelButton.setVisible(false);
            saveButton.setVisible(false);

            adminPanel = ViewUtil.getClearPanel();

            adminPanel.setLayout(new MigLayout("insets 0, center"));

            final JLabel adminLabel = ViewUtil.getSettingsHelpLabel("Requires administrative priviledges");
            final JButton createDBButton = ViewUtil.getTexturedButton("Create Database");
            final JButton deleteDBButton = ViewUtil.getTexturedButton("Delete Database");

            final JToggleButton admin = ViewUtil.getSoftToggleButton("Database Administration");

            createDBButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    createDatabaseSpecifiedByForm();
                }

            });

            deleteDBButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteDatabaseSpecifiedByForm();
                }
            });

            adminPanel.add(admin, "wrap");
            adminPanel.add(adminLabel, "wrap");
            adminPanel.add(createDBButton, "split");
            adminPanel.add(deleteDBButton, "wrap");

            adminLabel.setVisible(false);
            createDBButton.setVisible(false);
            deleteDBButton.setVisible(false);

            admin.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean isAdministrating = admin.isSelected();
                    adminLabel.setVisible(isAdministrating);
                    createDBButton.setVisible(isAdministrating);
                    deleteDBButton.setVisible(isAdministrating);
                }

            });

            container.add(adminPanel, "wrap");
            //adminPanel.setVisible(false);

            this.add(container, BorderLayout.CENTER);
            this.add(bottomMenu, BorderLayout.SOUTH);
            this.updateUI();

            updateStateOfForm();
        }

        private void createDatabaseSpecifiedByForm() {
            // validate the form
            if (form != null) {
                if (form.validateForm()) {

                    String database = form.getValueForStringField(databaseField);

                    if (DialogUtils.askYesNo(
                            "Create Database",
                            String.format(
                                    "<html>Are you sure you want to create the database <i>%s</i>?</html>", database)) == DialogUtils.YES) {
                        String host = form.getValueForStringField(hostField);
                        int port = form.getValueForIntegerField(portField);
                        String username = form.getValueForStringField(usernameField);
                        String password = form.getValueForStringField(passwordField);

                        createDatabase(host, port, database, username, password);
                    }
                }
            }
        }

        private void deleteDatabaseSpecifiedByForm() {
            // validate the form
            if (form != null) {
                if (form.validateForm()) {

                    String host = form.getValueForStringField(hostField);
                    int port = form.getValueForIntegerField(portField);
                    String database = form.getValueForStringField(databaseField);
                    String username = form.getValueForStringField(usernameField);
                    String password = form.getValueForStringField(passwordField);

                    removeDatabase(host, port, database, username, password);
                }
            }
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

        private void showBlockPanel() {
            this.removeAll();

            this.setLayout(new MigLayout("center, fillx, filly, wrap 1"));

            WaitPanel blockCard = new WaitPanel("No server selected");
            blockCard.setProgressBarVisible(false);
            this.add(blockCard, "width 100%, height 100%");
            this.updateUI();
        }

        private void doSave() {
            if (form != null) {
                if (form.validateForm()) {
                    String name = form.getValueForStringField(nameField);
                    String host = form.getValueForStringField(hostField);
                    int port = form.getValueForIntegerField(portField);
                    String database = form.getValueForStringField(databaseField);
                    String username = form.getValueForStringField(usernameField);
                    String password = form.getValueForStringField(passwordField);
                    boolean rememberPass = form.getValueForBooleanField(rememberPasswordField);

                    server.setHost(host);
                    server.setPort(port);
                    server.setDatabase(database);
                    server.setNickname(name);
                    server.setUsername(username);
                    server.setPassword(password);
                    server.setRememberPassword(rememberPass);

                    MedSavantServerInfo existingServer = ServerController.getInstance().getServerNamed(server.getNickname());

                    if (existingServer != null && existingServer != server) {
                        DialogUtils.displayMessage("There's already a server named " + name + ".");
                        return;
                    }

                    ServerController.getInstance().saveServers();

                    serverManagementComponent.normalSplitScreen.selectItemWithKey(name);
                    serverManagementComponent.setMode(ServerManagementComponent.NORMAL_MODE);
                }
            }
        }

        private void updateStateOfForm() {
            if (form != null) {
                form.setEditModeOn(isEditing);
                adminPanel.setVisible(isEditing);
                chooseButton.setVisible(!isEditing);
                cancelButton.setVisible(isEditing);
                saveButton.setVisible(isEditing);
                if (isEditing) {
                    form.focus();
                }
            }
        }
    }

    private static class ServerDetailedListEditor extends DetailedListEditor {

        private ServerManagementComponent serverManager;

        private ServerDetailedListEditor(ServerManagementComponent serverManager) {
            this.serverManager = serverManager;
        }

        @Override
        public boolean doesImplementAdding() {
            return true;
        }

        @Override
        public void addItems() {
            serverManager.addServerAndEdit();
        }

        @Override
        public boolean doesImplementDeleting() {
            return true;
        }

        public void deleteItems(List<Object[]> items) {

            for (Object[] o : items) {
                MedSavantServerInfo server = (MedSavantServerInfo) o[1];
                int result = DialogUtils.askYesNo("Remove Server", String.format("Really remove %s?", server.getNickname()));
                if (result == DialogUtils.YES) {

                    ServerController.getInstance().removeServer(server);
                }
            }
        }

        @Override
        public boolean doesImplementEditing() {
            return true;
        }

        @Override
        public boolean doesRefreshAfterEditing() {
            return false;
        }

        @Override
        public void editItem(Object[] item) {
            serverManager.setMode(ServerManagementComponent.EDIT_MODE);
        }
    }

    private class LoginComponent extends JPanel implements Listener<ServerController> {

        private final JPanel loginSettingsPanel;
        private final JPanel loggingInPanel;
        private final JPanel noServerAtAllPanel;

        private PlaceHolderTextField usernameField;
        private PlaceHolderPasswordField passwordField;
        private JCheckBox rememberPasswordCheckbox;
        private JButton serverNameLabel;

        private final int LOGIN_PAGE = 0;
        private final int LOGGING_IN_PAGE = 1;
        private final int NO_SERVER_ATALL_PAGE = 2;
        private boolean isLoggingIn = false;
        private JCheckBox autoLoginCheckBox;
        private MedSavantServerInfo server;
        private MedSavantWorker<Void> loginThread;

        public LoginComponent() {
            this.setBackground(Color.white);
            this.setLayout(new MigLayout("fillx, filly, insets 0, center"));

            LOG.info("Subscribing to login events");
            LoginController.getInstance().addListener(new Listener<LoginEvent>() {

                @Override
                public void handleEvent(LoginEvent event) {

                    LOG.info("Received login event " + event.getType().toString());
                    
                    switch (event.getType()) {
                        case LOGGED_IN:
                            
                            // don't log in if the cancel button was pressed
                            if (loginThread.isCancelled()) {
                                LOG.info("Login was cancelled " + loginThread.isCancelled() + " " + !isLoggingIn);
                                return;
                            }

                            MedSavantFrame frame = MedSavantFrame.getInstance();
                            frame.initializeSessionView();
                            frame.setLocationRelativeTo(null);
                            frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                            frame.setPreferredSize(frame.getSize());
                            frame.setVisible(true);
                            closeSplashFrame();

                            break;
                        case LOGGED_OUT:
                            MedSavantFrame frame2 = MedSavantFrame.getInstance();
                            frame2.setVisible(false);
                            break;
                        case LOGIN_FAILED:

                            Exception e = event == null ? null : event.getException();
                            String msg = (event == null || event.getException() == null) ? "" : event.getException().getLocalizedMessage();

                            DialogUtils.displayException("Login Failed", msg, e);
                            isLoggingIn = false;
                            break;
                        case LOGIN_CANCELLED:
                            isLoggingIn = false;
                            break;
                    }

                    isLoggingIn = false;
                    setPageAppropriately();
                }

            });

            JPanel container = ViewUtil.getClearPanel();
            container.setLayout(new MigLayout());

            JPanel leftSide = ViewUtil.getClearPanel();
            leftSide.setLayout(new MigLayout("wrap 1, insets 0, center, hidemode 3"));
            ImagePanel splash = new ImagePanel(IconFactory.getInstance().getIcon(IconFactory.ICON_ROOT + "medsavantlogo-flat.png").getImage(), 170, 171);
            leftSide.add(splash, "center");
            container.add(leftSide);

            JPanel rightSide = ViewUtil.getClearPanel();
            rightSide.setLayout(new MigLayout("wrap 1, hidemode 3"));

            ServerController.getInstance().addListener(this);

            noServerAtAllPanel = initNoServerAtAllPanel();
            loginSettingsPanel = initLoginSettingsPanel();
            loggingInPanel = initLoggingInPanel();

            leftSide.add(noServerAtAllPanel);
            rightSide.add(loginSettingsPanel);
            rightSide.add(loggingInPanel);

            setPageAppropriately();

            container.add(rightSide);

            this.add(container, "center, growx 1.0");

        }

        @Override
        public void handleEvent(ServerController event) {
            setPageAppropriately();
        }

        boolean isFirstTime = true;

        private void setPageAppropriately() {

            // try auto logging in the first time
            if (isFirstTime) {

                boolean isBootingFromLogout = SettingsController.getInstance().getBoolean("BootFromLogout", false);
                SettingsController.getInstance().setBoolean("BootFromLogout", false);

                // don't auto login if we're coming from a logout
                if (!isBootingFromLogout) {
                    if (SettingsController.getInstance().getAutoLogin()) {
                        MedSavantServerInfo server = ServerController.getInstance().getCurrentServer();
                        if (server != null) {
                            if (server.isRememberPassword() && !server.getUsername().isEmpty() && !server.getPassword().isEmpty()) {
                                loginUsingEnteredUsernameAndPassword(server);
                            }
                        }
                    }
                }
            }

            isFirstTime = false;

            if (isLoggingIn) {
                setPage(LOGGING_IN_PAGE);
            } else if (ServerController.getInstance().getServers().isEmpty()) {
                setPage(NO_SERVER_ATALL_PAGE);
            } else {
                setServer(ServerController.getInstance().getCurrentServer());
                setPage(LOGIN_PAGE);
            }
        }

        private JPanel initNoServerAtAllPanel() {
            JPanel panel = ViewUtil.getClearPanel();
            panel.setLayout(new MigLayout("gapx 0, gapy 5, wrap 1, insets 20 0 0 0"));

            JLabel welcomeLabel;
            panel.add(welcomeLabel = new JLabel("Welcome to MedSavant"), "wrap, center");
            panel.add(new JLabel("To get started"), "split, center, gapy 20");
            JButton addServerButton;
            panel.add(addServerButton = ViewUtil.getSoftButton("add a server"));
            panel.add(new JLabel("to connect to."));

            addServerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    serverManager.addServerAndEdit();
                    setPrimaryPanel(serverManager);
                }
            });

            welcomeLabel.setFont(new Font(ViewUtil.getDefaultFontFamily(), Font.PLAIN, 20));

            return panel;
        }

        private JPanel initLoggingInPanel() {
            JPanel panel = ViewUtil.getClearPanel();
            panel.setLayout(new MigLayout("gapy 5, insets 0, fillx"));

            panel.add(new JLabel("Signing In"));
            panel.add(ViewUtil.getIndeterminateProgressBar());

            JButton cancelButton;
            panel.add(cancelButton = new JButton("Cancel"), "growx 1.0, right");
            cancelButton.setFocusable(false);

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isLoggingIn = false;
                    loginThread.cancel(true);

                    LoginController.getInstance().cancelCurrentLoginAttempt();

                    LOG.info("After clicking cancel login thread cancelled? " + loginThread.isCancelled() + " " + loginThread.toString());

                    setPageAppropriately();
                }
            });

            return panel;
        }

        private void setPage(int page) {

            switch (page) {
                case NO_SERVER_ATALL_PAGE:
                    loginSettingsPanel.setVisible(false);
                    loggingInPanel.setVisible(false);
                    noServerAtAllPanel.setVisible(true);
                    break;
                case LOGIN_PAGE:
                    loginSettingsPanel.setVisible(true);
                    loggingInPanel.setVisible(false);
                    noServerAtAllPanel.setVisible(false);
                    break;
                case LOGGING_IN_PAGE:
                    loginSettingsPanel.setVisible(false);
                    loggingInPanel.setVisible(true);
                    noServerAtAllPanel.setVisible(false);
                    break;
            }
        }

        private JPanel initLoginSettingsPanel() {
            JPanel panel = ViewUtil.getClearPanel();
            panel.setLayout(new MigLayout("gapy 5, wrap 1, insets 0"));

            serverNameLabel = ViewUtil.getSoftButton("");
            panel.add(new JLabel("Connect to"), "split");
            panel.add(serverNameLabel, "wrap");

            serverNameLabel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    serverManager.normalSplitScreen.selectItemWithKey(serverNameLabel.getText());
                    serverManager.setMode(ServerManagementComponent.NORMAL_MODE);
                    setPrimaryPanel(serverManager);
                }
            });

            usernameField = new PlaceHolderTextField();
            usernameField.setPlaceholder("Username");
            usernameField.setFont(new Font(ViewUtil.getDefaultFontFamily(), Font.PLAIN, 20));
            usernameField.setForeground(ViewUtil.getSemiBlackColor());
            usernameField.setColumns(14);
            panel.add(usernameField, "wrap");
            usernameField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        doSignIntoServer(server);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }
            }
            );

            passwordField = new PlaceHolderPasswordField();
            passwordField.setPlaceholder("Password");
            passwordField.setFont(new Font(ViewUtil.getDefaultFontFamily(), Font.PLAIN, 20));
            passwordField.setForeground(ViewUtil.getSemiBlackColor());
            passwordField.setColumns(14);
            panel.add(passwordField, "wrap");
            passwordField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        doSignIntoServer(server);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }
            }
            );

            rememberPasswordCheckbox = new JCheckBox("Remember Password");
            rememberPasswordCheckbox.setFocusable(false);
            panel.add(rememberPasswordCheckbox, "wrap");

            autoLoginCheckBox = new JCheckBox("Auto-login");
            autoLoginCheckBox.setFocusable(false);

            rememberPasswordCheckbox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean remember = rememberPasswordCheckbox.isSelected();
                    autoLoginCheckBox.setEnabled(remember);
                    if (!remember) {
                        autoLoginCheckBox.setSelected(false);
                    }
                    if (server != null) {
                        server.setRememberPassword(rememberPasswordCheckbox.isSelected());
                        ServerController.getInstance().saveServers();
                    }
                }

            });
            autoLoginCheckBox.setEnabled(rememberPasswordCheckbox.isSelected());
            panel.add(autoLoginCheckBox, "split");

            autoLoginCheckBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    SettingsController.getInstance().setAutoLogin(autoLoginCheckBox.isSelected());
                }

            });

            JButton signInButton = new JButton("Sign In");
            signInButton.setFocusable(false);

            signInButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    doSignIntoServer(server);

                }

            });

            panel.add(signInButton, "right");

            return panel;
        }

        private void doSignIntoServer(MedSavantServerInfo server) {
            // save username
            server.setUsername(usernameField.getText());

            // save password
            server.setPassword(new String(passwordField.getPassword()));

            // save remember
            server.setRememberPassword(rememberPasswordCheckbox.isSelected());

            ServerController.getInstance().saveServers();

            // login , present errors
            loginUsingEnteredUsernameAndPassword(server);
            setPageAppropriately();
        }

        private void loginUsingEnteredUsernameAndPassword(final MedSavantServerInfo server) {

            if (isLoggingIn) {
                LOG.info("Already logging in");
                return;
            }

            isLoggingIn = true;

            loginThread = new MedSavantWorker<Void>("LoginView") {
                @Override
                protected void showProgress(double fract) {
                }

                @Override
                protected void showSuccess(Void result) {
                }

                @Override
                protected Void doInBackground() throws Exception {
                    LoginController.getInstance().login(server);
                    return null;
                }
            };

            LOG.info("Executing login thread");
            loginThread.execute();
        }

        public void setServer(MedSavantServerInfo server) {
            if (server == null) {
                return;
            }

            this.server = server;

            usernameField.setText(server.getUsername());
            passwordField.setText(server.getPassword());
            rememberPasswordCheckbox.setSelected(server.isRememberPassword());
            serverNameLabel.setText(ViewUtil.ellipsize(server.getNickname(), 23));

            autoLoginCheckBox.setEnabled(server.isRememberPassword());
            if (server.isRememberPassword()) {
                autoLoginCheckBox.setSelected(SettingsController.getInstance().getAutoLogin());
            }
        }

    }

    private class ServerManagementComponent extends JPanel implements Listener<ServerController> {

        private int mode;
        private NiceMenu topMenu;

        private final static int NORMAL_MODE = 0;
        private final static int EDIT_MODE = 1;

        private SplitScreenView normalSplitScreen;
        private ServerDetailedView serverDetailView;

        public ServerManagementComponent() {
            initUI();
            mode = NORMAL_MODE;
            refreshUI();
            ServerController.getInstance().addListener(this);

        }

        public void setMode(int mode) {
            this.mode = mode;
            refreshUI();
        }

        private void refreshUI() {
            serverDetailView.setEditing(mode == ServerManagementComponent.EDIT_MODE);
        }

        private void initUI() {

            this.setBackground(Color.white);
            this.setLayout(new BorderLayout());

            /**
             * MENUS
             */
            topMenu = new NiceMenu();
            topMenu.setTitle("Server Management");
            this.add(topMenu, BorderLayout.NORTH);

            /**
             * CENTRAL CONTAINER
             */
            JPanel container = ViewUtil.getClearPanel();
            container.setLayout(new MigLayout("insets 0, fillx, filly, center, hidemode 3"));
            this.add(container, BorderLayout.CENTER);

            /**
             * NORMAL FORM
             */
            serverDetailView = new ServerDetailedView(this);
            final ServerDetailedListEditor serverDetailListEditor = new ServerDetailedListEditor(this);

            normalSplitScreen = new SplitScreenView(new DetailedListModel() {

                @Override
                public Object[][] getList(int limit) throws Exception {

                    List<MedSavantServerInfo> servers = ServerController.getInstance().getServers();

                    Object[][] results = new Object[servers.size()][];
                    int counter = 0;
                    for (MedSavantServerInfo server : servers) {
                        results[counter++] = new Object[]{server.getNickname(), server};
                    }

                    return results;
                }

                @Override
                public String[] getColumnNames() {
                    return new String[]{"Server", "ServerObject"};
                }

                @Override
                public Class[] getColumnClasses() {
                    return new Class[]{String.class, MedSavantServerInfo.class};
                }

                @Override
                public int[] getHiddenColumns() {
                    return new int[0];
                }

            }, serverDetailView, serverDetailListEditor);

            container.add(normalSplitScreen, "width 100%, height 100%");

        }

        private void addServerAndEdit() {
            String baseName = "Untitled Server";
            String newName = baseName;
            int counter = 1;
            while (ServerController.getInstance().isServerNamed(newName)) {
                newName = String.format("%s (%d)", baseName, ++counter);
            }

            MedSavantServerInfo server = new MedSavantServerInfo();
            server.setNickname(newName);

            ServerController.getInstance().addServer(server);
            serverManager.setSelectedServer(server);
            serverManager.setMode(ServerManagementComponent.EDIT_MODE);
        }

        private void setSelectedServer(MedSavantServerInfo server) {
            normalSplitScreen.selectItemWithKey(server.getNickname());
        }

        @Override
        public void handleEvent(ServerController event) {
            refreshList();
        }

        private void refreshList() {
            normalSplitScreen.refresh();
        }
    }

    public static void main(String[] a) {
        SplashFrame loginFrame = new SplashFrame();
        loginFrame.setVisible(true);
    }
}
