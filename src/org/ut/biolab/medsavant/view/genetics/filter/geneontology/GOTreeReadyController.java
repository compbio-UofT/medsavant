/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.geneontology;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nirvana Nursimulu
 */
public class GOTreeReadyController {
    
    private static GOTree goTree;
    
    public static List<GOTreeReadyListener> listListeners = new ArrayList<GOTreeReadyListener>();
    
    public static void addGOTreeReadyListener(GOTreeReadyListener l){
        listListeners.add(l);
    }
    
    public static void addGOTree(GOTree goTree){
        GOTreeReadyController.goTree = goTree;
        fireGOTreeReady();
    }
    
    public static GOTree getGOTree(){
        return goTree;
    }
    
    /**
     * Tell me when the GO Tree is ready.
     */
    private static void fireGOTreeReady(){
        for (GOTreeReadyListener l: listListeners){
            l.goTreeReady();
        }
    }
}
