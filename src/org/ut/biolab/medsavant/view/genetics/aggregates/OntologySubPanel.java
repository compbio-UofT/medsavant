/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTree;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.filter.GOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author Nirvana Nursimulu
 */
public abstract class OntologySubPanel extends JPanel implements FiltersChangedListener, AggregatePanelGenerator{

    
    private WaitPanel waitPanel;
    /**
     * Point in the concatenated string (in locations) where chromosome's name.
     */
    public final int chromSplitIndex;
    /**
     * Point in the concatenated string (in locations) where start point.
     */
    public final int startSplitIndex;
    /**
     * Point in the concatenated string (in locations) where end point.
     */
    public final int endSplitIndex;
    
    private OntologyStatsWorker osw;

        
    public OntologySubPanel(int chromSplitIndex, int startSplitIndex, int endSplitIndex){
        
        this.setLayout(new BorderLayout());
        waitPanel = new WaitPanel("Getting aggregate statistics");
        this.add(waitPanel, BorderLayout.CENTER);
        this.updateUI();
        this.chromSplitIndex = chromSplitIndex;
        this.startSplitIndex = startSplitIndex;
        this.endSplitIndex = endSplitIndex;
    }
    
    /**
     * Start to gather info into the tree.
     */
    public void update(){
        
        // There are some threads that we are not doing anything about here...
                
        // Kill any existing threads, if any.
        if (osw != null && !osw.isDone()){  osw.cancel(true);     }
        
        osw = new OntologyStatsWorker(this, waitPanel);
        osw.execute();
    }
    
    protected abstract boolean treeIsReadyToBeFetched(); 
    
    protected abstract JTree getJTree();

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        // Do not use the same trees as were made earlier.
        OntologyStatsWorker.removeStatsFromVisibleNodes();
        OntologyStatsWorker.nodesThatWereAlreadyVisible.clear();
        OntologyStatsWorker.mapNameToTree.clear();
        OntologyStatsWorker.mapLocToFreq.clear();
        this.update();
    }
    
    public JPanel getPanel(){
        this.update();
        return this;
    }
    
}
