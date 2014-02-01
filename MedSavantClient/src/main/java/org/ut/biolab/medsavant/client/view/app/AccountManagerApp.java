/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dialog.ChangePasswordDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class AccountManagerApp implements LaunchableApp {

    public AccountManagerApp() {
    }

    private JPanel view;

    @Override
    public JPanel getView() {
        return view;
    }

    private void initView() {
        if (view == null) {
            view = new JPanel();

            JButton b = new JButton("Change password");
            b.addActionListener(new ActionListener() {

                private final String OLDPASS_LABEL = "Enter Current Password";
                private final String NEWPASS_LABEL1 = "Enter New Password";
                private final String NEWPASS_LABEL2 = "Confirm New Password";

                @Override
                public void actionPerformed(ActionEvent ae) {
                    JDialog jd = new ChangePasswordDialog();
                    jd.setVisible(true);
                }
            });

            view.add(b);
        }
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public String getName() {
        return "My Account";
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_ACCOUNT);
    }

    @Override
    public void didLogout() {
    }

    @Override
    public void didLogin() {
    }

}
