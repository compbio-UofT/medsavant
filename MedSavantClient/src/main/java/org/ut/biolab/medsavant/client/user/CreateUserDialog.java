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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.component.field.editable.*;
import org.ut.biolab.medsavant.component.field.validator.NonEmptyStringValidator;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

/**
 *
 * @author mfiume
 */
public class CreateUserDialog extends JDialog {
    private static final Log LOG = LogFactory.getLog(CreateUserDialog.class);
    private JLabel helpLabel;

    public CreateUserDialog() {
        super(DialogUtils.getFrontWindow(), "Create User", Dialog.ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(MedSavantFrame.getInstance());
        initComponents();
    }

    private void initComponents() {

        KeyValuePairPanel userKVP = new KeyValuePairPanel(1);

        final StringEditableField usernameField = new StringEditableField();
        usernameField.setValidator(new NonEmptyStringValidator());

        final PasswordEditableField passwordField = new PasswordEditableField();
        passwordField.setValidator(new NonEmptyStringValidator("password"));

        final EnumEditableField userlevelField = new EnumEditableField(
                new UserLevel[]{
                    UserLevel.GUEST,
                    UserLevel.USER,
                    UserLevel.ADMIN
                });
        userlevelField.setValue(UserLevel.USER);

        userKVP.addKeyWithValue("Username", usernameField);
        userKVP.addKeyWithValue("Password", passwordField);
        userKVP.addKeyWithValue("User Level", userlevelField);

        this.setBackground(ViewUtil.getDefaultBackgroundColor());

        JPanel padded = ViewUtil.getClearPanel();
        padded.setLayout(new MigLayout("filly, fillx"));
        padded.add(userKVP, "growx 1.0, growy 1.0");

        this.setLayout(new BorderLayout());
        this.add(padded, BorderLayout.NORTH);

        final String userHelp = "<html>Users may edit cohorts, region sets, and have read-only access to patients.</html>";
        final String adminHelp = "<html>Administrators may upload variants, edit patients, manage users, and configure projects.</html>";
        final String guestHelp = "<html>Guests have read-only access.</html>";

        helpLabel = new JLabel();
        helpLabel.setBorder(new EmptyBorder(10,10,10,10));
        this.add(helpLabel, BorderLayout.CENTER);
        helpLabel.setText(userHelp);
        userlevelField.addFieldEditedListener(new FieldEditedListener() {
            @Override
            public void handleEditEvent(EditableField f) {
                switch ((UserLevel) f.getValue()) {
                    case ADMIN:
                        helpLabel.setText(adminHelp);
                        break;
                    case USER:
                        helpLabel.setText(userHelp);
                        break;
                    case GUEST:
                        helpLabel.setText(guestHelp);
                        break;
                }

            }
        });


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

        this.setSize(new Dimension(360, 250));
    }

    public static void main(String[] argv) {
        CreateUserDialog cug = new CreateUserDialog();
        cug.setVisible(true);
    }
}
