/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoginForm.java
 *
 * Created on Jun 20, 2011, 11:11:22 AM
 */
package org.ut.biolab.medsavant.view.login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import main.ProgramInformation;

/**
 *
 * @author mfiume
 */
public class LoginForm extends javax.swing.JPanel {

    /** Creates new form LoginForm */
    public LoginForm() {
        initComponents();
        field_username.setText(LoginController.getUsername());
        field_password.setText(LoginController.getPassword());
        
        this.field_username.setText(SettingsController.getInstance().getUsername());
        if (SettingsController.getInstance().getRememberPassword()) {
            this.field_password.setText(SettingsController.getInstance().getPassword());
        }
        this.cb_rememberpassword.setSelected(SettingsController.getInstance().getRememberPassword());
        this.cb_autosignin.setSelected(SettingsController.getInstance().getAutoLogin());
        
        this.label_versioninfo.setText("MedSavant " + ProgramInformation.getVersion() + " " + ProgramInformation.getReleaseType());
        
        updateAutoSignInCheckBoxBasedOnPasswordCheckbox();
        
        //this.setOpaque(false);
        this.setMaximumSize(new Dimension(400, 400));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        field_username = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        field_password = new javax.swing.JPasswordField();
        cb_rememberpassword = new javax.swing.JCheckBox();
        cb_autosignin = new javax.swing.JCheckBox();
        label_status = new javax.swing.JLabel();
        label_versioninfo = new javax.swing.JLabel();
        button_login = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Username:");

        field_username.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_usernameKeyPressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Password:");

        field_password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_passwordKeyPressed(evt);
            }
        });

        cb_rememberpassword.setText("Remember my password");
        cb_rememberpassword.setOpaque(false);
        cb_rememberpassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cb_rememberpasswordActionPerformed(evt);
            }
        });

        cb_autosignin.setText("Sign me in automatically");
        cb_autosignin.setOpaque(false);
        cb_autosignin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cb_autosigninActionPerformed(evt);
            }
        });

        label_status.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        label_status.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        label_versioninfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_versioninfo.setText("version information");

        button_login.setText("Login");
        button_login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_loginActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label_status, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cb_autosignin, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cb_rememberpassword, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(field_password, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                            .addComponent(field_username, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                            .addComponent(label_versioninfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(button_login)
                        .addGap(103, 103, 103))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label_versioninfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_status, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cb_rememberpassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cb_autosignin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_login)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void field_usernameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_usernameKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_usernameKeyPressed

    private void field_passwordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_passwordKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_passwordKeyPressed

    private void cb_rememberpasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cb_rememberpasswordActionPerformed
        String value = SettingsController.booleanToString(this.cb_rememberpassword.isSelected());
        SettingsController.getInstance().setValue(SettingsController.KEY_REMEMBER_PASSWORD,value);
        updateAutoSignInCheckBoxBasedOnPasswordCheckbox();
    }//GEN-LAST:event_cb_rememberpasswordActionPerformed

    private void cb_autosigninActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cb_autosigninActionPerformed
        String value = SettingsController.booleanToString(this.cb_autosignin.isSelected());
        SettingsController.getInstance().setValue(SettingsController.KEY_AUTOLOGIN,value);
    }//GEN-LAST:event_cb_autosigninActionPerformed

    private void button_loginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_loginActionPerformed
        this.loginUsingEnteredUsernameAndPassword();
    }//GEN-LAST:event_button_loginActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_login;
    private javax.swing.JCheckBox cb_autosignin;
    private javax.swing.JCheckBox cb_rememberpassword;
    private javax.swing.JPasswordField field_password;
    private javax.swing.JTextField field_username;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel label_status;
    private javax.swing.JLabel label_versioninfo;
    // End of variables declaration//GEN-END:variables

    private void loginUsingEnteredUsernameAndPassword() {
        this.label_status.setText("signing in...");
        this.label_status.setFont(new Font("Tahoma",Font.BOLD,11));
        this.label_status.setForeground(Color.black);
        this.button_login.setEnabled(false);
        LoginController.login(field_username.getText(), field_password.getText());
        if (!LoginController.isLoggedIn()) {
            this.label_status.setText("login incorrect");
            this.label_status.setFont(new Font("Tahoma",Font.BOLD,11));
            this.label_status.setForeground(Color.red);
            this.field_username.requestFocus();
            this.button_login.setEnabled(true);
        }
    }

    private void updateAutoSignInCheckBoxBasedOnPasswordCheckbox() {
        boolean rememberpw = this.cb_rememberpassword.isSelected();
        boolean autosignin = this.cb_autosignin.isSelected();
        if (!rememberpw) {
            this.cb_autosignin.setEnabled(false);
            if (autosignin) {
                this.cb_autosignin.setSelected(false);
                SettingsController.getInstance().setAutoLogin(false);
            }
        } else {
            this.cb_autosignin.setEnabled(true);
        }
    }
}
