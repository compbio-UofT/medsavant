/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.gadget.filter;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;


/**
 *
 * @author mfiume
 */
public class FilterPanel extends JPanel {

    public FilterPanel() {
        this.setBackground(Color.black);
    }


    public void paintComponent(Graphics g) {

        GradientPaint p = new GradientPaint(0,0,Color.darkGray,0,40,Color.black);
        ((Graphics2D)g).setPaint(p);

        g.fillRect(0, 0, this.getWidth(), this.getHeight());

    }

}
