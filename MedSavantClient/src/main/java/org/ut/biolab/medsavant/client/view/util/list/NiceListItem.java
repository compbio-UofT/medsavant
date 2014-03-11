/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.util.list;

import javax.swing.ImageIcon;

/**
 *
 * @author mfiume
 */
public class NiceListItem {
    
    private final String label;
    private final Object item;
    private ImageIcon icon;
    private boolean removable;
 
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
    
    
    
}
