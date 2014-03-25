/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.view.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
class LaunchHistory {

    private final ArrayList<LaunchableApp> history;

    private int numRecents = 5;
    
    LaunchHistory() {
        history = new ArrayList<LaunchableApp>();
    }

    List<LaunchableApp> getRecentHistory() {
        if (history.size() <= numRecents) {
            return history;
        } else {
            return history.subList(0, numRecents);
        }
    }

    void add(LaunchableApp app) {
        if (history.contains(app)) {
            history.remove(app);
        }
        history.add(0, app);
    }
}
