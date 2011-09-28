/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.awt.BorderLayout;
import java.sql.SQLException;
import javax.swing.JPanel;
import javax.swing.JTree;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.OntologyPanelGenerator;
import org.ut.biolab.medsavant.view.genetics.filter.GOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;
import org.ut.biolab.medsavant.view.util.WaitPanel;

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
            return jTree;
        }
    }

}
