package org.ut.biolab.medsavant.view.list;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public abstract class DetailedListEditor {

    public boolean doesImplementAdding() {return false;}
    public boolean doesImplementDeleting() {return false;}
    public boolean doesImplementEditing() {return false;}
    
    public abstract void addItems();
    public abstract void editItems(Object[] item);
    public abstract void deleteItems(List<Object[]> items);
    
}
