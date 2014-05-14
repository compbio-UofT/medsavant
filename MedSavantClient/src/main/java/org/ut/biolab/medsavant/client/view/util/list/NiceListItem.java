/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.util.list;

import javax.swing.ImageIcon;

/**
 *
 * @author mfiume
 */
public class NiceListItem {
    
    private String label;
    private final Object item;
    private ImageIcon icon;
    private boolean removable;
    private String description;
 
    public NiceListItem(Object item) {
        this(null,item);
    }
    
    public NiceListItem(String label, Object item) {
        this.label = label;
        this.item = item;
    }

    public Object getItem() {
        return item;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    @Override
    public String toString() {
        return label == null ? item.toString() : label;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NiceListItem other = (NiceListItem) obj;
        if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.label != null ? this.label.hashCode() : 0);
        return hash;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
