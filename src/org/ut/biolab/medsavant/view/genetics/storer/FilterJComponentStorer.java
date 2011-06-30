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
    
    private static final HashMap<String, JComponent> filterElementToComponent
            = new HashMap<String, JComponent>();
    
    /**
     * Add a component to this list.
     * @param name the name by which this component should be identified.
     * @param component the corresponding component.
     */
    public static void addJComponent(String name, JComponent component){
        filterElementToComponent.put(name, component);
    }
}
