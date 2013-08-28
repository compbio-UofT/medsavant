package org.ut.biolab.medsavant.client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXCollapsiblePane;
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
    private JXCollapsiblePane connectionSettings;
    private JLabel connectingToLabel;

    public NewLoginView() {
        this.setBackground(new Color(237, 237, 237));//new Color(223,223,223));//Color.white);
        int padding = 100;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        initView();
        initFromCache();
        updateConnectionString();
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

    //int textFieldColumns = 20;
    //int textFieldAdminColumns = 20;
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


        ViewUtil.applyVerticalBoxLayout(this);
        //this.add(Box.createHorizontalGlue());
        this.add(container);
        this.add(Box.createVerticalGlue());
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

    private JXCollapsiblePane getSettingsPanel() {

        JXCollapsiblePane pan = new JXCollapsiblePane();//ViewUtil.getClearPanel();
        pan.setOpaque(false);

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);
        p.setBorder(BorderFactory.createEmptyBorder());

        addressField = new PlaceHolderTextField();
        portField = new PlaceHolderTextField();
        dbnameField = new PlaceHolderTextField();

        addressField.setPlaceholder("server address");
        portField.setPlaceholder("server port number");
        dbnameField.setPlaceholder("database");

        loginOnEnter(addressField);
        loginOnEnter(portField);
        loginOnEnter(dbnameField);

        sizeField(addressField);
        sizeField(portField);
        sizeField(dbnameField);

        updateConnectionStringOnChange(addressField);
        updateConnectionStringOnChange(dbnameField);

        p.add(ViewUtil.alignLeft(addressField));
        p.add(ViewUtil.alignLeft(portField));
        p.add(ViewUtil.alignLeft(dbnameField));

        pan.add(p);

        return pan;
    }

    private void showConnectionSettings() {
        //connectionSettingsButton.setSelected(true);
        connectionSettings.setCollapsed(false);
        //connectionSettingsButton.setText("▲");
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

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

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
        connectionSettingsButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EDIT));//new JButton("▼");//
        connectionSettingsButton.setToolTipText("Connection Settings");
        connectionSettingsButton.setFocusable(false);
        //connectionSettingsButton.setPreferredSize(new Dimension(23,23));
        //connectionSettingsButton.setBorder(null);
        //connectionSettingsButton.setBorderPainted(false);


        connectionSettings = getSettingsPanel();
        connectionSettings.setCollapsed(true);

        connectionSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                connectionSettings.setCollapsed(!connectionSettings.isCollapsed());
                //connectionSettingsButton.setText(connectionSettingsButton.getText().equals("▼") ? "▲" : "▼");
            }
        });

        userField = new PlaceHolderTextField();
        sizeField(userField);
        loginOnEnter(userField);
        userField.setPlaceholder("username");
        passwordField = new PlaceHolderPasswordField();
        loginOnEnter(passwordField);
        passwordField.setPlaceholder("password");
        sizeField(passwordField);

        connectionSettingsButton.setMaximumSize(new Dimension(26, 23));

        connectingToLabel = new JLabel();

        //p.add(ViewUtil.alignLeft(connectionSettingsButton));
        p.add(ViewUtil.horizontallyAlignComponents(new Component[]{connectingToLabel, Box.createHorizontalStrut(4), connectionSettingsButton}));
        p.add(Box.createVerticalStrut(3));
        p.add(ViewUtil.alignLeft(connectionSettings));

        p.add(ViewUtil.alignLeft(userField));
        p.add(ViewUtil.alignLeft(passwordField));
        p.add(ViewUtil.horizontallyAlignComponents(new Component[]{loginButton, progressSigningIn}));

        return p;
    }

    private void updateConnectionString() {
        this.connectingToLabel.setText("<html>Login to <b>" + dbnameField.getText() + "</b> on <b>" + addressField.getText() + "</b></html>");
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

    private void sizeField(JTextField f) {
        f.setColumns(15);
        Dimension d = new Dimension(120, f.getPreferredSize().height);
        f.setMinimumSize(d);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
    }

    private void updateConnectionStringOnChange(JTextField f) {
        f.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent ce) {
                updateConnectionString();
            }

        });

    }
}
