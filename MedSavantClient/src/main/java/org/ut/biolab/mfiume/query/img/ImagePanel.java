package org.ut.biolab.mfiume.query.img;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel(String path) {
        this.setOpaque(false);
        try {
            URL u = getClass().getClassLoader().getResource(path);
            image = ImageIO.read(u);
            this.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
        } catch (IOException ex) {
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, this.getHeight()/2-image.getHeight()/2, null); // see javadoc for more info on the parameters
    }
}