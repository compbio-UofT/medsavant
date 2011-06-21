/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import fiume.vcf.VCFParser;
import fiume.vcf.VariantSet;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.view.SessionView;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.view.login.LoginView;

/**
 *
 * @author mfiume
 */
public class Frame extends JFrame implements LoginListener {

    private final JPanel view;
    private final CardLayout viewCardLayout;
    
    private static final String LOGIN_CARD_NAME = "login";
    private static final String SESSION_VIEW_CARD_NAME = "main";
    private SessionView sessionView;
    private final JMenuItem logOutItem;
    private LoginView loginView;
    private String currentCard;

    private void switchToSessionView() {
        if (currentCard != null && currentCard.equals(SESSION_VIEW_CARD_NAME)) { return; }
        if (loginView != null) {
            view.remove(loginView);
        }
        sessionView = new SessionView();
        view.add(sessionView, SESSION_VIEW_CARD_NAME);
        logOutItem.setEnabled(true);
        BottomBar.getInstance().updateLoginStatus();
        switchToView(SESSION_VIEW_CARD_NAME);
    }

    public void switchToLoginView() {
        if (currentCard != null && currentCard.equals(LOGIN_CARD_NAME)) { return; }
        if (sessionView != null) {
            view.remove(sessionView);
        }
        loginView = new LoginView();
        view.add(loginView, LOGIN_CARD_NAME);
        logOutItem.setEnabled(false);
        BottomBar.getInstance().updateLoginStatus();
        switchToView(LOGIN_CARD_NAME);
    }

    private void switchToView(String cardname) {
         viewCardLayout.show(view, cardname);
         currentCard = cardname;
    }

    public Frame() {
        super("MedSavant");

        listen();

        this.setLayout(new BorderLayout());

        //initVariantCollection();

        view = new JPanel();
        viewCardLayout = new CardLayout();
        view.setLayout(viewCardLayout);
        
        this.add(view, BorderLayout.CENTER);

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        logOutItem = new JMenuItem("Sign out");
        logOutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginController.logout();
            }
        });
        JMenuItem closeItem = new JMenuItem("Exit");
        final Frame instance = this;
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                instance.requestClose();
            }
        });
        fileMenu.add(logOutItem);
        fileMenu.add(closeItem);
        menu.add(fileMenu);

        this.add(menu, BorderLayout.NORTH);

        this.add(BottomBar.getInstance(),BorderLayout.SOUTH);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (SettingsController.getInstance().getAutoLogin()) {
            LoginController.login(SettingsController.getInstance().getUsername(), SettingsController.getInstance().getPassword());
        } else {
            switchToLoginView();
        }
    }

    public void requestClose() {
        System.exit(0);
    }

    public void loginEvent(LoginEvent evt) {
        if (evt.getLoggedIn()) {
            switchToSessionView();
        } else {
            switchToLoginView();
        }
    }

    private void listen() {
        LoginController.addLoginListener(this);
    }

}
