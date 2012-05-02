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

package org.ut.biolab.medsavant.view.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author mfiume
 */
public class PathField extends JPanel {

    JTextField field;
    JButton button;
    boolean saving;
    boolean directoriesOnly;

     public PathField(int chooserType) {
         this(chooserType, false);
     }

    public PathField(int type, boolean directoriesOnly) {
        field = new JTextField();
        button = new JButton("...");
        saving = type == JFileChooser.SAVE_DIALOG;
        this.directoriesOnly = directoriesOnly;
        field.setMaximumSize(new Dimension(9999,22));
        
        if (saving) {
            field.setToolTipText("Path to output file");
            button.setToolTipText("Set output file");
        } else {
            field.setToolTipText("Path to input file");
            button.setToolTipText("Choose input file");
        }
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(field);
        add(button);

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                File f;
                if (saving) {
                    f = DialogUtils.chooseFileForSave(button.getToolTipText(), field.getText());
                } else {
                    f = DialogUtils.chooseFileForOpen(button.getToolTipText(), null, null);
                }
                setPath(f.getAbsolutePath());
            }
        });
    }

    public String getPath() {
        return field.getText();
    }

    public void setPath(String s) {
        field.setText(s);
    }

    public JTextField getPathArea() {
        return field;
    }
}
