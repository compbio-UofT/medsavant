package org.ut.biolab.mfiume.app.api;

import java.util.Set;
import org.ut.biolab.mfiume.app.AppInfo;

/**
 *
 * @author mfiume
 */
public interface AppInstaller {

    public boolean installApp(AppInfo i);

    public Set<AppInfo> getInstallRegistry();

    public boolean uninstallApp(AppInfo appInfo);
}
