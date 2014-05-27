/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.images;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ImagePanel extends JPanel {

    private Image image;
    private boolean doColorOverlay = false;
    private Color colorOverlay = Color.red;
    private Image recoloredImage;

    /**
     * Create an image from image resource
     *
     * @param path Path to icon in resources (e.g. icon/ui/image.png)
     */
    public ImagePanel(String path) {
        this.setOpaque(false);
        try {
            URL u = getClass().getClassLoader().getResource(path);
            Image im = ImageIO.read(u);
            ImageIcon icon = new ImageIcon(im);
            int height = icon.getIconHeight();
            int width = icon.getIconWidth();
            image = im;
            initDimensions(width, height);
        } catch (IOException ex) {
        }
    }

    public ImagePanel(Image im) {
        ImageIcon icon = new ImageIcon(im);
        int height = icon.getIconHeight();
        int width = icon.getIconWidth();
        image = im;
        initDimensions(width, height);
    }

    public ImagePanel(Image im, int width, int height, boolean smoothing) {
        this.image = im;
        if (smoothing) {
            this.image = image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
        }
        initDimensions(width, height);
    }

    public ImagePanel(Image im, int width, int height) {
        this(im, width, height, true);
    }

    @Override
    public void paintComponent(Graphics g) {

        Image i = image;

        if (doColorOverlay) {
            i = recoloredImage;
            
        }

        g.drawImage(i, 0, 0, this.getWidth(), this.getHeight(), this);

    }

    private static int[] singleToComponents(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return new int[]{red, green, blue};
    }

    private static int componentsToSingle(int red, int green, int blue) {
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return rgb;
    }

    

    private void initDimensions(int width, int height) {
        Dimension dim = new Dimension(width, height);
        this.setMaximumSize(dim);
        this.setMinimumSize(dim);
        this.setPreferredSize(dim);
        this.setBorder(null);
        this.setOpaque(false);
    }

    public void setDoColorOverlay(boolean doColorOverlay) {
        this.doColorOverlay = doColorOverlay;
        prepareColoredImage();
    }

    public void setColorOverlay(Color colorOverlay) {
        this.colorOverlay = colorOverlay;
        prepareColoredImage();
    }

    private void prepareColoredImage() {
        recoloredImage = ViewUtil.colorImage(image, colorOverlay);
    }
}
