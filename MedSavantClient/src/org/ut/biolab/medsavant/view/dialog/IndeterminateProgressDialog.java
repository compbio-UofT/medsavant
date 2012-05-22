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

package org.ut.biolab.medsavant.view.dialog;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 *
 * @author Andrew
 */
public abstract class IndeterminateProgressDialog extends JDialog {
    
    private final JLabel messageLabel;
    
    public IndeterminateProgressDialog(String title, String message) {
        super(DialogUtils.getFrontWindow(), title, Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        
        Container p = getContentPane();
        p.setLayout(new GridBagLayout());
        
        messageLabel = new JLabel(message);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 60, 10, 60);
        p.add(messageLabel, gbc);
        gbc.insets = new Insets(10, 60, 30, 60);
        p.add(bar, gbc);

        pack();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(getParent());
    }
    
    @Override
    public void setVisible(boolean flag) {
        if (flag) {
            new Thread() {
                @Override
                public void run() {
                    IndeterminateProgressDialog.this.run();
                    setVisible(false);
                }
            }.start();
        }
        super.setVisible(flag);
    }
    
    public abstract void run();
}
