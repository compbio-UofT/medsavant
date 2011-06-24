/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

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
    }
}
