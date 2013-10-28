/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
