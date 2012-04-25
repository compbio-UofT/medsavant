/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.login;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class LoginView extends JPanel {

    private LoginForm loginForm;

    public LoginView() {
        initView();
        this.setBackground(new Color(217,222,229));
    }

    private void initView() {
        this.setLayout(new BorderLayout());
        this.loginForm = new LoginForm();
        this.add(loginForm,BorderLayout.CENTER);
    }

    public LoginForm getLoginForm(){
        return this.loginForm;
    }

}
