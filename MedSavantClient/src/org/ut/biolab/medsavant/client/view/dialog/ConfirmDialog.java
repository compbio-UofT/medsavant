/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author AndrewBrook
 */
public class ConfirmDialog {

    private boolean confirmed = false;
    private JOptionPane optionPane;
    private JDialog dialog;

    public ConfirmDialog(String title, String message){
        optionPane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
        dialog = new JDialog();
        dialog.setContentPane(optionPane);
        dialog.setTitle(title);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(JOptionPane.NO_OPTION);
            }
        });
        optionPane.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    dialog.setVisible(false);
                }
            });
        dialog.setVisible(true);

        int value = ((Integer)optionPane.getValue()).intValue();
        if (value == JOptionPane.NO_OPTION) {
            confirmed = false;
        } else {
            confirmed = true;
        }
    }

    public boolean isConfirmed(){
        return confirmed;
    }

    public void dispose(){
        dialog.dispose();
    }


}
