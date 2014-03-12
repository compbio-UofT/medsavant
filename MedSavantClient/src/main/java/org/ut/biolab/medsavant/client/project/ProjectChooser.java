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
package org.ut.biolab.medsavant.client.project;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;

import org.ut.biolab.medsavant.client.view.util.DialogUtils;


/**
 * Dialog which lets user select a project if there's more than one in the current database.
 *
 * @author tarkvara
 */
public class ProjectChooser extends JDialog {
    private String selected;

    public ProjectChooser(String[] projNames) {
        super(DialogUtils.getFrontWindow(), "Choose Project", Dialog.ModalityType.APPLICATION_MODAL);
        setLayout(new MigLayout("wrap, fillx"));

        add(new JLabel("<html><center>This database has more than one project.<br>Choose the one to work with.</center></html>"),"growx 1.0");

        for (String p: projNames) {
            JButton b = new JButton(p);
            add(b,"center");
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    selected = ae.getActionCommand();
                    setVisible(false);
                }
            });
        }
        
        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    public String getSelected() {
        return selected;
    }
}
