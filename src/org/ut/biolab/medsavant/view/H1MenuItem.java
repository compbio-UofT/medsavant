/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToggleButton;

/**
 *
 * @author mfiume
 */
public class H1MenuItem extends JToggleButton {
    
    public H1MenuItem(String title, int num) {
        super(title);
        if (num == -1) {
            this.putClientProperty( "JButton.buttonType", "segmentedRoundRect" );
            this.putClientProperty( "JButton.segmentPosition", "first" );
        } else if (num == 1) {
            this.putClientProperty( "JButton.buttonType", "segmentedRoundRect" );
            this.putClientProperty( "JButton.segmentPosition", "last" );
        } else {
            this.putClientProperty( "JButton.buttonType", "segmentedRoundRect" );
            this.putClientProperty( "JButton.segmentPosition", "middle" );
        }
        this.setFont(new Font("Tahoma",Font.PLAIN,14));
    }
}
