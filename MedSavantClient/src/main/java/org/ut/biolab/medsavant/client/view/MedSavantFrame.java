/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
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
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Point;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.client.plugin.PluginManagerDialog;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.animation.IconTranslatorAnimation;
import org.ut.biolab.medsavant.client.view.animation.NotificationAnimation;
import org.ut.biolab.medsavant.client.view.animation.NotificationAnimation.Position;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.app.jAppStore;
import org.ut.biolab.medsavant.client.app.MedSavantAppFetcher;
import org.ut.biolab.medsavant.client.app.MedSavantAppInstaller;
import org.ut.biolab.medsavant.client.plugin.AppController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.util.VersionSettings;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.medsavant.complex.ComprehensiveConditionGenerator;
import org.ut.biolab.mfiume.query.medsavant.complex.OntologyConditionGenerator;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;
import org.ut.biolab.mfiume.query.view.SearchConditionPanel;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;

/**
 *
 * @author mfiume
 */
public class MedSavantFrame extends JFrame implements Listener<LoginEvent> {

    private static Log LOG = LogFactory.getLog(MedSavantFrame.class);
    private static final String LOGIN_CARD_NAME = "login";
    private static final String SESSION_VIEW_CARD_NAME = "main";
    private static final String WAIT_CARD_NAME = "wait";
    private static final long SEARCH_ANIMATION_RUNTIME = 350;
    private static MedSavantFrame instance;
    private AnimatablePanel view;
    private CardLayout viewCardLayout;
    private JPanel sessionView;
    private LoginView loginView;
    private String currentCard;
    private boolean queuedForExit = false;
    private int textFieldAdminColumns = 20;
    private jAppStore appStore;
    private static Map<String, Runnable> debugFunctions = new HashMap<String, Runnable>();

    private static final String FEEDBACK_URI = "mailto:feedback@genomesavant.com?subject=MedSavant%20Feedback";

    //Adds a new function under the 'Debug' menu. The debug menu is not shown if
    //it is empty
    public static void addDebugFunction(String name, Runnable r) {
        System.out.println("Adding " + name + " to debug menu");
        debugFunctions.put(name, r);
    }

    public static JMenu getDebugMenu() {
        if (debugFunctions.size() < 1) {
            return null;
        }
        JMenu menu = new JMenu("Debug");
        for (final Map.Entry<String, Runnable> e : debugFunctions.entrySet()) {
            JMenuItem debugItem = new JMenuItem(e.getKey());
            debugItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    e.getValue().run();
                }
            });
            menu.add(debugItem);
        }
        return menu;
    }

    private final JMenu viewMenuMenu;

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
        Point src = ViewUtil.getPositionRelativeTo(view, srcComponent);
        Point dst = ViewUtil.getPositionRelativeTo(view, dstComponent);
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

        JMenuBar menuBar = new JMenuBar();
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

        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu("View");

        viewMenuMenu = new JMenu("Menu");

        JMenuItem togglePrimaryMenu = new JMenuItem("Primary Menu");
        togglePrimaryMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ViewController.getInstance().getMenu().setPrimaryMenuVisible(!ViewController.getInstance().getMenu().isPrimaryMenuVisible());
            }
        });
        viewMenuMenu.add(togglePrimaryMenu);

        JMenuItem toggleSecondaryMenu = new JMenuItem("Secondary Menu");
        toggleSecondaryMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ViewController.getInstance().getMenu().setSecondaryMenuVisible(!ViewController.getInstance().getMenu().isSecondaryMenuVisible());
            }
        });
        viewMenuMenu.add(toggleSecondaryMenu);

        viewMenu.add(viewMenuMenu);
        viewMenuMenu.setEnabled(false);

        menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu("Help");

        JMenuItem feedbackItem = new JMenuItem("Send Feedback");
        feedbackItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URI uri = URI.create(FEEDBACK_URI);
                    Desktop.getDesktop().mail(uri);
                } catch (Exception ex) {
                }
            }
        });

        helpMenu.add(feedbackItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

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

        view.add(new WaitPanel("Preparing Session"), WAIT_CARD_NAME);
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
        LOG.info("Asking to quit");
        final LoginController controller = LoginController.getInstance();

        //String jobsMsg = "";
        //if(ThreadController.getInstance().areJobsRunning()){
        //    jobsMsg = "Jobs are running.  If you quit, job progress will be lost. ";
        //}
        if (!controller.isLoggedIn() || DialogUtils.askYesNo("Quit MedSavant", "Are you sure you want to quit?") == DialogUtils.YES) {
            controller.logout();
        }
        LOG.info("Refusing to quit");
    }

    @Override
    public void handleEvent(LoginEvent evt) {
        switch (evt.getType()) {
            case LOGGED_IN:
                switchToSessionView();
                viewMenuMenu.setEnabled(true);
                break;
            case LOGGED_OUT:
                switchToLoginView();
                viewMenuMenu.setEnabled(false);
                break;
        }
        if (queuedForExit) {
            AnalyticsAgent.onEndSession(true);
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
                            + VersionSettings.getVersionString()
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
