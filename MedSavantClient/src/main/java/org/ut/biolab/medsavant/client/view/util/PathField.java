/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.util;

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
               
                if(f != null){
                    setPath(f.getAbsolutePath());
                }
            }
        });
        
        this.setOpaque(false);
    }

    public File getFile() {
        return new File(field.getText());
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
