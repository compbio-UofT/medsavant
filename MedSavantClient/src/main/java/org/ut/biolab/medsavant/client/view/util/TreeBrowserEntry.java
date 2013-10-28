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
package org.ut.biolab.medsavant.client.view.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import com.jidesoft.grid.AbstractExpandableRow;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;

/**
 * Class which represents an entry in a tree-like display.  Used for the repository
 * browser and the plugin browser.
 *
 * @author mfiume
 */
public class TreeBrowserEntry extends AbstractExpandableRow implements Comparable<TreeBrowserEntry> {

    static FileSystemView _fileSystemView;

    private boolean isLeaf;
    private List<TreeBrowserEntry> children;
    private String name;
    private String type;
    private String description;
    private URL url;
    private String size;

    public TreeBrowserEntry(String name, List<TreeBrowserEntry> r) {
        this.name = name;
        setChildren(r);
    }

    public TreeBrowserEntry(
                String name,
                String type,
                String description,
                URL url,
                String size) {
            isLeaf = true;
            this.type = type;
            this.name = name;
            this.description = description;
            this.url = url;
            this.size = size;
        }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public Object getValueAt(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return this;
            case 1:
                return description;
            case 2:
                return type;
            case 3:
                return isLeaf ? url.getFile() : null;
            case 4:
                return isLeaf ? size : null;
        }
        return null;
    }

    @Override
    public Class<?> getCellClassAt(int columnIndex) {
        return null;
    }

    @Override
    public final void setChildren(List<?> value) {
        children = (List<TreeBrowserEntry>)value;
	if (children != null) {
            for (TreeBrowserEntry row : children) {
                row.setParent(this);
	    }
	}
    }

    @Override
    public boolean hasChildren() {
        return children != null;
    }

    @Override
    public List<?> getChildren() {
        return children;
    }

    static FileSystemView getFileSystemView() {
        if (_fileSystemView == null) {
            _fileSystemView = FileSystemView.getFileSystemView();
        }
        return _fileSystemView;
    }

    public Icon getIcon() {
        if (isLeaf) {
            String ext = ClientMiscUtils.getExtension(url);
            try {
                File f = File.createTempFile("savant_icon.", "." +ext);
                Icon i = getFileSystemView().getSystemIcon(f);
                f.delete();
                return i;
            } catch (IOException ex) {
                return null;
            }
        } else {
            return getFileSystemView().getSystemIcon(DirectorySettings.getMedSavantDirectory());
        }
    }


    @Override
    public int compareTo(TreeBrowserEntry o) {
        return getName().compareToIgnoreCase(o.getName());
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public URL getURL() {
        return url;
    }

    public boolean isLeaf() {
        return isLeaf;
    }
}
