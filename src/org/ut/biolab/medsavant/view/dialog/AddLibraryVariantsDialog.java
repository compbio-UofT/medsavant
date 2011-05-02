/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.dialog;

import fiume.component.PathField;
import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AddLibraryVariantsDialog extends JDialog {

    public AddLibraryVariantsDialog(Frame parent, boolean modal) {
        super(parent,modal);
        this.setLayout(new BorderLayout());
        JPanel p = new JPanel();
        p.setBorder(ViewUtil.getMediumBorder());
        PathField pf = new PathField(JFileChooser.OPEN_DIALOG);
        p.add(pf);
        JButton add = new JButton("Add");
        p.add(add);
        this.add(p, BorderLayout.CENTER);
        this.pack();
    }

}
