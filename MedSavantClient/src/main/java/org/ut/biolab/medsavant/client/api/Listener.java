/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.api;

/**
 * Generic event-listener class so that we don't have to write a new class for every
 * single event.
 *
 * @author tarkvara
 */
public interface Listener<E> {
    public void handleEvent(E event);
}
