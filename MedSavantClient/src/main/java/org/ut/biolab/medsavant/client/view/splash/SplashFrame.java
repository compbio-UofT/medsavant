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
import java.awt.Component;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.ServerController;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.component.NiceMenu;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.form.NiceForm;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormField;
import org.ut.biolab.medsavant.client.view.util.form.NiceFormModel;
import savant.api.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class SplashFrame extends JFrame {
    private final JPanel primaryPanel;

    private final LoginComponent loginComponent;
    private final ServerManagementComponent serverManager;


    public SplashFrame() {
        
        this.setResizable(false);
        
        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        this.setLayout(new MigLayout("filly, insets 0, gapx 0, height 530, width 900, hidemode 3"));

        primaryPanel = new JPanel();

        loginComponent = new LoginComponent();
        serverManager = new ServerManagementComponent();

        setPrimaryPanel(loginComponent);

        this.add(primaryPanel, "width 100%, growy 1.0, growx 1.0");

        this.pack();
        this.setLocationRelativeTo(null);
    }

    private void setPrimaryPanel(JPanel p) {
        primaryPanel.removeAll();
        primaryPanel.setLayout(new BorderLayout());
        primaryPanel.add(p, BorderLayout.CENTER);
        primaryPanel.updateUI();
    }

    private class LoginComponent extends JPanel implements Listener {

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

        public LoginComponent() {
            this.setBackground(Color.white);
            this.setLayout(new MigLayout("fillx, filly, insets 0, center"));

            JPanel container = ViewUtil.getClearPanel();
            container.setLayout(new MigLayout());

            JPanel leftSide = ViewUtil.getClearPanel();
            leftSide.setLayout(new MigLayout("wrap 1, insets 0, center, hidemode 3"));
            ImagePanel splash = new ImagePanel(IconFactory.getInstance().getIcon(IconFactory.ICON_ROOT + "medsavantlogo-flat.png").getImage(), 170,171);
            leftSide.add(splash,"center");
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
        public void handleEvent(Object event) {
            setPageAppropriately();
        }

        private void setPageAppropriately() {

            if (isLoggingIn) {
                System.out.println("SHOWING LOGGING IN PAGE");
                setPage(LOGGING_IN_PAGE);
            } else if (ServerController.getInstance().getServers().isEmpty()) {
                System.out.println("SHOWING NO SERVERS PAGE");
                setPage(NO_SERVER_ATALL_PAGE);
            } else {
                System.out.println("SHOWING LOGIN PAGE");
                setServer(ServerController.getInstance().getCurrentServer());
                setPage(LOGIN_PAGE);
            }
        }

        private JPanel initNoServerAtAllPanel() {
            JPanel panel = ViewUtil.getClearPanel();
            panel.setLayout(new MigLayout("gapx 0, gapy 5, wrap 1, insets 20 0 0 0"));

            JLabel welcomeLabel;
            panel.add(welcomeLabel = new JLabel("Welcome to MedSavant"),"wrap, center");
            panel.add(new JLabel("To get started"),"split, center, gapy 20");
            JButton addServerButton;
            panel.add(addServerButton = ViewUtil.getSoftButton("add a server"));
            panel.add(new JLabel("to connect to."));


            addServerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    serverManager.setMode(ServerManagementComponent.ADD_MODE);
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
            panel.add(cancelButton = new JButton("Cancel"),"growx 1.0, right");

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isLoggingIn = false;
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
            panel.add(new JLabel("Connect to"),"split");
            panel.add(serverNameLabel, "wrap");

            serverNameLabel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    serverManager.setMode(ServerManagementComponent.NORMAL_MODE);
                    setPrimaryPanel(serverManager);
                }
            });

            //panel.add(ViewUtil.getEmphasizedLabel("Login".toUpperCase()), "wrap");
            usernameField = new PlaceHolderTextField();
            usernameField.setPlaceholder("Username");
            usernameField.setFont(new Font(ViewUtil.getDefaultFontFamily(), Font.PLAIN, 20));
            usernameField.setForeground(ViewUtil.getSemiBlackColor());
            usernameField.setColumns(14);
            panel.add(usernameField, "wrap");

            passwordField = new PlaceHolderPasswordField();
            passwordField.setPlaceholder("Password");
            passwordField.setFont(new Font(ViewUtil.getDefaultFontFamily(), Font.PLAIN, 20));
            passwordField.setForeground(ViewUtil.getSemiBlackColor());
            passwordField.setColumns(14);
            panel.add(passwordField, "wrap");

            rememberPasswordCheckbox = ViewUtil.getSwitchCheckBox("Remember Password");
            panel.add(rememberPasswordCheckbox, "split");

            JButton signInButton = new JButton("Sign In");
            signInButton.setFocusable(false);

            signInButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // save username
                    // save password if appropriate

                    // login , present errors

                    isLoggingIn = true;
                    setPageAppropriately();
                }
            });

            panel.add(signInButton, "wrap");

            return panel;
        }

        public void setServer(MedSavantServerInfo server) {
            if (server == null) { return; }

            usernameField.setText(server.getUsername());
            passwordField.setText(server.getPassword());
            rememberPasswordCheckbox.setSelected(server.isRememberPassword());
            serverNameLabel.setText(ViewUtil.ellipsize(server.getNickname(),25));
        }

    }

    private class ServerManagementComponent extends JPanel {

        private int mode;
        private SourceListModel listModel;
        private SourceList sourceList;
        private NiceMenu topMenu;

        private NiceMenu bottomMenu;
        private JButton adminButton;
        private JButton adminCancelButton;
        private JButton createDBButton;
        private JButton deleteDBButton;
        private JButton cancelButton;
        private JButton saveButton;
        private JButton addButton;
        private JButton chooseButton;
        private JButton removeButton;

        private final static int NORMAL_MODE = 0;
        private final static int ADMIN_MODE = 1;
        private final static int EDIT_MODE = 2;
        private final static int ADD_MODE = 3;
        private Component[] adminComponents;
        private Component[] normalComponents;
        private Component[] editComponents;

        // edit form
        private NiceForm form;

        // whether this edits an existing server or adds a new one
        private boolean isAdding;

        // fields in this form
        private NiceFormField nameField;
        private NiceFormField hostField;
        private NiceFormField portField;
        private NiceFormField databaseField;
        private NiceFormField usernameField;
        private NiceFormField passwordField;
        private NiceFormField rememberPasswordField;

        // server to edit
        private MedSavantServerInfo server;

        // forms
        private JPanel editForm;
        private JPanel adminForm;
        private JPanel normalForm;
        private JPanel serverInfoContainer;

        public ServerManagementComponent(){
            initUI();
            mode = EDIT_MODE;
            refreshUI();
        }

        public void setMode(int mode) {
            this.mode = mode;
            refreshUI();
        }

        private void refreshUI() {

            hideComponents(adminComponents, editComponents, normalComponents);
            switch (mode) {
                case NORMAL_MODE:
                    showComponents(normalComponents);
                    updateNormalForm();
                    topMenu.setTitle("Server Management");
                    break;
                case EDIT_MODE:
                    isAdding = false;
                    updateEditForm();
                    showComponents(editComponents);
                    topMenu.setTitle("Edit Server");
                    break;
                case ADD_MODE:
                    isAdding = true;
                    setServerToEdit(new MedSavantServerInfo());
                    updateEditForm();
                    showComponents(editComponents);
                    topMenu.setTitle("Add Server");
                    break;
                case ADMIN_MODE:
                    showComponents(adminComponents);
                    updateAdminForm();
                    topMenu.setTitle("Server Administration");
                    break;
            }
        }



        private void showComponents(Component[]... components) {
            setVisibilityForComponents(true,components);
        }

        private void hideComponents(Component[]... components) {
            setVisibilityForComponents(false, components);
        }

        private void setVisibilityForComponents(boolean visible, Component[]... components) {
            for (Component[] cs : components) {
                for (Component c : cs) {
                    c.setVisible(visible);
                }
            }

            this.invalidate();
        }

        private void setServerToEdit(MedSavantServerInfo server) {
            this.server = server;
        }

        private void initUI() {

            this.setBackground(Color.white);
            this.setLayout(new BorderLayout());

            /**
             * MENUS
             */
            topMenu = new NiceMenu();
            bottomMenu = new NiceMenu(NiceMenu.MenuLocation.BOTTOM);
            
            this.add(topMenu,BorderLayout.NORTH);
            this.add(bottomMenu,BorderLayout.SOUTH);


            /**
             * CENTRAL CONTAINER
             */
            JPanel container = ViewUtil.getClearPanel();
            container.setLayout(new MigLayout("insets 0, fillx, filly, center, hidemode 3"));

            editForm = ViewUtil.getClearPanel();
            adminForm = ViewUtil.getClearPanel();
            normalForm = ViewUtil.getClearPanel();

            container.add(editForm, "growx 1.0, growy 1.0");
            container.add(adminForm, "growx 1.0, growy 1.0");
            container.add(normalForm, "growx 1.0, growy 1.0");

            this.add(container, BorderLayout.CENTER);

            /**
             * NORMAL FORM
             */

            normalForm.setLayout(new MigLayout("insets 0, fillx, filly, center, hidemode 3, gapx 0, gapy 0"));
            listModel = new SourceListModel();

            sourceList = new SourceList(listModel);
            sourceList.getComponent().setBorder(ViewUtil.getRightLineBorder());

            normalForm.add(sourceList.getComponent(), "width 250, growy 1.0");

            serverInfoContainer = ViewUtil.getClearPanel();

            normalForm.add(serverInfoContainer,"growx 1.0, growy 1.0");

            /**
             * EDIT FORM
             * refreshes everything dynamically
             */

            /**
             * ADMIN FORM
             * refreshes everything dynamically
             */

            /**
             * BUTTONS
             */
            // admin buttons
            bottomMenu.addLeftComponent(createDBButton = ViewUtil.getTexturedButton("Create Database"));
            bottomMenu.addLeftComponent(deleteDBButton = ViewUtil.getTexturedButton("Delete Database"));
            bottomMenu.addRightComponent(adminCancelButton = ViewUtil.getTexturedButton("Cancel"));
            adminComponents = new Component[] { adminForm, adminCancelButton, createDBButton, deleteDBButton };

            // editing buttons
            bottomMenu.addRightComponent(cancelButton = ViewUtil.getTexturedButton("Cancel"));
            bottomMenu.addRightComponent(saveButton = ViewUtil.getTexturedButton("Save"));
            editComponents = new Component[] { editForm, cancelButton, saveButton };

            // normal
            topMenu.addLeftComponent(adminButton = ViewUtil.getTexturedButton("Admin"));
            bottomMenu.addLeftComponent(addButton = ViewUtil.getTexturedButton("Add"));
            bottomMenu.addLeftComponent(removeButton = ViewUtil.getTexturedButton("Remove"));
            bottomMenu.addRightComponent(chooseButton = ViewUtil.getTexturedButton("Done"));
            normalComponents = new Component[] { normalForm, addButton, adminButton, removeButton, chooseButton };

            /**
             * NORMAL BUTTON ACTIONS
             */
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMode(ADD_MODE);
                }
            });

            chooseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginComponent.setPageAppropriately();
                    setPrimaryPanel(loginComponent);
                }
            });

            /**
             * NORMAL BUTTON ACTIONS
             */
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMode(ADD_MODE);
                }
            });

            /**
             * ADMIN BUTTON ACTIONS
             */

            adminCancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMode(NORMAL_MODE);
                }
            });

            /**
             * EDIT BUTTON ACTIONS
             */

            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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

                            if (isAdding) {
                                if (!ServerController.getInstance().isServerNamed(name)) {
                                    ServerController.getInstance().addServer(server);
                                } else {
                                    DialogUtils.displayMessage("There's already a server named " + name + ".");
                                }
                            }
                            ServerController.getInstance().setCurrentServer(server);

                            setPrimaryPanel(loginComponent);
                        }
                    }

                    setMode(NORMAL_MODE);
                }
            });

            adminButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMode(ADMIN_MODE);
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMode(NORMAL_MODE);
                }
            });
        }

        private void updateAdminForm() {
        }

        private void updateNormalForm() {

            // remove all existing categories
            while (!listModel.getCategories().isEmpty()) {
                listModel.removeCategoryAt(0);
            }

            SourceListCategory category = new SourceListCategory("Servers");
            listModel.addCategory(category);


            List<MedSavantServerInfo> servers = ServerController.getInstance().getServers();

            for (MedSavantServerInfo server : servers) {
                SourceListItem item = new SourceListItem(server.getNickname());
                listModel.addItemToCategory(item,category);
            }

        }

        private void updateEditForm() {

            editForm.removeAll();
            editForm.setLayout(new MigLayout("center, fillx, filly"));

            NiceFormModel model = new NiceFormModel();

            model.addField(nameField = new NiceFormField(true, "Name", NiceForm.FieldType.STRING, server.getNickname()));
            model.addField(hostField = new NiceFormField(true, "Hostname", NiceForm.FieldType.STRING, server.getHost()));
            model.addField(portField = new NiceFormField(true, "Port", NiceForm.FieldType.NUMBER, server.getPort()));
            model.addField(databaseField = new NiceFormField(true, "Database", NiceForm.FieldType.STRING, server.getDatabase()));
            model.addField(usernameField = new NiceFormField(true, "Username", NiceForm.FieldType.STRING, server.getUsername()));
            model.addField(passwordField = new NiceFormField(true, "Password", NiceForm.FieldType.PASSWORD, server.getPassword()));
            model.addField(rememberPasswordField = new NiceFormField(true, "Remember password", NiceForm.FieldType.BOOLEAN, server.isRememberPassword()));

            form = new NiceForm(model);

            editForm.add(form);
        }
    }
}