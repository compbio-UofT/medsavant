/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.serverapi;

/**
 * 
 * @author mfiume
 */
public class MedSavantProgramInformation {

    // version to user if the version could not be found
    private static final String UNDEFINED_VERSION = "";

    public static String getVersion() {
        String version = UNDEFINED_VERSION;

        Package aPackage = MedSavantProgramInformation.class.getPackage();
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
