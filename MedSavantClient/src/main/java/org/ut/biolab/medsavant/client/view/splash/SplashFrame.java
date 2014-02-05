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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    public SplashFrame() {
        
        this.setResizable(false);
        
        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        this.setLayout(new MigLayout("filly, insets 0, gapx 0, height 530, width 900, hidemode 3"));

        primaryPanel = new JPanel();

        loginComponent = new LoginComponent();
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
                    setPrimaryPanel(new EditServerComponent());
                }
            });

            welcomeLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));

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
            panel.add(new JLabel("Connect to"),"split, aligny base");
            panel.add(serverNameLabel, "aligny base");

            JButton connectionSettingsButton;
            panel.add(connectionSettingsButton = ViewUtil.getConfigureButton(), "split, gapy 10, wrap");
            connectionSettingsButton.setToolTipText("Server Settings");
            connectionSettingsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ServerController.getInstance().getCurrentServer() != null) {
                        setPrimaryPanel(new EditServerComponent(ServerController.getInstance().getCurrentServer()));
                    }
                }
            });

            //panel.add(ViewUtil.getEmphasizedLabel("Login".toUpperCase()), "wrap");
            usernameField = new PlaceHolderTextField();
            usernameField.setPlaceholder("Username");
            usernameField.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
            usernameField.setForeground(ViewUtil.getSemiBlackColor());
            usernameField.setColumns(14);
            panel.add(usernameField, "wrap");

            passwordField = new PlaceHolderPasswordField();
            passwordField.setPlaceholder("Password");
            passwordField.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
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

    private class EditServerComponent extends JPanel {

        private final JPanel p;
        private final NiceMenu m;
        private final MedSavantServerInfo server;
        private NiceForm form;

        // fields in this form
        private NiceFormField nameField;
        private NiceFormField hostField;
        private NiceFormField portField;
        private NiceFormField databaseField;
        private NiceFormField usernameField;
        private NiceFormField passwordField;
        private NiceFormField rememberPasswordField;

        public EditServerComponent() {
            this(new MedSavantServerInfo(), true);
        }

        public EditServerComponent(MedSavantServerInfo server) {
            this(server, false);
        }

        private EditServerComponent(MedSavantServerInfo info, final boolean isNew) {


            this.setLayout(new BorderLayout());
            this.setBackground(Color.white);

            m = new NiceMenu();

            if (isNew) {
                m.setTitle("Add Server");
            } else {
                m.setTitle("Edit Server Settings");
            }

            m.addRightComponent(ViewUtil.getHelpButton(
                    "Adding a server",
                    "To use MedSavant, you must connect to a server." +
                            " Your administrator will provide you with information required too add a server."));

            this.add(m,BorderLayout.NORTH);

            JPanel centerContainer = ViewUtil.getClearPanel();
            centerContainer.setLayout(new MigLayout("wrap 1, center, hidemode 3, fillx, filly"));

            p = ViewUtil.getClearPanel();
            this.add(centerContainer,BorderLayout.CENTER);

            centerContainer.add(p);

            final NiceMenu bottomMenu = new NiceMenu(NiceMenu.MenuLocation.BOTTOM);

            this.add(bottomMenu,BorderLayout.SOUTH);

            JButton cancelButton = ViewUtil.getTexturedButton("Cancel");
            cancelButton.setFocusable(false);

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setPrimaryPanel(loginComponent);
                }
            });

            JButton saveButton = ViewUtil.getTexturedButton("Save");
            saveButton.setFocusable(false);

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

                            if (isNew) {
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
                }
            });

            //JPanel buttonContainer = new JPanel();//ViewUtil.getClearPanel();
            //buttonContainer.setBackground(new Color(221,221,221));
            //buttonContainer.setLayout(new MigLayout("fillx, alignx trailing, insets 5"));

            final JToggleButton adminButton = ViewUtil.getTexturedToggleButton("Admin");
            m.addLeftComponent(adminButton);


            final NiceMenu adminMenu = new NiceMenu(NiceMenu.MenuLocation.BOTTOM);

            adminMenu.addRightComponent(ViewUtil.getTexturedButton("Create Database"));
            adminMenu.addRightComponent(ViewUtil.getTexturedButton("Delete Database"));

            final EditServerComponent instance = this;

            adminButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean isInAdminMode = adminButton.isSelected();

                    if (isInAdminMode) {
                        instance.remove(bottomMenu);
                        instance.add(adminMenu, BorderLayout.SOUTH);
                    } else {
                        instance.remove(adminMenu);
                        instance.add(bottomMenu,BorderLayout.SOUTH);
                    }

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            instance.invalidate();
                            instance.updateUI();
                        }
                    });
                }
            });

            if (!isNew) {
                JButton deleteButton = ViewUtil.getTexturedButton("Forget this server");
                deleteButton.setFocusable(false);
                deleteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ServerController.getInstance().removeServer(server);
                        setPrimaryPanel(loginComponent);
                    }
                });
                bottomMenu.addLeftComponent(deleteButton);
            }

            bottomMenu.addRightComponent(cancelButton);
            bottomMenu.addRightComponent(saveButton);

            this.server = info;

            refreshUI();
        }

        private void refreshUI() {
            p.removeAll();

            p.setLayout(new MigLayout("insets 30 60 30 30, center, filly"));

            NiceFormModel model = new NiceFormModel();

            model.addField(nameField = new NiceFormField(true, "Name", NiceForm.FieldType.STRING, server.getNickname()));
            model.addField(hostField = new NiceFormField(true, "Hostname", NiceForm.FieldType.STRING, server.getHost()));
            model.addField(portField = new NiceFormField(true, "Port", NiceForm.FieldType.NUMBER, server.getPort()));
            model.addField(databaseField = new NiceFormField(true, "Database", NiceForm.FieldType.STRING, server.getDatabase()));
            model.addField(usernameField = new NiceFormField(true, "Username", NiceForm.FieldType.STRING, server.getUsername()));
            model.addField(passwordField = new NiceFormField(true, "Password", NiceForm.FieldType.PASSWORD, server.getPassword()));
            model.addField(rememberPasswordField = new NiceFormField(true, "Remember password", NiceForm.FieldType.BOOLEAN, server.isRememberPassword()));

            form = new NiceForm(model);

            p.add(form,"center");
        }
    }
}