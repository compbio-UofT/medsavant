/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.awt.BorderLayout;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 * 
 * @author Nirvana Nursimulu
 */
public  class OntologyStatsWorker extends SwingWorker{
      
    private static volatile List<WorkingWithOneNode> listIndividualThreads = 
            new ArrayList<WorkingWithOneNode>();
    
    public static volatile HashMap<String, Integer> mapLocToFreq = 
            new HashMap<String, Integer>();
    
    public static volatile HashMap<String, Node> nodesThatWereAlreadyVisible = 
            new HashMap<String, Node>();    
    
    private volatile OntologySubPanel subPanel;    
    
    public static volatile HashMap<String, JTree> mapNameToTree = 
            new HashMap<String, JTree>();
    
    private volatile JTree jTree;
    
    private static volatile OntologyStatsWorker singleWorker;
    
    
    private OntologyStatsWorker(OntologySubPanel subPanel){
        this.subPanel = subPanel;
        OntologyStatsWorker.listIndividualThreads.clear();
    }
    
    public static void getNewInstance(OntologySubPanel subPanel){
        
        if (singleWorker != null){            
            singleWorker.cancel(true);
            
            // stop the present worker, and initiate a new one.
            singleWorker.stopEverything(subPanel);
        }
        else{
            singleWorker = new OntologyStatsWorker(subPanel);
            singleWorker.execute();
        }

    }

    @Override
    protected Object doInBackground() throws Exception {
        return getOntologyStats();
    }
    
    @Override
    protected void done(){
        try {

            JScrollPane scrollPane = new JScrollPane((JTree)get());
            subPanel.removeAll();
            subPanel.add(scrollPane);
            subPanel.getJTree().repaint();
            subPanel.updateUI();
        } 
        catch (Exception ex){
            
            OntologyStatsWorker.killIndividualThreads(subPanel);
            OntologyStatsWorker.mapLocToFreq.clear();
            OntologyStatsWorker.removeStatsFromVisibleNodes();
        }
    }    
    
    private JTree getOntologyStats(){
      
        // Get the tree if it is available.
        jTree = mapNameToTree.get(subPanel.getName());
        if (jTree == null){
            
            // TODO: change this approach: what if the tree is never loaded? Then, we're stuck in an infinite loop!
            while (!subPanel.treeIsReadyToBeFetched())
                ;
            jTree = subPanel.getJTree();
            mapNameToTree.put(subPanel.getName(), jTree);           
        }
             
        List<DefaultMutableTreeNode> purgedVisibleNodes = 
                getPurgedVisibleNodes(jTree);
        // Change statistics for only the NEWLY visible nodes.
        try{
            changeStatistics(jTree, purgedVisibleNodes, subPanel.chromSplitIndex, 
                    subPanel.startSplitIndex, subPanel.endSplitIndex);
        }
        catch(Exception e){
        }     
        
        jTree.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeExpanded(TreeExpansionEvent event) {
                if (!subPanel.getUpdateStatus()){
                    return;
                }
                List<DefaultMutableTreeNode> purgedVisibleNodes = 
                        getPurgedVisibleNodes(jTree);
                
                // Change statistics for only the NEWLY visible nodes.
                try{                
                    changeStatistics(jTree, purgedVisibleNodes, subPanel.chromSplitIndex, 
                        subPanel.startSplitIndex, subPanel.endSplitIndex);
                }
                catch(Exception e){
                }
            }
            
            // Don't do anything. Just let whatever's been happening keep going on.
            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
            
        return jTree;
    }
    
    private List<DefaultMutableTreeNode> getPurgedVisibleNodes(JTree newjtree){
        // Get all those nodes that are visible to the user.
        List<DefaultMutableTreeNode> visibleNodes = getVisibleNodes(newjtree);
        
        // Get only those nodes that used to be invisible to the user.
        List<DefaultMutableTreeNode> purgedVisibleNodes = new ArrayList<DefaultMutableTreeNode>();

        // First subtract all the nodes that were already visible from 
        // that list (using the identifiers), and add the identifiers of 
        // those nodes that are already visible to the hashset (This
        // hypothetically works).
        for (DefaultMutableTreeNode visibleNode: visibleNodes){

            Node node = (Node)visibleNode.getUserObject();
            if (!nodesThatWereAlreadyVisible.keySet().contains(node.getIdentifier())){
                purgedVisibleNodes.add(visibleNode);
                nodesThatWereAlreadyVisible.put(node.getIdentifier(), node);
            }
        }

        return purgedVisibleNodes;
    }
    
    
    /**
     * Change the statistics for the visible nodes given.
     * @param visibleNodes the nodes that are visible in the tree in question.
     * @param chromIndex index where the chromosome will be when the location info
     * is split.
     * @param startIndex index where the start position will be when the location
     * info is split.
     * @param endIndex index where the end position will be when the location info
     * is split.
     */
    private void changeStatistics
            (JTree tree, List<DefaultMutableTreeNode> visibleNodes, int chromIndex, int startIndex, int endIndex) {
        
        for (DefaultMutableTreeNode node: visibleNodes){
            
            if (Thread.interrupted()){
                throw new java.util.concurrent.CancellationException();
            }            
            WorkingWithOneNode curr = new WorkingWithOneNode
                    (tree, node, chromIndex, startIndex, endIndex, subPanel);
            listIndividualThreads.add(curr);
            ((Thread)curr).start();
        }
        OntologyStatsWorker.setTotalProgressInPanel(subPanel);
    }

