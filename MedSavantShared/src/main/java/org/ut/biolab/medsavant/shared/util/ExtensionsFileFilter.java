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
