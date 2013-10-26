/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.images;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class ImagePanel extends JPanel {
    private final Image image;

    public ImagePanel(Image im, int width, int height) {
        this.image = im;
        Dimension dim = new Dimension(width,height);
        this.setMaximumSize(dim);
        this.setMinimumSize(dim);
        this.setPreferredSize(dim);
        this.setBorder(null);
        this.setOpaque(false);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}
