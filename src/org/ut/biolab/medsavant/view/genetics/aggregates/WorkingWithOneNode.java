/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.util.HashSet;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;

/**
 *
 * @author Nirvana Nursimulu
 */
  public class WorkingWithOneNode extends SwingWorker{

      private DefaultMutableTreeNode node;
      private int chromIndex;
      private int startIndex;
      private int endIndex;
      private JTree tree;
      OntologySubPanel subPanel;

      public WorkingWithOneNode
              (JTree tree, DefaultMutableTreeNode node, int chromIndex, 
              int startIndex, int endIndex, OntologySubPanel subPanel){
          this.node = node;
          this.chromIndex = chromIndex;
          this.startIndex = startIndex;
          this.endIndex = endIndex;
          this.tree = tree;
          this.subPanel = subPanel;
      }

    @Override
    protected Object doInBackground() throws Exception {

        HashSet<String> locs = ((Node)(node.getUserObject())).getLocs();
        int numVariants = 0;
        for (String loc: locs){

            if (Thread.currentThread().isInterrupted()){
                throw new java.util.concurrent.CancellationException();
                // TODO: Not doing more here is probably creating tonnes of problems.
            }
            String[] split = loc.split("\t");
            String chrom = split[chromIndex].trim();
            int start = Integer.parseInt(split[startIndex].trim());
            int end = Integer.parseInt(split[endIndex].trim());

            String key = chrom + "_" + start + "_" + end;
            Integer numCurr = OntologyStatsWorker.mapLocToFreq.get(key);
            if (numCurr == null){

                numCurr = QueryUtil.getNumVariantsInRange
                        (ConnectionController.connect(), chrom, start, end);
                OntologyStatsWorker.mapLocToFreq.put(key, numCurr);
            }

            numVariants = numVariants + numCurr;
            ((Node)node.getUserObject()).setTotalDescription(" [>=" + numVariants + " records]");
//                System.out.println(numVariants);
//            tree.repaint();

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
//                    tree.repaint();
                    tree.updateUI();
                }
            });
        }

        return numVariants;
    }

        @Override
        protected void done(){
            try {
                String additionalDesc = " [" + get() + " records in all]";
                ((Node)node.getUserObject()).setTotalDescription(additionalDesc);
            }
            catch(Exception e){
                
                subPanel.stopEverything();
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    tree.repaint();
                }
        });
      }
  }