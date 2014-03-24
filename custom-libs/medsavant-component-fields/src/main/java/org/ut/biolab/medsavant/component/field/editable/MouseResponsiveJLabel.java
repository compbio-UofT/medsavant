/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.component.field.editable;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.JLabel;

/**
 *
 * @author mfiume
 */
class MouseResponsiveJLabel extends JLabel {

    public MouseResponsiveJLabel() {

        final Font font = this.getFont();
        final Map attributes = font.getAttributes();
        final JLabel instance = this;

        this.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                instance.setFont(font.deriveFont(attributes));
            }

            public void mouseExited(MouseEvent e) {
                attributes.remove(TextAttribute.UNDERLINE);
                instance.setFont(font.deriveFont(attributes));
            }

        });
    }
    
    

}
