/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

import java.awt.event.ActionListener;
import javax.swing.Timer;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.login.LoginEvent;

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
        timer = new Timer(interval, this);
        timer.setInitialDelay(0);
        timer.start();
    }
}
