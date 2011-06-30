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
    public static void addObject(String name, Object o){
        mapToObject.put(name, o);
    }
    
    /**
     * Get the object by this name
     * @param name the name of the object.
     * @return the corresponding object, and null if the object was not even 
     * inserted here in the first place.  Note that IF the object returned is 
     * null, this does not necessarily mean that a null object was not inserted
     * in the dictionary in the first place.
     */
    public static Object getObject(String name){
        return mapToObject.get(name);
    }
    
    public static boolean containsObjectWithName(String name){
        return mapToObject.containsKey(name);
    }
}
