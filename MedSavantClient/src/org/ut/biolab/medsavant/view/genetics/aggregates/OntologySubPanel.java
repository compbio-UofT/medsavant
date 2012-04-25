/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.awt.BorderLayout;
import java.sql.SQLException;
import javax.swing.JPanel;
import javax.swing.JTree;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.FatalDatabaseException;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.OntologyPanelGenerator;
import org.ut.biolab.medsavant.view.util.WaitPanel;


/**
 *
 * @author Nirvana Nursimulu
 */
public abstract class OntologySubPanel extends JPanel implements 
        AggregatePanelGenerator, FiltersChangedListener {

    
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
    
    protected boolean updatePanelUponFilterChanges;
    
    protected OntologyPanelGenerator.OntologyPanel panel;
    
    private String pageName;
    
        
    public OntologySubPanel(OntologyPanelGenerator.OntologyPanel panel, String pageName,
            int chromSplitIndex, int startSplitIndex, int endSplitIndex) {
             
        this.pageName = pageName;
        this.setLayout(new BorderLayout());
        waitPanel = new WaitPanel("Getting aggregate statistics");
        this.add(waitPanel, BorderLayout.CENTER);
        this.updateUI();
        this.chromSplitIndex = chromSplitIndex;
        this.startSplitIndex = startSplitIndex;
        this.endSplitIndex = endSplitIndex;
        this.panel = panel;
        FilterController.addFilterListener(this);
    }
    
    
    /**
     * Start to gather info into the tree.
     */
    //public void update() {
    //    
    //}
    
    protected abstract boolean treeIsReadyToBeFetched(); 
    
    protected abstract JTree getJTree();

    @Override
    public void filtersChanged() throws SQLException, FatalDatabaseException, 
            NonFatalDatabaseException {
        //if (updatePanelUponFilterChanges) {
        //    this.update();
        //}
    }
    
    @Override
    public JPanel getPanel() {
        return this;
    }
    
    /*public void setUpdate(boolean updatePanelUponFilterChanges) {
        this.updatePanelUponFilterChanges = updatePanelUponFilterChanges;
        if (updatePanelUponFilterChanges) {
            this.update();
        }
    }*/
    
    @Override
    public void run(boolean reset) {
        OntologyStatsWorker.getNewInstance(this, pageName);
    }
    
    public void stop() {
        OntologyStatsWorker.stopInstance(this);
    }
    
    public boolean getUpdateStatus() {
        return updatePanelUponFilterChanges;
    }
    
    /**
     * Set the progress in the progress bar.
     * @param value 
     */
    public void setProgress(int value) {
        panel.updateProgess(value);
    }
    
//    protected void setActionUponApply() {
//        final OntologySubPanel curr = this;
//        panel.getApplyButton().addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                if (!curr.updatePanelUponFilterChanges) {
//                    return;
//                }
//                OntologyStatsWorker.updateStatistics(null);
//            }
//        });
//    }
}
