/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
public class OntologyStatsWorker extends SwingWorker{
    
    // Expecting only one such panel to be instantiated at a time.
    private static List<WorkingWithOneNode> listIndividualThreads = new ArrayList<WorkingWithOneNode>();
    public static HashMap<String, Integer> mapLocToFreq = new HashMap<String, Integer>();
    
    // We don't want to reload the information that has already been loaded.
    // Actually contains the identifiers of those nodes (unique).
    public static HashMap<String, Node> nodesThatWereAlreadyVisible = new HashMap<String, Node>();
    
    private OntologySubPanel subPanel;
    private WaitPanel waitPanel;
    
    // Map of a tree to the tree itself so that it does not need to be loaded over and over again.
    public static HashMap<String, JTree> mapNameToTree = new HashMap<String, JTree>();
    
    public OntologyStatsWorker(OntologySubPanel subPanel, WaitPanel waitPanel){
        this.subPanel = subPanel;
        this.waitPanel = waitPanel;
    }

    @Override
    protected Object doInBackground() throws Exception {
        return getOntologyStats();
    }
    
    @Override
    protected void done(){
        try {
//                System.out.println("Beginning of done");
            JScrollPane scrollPane = new JScrollPane((JTree)get());

            // Remove the wait panel first.
            subPanel.remove(waitPanel);
            subPanel.add(scrollPane);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    subPanel.getJTree().repaint();
                }
            });
            
            
            subPanel.updateUI();

//                System.out.println("Ending of done");
        } catch (Exception ex){
//            ex.printStackTrace();                
        }
    }    
    
    private JTree getOntologyStats(){
      
        // Get the tree if it is available.
        JTree jTree = mapNameToTree.get(subPanel.getName());
        if (jTree == null){
            // TODO: change this approach: what if the tree is never loaded?
            while (!subPanel.treeIsReadyToBeFetched())
                ;
            jTree = subPanel.getJTree();
            mapNameToTree.put(subPanel.getName(), jTree);
        }
             
        // Get all those nodes that are visible to the user.
        List<DefaultMutableTreeNode> visibleNodes = getVisibleNodes(jTree);
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

        // Change statistics for only the NEWLY visible nodes.
        changeStatistics(jTree, purgedVisibleNodes, subPanel.chromSplitIndex, 
                subPanel.startSplitIndex, subPanel.endSplitIndex);
        
        final JTree newjtree = jTree;     
        
        // Do something when the tree is expanded.
        jTree.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeExpanded(TreeExpansionEvent event) {
                
                if (!subPanel.getUpdateStatus()){
                    return;
                }
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

                // Change statistics for only the NEWLY visible nodes.
                try{                
                    changeStatistics(newjtree, purgedVisibleNodes, subPanel.chromSplitIndex, 
                        subPanel.startSplitIndex, subPanel.endSplitIndex);
                }
                catch(Exception e){
                    subPanel.stopEverything();
                }
            }

            public void treeCollapsed(TreeExpansionEvent event) {
                // Don't do anything. Just let whatever's been happening keep going on.
            }
        });
            
        return jTree;
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
        
        mapLocToFreq.clear();
        
//        System.out.println("WE ARE CHANGING THE STATISTICS.");
//        killIndividualThreads();
        
//        System.out.println("\n\n-------------");
//        listIndividualThreads.clear();
        for (DefaultMutableTreeNode node: visibleNodes){
            
            if (Thread.currentThread().isInterrupted()){
                throw new java.util.concurrent.CancellationException();
            }
            WorkingWithOneNode curr = new WorkingWithOneNode
                    (tree, node, chromIndex, startIndex, endIndex, subPanel);
            listIndividualThreads.add(curr);
            curr.execute();
//            System.out.println(node);
        }
//        System.out.println("-------------\n\n");
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
      public static void removeStatsFromVisibleNodes(){
          Iterator<Node> ite = nodesThatWereAlreadyVisible.values().iterator();
          while (ite.hasNext()){
              Node node = ite.next();
              node.setTotalDescription("");
          }
      }
    
      
      public static void killIndividualThreads(){
          // Kill each of those threads from before.
          for (WorkingWithOneNode thread: listIndividualThreads){
            if (!thread.isDone()){
                thread.cancel(true);
//                System.out.println("THIS IS NOW BEING CANCELLED.");
            }
//            System.out.println("Cancelling");
        }
      }
}
