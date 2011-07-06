/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.awt.BorderLayout;
import java.sql.SQLException;
import javax.swing.JPanel;
import javax.swing.JTree;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
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
    
    private WaitPanel waitPanel;
    
    public GOsubPanel(){
        super(1, 2, 3);
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        super.filtersChanged();
    }
    
    @Override
    public String getName(){
        return "Gene Ontology";
    }
    
    public boolean treeIsReadyToBeFetched(){
        return FilterObjectStorer.containsObjectWithName(GOFilter.NAME_TREE);
    }
    
    public JTree getJTree(){
        Tree tree = (Tree)FilterObjectStorer.getObject(GOFilter.NAME_TREE);
        return ConstructJTree.getTree(tree, true, false);
    }

}
