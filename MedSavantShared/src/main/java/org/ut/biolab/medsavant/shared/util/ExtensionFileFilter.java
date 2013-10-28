/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
