/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.storer;

import java.util.HashMap;
import javax.swing.JComponent;

/**
 * Use this class to store those JComponents that took a long while to make, and
 * which you would rather just store that remake.
 * @author Nirvana Nursimulu
 */
public class FilterJComponentStorer {
    
    private static final HashMap<String, JComponent> mapToComponent
            = new HashMap<String, JComponent>();
    
    /**
     * Add a component to this list.
     * @param name the name by which this component should be identified.
     * @param component the corresponding component.
     */
    public static void addJComponent(String name, JComponent component){
        mapToComponent.put(name, component);
    }
    
    /**
     * Get the object by this name
     * @param name the name of the jComponent.
     * @return the corresponding object, and null if the object was not even 
     * inserted here in the first place.  Note that IF the object returned is 
     * null, this does not necessarily mean that a null object was not inserted
     * in the dictionary in the first place.
     */
    public static JComponent getJComponent(String name){
        return mapToComponent.get(name);
    } 
    
    /**
     * Returns true iff a component with this name has been inserted in the 
     * static dict.
     * @param name the name of the component.
     * @return 
     */
    public static boolean containsComponentWithName(String name){
        return mapToComponent.containsKey(name);
    }    
}
