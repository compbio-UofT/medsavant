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
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

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
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class MedSavantFrame extends JFrame implements Listener<LoginEvent> {

    private static final String LOGIN_CARD_NAME = "login";
    private static final String SESSION_VIEW_CARD_NAME = "main";
    private static final String WAIT_CARD_NAME = "wait";
    private static MedSavantFrame instance;
    private AnimatablePanel view;
    private CardLayout viewCardLayout;
    private JPanel sessionView;
    private LoginView loginView;
    private BottomBar bottomBar;
    private String currentCard;
    private boolean animationRunning = false;
    private boolean queuedForExit = false;
    
    private class AnimatablePanel extends JPanel implements Runnable{
        private Image img;
        private int srcX, srcY;
        private int dstX, dstY;
        private int currX, currY;
        private int transitTime = 350;         
        private int DELAY = 30;
        
        private double stepX;
        private double stepY;
        
        //Permit 5000 ms max runtime.
        private int maxRunTime = 5000;
        private Thread animationThread;
             
        private boolean threadStopped = false;
     
        public void setMaxRunTime(int m){
            this.maxRunTime = m;
        }
        public void setFPS(double fps){
            this.DELAY = (int)(Math.ceil(1000 / fps));
        }
        
        
        @Override
        public void paint(Graphics g){
            super.paint(g);             
            if(animationRunning){                
                Graphics2D g2d = (Graphics2D)g;
                g2d.drawImage(img, currX, currY, this);                  
                Toolkit.getDefaultToolkit().sync();
                g2d.dispose();
            }
        }
        
        public void animate(Image i, Point src, Point dst, int transitTime){            
            this.img = i;

            this.srcX = src.x;
            this.srcY = src.y;
            this.dstX = dst.x;
            this.dstY = dst.y;

            this.transitTime = transitTime;
            animationThread = new Thread(this);
            animationThread.start();
        }
        
        public void animate(Image i, Point src, Point dst){
            animate(i, src, dst, transitTime);
        }
        
        private double getCurrX(long t){
            return srcX + t*((dstX-srcX)/(double)transitTime);
        }
        
        private double getCurrY(long t){
            return srcY + t*((dstY - srcY)/(double)transitTime);
        }
        
        public boolean cycle(long t){            
            this.currX = (int)Math.round(getCurrX(t));
            this.currY = (int)Math.round(getCurrY(t));
          //  System.out.println("\tsx="+srcX+", sy="+srcY+" dx="+dstX+", dy="+dstY+", cx="+currX+", cy="+currY);
            
            if(t >= transitTime){
                currX = dstX;               
                currY = dstY;
            }
                                    
            return ((currX==dstX) && (currY==dstY));
        }
        
        public void run(){
            animationRunning = true;
            long beforeTime, timeDiff, sleep, startTime;
            startTime = beforeTime = System.currentTimeMillis();
            while (true) {                                
                long t = System.currentTimeMillis();
                if(cycle(t-startTime) || ((t-startTime) > maxRunTime)){
                    animationRunning = false;
                    repaint();
                    return;
                }
                timeDiff = t - beforeTime;
                repaint();
                sleep = DELAY - timeDiff;

                if (sleep < 0){
                    sleep = 2;
                }
                
                try {                    
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                   // System.out.println("interrupted");
                }

                beforeTime = System.currentTimeMillis();
            }
        }                        
    };
    
    
    private static Point getPositionRelativeTo(Component root, Component comp){
        if (comp.equals(root)) { return new Point(0,0); }
        Point pos = comp.getLocation();
        Point parentOff = getPositionRelativeTo(root, comp.getParent());
        return new Point(pos.x + parentOff.x, pos.y + parentOff.y);
    }
        
    /**
     * Creates a search animation from the current mouse position.  i.e. creates an image
     * that moves from the current mouse position to the 'Variants' button on the toolbar.
     * 
     * Do not call until the previous animation thread is finished.  This 
     * shouldn't happen as long as the animation time is short. (< 500ms ).  
     */
    public void searchAnimationFromMousePos(){
        ImageIcon img = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SECTION_SEARCH);                                                                                                                                                                                    
        Point src = view.getMousePosition();
        if(src == null){
            return;
        }
        
        Point dst = null;
        Enumeration<AbstractButton> e = ViewController.getInstance().getMenu().primaryMenuButtons.getElements();
        
        while(e.hasMoreElements()){
            AbstractButton b = e.nextElement();
            if(b.getName().equalsIgnoreCase("Variants")){
                dst = getPositionRelativeTo(view, b);
            }
        }
                        
        if(dst != null){
            view.animate(img.getImage(), src, dst);            
        }
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
        setMinimumSize(new Dimension(500, 500));

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
