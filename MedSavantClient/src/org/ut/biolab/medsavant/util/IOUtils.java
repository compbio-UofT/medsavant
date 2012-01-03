/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import java.io.File;
import org.ut.biolab.medsavant.settings.DirectorySettings;

/**
 *
 * @author Andrew
 */
public class IOUtils {
    
    public static void removeTmpFiles() {
        for (File f : DirectorySettings.getTmpDirectory().listFiles()) {
            f.delete();
        }
    }
    
}
