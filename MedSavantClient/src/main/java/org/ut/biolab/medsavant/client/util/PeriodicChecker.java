/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Class which performs a periodic task while the user is logged in.
 * 
 * @author tarkvara
 */
public abstract class PeriodicChecker implements ActionListener {
    private final int interval;

    private Timer timer;

    public PeriodicChecker(int interval) {
        this.interval = interval;
        this.timer = new Timer(interval, this);
        this.timer.setInitialDelay(0);
        this.timer.start();
    }
    
    public void stop(){
        timer.stop();
    }
}
