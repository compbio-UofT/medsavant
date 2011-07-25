/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ClassifiedPositionInfo;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;


/**
 *
 * @author Nirvana Nursimulu
 */
  public class WorkingWithOneNode extends Thread{

      private boolean isDoneWorking;
      private DefaultMutableTreeNode node;
      private int chromIndex;
      private int startIndex;
      private int endIndex;
      private JTree tree;
      private OntologySubPanel subPanel;
      private volatile boolean stop;

      public WorkingWithOneNode
              (JTree tree, DefaultMutableTreeNode node, int chromIndex, 
              int startIndex, int endIndex, OntologySubPanel subPanel){
          
          this.isDoneWorking = false;
          this.node = node;
          this.chromIndex = chromIndex;
          this.startIndex = startIndex;
          this.endIndex = endIndex;
          this.tree = tree;
          this.subPanel = subPanel;
          this.stop = false;
      }
      
      public DefaultMutableTreeNode getNode(){
          return node;
      }

    protected Object doTheWork() throws Exception {

        HashSet<String> locs = ((Node)(node.getUserObject())).getLocs();
        int numVariants = 0;
        
        ClassifiedPositionInfo cpi = new ClassifiedPositionInfo();
        
        for (String loc: locs){
            String[] split = loc.split("\t");
            String chrom = split[chromIndex].trim();
            int start = Integer.parseInt(split[startIndex].trim());
            int end = Integer.parseInt(split[endIndex].trim());
            
            // Note that we're adding one here.  Works for both GO and HPO since
            // the locations are not in BED format.
            cpi.addCondition(chrom, start, end + 1);
        }
        
        List<String> mergedRanges = cpi.getAllMergedRanges();

        for (String loc: mergedRanges){

            if (this.isInterrupted() || this.stop){
                throw new java.util.concurrent.CancellationException();
            }
            String[] split = loc.split("\t");
            String chrom = split[0].trim();
            int start = Integer.parseInt(split[1].trim());
            int end = Integer.parseInt(split[2].trim());

            String key = chrom + "_" + start + "_" + end;
            Integer numCurr = OntologyStatsWorker.mapLocToFreq.get(key);
            if (numCurr == null){;
                numCurr = QueryUtil.getNumVariantsInRange
                        (ConnectionController.connect(), chrom, start, end);
                OntologyStatsWorker.mapLocToFreq.put(key, numCurr);
            }
            else{
                OntologyStatsWorker.mapLocToFreq.put(key, numCurr);
            }

            if (this.isInterrupted() || this.stop){
                throw new java.util.concurrent.CancellationException();
            }
            numVariants = numVariants + numCurr;
            if (numVariants == 1){
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
        if (numVariants == 1){
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
    public void run() {
        try {
            doTheWork();
        } catch (Exception ex) {
//            System.out.println("Destroyed!\t" + node);
        }
    }
    

    /**
     * Interrupt this thread properly.
     */
    public void interrupt(){
        // Not sure if using this variable changes anything in terms of 
        // performance
        this.stop = true;
        super.interrupt();
    }
    
    public synchronized boolean isThreadDoneWorking(){
        return isDoneWorking;
    }
        
}