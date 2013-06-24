/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.animation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A panel that supports animations.  Animations can be added and immediately
 * run through the 'animation' method.  All animations execute on the same 
 * background thread, and the frame updates for all animations execute sequentially
 * every 'tickTime' ms.
 * 
 * @author jim
 */
 public class AnimatablePanel extends JPanel{        
        //By default, execute a tick every 30ms.  ALL running animations 
        //need to be able to complete within that time.  If they do not, 
        //the tick time will end up being longer.
        private int tickTime = 30;

        private Thread animationThread;        
             
        private List<Animation> animations = new CopyOnWriteArrayList<Animation>();
               
        public void setTickTime(int tt){
            this.tickTime = tt;
        }               
           
        /**
         * Cancels ALL running animations.
         */
        public void cancel(){
            if(animationThread != null && animationThread.isAlive()){               
                this.animations.clear();
                repaint();
            }
        }
        
        /**
         * Cancels the given animation within 'tick time' ms, if it is running. 
         * Otherwise does nothing.
         */
        public void cancel(Animation anim){
            animations.remove(anim);
        }
       
        @Override
        public void paint(Graphics g){
            super.paint(g);                                       
            for(Animation animation : animations){   
                Graphics2D g2d = (Graphics2D)g.create();
                animation.drawUpdate(g2d, this);                
                g2d.dispose();
            }           
            Toolkit.getDefaultToolkit().sync();
            g.dispose();
        }
                
               
        public void animate(Animation anim){  
            animations.add(anim);
            if(animationThread == null || !animationThread.isAlive()){
                animationThread = new Thread(new AnimationTask());                
                animationThread.start();
            }
        }
               
                            
        
        private class AnimationTask implements Runnable{                                   
            
            @Override
            public void run(){
                long beforeTime, timeDiff, sleep, startTime;
                startTime = beforeTime = System.currentTimeMillis();
                List<Animation> stoppedAnimations = new ArrayList<Animation>();
                
                while (animations.iterator().hasNext()) {  
                    long t = System.currentTimeMillis();
                    for(final Animation animation : animations){                            
                        if(!animation.isStarted()){
                            animation.setStartTime(startTime);
                        }
                        
                        long t2 = System.currentTimeMillis();
                        if(animation.tick(t2 - animation.getStartTime()) || 
                                ((t2 - animation.getStartTime()) > animation.getMaxRunTime())){                            
                            stoppedAnimations.add(animation);
                            repaint(); 
                                                       
                            SwingUtilities.invokeLater(new Runnable(){
                               @Override
                               public void run(){
                                   animation.done();
                               } 
                            });
                            //animation.done();
                        }                        
                    }
                    
                    animations.removeAll(stoppedAnimations);
                    stoppedAnimations.clear();
                    
                    timeDiff = t - beforeTime;
                    repaint();
                    sleep = tickTime - timeDiff;

                    if (sleep < 0){
                        sleep = 2;
                    }

                    try {                    
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                       // System.out.println("interrupted");
                    }

                    beforeTime = System.currentTimeMillis();
                    
                    
                }
            }                          
        }
    };
    
    