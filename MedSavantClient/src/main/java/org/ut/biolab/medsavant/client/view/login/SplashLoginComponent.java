/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.login;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.ServerController;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.util.CryptoUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SplashLoginComponent extends JPanel implements Listener<ServerController> {

    private static Log LOG = LogFactory.getLog(SplashLoginComponent.class);
    
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
    private SplashFrame splash;
    private SplashServerManagementComponent serverManager;

    public SplashLoginComponent(final SplashFrame splash, SplashServerManagementComponent serverManager) {
        this.setBackground(Color.white);
        this.setLayout(new MigLayout("fillx, filly, insets 0, center"));

        this.serverManager = serverManager;
        this.splash = splash;
        
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
                        splash.closeSplashFrame();

                        break;
                    case LOGGED_OUT:
                        MedSavantFrame frame2 = MedSavantFrame.getInstance();
                        frame2.setVisible(false);
                        break;
                    case LOGIN_FAILED:
                        LOG.info("Login failed");
                        
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
        ImagePanel logo = new ImagePanel(IconFactory.getInstance().getIcon(IconFactory.ICON_ROOT + "medsavantlogo-flat.png").getImage(), 170, 171);
        leftSide.add(logo, "center");
        container.add(leftSide, "width 180");

        JPanel rightSide = ViewUtil.getClearPanel();
        rightSide.setLayout(new MigLayout("wrap 1, hidemode 3"));

        ServerController.getInstance().addListener(this);

        noServerAtAllPanel = initNoServerAtAllPanel();
        loginSettingsPanel = initLoginSettingsPanel();
        loggingInPanel = initLoggingInPanel();

        rightSide.add(noServerAtAllPanel);
        rightSide.add(loginSettingsPanel);
        rightSide.add(loggingInPanel);

        setPageAppropriately();

        container.add(rightSide, "width 250");

        this.add(container, "center, growx 1.0");

    }

    @Override
    public void handleEvent(ServerController event) {
        setPageAppropriately();
    }

    boolean isFirstTime = true;

    public void setPageAppropriately() {

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
                splash.switchToServerManager();
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
                
                LOG.info("Cancelling login");
                
                isLoggingIn = false;
                loginThread.cancel(true);

                LoginController.getInstance().cancelCurrentLoginAttempt();

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
        
        if (SwingUtilities.isEventDispatchThread()) {
            this.updateUI();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    
                    @Override
                    public void run() {
                        SplashLoginComponent.this.updateUI();
                    }
                    
                });
            } catch (Exception ex) {
                LOG.error(ex);
            }
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
                serverManager.getServerList().selectItemWithKey(serverNameLabel.getText());
                splash.switchToServerManager();
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

        loginThread.execute();
    }

    public void setServer(MedSavantServerInfo server) {
        if (server == null) {
            return;
        }
        if(server.getPassword() == null){
            return;
        }
        this.server = server;

        //System.out.println("Setting server with username "+server.getUsername()+" and pw "+server.getPassword());        
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
