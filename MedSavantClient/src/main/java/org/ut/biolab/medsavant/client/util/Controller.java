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
