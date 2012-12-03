/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.shared.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * Used for save dialogs.   Not to be confused with ExtensionsFileFilter, which is almost identical, but used for Open dialogs.
 *
 * @author Andrew
 */
public class ExtensionFileFilter extends FileFilter implements java.io.FileFilter {

    private final String extension;

    public static ExtensionFileFilter[] createFilters(String[] extensions){
        ExtensionFileFilter[] filters = new ExtensionFileFilter[extensions.length];
        for(int i = 0; i < extensions.length; i++){
            filters[i] = new ExtensionFileFilter(extensions[i]);
        }
        return filters;
    }

    public ExtensionFileFilter(String ext) {
        this.extension = ext;
    }

    @Override
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
        if (extension.equals("vcf")) {
            return "VCF files | " + getExtensionString();
        } else if (extension.equals("svp")) {
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
