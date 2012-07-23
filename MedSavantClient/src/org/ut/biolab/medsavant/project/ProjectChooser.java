/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.project;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.util.ClientMiscUtils;

import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 * Dialog which lets user select a project if there's more than one in the current database.
 *
 * @author tarkvara
 */
public class ProjectChooser extends JDialog {
    private String selected;

    public ProjectChooser(String[] projNames) {
        super(DialogUtils.getFrontWindow(), "Choose Project", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 30, 0, 30);

        add(new JLabel("<html><center>This database has more than one project.<br>Choose the one to work with.</center></html>"), gbc);
        
        gbc.weightx = 0.0;
        gbc.insets = new Insets(10, 30, 0, 30);
        for (String p: projNames) {
            JButton b = new JButton(p);
            add(b, gbc);
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    selected = ae.getActionCommand();
                    setVisible(false);
                }
            });
        }

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 30, 20, 30);
        add(cancelButton, gbc);
        
        ClientMiscUtils.registerCancelButton(cancelButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
    }
    
    public String getSelected() {
        return selected;
    }
}
