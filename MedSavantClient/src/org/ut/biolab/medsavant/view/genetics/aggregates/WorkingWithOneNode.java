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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ClassifiedPositionInfo;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;


/**
 *
 * @author Nirvana Nursimulu
 */

public class WorkingWithOneNode extends MedSavantWorker {

    private boolean isDoneWorking;
    private DefaultMutableTreeNode node;
    private int chromIndex;
    private int startIndex;
    private int endIndex;
    private JTree tree;
    private OntologySubPanel subPanel;
    private volatile boolean stop;
    
    public WorkingWithOneNode
            (String pageName, JTree tree, DefaultMutableTreeNode node, int chromIndex, 
            int startIndex, int endIndex, OntologySubPanel subPanel) {
        super(pageName);
        this.isDoneWorking = false;
        this.node = node;
        this.chromIndex = chromIndex;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.tree = tree;
        this.subPanel = subPanel;
        this.stop = false;
    }
    
    public DefaultMutableTreeNode getNode() {
        return node;
    }
    
    public synchronized boolean isThreadDoneWorking() {
        return isDoneWorking;
    }
    
    @Override
    protected Object doInBackground() throws RemoteException, SQLException {
        HashSet<String> locs = ((Node)(node.getUserObject())).getLocs();
        int numVariants = 0;
        
        ClassifiedPositionInfo cpi = new ClassifiedPositionInfo();
        
        for (String loc: locs) {
            String[] split = loc.split("\t");
            String chrom = split[chromIndex].trim();
            int start = Integer.parseInt(split[startIndex].trim());
            int end = Integer.parseInt(split[endIndex].trim());
            
            // Note that we're adding one here.  Works for both GO and HPO since
            // the locations are not in BED format.
            cpi.addCondition(chrom, start, end + 1);
        }
        
        List<String> mergedRanges = cpi.getAllMergedRanges();

        for (String loc: mergedRanges) {

            if (this.isCancelled() || this.stop) {
                throw new java.util.concurrent.CancellationException();
            }
            String[] split = loc.split("\t");
            String chrom = split[0].trim();
            int start = Integer.parseInt(split[1].trim());
            int end = Integer.parseInt(split[2].trim());

            String key = chrom + "_" + start + "_" + end;
            Integer numCurr = OntologyStatsWorker.mapLocToFreq.get(key);
            if (numCurr == null) {
                numCurr = MedSavantClient.VariantManager.getNumVariantsInRange(
                        LoginController.sessionId, 
                        ProjectController.getInstance().getCurrentProjectID(), 
                        ReferenceController.getInstance().getCurrentReferenceID(), 
                        FilterController.getQueryFilterConditions(), 
                        chrom, 
                        start, 
                        end);
                OntologyStatsWorker.mapLocToFreq.put(key, numCurr);
            }
            else{
                OntologyStatsWorker.mapLocToFreq.put(key, numCurr);
            }

            if (this.isCancelled() || this.stop) {
                throw new java.util.concurrent.CancellationException();
            }
            numVariants = numVariants + numCurr;
            if (numVariants == 1) {
                ((Node)node.getUserObject()).setTotalDescription(" [>=" + numVariants + " record]");
            }
            else{
                ((Node)node.getUserObject()).setTotalDescription(" [>=" + numVariants + " records]");
            }
                    
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tree.repaint();
                    tree.updateUI();
                }     
            });
        }
        String additionalDesc = null;
        if (numVariants == 1) {
            additionalDesc = " [" + numVariants + " record in all]";
        }
        else{
            additionalDesc = " [" + numVariants + " records in all]";
        }
        ((Node)node.getUserObject()).setTotalDescription(additionalDesc);
        isDoneWorking = true;
        OntologyStatsWorker.setTotalProgressInPanel(subPanel);
        
        return numVariants;
    }
    
    @Override
    protected void showProgress(double fraction) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void showSuccess(Object result) {
        tree.repaint();
        tree.updateUI();
    }

}

