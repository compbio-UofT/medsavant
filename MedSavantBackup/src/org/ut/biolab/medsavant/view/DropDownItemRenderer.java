/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.ut.biolab.medsavant.view.util.PaintUtil;

/**
 *
 * @author mfiume
 */
public class DropDownItemRenderer extends JLabel implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
       
        String label = "A";//list.getModel().getElementAt(index).toString();
        
        if (isSelected) {
            return selectedRenderer(label);
        } else {
            return unselectedRenderer(label);
        }
    }

    private Component unselectedRenderer(final String label) {
        return new Component() {
            public void paintComponent(Graphics g) {
                g.drawString(label,0,0);
                PaintUtil.paintDarkMenu(g, this);
            }
        };
    }

    private Component selectedRenderer(final String label) {
        return new Component() {
            
            public void paintComponent(Graphics g) {
                g.drawString(label,0,0);
                PaintUtil.paintLightMenu(g, this);
            }
        };
    }
  
}
