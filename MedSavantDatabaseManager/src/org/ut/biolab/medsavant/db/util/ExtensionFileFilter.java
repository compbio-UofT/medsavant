/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author mfiume
 */
public class ExtensionFileFilter extends FileFilter {

    private final String[] extensions;

    public ExtensionFileFilter(String extension) {
        this(new String[] {extension});
    }

    public ExtensionFileFilter(String[] exts) {
        this.extensions = exts;
    }

    public boolean accept(File f) {
        if (f.isDirectory()) { return true; }
        if (f.isFile()) {
            for (String extension : extensions) {
                if (f.getAbsolutePath().toLowerCase().endsWith("." + extension)) { return true; }
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        if(extensions[0].equals("vcf")){
            return "VCF files | " + getExtensionString();
        } else if (extensions[0].equals("svp")){
            return "Savant Project Files | " + getExtensionString();
        } else {
            return "*." + extensions[0];
        }        
    }

    private String getExtensionString() {
        String s = "";
        for (String e : extensions) {
            s += " *." + e;
        }
        return s;
    }

}
