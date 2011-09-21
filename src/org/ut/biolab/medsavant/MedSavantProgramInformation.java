/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant;

/**
 *
 * @author mfiume
 */
public class MedSavantProgramInformation {
    
    private static final String version = "1.0";
    private static final String releaseType = "beta"; // either "beta" or "release"

    public static String getVersion() {
        return version;
    }
    
    public static String getReleaseType() {
        return releaseType;
    }
}
