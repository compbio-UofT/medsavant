package org.ut.biolab.medsavant.server.db;

/**
 *
 * @author mfiume
 */
public class VersionSettings {

    // version to user if the version could not be found
    private static final String UNDEFINED_VERSION = "";
    private static final String BETA_VERSION = "SNAPSHOT";

    /**
     * Is this version a beta release?
     */
    public static boolean isBeta() {
        // determine if the version is beta based on version substring - is this
        // needed?
        return getVersionString().toLowerCase().contains(BETA_VERSION.toLowerCase());
    }

    public static String getVersionString() {
        String version = UNDEFINED_VERSION;

        Package aPackage = VersionSettings.class.getPackage();
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
}