    /**
     * Get the nodes that are visible in this tree.
     * @param tree
     * @return a listPaths containing all those nodes that are visible.
     */
    private static List<DefaultMutableTreeNode> getVisibleNodes(JTree tree){
        
        List<TreePath> listPaths = new ArrayList<TreePath>();
        // path of the root.
        TreePath rootPath = tree.getPathForRow(1).getParentPath();
        getPaths(tree, rootPath, true, listPaths);
        
        List<DefaultMutableTreeNode> nodes = 
                new ArrayList<DefaultMutableTreeNode>();
        for (TreePath path: listPaths){
            DefaultMutableTreeNode jnode = 
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            nodes.add(jnode);
        }
        return nodes;
    }
    
    /**
     * Get all the visible paths.
     * @param tree the tree in question
     * @param parent the parent of this node
     * @param expanded true iff we want the expanded nodes
     * @param list the list to contain all the paths.
     */
      private static void getPaths
              (JTree tree, TreePath parent, boolean expanded, List<TreePath> list) {
        
          if (expanded && !tree.isVisible(parent)) {
              return;
          }
          list.add(parent);
          TreeNode node = (TreeNode) parent.getLastPathComponent();
        
          if (node.getChildCount() >= 0) {          
              for (Enumeration e = node.children(); e.hasMoreElements();) {            
                  TreeNode n = (TreeNode) e.nextElement();            
                  TreePath path = parent.pathByAddingChild(n);            
                  getPaths(tree, path, expanded, list);          
              }        
          }  
      }
      
      /**
       * Remove all the stats from those nodes that used to be visible.
       */
      private static void removeStatsFromVisibleNodes(){
          Iterator<Node> ite = nodesThatWereAlreadyVisible.values().iterator();
          while (ite.hasNext()){
              Node node = ite.next();
              node.setTotalDescription("");
          }
         
      }
    
      
      private synchronized static void killIndividualThreads(OntologySubPanel subPanel){         
          // Kill each of those threads from before.
          for (WorkingWithOneNode thread: listIndividualThreads){
              ((WorkingWithOneNode)thread).interrupt();
             
          }
          
          for (WorkingWithOneNode thread: listIndividualThreads){
              while (thread.isAlive()){
//                  System.out.println("Trying to destroy " + thread.getNode());
              }
              ((Node)thread.getNode().getUserObject()).setTotalDescription("");
          }
            if (subPanel.getJTree() != null){
                subPanel.getJTree().repaint();
            }
            subPanel.updateUI();   
      }
      
    
      private static float getTotalLoaded(){            
          int counter = 0;  
          for (WorkingWithOneNode thread: listIndividualThreads){  
              if (thread.isThreadDoneWorking()){
                  counter += 1;
              }
          }            
//          System.out.println("Get total already loaded: " + counter);
          return counter;
      }

      private static float getTotalToLoad(){
//          System.out.println("Get total to load: " + listIndividualThreads.size());
          return listIndividualThreads.size();
      }

      public static void setTotalProgressInPanel(OntologySubPanel subPanel){   
          float totalLoaded = OntologyStatsWorker.getTotalLoaded();
          float totalToLoad = OntologyStatsWorker.getTotalToLoad();
          int percentage = Math.round(totalLoaded/totalToLoad * 100);
          subPanel.setProgress(percentage);
      }
      
      class Stopper extends SwingWorker{
            
            private OntologySubPanel subPanel;
            
            Stopper(OntologySubPanel subPanel){
                this.subPanel = subPanel;
            }
                  
            @Override
            protected Object doInBackground() throws Exception {
                
                    subPanel.removeAll();
                    subPanel.add(new WaitPanel("Getting aggregate statistics"));
                    OntologyStatsWorker.killIndividualThreads(subPanel);          
                    OntologyStatsWorker.nodesThatWereAlreadyVisible.clear();
                    OntologyStatsWorker.mapLocToFreq.clear();
                    OntologyStatsWorker.listIndividualThreads.clear();
                    OntologyStatsWorker.removeStatsFromVisibleNodes();
                    return null;
            }
            
            protected void done(){

                    subPanel.removeAll();                    
                    subPanel.updateUI();
                    subPanel.repaint();
                    singleWorker = new OntologyStatsWorker(subPanel);
                    singleWorker.execute();
            }
      }
      
      private synchronized void stopEverything(OntologySubPanel subPanel){
              
           Stopper stopper = new Stopper(subPanel);
           stopper.execute();
      }      
}
