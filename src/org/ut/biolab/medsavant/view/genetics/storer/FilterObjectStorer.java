/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.storer;

import java.util.HashMap;

/**
 *
 * @author Nirvana Nursimulu
 */
public class FilterObjectStorer {
    
    private static final HashMap<String, Object> mapToObject
            = new HashMap<String, Object>();
    
    /**
     * Add a component to this list.
     * @param name the name by which this component should be identified.
     * @param component the corresponding component.
     */
    public static void addJComponent(String name, Object o){
        mapToObject.put(name, o);
    }    
}
