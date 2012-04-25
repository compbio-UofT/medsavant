/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.list;

import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public interface DetailedListModel {
    
    public List<Object[]> getList(int limit) throws Exception;

    public List<String> getColumnNames();

    public List<Class> getColumnClasses();

    public List<Integer> getHiddenColumns();
    
}
