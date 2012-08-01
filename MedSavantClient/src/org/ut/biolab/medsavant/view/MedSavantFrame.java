/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import java.awt.Color;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.login.LoginEvent;
import org.ut.biolab.medsavant.plugin.PluginManagerDialog;
import org.ut.biolab.medsavant.serverapi.MedSavantProgramInformation;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class MedSavantFrame extends JFrame implements Listener<LoginEvent> {
    private static final String LOGIN_CARD_NAME = "login";
    private static final String SESSION_VIEW_CARD_NAME = "main";
    private static final String WAIT_CARD_NAME = "wait";

    private static MedSavantFrame instance;

    private JPanel view;
    private CardLayout viewCardLayout;
    private JPanel sessionView;
    private JMenuItem logOutItem;
    private LoginView loginView;
    private BottomBar bottomBar;

    private String currentCard;
    private boolean queuedForExit = false;

    public static MedSavantFrame getInstance() {
        if (instance == null) {
            instance = new MedSavantFrame();
            LoginController.getInstance().addListener(instance);
        }
        return instance;
    }

    private MedSavantFrame() {
        super("MedSavant");

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(500,500));

        view = new JPanel();
        view.setBackground(new Color(217,222,229));
        viewCardLayout = new CardLayout();
        view.setLayout(viewCardLayout);

        add(view, BorderLayout.CENTER);

        if (ClientMiscUtils.MAC) {
            customizeForMac();
        }

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem pluginsItem = new JMenuItem("Plugins…");
        pluginsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                PluginManagerDialog.getInstance().setVisible(true);
            }

        });

        logOutItem = new JMenuItem("Sign out");
        logOutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginController.getInstance().logout();
            }
        });
        //fileMenu.add(manageDBItem);
        fileMenu.add(pluginsItem);
        fileMenu.add(logOutItem);
        if (!ClientMiscUtils.MAC) {
            JMenuItem closeItem = new JMenuItem("Exit");
            closeItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    instance.requestClose();
                }
            });
            fileMenu.add(closeItem);
        }
        menu.add(fileMenu);

        setJMenuBar(menu);

        bottomBar = new BottomBar();
        add(bottomBar,BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                requestClose();
            }
        });

        SettingsController settings = SettingsController.getInstance();
        if (settings.getAutoLogin()) {
            LoginController.getInstance().login(settings.getUsername(),
                                                settings.getPassword(),
                                                settings.getDBName(),
                                                settings.getServerAddress(),
                                                settings.getServerPort());
        } else {
            switchToLoginView();
        }
    }

    public void switchToSessionView() {
        if (!LoginController.getInstance().isLoggedIn() || (currentCard != null && currentCard.equals(SESSION_VIEW_CARD_NAME))) {
            return;
        }

        view.add(new WaitPanel("Loading Projects"), WAIT_CARD_NAME);
        switchToView(WAIT_CARD_NAME);

        ClientMiscUtils.invokeLaterIfNecessary(new Runnable() {
            @Override
            public void run() {
                sessionView = new LoggedInView();
                view.add(sessionView, SESSION_VIEW_CARD_NAME);
                logOutItem.setEnabled(true);

                bottomBar.updateLoginStatus();
                switchToView(SESSION_VIEW_CARD_NAME);
            }
        });
    }

    public final void switchToLoginView() {
        if (currentCard != null && currentCard.equals(LOGIN_CARD_NAME)) { return; }
        if (sessionView != null) {
            view.remove(sessionView);
        }

        if (loginView != null) {
            LoginController.getInstance().removeListener(loginView);
        }
        loginView = new LoginView();
        LoginController.getInstance().addListener(loginView);
        view.add(loginView, LOGIN_CARD_NAME);

        logOutItem.setEnabled(false);
        bottomBar.updateLoginStatus();
        switchToView(LOGIN_CARD_NAME);
    }

    private void switchToView(String cardname) {
         viewCardLayout.show(view, cardname);
         currentCard = cardname;
    }

    public void requestClose() {
        LoginController controller = LoginController.getInstance();
        if (!controller.isLoggedIn() || DialogUtils.askYesNo("Exit MedSavant?", "Are you sure you want to quit?") == DialogUtils.YES) {
            queuedForExit = true; //sometimes logout aborts this exit, so wait for event
            //LoginController.logout();
            controller.addLog("Logged out");
            controller.unregister();
            System.exit(0);
        }
    }

    @Override
    public void handleEvent(LoginEvent evt) {
        switch (evt.getType()) {
            case LOGGED_IN:
                switchToSessionView();
                break;
            case LOGGED_OUT:
                switchToLoginView();
                break;
        }
        if (queuedForExit) {
            System.exit(0);
        }
    }

    private void customizeForMac() {

        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MedSavant");


            UIManager.put("TitledBorder.border", UIManager.getBorder("TitledBorder.aquaVariant"));
//            com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(this, true);
            Application macOSXApplication = Application.getApplication();
            macOSXApplication.setAboutHandler(new AboutHandler() {

                @Override
                public void handleAbout(AboutEvent evt) {
                    JOptionPane.showMessageDialog(MedSavantFrame.this, "MedSavant " +
                            MedSavantProgramInformation.getVersion() + " " +
                            MedSavantProgramInformation.getReleaseType() +
                            "\nCreated by Biolab at University of Toronto.");
                }
            });
            macOSXApplication.setPreferencesHandler(new PreferencesHandler() {
                @Override
                public void handlePreferences(PreferencesEvent pe) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            macOSXApplication.setQuitHandler(new QuitHandler() {
                @Override
                public void handleQuitRequestWith(QuitEvent evt, QuitResponse resp) {
                    instance.requestClose();
                    resp.cancelQuit();      // If user accepted close request, System.exit() was called and we never get here.
                }
            });
        } catch (Throwable x) {
            System.err.println("Warning: MedSavant requires Java for Mac OS X 10.6 Update 3 (or later).\nPlease check Software Update for the latest version.");
        }
    }
}
