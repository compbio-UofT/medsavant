/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * Used for save dialogs. 
 * 
 * @author Andrew
 */
public class ExtensionFileFilter extends FileFilter {

    private final String extension;
    
    public static ExtensionFileFilter[] createFilters(String[] extensions){
        ExtensionFileFilter[] filters = new ExtensionFileFilter[extensions.length];
        for(int i = 0; i < extensions.length; i++){
            filters[i] = new ExtensionFileFilter(extensions[i]);
        }
        return filters;
    }

    public ExtensionFileFilter(String extension) {
        this.extension = extension;
    }

    public boolean accept(File f) {
        if (f.isDirectory()) { 
            return true; 
        } else {
            if (f.getAbsolutePath().toLowerCase().endsWith("." + extension)) { return true; }
        }
        return false;
    }

    @Override
    public String getDescription() {
        if(extension.equals("vcf")){
            return "VCF files | " + getExtensionString();
        } else if (extension.equals("svp")){
            return "Savant Project Files | " + getExtensionString();
        } else {
            return "*." + extension;
        }        
    }

    private String getExtensionString() {
        return "." + extension;
    }
    
    public String forceExtension(String dir, String name){
        if(accept(new File(dir, name))){
            return name;
        } else {
            return name + "." + extension;
        }
    }
    
    public String forceExtension(File f){
        if(accept(f)){
            return f.getAbsolutePath();
        } else {
            return f.getAbsolutePath() + "." + extension;
        }
    }

}
