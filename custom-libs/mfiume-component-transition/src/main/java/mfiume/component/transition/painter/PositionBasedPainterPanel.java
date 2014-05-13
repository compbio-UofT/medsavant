package mfiume.component.transition.painter;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author mfiume
 */
public class PositionBasedPainterPanel implements PanelPainter {

    private BufferedImage newImage;
    private Point newOrigin;
    private BufferedImage previousImage;
    private Point previousOrigin;

    public PositionBasedPainterPanel() {
    }

    @Override
    public void paintComponent(Graphics g) {
        if (newImage != null) {
            g.drawImage(newImage, (int) newOrigin.getX(), (int) newOrigin.getY(), null);
        }
        if (previousImage != null) {
            g.drawImage(previousImage, (int) previousOrigin.getX(), (int) previousOrigin.getY(), null);
        }
        g.dispose();
    }

    public void setNewImageSource(JPanel source, Dimension dim) {
        this.newImage = PaintUtils.getImageFromSource(source, dim);
    }

    public void setNewOrigin(Point origin) {
        this.newOrigin = origin;
    }
    
    public void setPreviousImageSource(JPanel source, Dimension dim) {
        this.previousImage = PaintUtils.getImageFromSource(source, dim);
    }

    public void setPreviousOrigin(Point origin) {
        this.previousOrigin = origin;
    }

}
