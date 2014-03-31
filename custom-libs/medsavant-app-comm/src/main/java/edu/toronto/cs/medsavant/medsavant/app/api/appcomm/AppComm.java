/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.toronto.cs.medsavant.medsavant.app.api.appcomm;

import org.ut.biolab.medsavant.shared.appapi.MedSavantApp;

/**
 *
 * @author mfiume
 */
public class AppComm<T> {
    
    private final MedSavantApp sender;
    private final T eventData;

    public AppComm(MedSavantApp sender, T eventData) {
        this.sender = sender;
        this.eventData = eventData;
    }

    public MedSavantApp getSender() {
        return sender;
    }

    public T getEventData() {
        return eventData;
    }
}
