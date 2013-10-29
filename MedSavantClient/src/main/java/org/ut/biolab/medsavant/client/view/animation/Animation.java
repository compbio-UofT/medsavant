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
