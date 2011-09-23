/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import org.ut.biolab.medsavant.olddb.ManageDB;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.view.login.LoginView;

/**
 *
 * @author mfiume
 */
public class MainFrame extends JFrame implements LoginListener{

    private final JPanel view;
    private final CardLayout viewCardLayout;
    
    private static final String LOGIN_CARD_NAME = "login";
    private static final String SESSION_VIEW_CARD_NAME = "main";
    private JPanel sessionView;
    private final JMenuItem logOutItem;
    //private final JMenuItem manageDBItem;
    private LoginView loginView;
    private String currentCard;
    
    private boolean queuedForExit = false;
    private static MainFrame instance;

    public void switchToSessionView() {
         //System.out.println("Requesting switch to session view");
        if (!LoginController.isLoggedIn() || (currentCard != null && currentCard.equals(SESSION_VIEW_CARD_NAME))) { return; }
        if (loginView != null) {
            view.remove(loginView);
        }
        
        //if (sessionView == null) {
            sessionView = new LoggedInView();
            view.add(sessionView, SESSION_VIEW_CARD_NAME);
        //}
        logOutItem.setEnabled(true);
        //manageDBItem.setEnabled(true);
        BottomBar.getInstance().updateLoginStatus();
        switchToView(SESSION_VIEW_CARD_NAME);
    }

    public void switchToLoginView() {
        //System.out.println("Requesting switch to login view");
        if (currentCard != null && currentCard.equals(LOGIN_CARD_NAME)) { return; }
        if (sessionView != null) {
            view.remove(sessionView);
        }
        
        //if (loginView == null) {
            loginView = new LoginView();
            view.add(loginView, LOGIN_CARD_NAME);
        //}
        
        logOutItem.setEnabled(false);
        //manageDBItem.setEnabled(false);
        BottomBar.getInstance().updateLoginStatus();
        switchToView(LOGIN_CARD_NAME);
    }

    private void switchToView(String cardname) {
         viewCardLayout.show(view, cardname);
         currentCard = cardname;
    }
    
    public static MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }

    private MainFrame() {
        super("MedSavant");

        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(500,500));
        
        LoginController.addLoginListener(this);

        view = new JPanel();
        viewCardLayout = new CardLayout();
        view.setLayout(viewCardLayout);
        
        this.add(view, BorderLayout.CENTER);

        JMenuBar menu = new JMenuBar();   
        JMenu fileMenu = new JMenu("File");
        /*
        manageDBItem = new JMenuItem("Manage Database");
        manageDBItem.setEnabled(false);
        final MainFrame instance = this;
        manageDBItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManageDB(instance, true);
            }
        });
         * 
         */
        logOutItem = new JMenuItem("Sign out");
        logOutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginController.logout();
            }
        });
        JMenuItem closeItem = new JMenuItem("Exit");
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                instance.requestClose();
            }
        });
        //fileMenu.add(manageDBItem);
        fileMenu.add(logOutItem);
        fileMenu.add(closeItem);
        menu.add(fileMenu);
        
        setJMenuBar(menu);

        this.add(BottomBar.getInstance(),BorderLayout.SOUTH);

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                requestClose();
            }
        });

        if (SettingsController.getInstance().getAutoLogin()) {
            LoginController.login(SettingsController.getInstance().getUsername(), SettingsController.getInstance().getPassword());
        } else {
            switchToLoginView();
        }
    }

    public void requestClose() {   
       int confirm = JOptionPane.showConfirmDialog(
               this, 
               "Are you sure you want to quit?", 
               "Exit MedSavant?", 
               JOptionPane.YES_NO_OPTION, 
               JOptionPane.QUESTION_MESSAGE);   
       
       if(confirm == JOptionPane.YES_OPTION){
           queuedForExit = true; //sometimes logout aborts this exit, so wait for event
           ConnectionController.disconnectAll();
           LoginController.logout();
           System.exit(0);
       }       
    }

    public void loginEvent(LoginEvent evt) {
        switch(evt.getType()) {
            case LOGGED_IN:
                switchToSessionView();
                break;
            case LOGGED_OUT:
                switchToLoginView();
                break;
        }
        if(queuedForExit)System.exit(0);
    }
}
