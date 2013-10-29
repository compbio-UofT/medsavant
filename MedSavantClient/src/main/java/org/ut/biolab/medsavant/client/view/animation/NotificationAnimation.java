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
package org.ut.biolab.medsavant.client.view.animation;

import java.awt.Font;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author jim
 */
public final class NotificationAnimation extends Animation {
     
    private String message;
    
    
    public enum Position{
        TOP_RIGHT,
        TOP_LEFT,
        TOP_CENTER,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT        
    };   
    
    private Position position = null;
    
    private static final Position DEFAULT_POSITION = Position.TOP_CENTER;
    private static final int MIN_DISTANCE_FROM_PANELEDGE = 10;
    private static final int DEFAULT_STAYTIME = 6000;
    private static final int DEFAULT_FADETIME = 2000;
    private static final int AUTO_HEIGHT = -1;    
    private static final int ROUNDRECT_ARCWIDTH = 25;
    private static final int ROUNDRECT_ARCHEIGHT = 25;   
    
    private int margin_x = 10;
    private int margin_y = 10;
    private int x, y;
    private int width = 300;
    private int height = AUTO_HEIGHT; 
    private final int containingPanelWidth;
    private final int containingPanelHeight;
    
    private long stayTime = DEFAULT_STAYTIME;
    private long fadeTime = DEFAULT_FADETIME;
    
    private float initial_alpha = 0.8f;
    private float alpha = initial_alpha;
    private float textalpha = 1.0f;
    
    private Font font = ViewUtil.detailFontPlain.deriveFont(10);
       
    
    public NotificationAnimation(long stayTime, long fadeTime, String message, JPanel containingPanel, Position pos){
        super(stayTime+fadeTime);
        this.stayTime = stayTime;
        this.fadeTime = fadeTime;        
        this.message = message;  
        this.containingPanelWidth = containingPanel.getWidth();
        this.containingPanelHeight = containingPanel.getHeight();
        setPosition(pos);
    }            
    
    public NotificationAnimation(String msg, JPanel containingPanel, Position pos){
        this(DEFAULT_STAYTIME, DEFAULT_FADETIME, msg, containingPanel, pos);        
    }
    
    public NotificationAnimation(String msg, JPanel containingPanel){
        this(DEFAULT_STAYTIME, DEFAULT_FADETIME, msg, containingPanel, DEFAULT_POSITION);
    }
    
    public NotificationAnimation setFont(Font font){
        this.font = font;
        return this;                
    }
    
    public NotificationAnimation setMargins(int marginx, int marginy){
        this.margin_x = marginx;
        this.margin_y = marginy;
        return this;
    }
    
    public NotificationAnimation setDimensions(int width, int height){        
        this.width = width;
        this.height = height;
               
        return this;
    }
    
    public NotificationAnimation setPosition(int x, int y){       
        this.position = null;
        this.x = x;
        this.y = y;
        return this;
    }
    
    public NotificationAnimation setPosition(Position pos){
        this.position = pos;
        return this;
    } 
    
    private void updateCoordsFromPosition(){       
        switch(this.position){
            case TOP_RIGHT:
                this.x = containingPanelWidth - width - MIN_DISTANCE_FROM_PANELEDGE;
                this.y = MIN_DISTANCE_FROM_PANELEDGE;
                break;
                
            case TOP_LEFT:
                this.x = MIN_DISTANCE_FROM_PANELEDGE;
                this.y =  MIN_DISTANCE_FROM_PANELEDGE;
                break;
                
            case TOP_CENTER:
                this.x = (int)Math.round(containingPanelWidth / 2.0 - width/2.0);
                this.y =  MIN_DISTANCE_FROM_PANELEDGE;
                break;
                
            case BOTTOM_LEFT:
                this.x = MIN_DISTANCE_FROM_PANELEDGE;
                this.y = containingPanelHeight - height - MIN_DISTANCE_FROM_PANELEDGE;
                break;
                        
            case BOTTOM_CENTER:
                this.x = (int)Math.round(containingPanelWidth / 2.0 - width/2.0);
                this.y = containingPanelHeight - height - MIN_DISTANCE_FROM_PANELEDGE;
                break;

            case BOTTOM_RIGHT: 
                this.x = containingPanelWidth - width - MIN_DISTANCE_FROM_PANELEDGE;
                this.y = containingPanelHeight - height - MIN_DISTANCE_FROM_PANELEDGE;
                break;
        }
        
        
    }
 

    public NotificationAnimation setMessage(String message){
        this.message = message;
        return this;
    }
    
    /*
     * Sets the initial alpha value for the background, NOT the text.
     * The initial text alpha is always 1.
     */
    public NotificationAnimation setInitialAlpha(float alpha){       
        this.initial_alpha = alpha;
        this.alpha = initial_alpha;
        return this;
    }

    //Draws the string with primitive word wrapping.  Lines are only wrapped
    //on spaces, and no dashes are introduced.
    //Returns the total height of the text.    
    private int drawString(Graphics2D g2d, String msg, boolean draw) {
        //figure x, y, width
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int msg_x = x + margin_x;
        int curX = msg_x;
        //int curY = y + margin_y + lineHeight;        
        int curY = y + margin_y + fm.getAscent();
        int msg_width = width - 2 * margin_x;

    
        String[] words = msg.split(" ");

        for (String word : words) {
            // Find out the width of the word.
            int wordWidth = fm.stringWidth(word + " ");

            // If text exceeds the width, then move to next line.
            if (curX + wordWidth >= msg_x + msg_width) {
                curY += lineHeight;
                curX = msg_x;
            }
            if (draw) {
                g2d.drawString(word, curX, curY);
            }

            // Move over to the right for next word.
            curX += wordWidth;
        }
        return (curY+fm.getDescent()+margin_y)-y;
    }
    
    @Override
    public void drawUpdate(Graphics2D g2d, AnimatablePanel animatablePanel) {    
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);                                                
        g2d.setFont(font);
        int w = width;
        int h = height;
        if(height == AUTO_HEIGHT){
            h = drawString(g2d, message, false);
        }
        if(position != null){
            updateCoordsFromPosition();
        }
        
        g2d.setColor(Color.BLACK);        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);         
        g2d.fillRoundRect(x, y, w, h, ROUNDRECT_ARCWIDTH, ROUNDRECT_ARCHEIGHT);    
        
        g2d.setColor(Color.WHITE); 
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textalpha));
    
        
        drawString(g2d, message, true);      
    }

    @Override
    public boolean tick(long runTime) {        
        if(runTime > stayTime){            
            alpha = (float)Math.max(0, initial_alpha - (runTime-stayTime) / (float)fadeTime);
            textalpha = (float)Math.max(0, 1.0 - (runTime - stayTime) / (float)fadeTime);
        }
        return (runTime >= (stayTime + fadeTime));
    }
    
    @Override
    public void done(){}
    
}
