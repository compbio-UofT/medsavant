/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import javax.swing.JTree;
import org.ut.biolab.medsavant.view.genetics.OntologyPanelGenerator;
import org.ut.biolab.medsavant.view.genetics.filter.hpontology.HPOTreeReadyController;
import org.ut.biolab.medsavant.view.genetics.filter.hpontology.HPOTreeReadyListener;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;

/**
 *
 * @author Nirvana Nursimulu
 */
public class HPOsubPanel extends OntologySubPanel implements HPOTreeReadyListener{
    
    private JTree jTree;
    OntologyPanelGenerator.OntologyPanel panel;
    
    public HPOsubPanel(OntologyPanelGenerator.OntologyPanel panel){
        super(panel, 0, 1, 2);
        this.panel = super.panel;
        HPOTreeReadyController.addHPOTreeReadyListener(this);
    }

    
    @Override
    public String getName(){
        return "Human Phenotype Ontology";
    }
    
    public boolean treeIsReadyToBeFetched(){
        return HPOTreeReadyController.getHPOTree() != null;
    } 
    
    
    public JTree getJTree(){
//        if (jTree != null){
//            return jTree;
//        }
//        else {
//            Tree tree = (Tree)FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
//            jTree = ConstructJTree.getTree(tree, false, true, false);
//            return jTree;
//        }
        return jTree;
    }

    public void hpoTreeReady() {
        Tree tree = HPOTreeReadyController.getHPOTree();
        jTree = ConstructJTree.getTree(tree, false, true, false);
        if (this.updatePanelUponFilterChanges){
            this.update();
        }
    }
    
    
}
