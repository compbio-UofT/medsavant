/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.login;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class LoginView extends JPanel {
    
    private LoginForm loginForm;

    public LoginView() {
        initView();
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
