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
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.Listener;

/**
 *
 * @author mfiume
 */
public class DashboardSection {

    private String name;
    private ArrayList<LaunchableApp> apps;
    private boolean enabled;

    public DashboardSection(String name) {
        this.name = name;
        this.enabled = true;
        this.apps = new ArrayList<LaunchableApp>();
    }

    public String getName() {
        return name;
    }

    public void addLaunchableApp(LaunchableApp app) {
        this.apps.add(app);
    }

    public List<LaunchableApp> getApps() {
        return this.apps;
    }

    public void setEnabled(boolean b) {
        this.enabled = b;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
