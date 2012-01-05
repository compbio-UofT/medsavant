/*
 *    Copyright 2011 University of Toronto
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
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.MedSavantProgramInformation;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.plugin.PluginManagerDialog;
import org.ut.biolab.medsavant.view.login.LoginView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class MainFrame extends JFrame implements LoginListener {
    private static final String LOGIN_CARD_NAME = "login";
    private static final String SESSION_VIEW_CARD_NAME = "main";
    private static final String WAIT_CARD_NAME = "wait";

    private static MainFrame instance;

    private JPanel view;
    private CardLayout viewCardLayout;
    private JPanel sessionView;
    private JMenuItem logOutItem;
    private LoginView loginView;

    private String currentCard;
    private boolean queuedForExit = false;

    public static MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
            LoginController.addLoginListener(instance);
        }
        return instance;
    }

    private MainFrame() {
        super("MedSavant");

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(500,500));
        
        view = new JPanel();
        viewCardLayout = new CardLayout();
        view.setLayout(viewCardLayout);
        
        add(view, BorderLayout.CENTER);

        if (ViewUtil.isMac()) {
            customizeForMac();
        }

        JMenuBar menu = new JMenuBar();   
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem pluginsItem = new JMenuItem("Plugins…");
        pluginsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                PluginManagerDialog.getInstance().setVisible(true);
            }
            
        });

        logOutItem = new JMenuItem("Sign out");
        logOutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginController.logout();
            }
        });
        //fileMenu.add(manageDBItem);
        fileMenu.add(pluginsItem);
        fileMenu.add(logOutItem);
        if (!ViewUtil.isMac()) {
            JMenuItem closeItem = new JMenuItem("Exit");
            closeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    instance.requestClose();
                }
            });
            fileMenu.add(closeItem);
        }
        menu.add(fileMenu);

        setJMenuBar(menu);

        add(BottomBar.getInstance(),BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                requestClose();
            }
        });

        if (SettingsController.getInstance().getAutoLogin()) {
            LoginController.login(
                    SettingsController.getInstance().getUsername(), 
                    SettingsController.getInstance().getPassword(), 
                    SettingsController.getInstance().getDBName(), 
                    SettingsController.getInstance().getServerAddress(), 
                    SettingsController.getInstance().getServerPort());
        } else {
            switchToLoginView();
        }
        
        FilterController.init();
    }

    public void switchToSessionView() {
        if (!LoginController.isLoggedIn() || (currentCard != null && currentCard.equals(SESSION_VIEW_CARD_NAME))) { return; }
        if (loginView != null) {
            view.remove(loginView);
        }
               
        view.add(new WaitPanel("Loading Projects"), WAIT_CARD_NAME);
        switchToView(WAIT_CARD_NAME);
        
        sessionView = new LoggedInView();
        view.add(sessionView, SESSION_VIEW_CARD_NAME);
        logOutItem.setEnabled(true);

        BottomBar.getInstance().updateLoginStatus();
        switchToView(SESSION_VIEW_CARD_NAME);
    }

    public final void switchToLoginView() {
        if (currentCard != null && currentCard.equals(LOGIN_CARD_NAME)) { return; }
        if (sessionView != null) {
            view.remove(sessionView);
        }
        
        if (loginView != null) {
            LoginController.removeLoginListener(loginView.getLoginForm());
        }
        loginView = new LoginView();
        view.add(loginView, LOGIN_CARD_NAME);
        
        logOutItem.setEnabled(false);
        //manageDBItem.setEnabled(false);
        BottomBar.getInstance().updateLoginStatus();
        switchToView(LOGIN_CARD_NAME);
    }

    private void switchToView(String cardname) {
         viewCardLayout.show(view, cardname);
         currentCard = cardname;
    }
    
    public void requestClose() {
        if (!LoginController.isLoggedIn() || DialogUtils.askYesNo("Exit MedSavant?", "Are you sure you want to quit?") == DialogUtils.YES) {
            queuedForExit = true; //sometimes logout aborts this exit, so wait for event
            LoginController.logout();
            System.exit(0);
        }
    }

    public void loginEvent(LoginEvent evt) {
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

            Application macOSXApplication = Application.getApplication();
            macOSXApplication.setAboutHandler(new AboutHandler() {

                @Override
                public void handleAbout(AboutEvent evt) {
                    JOptionPane.showMessageDialog(MainFrame.this, "MedSavant " + 
                            MedSavantProgramInformation.getVersion() + " " + 
                            MedSavantProgramInformation.getReleaseType() + 
                            "\nCreated by Biolab at University of Toronto.");
                }
            });
            macOSXApplication.setPreferencesHandler(new PreferencesHandler() {
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
