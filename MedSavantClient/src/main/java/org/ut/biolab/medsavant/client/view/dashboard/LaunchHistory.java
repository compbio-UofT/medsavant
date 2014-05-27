/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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
