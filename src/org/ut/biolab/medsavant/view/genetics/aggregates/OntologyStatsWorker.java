/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import com.jidesoft.swing.CheckBoxTree;
import com.jidesoft.swing.CheckBoxTreeSelectionModel;
import com.jidesoft.tree.FilterableCheckBoxTreeSelectionModel;
import com.jidesoft.tree.QuickTreeFilterField;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.TreeUtils;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.CheckBoxTreeNew;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.util.ViewUtil;
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
    
    private static volatile HashMap<String, Node> nodesThatWereAlreadySelected = 
            new HashMap<String, Node>();   
    
    private volatile OntologySubPanel subPanel;    
    
    public static volatile HashMap<String, JTree> mapNameToTree = 
            new HashMap<String, JTree>();
    
    public static volatile HashMap<String, JPanel> mapNameToTopPanel = 
            new HashMap<String, JPanel>();
    
    private volatile JTree jTree;
    
    private volatile JPanel topPanel;
    
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

            if (get() == null){
                return;
            }
            JScrollPane scrollPane = new JScrollPane((JTree)get());
            subPanel.removeAll();
            
            subPanel.add(topPanel, BorderLayout.NORTH);
            subPanel.add(scrollPane, BorderLayout.CENTER);
            subPanel.getJTree().repaint();
            subPanel.updateUI();
        } 
        catch (Exception ex){
            
            OntologyStatsWorker.killIndividualThreads(subPanel);
            OntologyStatsWorker.mapLocToFreq.clear();
            OntologyStatsWorker.removeStatsFromVisibleNodes();
        }
    }
    
    /**
     * Update statistics for nodes that have been selected or for nodes which 
     * are children of nodes which have previously been selected.
     * @param userProvPath null if we want to look at nodes which have been
     * selected; otherwise, give the path of the tree which has been expanded.
     */
    public static void updateStatistics(TreePath userProvPath){
        
        if (singleWorker == null){
            return;
        }

        List<DefaultMutableTreeNode> purgedSelectedNodes = 
                singleWorker.getPurgedSelectedNodes(singleWorker.jTree, userProvPath);
        // Change statistics for only the NEWLY visible nodes.
        try{
            singleWorker.changeStatistics(singleWorker.jTree, purgedSelectedNodes, 
                    singleWorker.subPanel.chromSplitIndex, 
                    singleWorker.subPanel.startSplitIndex, 
                    singleWorker.subPanel.endSplitIndex);
        }
        catch(Exception e){
        } 
    }
    
    private JTree getOntologyStats(){
            
        // Get the tree if it is available.
        jTree = mapNameToTree.get(subPanel.getName());
        topPanel = mapNameToTopPanel.get(subPanel.getName());
        if (jTree == null){
            
            if (!subPanel.treeIsReadyToBeFetched()){
                subPanel.removeAll();
                subPanel.add(new WaitPanel("Getting aggregate statistics"));
                subPanel.updateUI();
                return null;
            }
            jTree = subPanel.getJTree();
            
            topPanel = ViewUtil.getClearPanel();
            topPanel.setLayout(new BorderLayout());

            JButton applyButton = new JButton("Apply");
            applyButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    singleWorker.updateStatistics(null);
                }
            });
            topPanel.add(applyButton, BorderLayout.EAST);
            QuickTreeFilterField filterField = new QuickTreeFilterField(jTree.getModel());
            filterField.setHintText("Type to search");
            
            // TODO: fix the slow filtering...then, un-comment the following line.
