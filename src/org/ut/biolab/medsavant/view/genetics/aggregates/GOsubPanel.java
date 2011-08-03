/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import javax.swing.JTree;
import org.ut.biolab.medsavant.view.genetics.OntologyPanelGenerator;
import org.ut.biolab.medsavant.view.genetics.filter.GOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;

/**
 *
 * @author Nirvana Nursimulu
 */
public class GOsubPanel extends OntologySubPanel{
    
    private JTree jTree;
    OntologyPanelGenerator.OntologyPanel panel;

    public GOsubPanel(OntologyPanelGenerator.OntologyPanel panel){
        super(panel, 1, 2, 3);
        this.panel = super.panel;
    }

    
    @Override
    public String getName(){
        return "Gene Ontology";
    }
    
    public boolean treeIsReadyToBeFetched(){
        return FilterObjectStorer.containsObjectWithName(GOFilter.NAME_TREE);
    }
    
    public JTree getJTree(){
        if (jTree != null){
            return jTree;
        }
        else{
            Tree tree = (Tree)FilterObjectStorer.getObject(GOFilter.NAME_TREE);
            jTree = ConstructJTree.getTree(tree, true, true, false);
//            System.out.println("Height of GO tree: " + TreeUtils.getHeight(jTree));
            return jTree;
        }
    }
    
}
