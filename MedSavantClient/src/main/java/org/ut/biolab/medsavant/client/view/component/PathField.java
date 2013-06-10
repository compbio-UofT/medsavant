/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    JTextField f;
    JButton b;
    JFileChooser fc;

     public PathField(final int JFileChooserDialogType) {
         this(JFileChooserDialogType,false);
     }

    public PathField(final int JFileChooserDialogType, boolean directoriesOnly) {

        f = new JTextField();
        b = new JButton("...");
        fc = new JFileChooser();
        f.setMaximumSize(new Dimension(9999,22));
        if (JFileChooserDialogType == JFileChooser.SAVE_DIALOG) {
            fc.setDialogTitle("Save File");
            f.setToolTipText("Path to output file");
            b.setToolTipText("Set output file");
        } else {
            fc.setDialogTitle("Open File");
            f.setToolTipText("Path to input file");
            b.setToolTipText("Choose input file");
        }
        if(directoriesOnly){
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(f);
        this.add(b);

        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fc.setDialogType(JFileChooserDialogType);
                int result = fc.showDialog(null, null);
                if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
                    return;
                }
                setPath(fc.getSelectedFile().getAbsolutePath());
            }
        });

    }

    public String getPath() {
        return this.f.getText();
    }

    public void setPath(String s) {
        this.f.setText(s);
    }

    public JFileChooser getFileChooser() {
        return this.fc;
    }
}
