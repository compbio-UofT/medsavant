/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.animation;

import java.awt.Graphics2D;

/**
 *
 * @author jim
 */
public abstract class Animation {
    private long startTime = 0;
    private long maxRunTime = 0;
    
    public Animation(long maxRunTime){
        this.startTime = 0;
        this.maxRunTime = maxRunTime;
    }
    
    void setStartTime(long startTime){
        this.startTime = startTime;
    }
    
    long getStartTime(){
        return startTime;
    }
    
    boolean isStarted(){
        return startTime > 0;
    }
    
    long getMaxRunTime(){
        return maxRunTime;
    }
    
    /**
     * Called when the animation is finished (from the event dispatch thread).
     */
    public abstract void done();
    
    
    /**
     * Called when the animation needs to draw an update.
     */
    public abstract void drawUpdate(Graphics2D g2d, AnimatablePanel animatablePanel);        
    
    
    /**
     * Updates the state of the animation, but does not do any drawing.  This is 
     * called periodically from a background thread.
     *
     * @param runTime  How long this animation has been running, in ms.
     * @return true if the animation should stop, false otherwise.
     */
    public abstract boolean tick(long runTime);
}
