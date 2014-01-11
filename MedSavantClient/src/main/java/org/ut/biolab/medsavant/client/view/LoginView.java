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
package org.ut.biolab.medsavant.client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.NoRouteToHostException;
import java.rmi.ConnectIOException;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.db.Settings;

/**
 *
 * @author mfiume
 */
public class LoginView extends JPanel implements Listener<LoginEvent> {

    private static final Log LOG = LogFactory.getLog(LoginView.class);
    private PlaceHolderTextField userField;
    private JButton loginButton;
    private ProgressWheel progressSigningIn;
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
    private JPanel progressPanel;

    public LoginView() {
        //this.setBackground(Color.white);
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
            cancelCurrentLogin();
            if (ex instanceof SQLException) {
                String SQLState = ((SQLException)ex).getSQLState();

                //if (ex.getMessage().contains("Access denied")) {

                if(SQLState.equals(Settings.SQLSTATE_ACCESS_DENIED)){
                    DialogUtils.displayError("Login Error", "<html>Incorrect username and password combination entered.<br><br>Please try again.</html>");
                    //statusLabel.setText("access denied");
                } else {
                    DialogUtils.displayError("Login Error", "<html>Problem contacting server.<br><br>Please contact your administrator.</html>");
                    //statusLabel.setText("problem contacting server");
                }
            } else if (ex instanceof java.rmi.UnknownHostException) {
                DialogUtils.displayError("Login Error", "<html>Problem contacting server.<br><br>Please contact your administrator.</html>");
            } else if(ex instanceof NoRouteToHostException){
                DialogUtils.displayError("Can't connect", "Can't connect to the server at "+addressField.getText()+":"+portField.getText());
                //JOptionPane.showMessageDialog(this, "Can't connect to the server at "+addressField.getText()+":"+portField.getText(), "Can't connect", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //int textFieldColumns = 20;
    //int textFieldAdminColumns = 20;
    private void initView() {

        ImagePanel logo = new ImagePanel(IconFactory.getInstance().getIcon("/org/ut/biolab/medsavant/client/view/images/icon/" + "medsavantlogo.png").getImage(), 128, 128);

        MigLayout ml = new MigLayout("wrap 1");

        JPanel container = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(container);

        JPanel loginForm = getLoginForm();

        container.add(ViewUtil.centerHorizontally(logo));
        container.add(Box.createRigidArea(new Dimension(3, 3)));
        container.add(loginForm);


        //ViewUtil.applyVerticalBoxLayout(this);
        //this.add(Box.createHorizontalGlue());
        this.add(container);
        //this.add(Box.createVerticalGlue());
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

        sizeConnectionField(addressField);
        sizeConnectionField(portField);
        sizeConnectionField(dbnameField);

        updateConnectionStringOnChange(addressField);
        updateConnectionStringOnChange(dbnameField);

        p.add(ViewUtil.centerHorizontally(new JLabel("Connection Settings")));
        p.add(ViewUtil.centerHorizontally(addressField));
        p.add(ViewUtil.centerHorizontally(portField));
        p.add(ViewUtil.centerHorizontally(dbnameField));
        p.add(ViewUtil.centerHorizontally(new JLabel("Login")));

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

    private void cancelCurrentLogin() {
        LoginController.getInstance().cancelCurrentLoginAttempt();
        this.progressPanel.setVisible(false);
        this.progressSigningIn.setVisible(false);
        loginButton.setEnabled(true);
        this.loginButton.setText("Log In");
    }
    private MedSavantWorker loginThread;

    private void loginUsingEnteredUsernameAndPassword() {
        if (validateLoginParams()) {
            SettingsController settings = SettingsController.getInstance();
            settings.setDBName(dbnameField.getText());
            settings.setServerAddress(addressField.getText());
            settings.setServerPort(portField.getText());

            this.loginButton.setText("Logging in...");
            //statusLabel.setText("Logging In...");
            this.progressSigningIn.setVisible(true);
            this.progressPanel.setVisible(true);
            loginButton.setEnabled(false);


            loginThread = new MedSavantWorker<Void>("LoginView") {
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
            };
            loginThread.execute();
        }
    }

    private JPanel getLoginForm() {

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        loginButton = new JButton("Log In");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                loginUsingEnteredUsernameAndPassword();
            }
        });
        progressSigningIn = ViewUtil.getIndeterminateProgressBar();
        progressSigningIn.setVisible(false);

        connectionSettingsButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CONFIGURE));//new JButton("▼");//
        connectionSettingsButton.setToolTipText("Edit Connection Settings");
        connectionSettingsButton.setFocusable(false);


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
        p.add(ViewUtil.centerHorizontally(ViewUtil.getHelpButton("How to log in", "You can login to a MedSavant server using connection settings and credentials supplied by your administrator.<br/><br/>To edit the connection settings, click the gear icon.")));
        p.add(Box.createVerticalStrut(3));
        p.add(ViewUtil.centerHorizontally(ViewUtil.horizontallyAlignComponents(new Component[]{connectingToLabel, Box.createHorizontalStrut(4), connectionSettingsButton})));
        p.add(Box.createVerticalStrut(3));
        p.add(ViewUtil.centerHorizontally(connectionSettings));

        p.add(ViewUtil.centerHorizontally(userField));
        p.add(ViewUtil.centerHorizontally(passwordField));
        p.add(ViewUtil.centerHorizontally(loginButton));
        progressPanel = ViewUtil.getClearPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.add(ViewUtil.centerHorizontally(progressSigningIn));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                cancelCurrentLogin();
            }
        });

        progressPanel.add(ViewUtil.centerHorizontally(cancelButton));
        progressPanel.setVisible(false);
        //p.add(ViewUtil.centerHorizontally(progressSigningIn));
        p.add(ViewUtil.centerHorizontally(progressPanel));

        //p.add(ViewUtil.centerHorizontally(ViewUtil.horizontallyAlignComponents(new Component[]{loginButton, progressSigningIn})));

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

    private void sizeConnectionField(JTextField f) {
        f.setColumns(20);
        Dimension d = new Dimension(200, f.getPreferredSize().height);
        f.setMinimumSize(d);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
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
