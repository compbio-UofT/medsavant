/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.sql.SQLException;
import javax.swing.JPanel;
import javax.swing.JTree;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.filter.HPOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;

/**
 *
 * @author Nirvana Nursimulu
 */
public class HPOsubPanel extends OntologySubPanel{
    
    private JTree jTree;
    
    public HPOsubPanel(){
        super(0, 1, 2);
    }

    
    @Override
    public String getName(){
        return "Human Phenotype";
    }
    
    public boolean treeIsReadyToBeFetched(){
        return FilterObjectStorer.containsObjectWithName(HPOFilter.NAME_TREE);
    } 
    
    public Tree getTree(){
        return (Tree)FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
    }
    
    public JTree getJTree(){
        if (jTree != null){
            return jTree;
        }
        else {
            Tree tree = (Tree)FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
            jTree = ConstructJTree.getTree(tree, false, false);
            return jTree;
        }
    }
    
}
