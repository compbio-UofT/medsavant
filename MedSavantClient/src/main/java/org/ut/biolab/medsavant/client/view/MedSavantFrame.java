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

import org.ut.biolab.medsavant.client.view.dialog.AdminDialog;
import org.ut.biolab.medsavant.client.view.animation.AnimatablePanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.explodingpixels.macwidgets.MacUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.ColorUIResource;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.client.plugin.PluginManagerDialog;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantProgramInformation;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.animation.IconTranslatorAnimation;
import org.ut.biolab.medsavant.client.view.animation.NotificationAnimation;
import org.ut.biolab.medsavant.client.view.animation.NotificationAnimation.Position;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.dialog.FeedbackDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.app.jAppStore;
import org.ut.biolab.medsavant.client.app.MedSavantAppFetcher;
import org.ut.biolab.medsavant.client.app.MedSavantAppInstaller;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.settings.VersionSettings;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;

/**
 *
 * @author mfiume
 */
public class MedSavantFrame extends JFrame implements Listener<LoginEvent> {

    private static final String LOGIN_CARD_NAME = "login";
    private static final String SESSION_VIEW_CARD_NAME = "main";
    private static final String WAIT_CARD_NAME = "wait";
    private static final long SEARCH_ANIMATION_RUNTIME = 350;
    private static MedSavantFrame instance;
    private AnimatablePanel view;
    private CardLayout viewCardLayout;
    private JPanel sessionView;
    private NewLoginView loginView;
    private String currentCard;
    private boolean queuedForExit = false;

    private static Point getPositionRelativeTo(Component root, Component comp) {
        if (comp.equals(root)) {
            return new Point(0, 0);
        }
        Point pos = comp.getLocation();
        Point parentOff = getPositionRelativeTo(root, comp.getParent());
        return new Point(pos.x + parentOff.x, pos.y + parentOff.y);
    }
    private int textFieldAdminColumns = 20;
    private jAppStore appStore;

    public void translationAnimation(Point src, Point dst, ImageIcon img, final String notificationMsg) {
        if (src != null && dst != null) {
            view.animate(new IconTranslatorAnimation(img.getImage(), src, dst, SEARCH_ANIMATION_RUNTIME) {
                public void done() {
                    if (notificationMsg != null) {
                        notificationMessage(notificationMsg);
                    }
                }
            });
        }
    }

    public void translationAnimation(Component srcComponent, Component dstComponent, ImageIcon img, String notificationMsg) {
        Point src = getPositionRelativeTo(view, srcComponent);
        Point dst = getPositionRelativeTo(view, dstComponent);
        translationAnimation(src, dst, img, notificationMsg);
    }

    public void animationFromMousePos(Component dstComponent, ImageIcon img, final String notificationMsg) {
        translationAnimation(view, dstComponent, img, notificationMsg);
    }

    public void browserAnimationFromMousePos(final String notificationMsg) {

        /*Point src = view.getMousePosition();
         if(src == null){
         return;
         }

         Point dst = null;
         Enumeration<AbstractButton> e = ViewController.getInstance().getMenu().primaryMenuButtons.getElements();
         */
        ImageIcon img = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_SEARCH);
        Component dstComponent = null;
        Menu menu = ViewController.getInstance().getMenu();
        for (SubSectionView sv : menu.subSectionViews) {
            if (sv.getPageName().equalsIgnoreCase("Browser")) {
                //dstComponent = getPositionRelativeTo(view, menu.getSubSectionButton(sv));
                dstComponent = menu.getSubSectionButton(sv);
                break;
            }
        }

        animationFromMousePos(dstComponent, img, notificationMsg);
        if (dstComponent != null) {
            animationFromMousePos(dstComponent, img, notificationMsg);
        }

