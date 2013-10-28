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
 *
 * Used for open dialogs.  Not to be confused with ExtensionFileFilter, which is almost identical, but used for Save dialogs.
 *
 * @author mfiume
 */
public class ExtensionsFileFilter extends FileFilter implements java.io.FileFilter {

    private final String[] extensions;

    public ExtensionsFileFilter(String[] exts) {
        this.extensions = exts;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        if (f.isFile()) {
            for (String ext: extensions) {
                if (f.getAbsolutePath().toLowerCase().endsWith("." + ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        if (extensions[0].equals("vcf")) {
            return "VCF files | " + getExtensionString();
        } else if (extensions[0].equals("svp")) {
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
