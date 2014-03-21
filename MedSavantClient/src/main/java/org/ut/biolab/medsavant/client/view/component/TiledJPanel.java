/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package org.ut.biolab.medsavant.client.view.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class TiledJPanel extends JPanel {  
    BufferedImage tileImage;  
   
    public TiledJPanel(Image image) {  
        tileImage = ViewUtil.getBufferedImage(image);
    }  
   
    protected void paintComponent(Graphics g) {  
        int width = getWidth();  
        int height = getHeight();  
        int imageW = tileImage.getWidth(this);  
        int imageH = tileImage.getHeight(this);  
   
        // Tile the image to fill our area.  
        for (int x = 0; x < width; x += imageW) {  
            for (int y = 0; y < height; y += imageH) {  
                g.drawImage(tileImage, x, y, this);  
            }  
        }  
    }  
   
    public Dimension getPreferredSize() {  
        return new Dimension(240, 240);  
    }  
   
    public static void main(String[] args) throws IOException {  
        BufferedImage image = ImageIO.read(new File("/img/bg/bg1.jpg"));  
        TiledJPanel test = new TiledJPanel(image);  
        JOptionPane.showMessageDialog(null, test, "", JOptionPane.PLAIN_MESSAGE);  
    }  
}  