        /*
         if(dst != null){
         //view.cancel();
         view.animate(new IconTranslatorAnimation(img.getImage(), src, dst, SEARCH_ANIMATION_RUNTIME){
         public void done(){
         if(notificationMsg != null){
         //view.animate(new NotificationAnimation(notificationMsg, view, Position.TOP_CENTER));
         notificationMessage(notificationMsg);
         }
         }
         });



         //view.animate(img.getImage(), src, dst);
         }
         */
    }

    public void notificationMessage(String notificationMsg) {
        view.animate(new NotificationAnimation(notificationMsg, view, Position.TOP_CENTER));
    }

    /**
     * Creates a search animation from the current mouse position. i.e. creates
     * an image that moves from the current mouse position to the 'Variants'
     * button on the toolbar.
     *
     * Do not call until the previous animation thread is finished. This
     * shouldn't happen as long as the animation time is short. (< 500ms ).
     */
    public void searchAnimationFromMousePos(final String notificationMsg) {
        ImageIcon img = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_SEARCH);
        /*Point src = view.getMousePosition();
         if(src == null){
         return;
         }

         Point dst = null;
         */
        Component dstComponent = null;
        Enumeration<AbstractButton> e = ViewController.getInstance().getMenu().primaryMenuButtons.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (b.getName().equalsIgnoreCase("Variants")) {
                //dst = getPositionRelativeTo(view, b);
                dstComponent = b;
            }
        }
        animationFromMousePos(dstComponent, img, notificationMsg);
        /*
         if(dst != null){
         //view.cancel();

         view.animate(new IconTranslatorAnimation(img.getImage(), src, dst, SEARCH_ANIMATION_RUNTIME){
         public void done(){
         if(notificationMsg != null){
         notificationMessage(notificationMsg);
         //view.animate(new NotificationAnimation(notificationMsg, view, Position.TOP_CENTER));
         }
         }
         });

         }
         */
    }

    public void searchAnimationFromMousePos() {
        searchAnimationFromMousePos(null);
    }

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
        setMinimumSize(new Dimension(550, 550));

        view = new AnimatablePanel();
        view.setDoubleBuffered(true);
        //view = new JPanel();
        view.setBackground(new Color(217, 222, 229));
        viewCardLayout = new CardLayout();
        view.setLayout(viewCardLayout);
        //view.setBorder(BorderFactory.createLineBorder(Color.red, 2));

        UIManager.put("ToolTip.background", Color.black);
        UIManager.put("ToolTip.foreground", Color.white);
        UIManager.put("ToolTip.border", ViewUtil.getMediumBorder());
        UIManager.put("ToolTip.font", ViewUtil.detailFontBold);
        UIManager.put("Table.gridColor", new Color(250, 250, 250));

        add(view, BorderLayout.CENTER);

        if (ClientMiscUtils.MAC) {
            customizeForMac();
        }

        AppController pc = AppController.getInstance();
        pc.loadPlugins(DirectorySettings.getPluginsDirectory());

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem pluginsItem = new JMenuItem("Plugins…");
        pluginsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                PluginManagerDialog.getInstance().setVisible(true);
            }
        });
        //fileMenu.add(pluginsItem);

        JMenuItem dbManagementItem = new JMenuItem("Database Management");
        dbManagementItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog adminDialog = new AdminDialog();
                adminDialog.setVisible(true);
            }
        });
        fileMenu.add(dbManagementItem);


        JMenuItem appItem = new JMenuItem("App Store");
        appItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {


                showAppStore();
            }
        });
        fileMenu.add(appItem);

        // Debug code that adds a 'Restart' function to the File menu.
        /*
         JMenuItem restartItem = new JMenuItem("Restart");
         restartItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent ae) {
         MedSavantClient.restart();
         }
         });
         fileMenu.add(restartItem);
         */


        //fileMenu.add(manageDBItem);


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
        loginView = new NewLoginView();
        LoginController.getInstance().addListener(loginView);
        view.add(loginView, LOGIN_CARD_NAME);

        switchToView(LOGIN_CARD_NAME);

    }

    private void switchToView(String cardname) {
        viewCardLayout.show(view, cardname);
        currentCard = cardname;
    }

    public void requestClose() {
        System.out.print("Asking to quit...");
        final LoginController controller = LoginController.getInstance();
        if (!controller.isLoggedIn() || DialogUtils.askYesNo("Quit MedSavant", "Are you sure you want to quit?") == DialogUtils.YES) {
            controller.logout();
            /*controller.unregister();
             try {
             Thread.sleep(100);
             } catch (InterruptedException ex) {
             }
             System.exit(0);*/
        }
        System.out.print("NOT QUITTING!");
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
            MacUtils.makeWindowLeopardStyle(this.getRootPane());
            UIManager.put("Panel.background", new Color(237, 237, 237)); // the above line makes the bg dark, setting back

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
                            + MedSavantProgramInformation.getVersion()
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
                    System.out.println("Requesting close...");
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

    void showAppStore() {

        if (appStore == null) {
            final MedSavantAppFetcher maf = new MedSavantAppFetcher();
            final MedSavantAppInstaller mai = new MedSavantAppInstaller();

            appStore = new jAppStore("MedSavant App Store", maf, mai);
        }
        appStore.showStore();

    }
}
