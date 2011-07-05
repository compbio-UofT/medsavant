/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author mfiume
 */
public class ExtensionFileFilter extends FileFilter {
    private final String extension;

    public ExtensionFileFilter(String extension) {
        this.extension = extension.toLowerCase();
    }

    public boolean accept(File f) {
        if (f.isDirectory()) { return true; }
        if (f.isFile()) {
            if (f.getAbsolutePath().toLowerCase().endsWith("." + extension)) { return true; }
        }
        return false;
    }

    @Override
    public String getDescription() {
        if(extension.equals("vcf")){
            return "VCF files | *.vcf";
        } else if (extension.equals("svp")){
            return "Savant Project Files | *.svp";
        } else {
            return "*." + extension;
        }        
    }

}
