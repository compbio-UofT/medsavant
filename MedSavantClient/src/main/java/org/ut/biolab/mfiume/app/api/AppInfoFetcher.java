package org.ut.biolab.mfiume.app.api;

import java.util.List;
import org.ut.biolab.mfiume.app.AppInfo;


/**
 *
 * @author mfiume
 */
public interface AppInfoFetcher {

    public List<AppInfo> fetchApplicationInformation(String search) throws Exception;

}
