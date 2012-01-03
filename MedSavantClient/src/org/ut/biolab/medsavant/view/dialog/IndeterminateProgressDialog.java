/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.dialog;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class IndeterminateProgressDialog extends JDialog {
    
    private final JLabel messageLabel;
    
    public IndeterminateProgressDialog(String title, String message, boolean modal){
        this(title, message, modal, null);
    }
    
    public IndeterminateProgressDialog(String title, String message, boolean modal, Component parent){
        
        this.setModal(modal);
        this.setTitle(title);
        this.setResizable(false);
        
        JPanel p = new JPanel(); 
        ViewUtil.applyVerticalBoxLayout(p);
        
        messageLabel = new JLabel(message);
        messageLabel.setHorizontalTextPosition(JLabel.CENTER);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setMinimumSize(new Dimension(300,70));
        messageLabel.setPreferredSize(new Dimension(300,70));
        //messageLabel.setFont(ViewUtil.);
        p.add(messageLabel);
        JProgressBar b = new JProgressBar();
        b.setIndeterminate(true);
        p.add(b);
        p.setBorder(ViewUtil.getBigBorder());
        this.setContentPane(p);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setMinimumSize(new Dimension(200,50));
        this.pack();
        this.setLocationRelativeTo(parent);
        //this.setVisible(true);
    }
    
    public JLabel getMessageLabel(){
        return messageLabel;
    }
    
    public void close(){
        this.setVisible(false);
        this.dispose();
    }
    
}
