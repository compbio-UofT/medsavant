/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mfiume.component.transition.painter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class PaintUtils {
    
    static BufferedImage getImageFromSource(JPanel source, Dimension dim) {
        int width = dim.width;
        int height = dim.height;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        Rectangle originalBounds = source.getBounds();
        source.setBounds(0, 0, width, height);
        source.updateUI();
        source.paint(g);
        source.setBounds(originalBounds);
        g.dispose();
        return image;
    }
}
