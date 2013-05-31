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
package org.ut.biolab.medsavant.client.view;

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
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.client.plugin.PluginManagerDialog;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantProgramInformation;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.dialog.FeedbackDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

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

        setIconImage(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.MENU_USER).getImage());

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(500, 500));

        view = new JPanel();
        view.setBackground(new Color(217, 222, 229));
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

        //fileMenu.add(manageDBItem);
        fileMenu.add(pluginsItem);

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

        JMenu helpMenu = new JMenu("Help");

        JMenuItem feedbackItem = new JMenuItem("Feedback");
        feedbackItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog d = new FeedbackDialog(MedSavantFrame.getInstance(), true);
                d.setVisible(true);
            }
        });
        helpMenu.add(feedbackItem);
        menu.add(helpMenu);

        setJMenuBar(menu);

        //bottomBar = new BottomBar();
        //add(bottomBar, BorderLayout.SOUTH);

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

        new MedSavantWorker<Void>("MedSavantFrame") {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Void result) {
            }

            @Override
            protected Void doInBackground() throws Exception {
                sessionView = new LoggedInView();
                view.add(sessionView, SESSION_VIEW_CARD_NAME);

                ViewController.getInstance().getMenu().updateLoginStatus();
                //bottomBar.updateLoginStatus();
                switchToView(SESSION_VIEW_CARD_NAME);
                return null;
            }
        }.execute();

    }

    public final void switchToLoginView() {
        if (currentCard != null && currentCard.equals(LOGIN_CARD_NAME)) {
            return;
        }
        if (sessionView != null) {
            view.remove(sessionView);
        }

        if (loginView != null) {
            LoginController.getInstance().removeListener(loginView);
        }
        loginView = new LoginView();
        LoginController.getInstance().addListener(loginView);
        view.add(loginView, LOGIN_CARD_NAME);

        switchToView(LOGIN_CARD_NAME);

    }

    private void switchToView(String cardname) {
        viewCardLayout.show(view, cardname);
        currentCard = cardname;
    }

    public void requestClose() {
        final LoginController controller = LoginController.getInstance();
        if (!controller.isLoggedIn() || DialogUtils.askYesNo("Exit MedSavant?", "Are you sure you want to quit?") == DialogUtils.YES) {
            controller.unregister();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
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

            batchApplyProperty(new String[]{
                        "Button.font",
                        "ToggleButton.font",
                        "RadioButton.font",
                        "CheckBox.font",
                        "ColorChooser.font",
                        "ComboBox.font",
                        "Label.font",
                        "List.font",
                        "MenuBar.font",
                        "MenuItem.font",
                        "RadioButtonMenuItem.font",
                        "CheckBoxMenuItem.font",
                        "Menu.font",
                        "PopupMenu.font",
                        "OptionPane.font",
                        "Panel.font",
                        "ProgressBar.font",
                        "ScrollPane.font",
                        "Viewport.font",
                        "TabbedPane.font",
                        "Table.font",
                        "TableHeader.font",
                        "TextField.font",
                        "PasswordField.font",
                        "TextArea.font",
                        "TextPane.font",
                        "EditorPane.font",
                        "TitledBorder.font",
                        "ToolBar.font",
                        "ToolTip.font",
                        "Tree.font"}, new Font("Helvetica Neue", Font.PLAIN, 13));

            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            UIManager.put("TitledBorder.border", UIManager.getBorder("TitledBorder.aquaVariant"));
//            com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(this, true);
            Application macOSXApplication = Application.getApplication();
            macOSXApplication.setAboutHandler(new AboutHandler() {
                @Override
                public void handleAbout(AboutEvent evt) {
                    JOptionPane.showMessageDialog(MedSavantFrame.this, "MedSavant "
                            + MedSavantProgramInformation.getVersion() + " "
                            + MedSavantProgramInformation.getReleaseType()
                            + "\nCreated by Biolab at University of Toronto.");
                }
            });
            macOSXApplication.setPreferencesHandler(new PreferencesHandler() {
                @Override
                public void handlePreferences(PreferencesEvent pe) {
                    DialogUtils.displayMessage("Preferences available for Administrators only");
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

    private void batchApplyProperty(String[] propn, Object o) {
        for (String s : propn) {
            UIManager.put(s, o);
        }
    }
}
