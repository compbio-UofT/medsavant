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
