/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.user;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.component.field.editable.EnumEditableField;
import org.ut.biolab.medsavant.component.field.editable.PasswordEditableField;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;
import org.ut.biolab.medsavant.component.field.validator.NonEmptyStringValidator;
import org.ut.biolab.medsavant.shared.format.UserRole;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

/**
 *
 * @author mfiume
 */
public class CreateUserDialog extends JDialog {
    private static final Log LOG = LogFactory.getLog(CreateUserDialog.class);
    public CreateUserDialog() {
        super(DialogUtils.getFrontWindow(), "Create User", Dialog.ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(MedSavantFrame.getInstance());
        initComponents();
    }

    private void initComponents() {

        KeyValuePairPanel userKVP = new KeyValuePairPanel(1);

        final StringEditableField usernameField = new StringEditableField();
        //usernameField.setAutonomousEditingEnabled(false);
        //usernameField.setEditing(true);
        //usernameField.setValidator(new EmailValidator());
        usernameField.setValidator(new NonEmptyStringValidator());

        final PasswordEditableField passwordField = new PasswordEditableField();
        //passwordField.setAutonomousEditingEnabled(false);
        //passwordField.setEditing(true);
        passwordField.setValidator(new NonEmptyStringValidator("password"));

        final EnumEditableField userlevelField = new EnumEditableField(
                new UserLevel[]{
                    UserLevel.GUEST,
                    UserLevel.USER,
                    UserLevel.ADMIN
                });
        userlevelField.setValue(UserLevel.USER);
        //userlevelField.setAutonomousEditingEnabled(false);
        //userlevelField.setEditing(true);
        JButton userLevelButton = ViewUtil.getHelpButton("User Levels",
                "<html><b>Guests</b> have read-only access<br/><br/>"
                + "<b>Users</b> may upload variants and edit patients<br/><br/>"
                + "<b>Administrators</b> may upload variants, edit patients, manage users, and configure projects</html>", true);

        userKVP.addKeyWithValue("Username", usernameField);
        userKVP.addKeyWithValue("Password", passwordField);
        userKVP.addKeyWithValue("User Level", userlevelField);

        /*
        try {
           Set<UserRole> roleSet = MedSavantClient.UserManager.getAllRoles(LoginController.getSessionID());
           final EnumEditableField userRoleField = new EnumEditableField(roleSet.toArray());
           userKVP.addKeyWithValue("User Role", userRoleField);
        } catch (Exception ex) {
           DialogUtils.displayError("Error", ex.getMessage());
           LOG.error(ex);
           dispose();
           return;
        }
        userKVP.setAdditionalColumn("User Level", 0, userLevelButton);
*/
        //this.setPreferredSize(new Dimension(350,200));
        this.setBackground(ViewUtil.getDefaultBackgroundColor());

        JPanel padded = ViewUtil.getClearPanel();
        padded.setLayout(new MigLayout("filly, fillx"));
        padded.add(userKVP, "growx 1.0, growy 1.0");

        this.setLayout(new BorderLayout());
        this.add(padded, BorderLayout.NORTH);

        JPanel bottom = ViewUtil.getClearPanel();
        bottom.setBorder(ViewUtil.getTopLineBorder());
        this.add(bottom, BorderLayout.SOUTH);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateUserDialog.this.dispose();
            }
        });

        JButton createButton = new JButton("Create");
        createButton.setFocusable(false);
        createButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (usernameField.validateCurrentValue() && passwordField.validateCurrentValue()) {
                    try {
                        if (MedSavantClient.UserManager.userExists(LoginController.getSessionID(), usernameField.getValue())) {
                            DialogUtils.displayMessage("User already exists.");
                        } else {
                            UserController.getInstance().addUser(
                                    usernameField.getValue(),
                                    passwordField.getValue().toCharArray(),
                                    (UserLevel) userlevelField.getValue());

                            DialogUtils.displayMessage("User Added", String.format("<html>Added user <i>%s</i></html>", usernameField.getValue()));
                            CreateUserDialog.this.dispose();
                        }
                    } catch (SessionExpiredException ex) {
                        MedSavantExceptionHandler.handleSessionExpiredException(ex);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        DialogUtils.displayException("Error adding user", "", ex);
                    }
                }
            }

        });

        //this.getRootPane().setDefaultButton(createButton);
        bottom.setLayout(new MigLayout("insets 0, fillx, nogrid"));

        bottom.add(cancelButton, "right");
        bottom.add(createButton, "right");
        //this.pack();

        this.setSize(new Dimension(360, 200));
    }

    public static void main(String[] argv) {
        CreateUserDialog cug = new CreateUserDialog();
        cug.setVisible(true);
    }
}
