/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.animation;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

/**
 *
 * @author jim
 */
public class IconTranslatorAnimation extends Animation {

    private Image img;
    private int srcX, srcY, dstX, dstY, currX, currY;

    public IconTranslatorAnimation(Image img, Point src, Point dst, long maxRunTime) {
        super(maxRunTime);
        
        this.srcX = this.currX = src.x;
        this.srcY = this.currY = src.y;
        this.dstX = dst.x;
        this.dstY = dst.y;
        this.img = img;       
    }

    private double getCurrX(long t) {
        return srcX + t * ((dstX - srcX) / (double) getMaxRunTime());
    }

    private double getCurrY(long t) {
        return srcY + t * ((dstY - srcY) / (double) getMaxRunTime());
    }

    @Override
    public boolean tick(long t) {
        this.currX = (int) Math.round(getCurrX(t));
        this.currY = (int) Math.round(getCurrY(t));
        //  System.out.println("\tsx="+srcX+", sy="+srcY+" dx="+dstX+", dy="+dstY+", cx="+currX+", cy="+currY);

        if (t >= getMaxRunTime()) {
            currX = dstX;
            currY = dstY;
        }
        return ((currX == dstX) && (currY == dstY));
    }

    @Override
    public void drawUpdate(Graphics2D g2d, AnimatablePanel ap) {        
        g2d.drawImage(img, currX, currY, ap);       
    }
    
    @Override
    public void done(){}
}