//            topPanel.add(filterField, BorderLayout.CENTER);

            jTree = new CheckBoxTreeNew(filterField.getDisplayTreeModel()){
                @Override
                protected CheckBoxTreeSelectionModel createCheckBoxTreeSelectionModel(TreeModel model) {
                    return new FilterableCheckBoxTreeSelectionModel(model);
                }
            };
            filterField.setTree(jTree);
                        
            mapNameToTree.put(subPanel.getName(), jTree); 
            mapNameToTopPanel.put(subPanel.getName(), topPanel);
        }
        
        jTree.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeExpanded(TreeExpansionEvent event) {               
                
                DefaultMutableTreeNode lastElem = 
                        (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
                Node lastNode = (Node)lastElem.getUserObject();
                
                // Only if this node has been selected earlier do we update
                // statistics for it and for its children.
                if (!nodesThatWereAlreadySelected.containsKey(lastNode.getIdentifier())){
                    return;
                }
                        
                updateStatistics(event.getPath());
            }

            // Don't do anything here, just let whatever's been happening keep going on.
            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
            
        return jTree;
    }
    
    private static List<DefaultMutableTreeNode> getPurgedSelectedNodes
            (JTree newjtree, TreePath userProvPath){
        
        List<DefaultMutableTreeNode> selectedNodes = null;

        selectedNodes = getSelectedNodes(newjtree, userProvPath);
        
        // Get only those nodes that used to be invisible to the user, also, 
        // consider those nodes that are children of the selected or user provided
        // nodes.
        List<DefaultMutableTreeNode> purgedSelectedNodes = new ArrayList<DefaultMutableTreeNode>();

        // First subtract all the nodes that were already visible from 
        // that list (using the identifiers), and add the identifiers of 
        // those nodes that are already visible to the hashset.
        for (DefaultMutableTreeNode selectedNode: selectedNodes){

            Node node = (Node)selectedNode.getUserObject();
            
            // Update only nodes which have not been updated yet.
            if (!nodesThatWereAlreadySelected.keySet().contains(node.getIdentifier())){
                purgedSelectedNodes.add(selectedNode);
                nodesThatWereAlreadySelected.put(node.getIdentifier(), node);
            }
        }

        return purgedSelectedNodes;
    }
    
    private static List<DefaultMutableTreeNode> getSelectedNodes
            (JTree jtree, TreePath userProvPath){
        
        List<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>();
        TreePath[] selectedPaths = null;
        
        // If we are to look at selected nodes, look at them PLUS at children
        // nodes that happen to be visible at the time.
        if (userProvPath == null){
            selectedPaths = ((CheckBoxTree)jtree).getCheckBoxTreeSelectionModel().getSelectionPaths();
            List<TreePath> allSelectedPaths = new ArrayList<TreePath>();            
            if (selectedPaths != null){
                for (TreePath selectedPath: selectedPaths){
                    TreeUtils.getPaths(jtree, selectedPath, true, allSelectedPaths);
                }
            }
            selectedPaths = allSelectedPaths.toArray(new TreePath[0]);
        }
        // Otherwise, look at user provided path, PLUS any nodes that happen to 
        // be visible under the user provided path.
        else{
            List<TreePath> visiblePaths = new ArrayList<TreePath>();
            TreeUtils.getPaths(singleWorker.jTree, userProvPath, true, visiblePaths);
            selectedPaths = visiblePaths.toArray(new TreePath[0]);
        }
        
        if (selectedPaths == null){
            return selectedNodes;
        }
        for (TreePath selectedPath: selectedPaths){
            selectedNodes.add((DefaultMutableTreeNode)selectedPath.getLastPathComponent());
        }
        return selectedNodes;
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
       * Remove all the stats from those nodes that used to be visible.
       */
      private static void removeStatsFromVisibleNodes(){
          Iterator<Node> ite = nodesThatWereAlreadySelected.values().iterator();
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
                    OntologyStatsWorker.nodesThatWereAlreadySelected.clear();
                    OntologyStatsWorker.mapLocToFreq.clear();
                    OntologyStatsWorker.listIndividualThreads.clear();
                    OntologyStatsWorker.removeStatsFromVisibleNodes();
                    return null;
            }
            
            protected void done(){

                    subPanel.removeAll();                    
                    subPanel.updateUI();
                    subPanel.repaint();
                    subPanel.setProgress(0);
                    singleWorker = new OntologyStatsWorker(subPanel);
                    singleWorker.execute();
            }
      }
      
      private synchronized void stopEverything(OntologySubPanel subPanel){
              
           Stopper stopper = new Stopper(subPanel);
           stopper.execute();
      }      
}
