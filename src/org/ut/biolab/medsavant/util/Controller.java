/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Generic controller class which provides functionality which can be used by other
 * controllers.
 *
 * @author tarkvara
 */
public abstract class Controller<E> {
    private static final Log LOG = LogFactory.getLog(Controller.class);

    protected List<Listener<E>> listeners = new ArrayList<Listener<E>>();
    private List<Listener<E>> listenersToAdd;
    private List<Listener<E>> listenersToRemove;

    /**
     * Fire the specified event to all our listeners.
     */
    public synchronized void fireEvent(E event) {
        listenersToAdd = new ArrayList<Listener<E>>();
        listenersToRemove = new ArrayList<Listener<E>>();
        for (final Listener l: listeners) {
            try {
                l.handleEvent(event);
            } catch (Throwable x) {
                LOG.warn(l + " threw exception while handling event.", x);
            }
        }
        for (Listener<E> l: listenersToAdd) {
            listeners.add(l);
        }
        listenersToAdd = null;
        for (Listener<E> l: listenersToRemove) {
            listeners.remove(l);
        }
        listenersToRemove = null;
    }

    public void addListener(Listener<E> l) {
        if (listenersToAdd != null) {
            // Currently enumerating, so delay the add until the loop is done.
            listenersToAdd.add(l);
        } else {
            // Not in a loop, so add the listener immediately.
            listeners.add(l);
        }
    }

    public void removeListener(Listener<E> l) {
        if (listenersToRemove != null) {
            // Currently enumerating, so delay the removal until the loop is done.
            listenersToRemove.add(l);
        } else {
            // Not in a loop, so remove the listener immediately.
            listeners.remove(l);
        }
    }
}
