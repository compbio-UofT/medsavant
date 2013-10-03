/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.serverapi;

/**
 *
 * @author mfiume
 */
public class MedSavantSDKInformation {

    // version to user if the version could not be found
    private static final String UNDEFINED_VERSION = "";

    public static String getSDKVersion() {
        String version = UNDEFINED_VERSION;

        Package aPackage = MedSavantSDKInformation.class.getPackage();
        if (aPackage != null) {
            version = aPackage.getImplementationVersion();
            if (version == null) {
                version = aPackage.getSpecificationVersion();
            }
        }
        if (version == null) {
            version = UNDEFINED_VERSION;
        }

        return version;
    }

    public static boolean isAppCompatible(String appSDKVersion) {
        return getSDKVersion().equals(appSDKVersion); // TODO: support backwards compatibility
    }
}
