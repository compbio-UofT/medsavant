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
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dialog.ChangePasswordDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.StandardFixedWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AccountManagerApp implements LaunchableApp {

    private JPanel accountBlock;

    public AccountManagerApp() {
    }

    private StandardFixedWidthAppPanel view;

    @Override
    public JPanel getView() {
        return view;
    }

    private void initView() {
        if (view == null) {
            view = new StandardFixedWidthAppPanel();

            accountBlock = view.addBlock("Account Information");

        }
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();
        refreshInfo();
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

    private void refreshInfo() {
        accountBlock.removeAll();
        
        KeyValuePairPanel kvp = new KeyValuePairPanel(1,true);
        
        JButton b = ViewUtil.getSoftButton("Change");
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
        
        kvp.addKeyWithValue("Username", LoginController.getInstance().getUserName());
        kvp.addKeyWithValue("Password", ViewUtil.bulletStringOfLength(LoginController.getInstance().getPassword().length()));
        
        kvp.setAdditionalColumn("Password", 0, b);

        accountBlock.add(kvp);
    }

}
