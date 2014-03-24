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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class RoundedShadowPanel extends JPanel {

    private BufferedImage shadow;

    public RoundedShadowPanel() {
        setOpaque(false);
    }

    int arc = 10;
    int shadowSize = 2;

    @Override
    protected void paintComponent(Graphics g) {
        int x = shadowSize;
        int y = shadowSize;
        int w = getWidth() - 2*shadowSize;
        int h = getHeight() - 2*shadowSize;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(225, 225, 225));

        g2.drawRoundRect(x-1, y, w+2, h+1, arc, arc);
        
        g2.setColor(Color.white);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(247, 247, 247));

        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.dispose();
    }

    
    /*
     @Override
     public void setBounds(int x, int y, int width, int height) {
     super.setBounds(x, y, width, height);

     int w = getWidth() - 2*shadowSize;
     int h = getHeight() - 2*shadowSize;
     
     shadow = GraphicsUtilities.createCompatibleTranslucentImage(w, h);
     Graphics2D g2 = shadow.createGraphics();
     g2.setColor(Color.WHITE);
     g2.fillRoundRect(0, 0, w, h, arc, arc);
     g2.dispose();

     ShadowRenderer renderer = new ShadowRenderer(shadowSize, 0.5f, new Color(225,225,255));
     shadow = renderer.createShadow(shadow);

     g2 = shadow.createGraphics();
     // The color does not matter, red is used for debugging
     g2.setColor(Color.RED);
     g2.setComposite(AlphaComposite.Clear);
     g2.fillRoundRect(shadowSize, shadowSize, w, h, arc, arc);
     g2.dispose();
     }
    */
}
