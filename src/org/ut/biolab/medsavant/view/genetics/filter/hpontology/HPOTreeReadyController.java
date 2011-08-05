/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.hpontology;

import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;

/**
 *
 * @author Nirvana Nursimulu
 */
public class HPOTreeReadyController {
    
    private static Tree hpoTree;
    
    public static List<HPOTreeReadyListener> listListeners = new ArrayList<HPOTreeReadyListener>();
    
    // Synchronized
    public static synchronized void addHPOTreeReadyListener(HPOTreeReadyListener l){
        listListeners.add(l);
    }
    
    public static void addHPOTree(Tree hpoTree){
        HPOTreeReadyController.hpoTree = hpoTree;
        fireHPOTreeReady();
    }
    
    public static Tree getHPOTree(){
        return hpoTree;
    }
    
    /**
     * Tell me when the HPO tree is ready.
     */
    private static void fireHPOTreeReady(){
        for (HPOTreeReadyListener l: listListeners){
            l.hpoTreeReady();
        }
    }
    
}
