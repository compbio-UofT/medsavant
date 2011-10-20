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
        //this.setBackground(ViewUtil.getDarkColor());
        initView();
    }

    private void initView() {
        this.setLayout(new BorderLayout());
        this.loginForm = new LoginForm();
        this.add(loginForm,BorderLayout.CENTER);
        //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //this.add(Box.createVerticalGlue());
        //JPanel p = new JPanel();
        //p.setOpaque(false);
        //p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        //p.add(Box.createHorizontalGlue());
        //p.add(new LoginForm());
        //p.add(new JPanel());
        //p.add(Box.createHorizontalGlue());
        //this.add(p);
        //this.add(Box.createVerticalGlue());
    }

    public LoginForm getLoginForm(){
        return this.loginForm;
    }
    /*
    @Override
    public void paintComponent(Graphics g) {
        
        PaintUtil.paintMeBackground(g,this);
 
    }
     * 
     */
}
