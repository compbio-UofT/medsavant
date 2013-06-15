/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.client.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;

/**
 * Generic controller class which provides functionality which can be used by other controllers.
 * 
 * @author tarkvara
 */
public abstract class Controller<E> {
    private static final Log LOG = LogFactory.getLog(Controller.class);

    protected List<Listener<E>> listeners = new ArrayList<Listener<E>>();

    private List<Listener<E>> listenersToAdd;

    private List<Listener<E>> listenersToRemove;

    private boolean looping;

    /**
     * Fire the specified event to all our listeners.
     */
    public synchronized void fireEvent(E event) {
        this.listenersToAdd = new ArrayList<Listener<E>>();
        this.listenersToRemove = new ArrayList<Listener<E>>();
        this.looping = true;
        for (final Listener l : this.listeners) {
            try {
                l.handleEvent(event);
            } catch (Throwable ex) {
                LOG.warn(String.format("%s threw exception while handling event.", l), ex);
            }
        }
        this.looping = false;
        for (Listener<E> l : this.listenersToAdd) {
            this.listeners.add(l);
        }
        this.listenersToAdd = null;
        for (Listener<E> l : this.listenersToRemove) {
            this.listeners.remove(l);
        }
        this.listenersToRemove = null;
    }

    public void addListener(Listener<E> l) {
        if (this.looping) {
            // Currently enumerating, so delay the add until the loop is done.
            this.listenersToAdd.add(l);
        } else {
            // Not in a loop, so add the listener immediately.
            this.listeners.add(l);
        }
    }

    public void removeListener(Listener<E> l) {
        if (this.looping) {
            // Currently enumerating, so delay the removal until the loop is done.
            this.listenersToRemove.add(l);
        } else {
            // Not in a loop, so remove the listener immediately.
            this.listeners.remove(l);
        }
    }
}
