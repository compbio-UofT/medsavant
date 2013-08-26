package org.ut.biolab.medsavant.client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.client.settings.VersionSettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.manage.AddRemoveDatabaseDialog;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NewLoginView extends JPanel implements Listener<LoginEvent> {

    private static final Log LOG = LogFactory.getLog(NewLoginView.class);
    private PlaceHolderTextField userField;
    private JButton loginButton;
    private JProgressBar progressSigningIn;
    private JLabel medsavantTitle;
    private JLabel medsavantTagline;
    private LoginController controller = LoginController.getInstance();
    private PlaceHolderPasswordField passwordField;
    private PlaceHolderTextField addressField;
    private PlaceHolderTextField portField;
    private PlaceHolderTextField dbnameField;
    private PlaceHolderTextField addressAdminField;
    private PlaceHolderTextField portAdminField;
    private PlaceHolderTextField dbnameAdminField;
    private PlaceHolderTextField userAdminField;
    private PlaceHolderPasswordField passwordAdminField;
    private JButton connectionSettingsButton;
    private JPanel connectionSettings;

    public NewLoginView() {
        this.setBackground(new Color(237, 237, 237));//new Color(223,223,223));//Color.white);
        int padding = 100;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        initView();
        initFromCache();
    }

    @Override
    public void handleEvent(LoginEvent event) {
        switch (event.getType()) {
            case LOGGED_IN:
                break;
            case LOGGED_OUT:
                break;
            case LOGIN_FAILED:
                notifyOfUnsuccessfulLogin(event.getException());
                break;
        }
    }

    private void notifyOfUnsuccessfulLogin(Exception ex) {

        userField.requestFocus();
        loginButton.setEnabled(true);
        this.progressSigningIn.setVisible(false);
        this.loginButton.setText("Log In");

        // Exception may be null if we got here as a result of a failed version check.
        if (ex != null) {
            //statusLabel.setText("login error");
            LOG.error("Problem contacting server.", ex);
            if (ex instanceof SQLException) {
                if (ex.getMessage().contains("Access denied")) {
                    DialogUtils.displayError("Login Error", "<html>Incorrect username and password combination entered.<br><br>Please try again.</html>");
                    //statusLabel.setText("access denied");
                } else {
                    DialogUtils.displayError("Login Error", "<html>Problem contacting server.<br><br>Please contact your administrator.</html>");
                    //statusLabel.setText("problem contacting server");
                }
            } else if (ex instanceof java.rmi.UnknownHostException) {
                DialogUtils.displayError("Login Error", "<html>Problem contacting server.<br><br>Please contact your administrator.</html>");
            }
        }
    }
    int textFieldColumns = 15;
    int textFieldSettingsColumns = 20;

    private void initView() {

        ImagePanel logo = new ImagePanel(IconFactory.getInstance().getIcon("/org/ut/biolab/medsavant/client/view/images/icon/" + "ms-logo-wtext-original.png").getImage(), 450, 90);
        //ImagePanel logo = new ImagePanel(IconFactory.getInstance().getIcon("/org/ut/biolab/medsavant/client/view/images/icon/" + "ms-logo.png").getImage(),149,76);
        //ImagePanel logo = new ImagePanel(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOGO).getImage(),128,128);
        JLabel versionLabel = new JLabel("VERSION " + VersionSettings.getVersionString());
        ViewUtil.setFontSize(versionLabel, 10);

        medsavantTitle = new JLabel("MedSavant");
        ViewUtil.setFontSize(medsavantTitle, 39);

        medsavantTagline = new JLabel("stop browsing start searching");
        ViewUtil.setFontSize(medsavantTagline, 20);

        MigLayout ml = new MigLayout();

        JPanel container = ViewUtil.getClearPanel();//new JPanel(); container.setBackground(Color.yellow);
        container.setLayout(ml);

        JPanel loginForm = getLoginForm();

        container.add(logo, "wrap");
        container.add(Box.createVerticalStrut(10), "wrap");
        container.add(loginForm);

        ViewUtil.applyHorizontalBoxLayout(this);
        //this.add(Box.createHorizontalGlue());
        this.add(container);
        this.add(Box.createHorizontalGlue());

    }

    private void initFromCache() {
        userField.setText(controller.getUserName());
        passwordField.setText(controller.getPassword());

        SettingsController settings = SettingsController.getInstance();
        userField.setText(settings.getUsername());
        if (settings.getRememberPassword()) {
            passwordField.setText(settings.getPassword());
        }

        dbnameField.setText(settings.getDBName());
        portField.setText(settings.getServerPort());
        addressField.setText(settings.getServerAddress());
    }

    private JPanel getAdminPanel() {
        MigLayout ml = new MigLayout();
        final JPanel p = new JPanel(ml);
        p.setBackground(Color.white);

        addressAdminField = new PlaceHolderTextField();
        addressAdminField.setPlaceholder("address");
        addressAdminField.setText(addressField.getText());
        addressAdminField.setColumns(textFieldSettingsColumns);
        portAdminField = new PlaceHolderTextField();
        portAdminField.setPlaceholder("port number");
        portAdminField.setText(portField.getText());
        portAdminField.setColumns(textFieldSettingsColumns);
        dbnameAdminField = new PlaceHolderTextField();
        dbnameAdminField.setPlaceholder("database");
        dbnameAdminField.setColumns(textFieldSettingsColumns);
        userAdminField = new PlaceHolderTextField();
        userAdminField.setPlaceholder("admin username");
        userAdminField.setColumns(textFieldSettingsColumns);
        passwordAdminField = new PlaceHolderPasswordField();
        passwordAdminField.setPlaceholder("password");
        passwordAdminField.setColumns(textFieldSettingsColumns);

        JButton addDB = new JButton("Create");
        JButton removeDB = new JButton("Delete");

        p.add(ViewUtil.makeSmall(new JLabel("Administrators only")), "wrap");
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

    private void closeDialogContainingPanel(Component c) {
        if (c instanceof JDialog) {
            ((JDialog)c).setVisible(false);
        } else {
            closeDialogContainingPanel(c.getParent());
        }
    }

    private boolean validateAdminParams() {



        if (addressAdminField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Server address required");
            showConnectionSettings();
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

    private JPanel getSettingsPanel() {

        MigLayout ml = new MigLayout("insets 0");
        JPanel p = new JPanel(ml);
        p.setBorder(BorderFactory.createEmptyBorder());
        //p.setBorder(null);
        //p.setOpaque(false);

        addressField = new PlaceHolderTextField();
        loginOnEnter(addressField);
        addressField.setPlaceholder("address");
        addressField.setColumns(textFieldSettingsColumns);
        portField = new PlaceHolderTextField();
        loginOnEnter(portField);
        portField.setPlaceholder("port number");
        portField.setColumns(textFieldSettingsColumns);
        dbnameField = new PlaceHolderTextField();
        loginOnEnter(dbnameField);
        dbnameField.setPlaceholder("database");
        dbnameField.setColumns(textFieldSettingsColumns);

        JButton addDB = new JButton("Create");
        JButton removeDB = new JButton("Delete");

        p.add(new JLabel("Server Settings"), "wrap, gapx 0, gapy 0");
        p.add(addressField, "wrap");
        p.add(portField, "wrap");
        p.add(dbnameField, "wrap");

        return p;
    }

    private void showConnectionSettings() {
        //connectionSettingsButton.setSelected(true);
        connectionSettings.setVisible(true);
    }

    private void loginOnEnter(JTextField f) {
        f.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                int key = ke.getKeyCode();
                if (key == ke.VK_ENTER) {
                    loginUsingEnteredUsernameAndPassword();
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }
        });
    }

    private void loginUsingEnteredUsernameAndPassword() {

        if (validateLoginParams()) {



            SettingsController settings = SettingsController.getInstance();
            settings.setDBName(dbnameField.getText());
            settings.setServerAddress(addressField.getText());
            settings.setServerPort(portField.getText());

            this.loginButton.setText("Logging in...");
            //statusLabel.setText("Logging In...");
            this.progressSigningIn.setVisible(true);

            loginButton.setEnabled(false);


            new MedSavantWorker<Void>("LoginView") {
                @Override
                protected void showProgress(double fract) {
                }

                @Override
                protected void showSuccess(Void result) {
                }

                @Override
                protected Void doInBackground() throws Exception {
                    LoginController.getInstance().login(userField.getText(), passwordField.getText(), dbnameField.getText(), addressField.getText(), portField.getText());
                    return null;
                }
            }.execute();

            /*SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {

             }
             });*/
        }
    }

    private JPanel getLoginForm() {

        JPanel p = new JPanel();
        p.setLayout(new MigLayout());

        loginButton = new JButton("Log In");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Logging in...");
                loginUsingEnteredUsernameAndPassword();
            }
        });
        progressSigningIn = ViewUtil.getIndeterminateProgressBar();
        progressSigningIn.setVisible(false);

        //final JToggleButton connectionSettingsButton = new JToggleButton("Settings");
        connectionSettingsButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.NETWORK));

        final JButton adminButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.GEAR));
        adminButton.setBorderPainted(false);
        adminButton.setFocusable(false);
        adminButton.putClientProperty("JButton.buttonType", "textured");
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog adminDialog = new JDialog(MedSavantFrame.getInstance(), "Database Management", true);
                adminDialog.add(getAdminPanel());
                adminDialog.setResizable(false);
                adminDialog.pack();
                adminDialog.setLocationRelativeTo(MedSavantFrame.getInstance());
                adminDialog.setVisible(true);
            }
        });

        connectionSettings = getSettingsPanel();
        connectionSettings.setVisible(false);

        connectionSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                connectionSettings.setVisible(!connectionSettings.isVisible());
            }
        });

        userField = new PlaceHolderTextField();
        loginOnEnter(userField);
        userField.setPlaceholder("username");
        userField.setColumns(textFieldColumns);
        passwordField = new PlaceHolderPasswordField();
        loginOnEnter(passwordField);
        passwordField.setPlaceholder("password");
        passwordField.setColumns(textFieldColumns);

        connectionSettingsButton.setMaximumSize(new Dimension(26, 23));
        adminButton.setMaximumSize(new Dimension(26, 23));

        p.add(ViewUtil.makeSmall(connectionSettingsButton), "split 2");
        p.add(ViewUtil.makeSmall(adminButton), "gapx 2");
        p.add(connectionSettings, "wrap, gapx 20, span 1 4");
        p.add(userField, "wrap, gapy 8");
        p.add(passwordField, "wrap");
        p.add(loginButton, "split2");
        p.add(progressSigningIn, "wrap");

        return p;
    }

    private boolean validateLoginParams() {

        if (userField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Username required");
            userField.requestFocus();
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Password required");
            passwordField.requestFocus();
            return false;
        }

        if (addressField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Server address required");
            showConnectionSettings();
            addressField.requestFocus();
            return false;
        }

        if (portField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Server port required");
            showConnectionSettings();
            portField.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(portField.getText());
        } catch (Exception e) {
            MedSavantFrame.getInstance().notificationMessage("Invalid port number");
            showConnectionSettings();
            portField.requestFocus();
            return false;
        }

        if (dbnameField.getText().isEmpty()) {
            MedSavantFrame.getInstance().notificationMessage("Database name required");
            showConnectionSettings();
            dbnameField.requestFocus();
            return false;
        }

        return true;
    }
}
