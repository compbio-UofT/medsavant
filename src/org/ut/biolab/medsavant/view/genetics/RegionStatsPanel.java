/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.filter.GOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.HPOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;



/**
 *
 * @author Nirvana Nursimulu
 */
public class RegionStatsPanel extends JPanel implements FiltersChangedListener{
    
    private JPanel toolBarPanel;
    private String currentRegionStat;
    private RegionStatsWorker rsw;
    private WaitPanel waitPanel;
    // Expecting only one such panel to be instantiated.
    private static List<WorkingWithOneNode> listIndividualThreads = 
            new ArrayList<WorkingWithOneNode>();
    private static HashMap<String, Integer> mapLocToFreq = 
            new HashMap<String, Integer>();
    
    /**
     * List containing the name of region stats the user can view.
     */
    private static final String[] regionStatsNames = {"Gene Ontology", "Human Phenotype Ontology"};
    

    
    public RegionStatsPanel(){
        this.setLayout(new BorderLayout());

        initToolBar();
        updateRegionStats();
        FilterController.addFilterListener(this);
    }
    
    private void updateRegionStats(){
        
//        this.remove(bar);
        this.removeAll();
        this.add(toolBarPanel, BorderLayout.NORTH);
        waitPanel = new WaitPanel("Getting region statistics");
        this.add(waitPanel, BorderLayout.CENTER);
        this.updateUI();
        
        // Kill any existing threads, if any.
        if (rsw != null && !rsw.isDone()){  rsw.cancel(true);     }
        
        rsw = new RegionStatsWorker(currentRegionStat, this, waitPanel);
        rsw.execute();
//        this.updateUI();
    }  
    
//    private void drawRegionStats(JComponent component){
//        // Remove the wait panel first.
//        this.remove(waitPanel);
//        this.add(component);
//    }    
    
    private void initToolBar(){
        
        toolBarPanel = ViewUtil.getBannerPanel();
        toolBarPanel.setBorder(ViewUtil.getMediumBorder());
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));

        toolBarPanel.add(Box.createHorizontalGlue());

        toolBarPanel.add(new JLabel("Region statistics for: "));      
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        toolBarPanel.add(bar);
        
//        bar.setFloatable(false);        
        JComboBox b = new JComboBox();
        
        for (String regionStatsName: regionStatsNames){
            b.addItem(regionStatsName);
        }
        
        setCurrentRegionStats(regionStatsNames[1]);
        
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String regionStatsName = (String) cb.getSelectedItem();
                setCurrentRegionStats(regionStatsName);
                updateRegionStats();
            }
        });
        
        bar.add(b);
//        bar.add(Box.createHorizontalStrut(5));
        this.add(toolBarPanel, BorderLayout.NORTH);   
        this.updateUI();
    }

    private void setCurrentRegionStats(String regionStatsName) {
        currentRegionStat = regionStatsName;
    }
    
    
    public class RegionStatsWorker extends SwingWorker{
        
        private String regionStatsName;
        private JPanel panel;
        private JPanel waitPanel;
        
        public RegionStatsWorker(String regionStatsName, JPanel panel, WaitPanel waitPanel){
            this.regionStatsName = regionStatsName;
            this.panel = panel;
            this.waitPanel = waitPanel;
        }
        
        // TODO: make this more general if possible, for future purposes.
        private JComponent getRegionStatsFor(String regionStatsName){
            
            JComponent component = null;
            if (regionStatsName.equals("Gene Ontology")){  
                // TODO: change this approach: what if the GO tree is never loaded?
                while (!FilterObjectStorer.containsObjectWithName(GOFilter.NAME_TREE))
                    ;
                Object o = FilterObjectStorer.getObject(GOFilter.NAME_TREE);
                component = ConstructJTree.getTree((Tree)o, true, false);
//                    System.out.println("Gene Ontology tree constructed for Regions stats.");
            }
            else if (regionStatsName.equals("Human Phenotype Ontology")){
                // TODO: change this approach: what if the HPO tree is never loaded?
                while (!FilterObjectStorer.containsObjectWithName(HPOFilter.NAME_TREE))
                    ;
                Object o = FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
                component = ConstructJTree.getTree((Tree)o, false, false);
//                    System.out.println("HPO tree constructed for Region stats");
            }

            if (regionStatsName.equals("Gene Ontology") || 
                    regionStatsName.equals("Human Phenotype Ontology")){
             
                // Get those nodes that are visible to the user.
                List<DefaultMutableTreeNode> visibleNodes = 
                        RegionStatsPanel.getVisibleNodes((JTree)component);
                // Change the descriptions of those nodes that are visible
                // according to the statistics.
                if (regionStatsName.equals("Gene Ontology")){
                    RegionStatsPanel.changeStatistics((JTree)component, visibleNodes, 1, 2, 3);
                }
                else{
                    RegionStatsPanel.changeStatistics((JTree)component, visibleNodes, 0, 1, 2);
                }
            }
            return component;
        }

        @Override
        protected Object doInBackground() throws Exception {
            return getRegionStatsFor(regionStatsName);
        }
        
        @Override
        protected void done(){
            try {
//                System.out.println("Beginning of done");
                JScrollPane scrollPane = new JScrollPane((JComponent)get());

                // Remove the wait panel first.
                panel.remove(waitPanel);
                panel.add(scrollPane);
                panel.updateUI();
        
//                System.out.println("Ending of done");
            } catch (Exception ex){
                ex.printStackTrace();
                Logger.getLogger(RegionStatsPanel.class.getName()).log(Level.SEVERE, null, ex);                
            }
        }
        
    }
    
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        updateRegionStats();
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
     * Change the statistics for the visible nodes given.
     * @param visibleNodes the nodes that are visible in the tree in question.
     * @param chromIndex index where the chromosome will be when the location info
     * is split.
     * @param startIndex index where the start position will be when the location
     * info is split.
     * @param endIndex index where the end position will be when the location info
     * is split.
     */
    private static void changeStatistics
            (JTree tree, List<DefaultMutableTreeNode> visibleNodes, int chromIndex, int startIndex, int endIndex) {
        
        RegionStatsPanel.mapLocToFreq.clear();
        
        // Kill each of those threads from before.
        for (WorkingWithOneNode thread: listIndividualThreads){
            if (!thread.isDone()){
                thread.cancel(true);
            }
//            System.out.println("Cancelling");
        }
        listIndividualThreads.clear();
        for (DefaultMutableTreeNode node: visibleNodes){
            WorkingWithOneNode curr = 
                    new WorkingWithOneNode(tree, node, chromIndex, startIndex, endIndex);
            listIndividualThreads.add(curr);
            curr.execute();
        }
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
      
      
      public static class WorkingWithOneNode extends SwingWorker{
          
          private DefaultMutableTreeNode node;
          private int chromIndex;
          private int startIndex;
          private int endIndex;
          private JTree tree;
          
          public WorkingWithOneNode
                  (JTree tree, DefaultMutableTreeNode node, int chromIndex, 
                  int startIndex, int endIndex){
              this.node = node;
              this.chromIndex = chromIndex;
              this.startIndex = startIndex;
              this.endIndex = endIndex;
              this.tree = tree;
          }

        @Override
        protected Object doInBackground() throws Exception {
            
            HashSet<String> locs = ((Node)(node.getUserObject())).getLocs();
            int numVariants = 0;
            for (String loc: locs){
                
                String[] split = loc.split("\t");
                String chrom = split[chromIndex].trim();
                int start = Integer.parseInt(split[startIndex].trim());
                int end = Integer.parseInt(split[endIndex].trim());
                
                String key = chrom + "_" + start + "_" + end;
                Integer numCurr = RegionStatsPanel.mapLocToFreq.get(key);
                if (numCurr == null){
                    
                    numCurr = QueryUtil.getNumVariantsInRange
                            (ConnectionController.connect(), chrom, start, end);
                    RegionStatsPanel.mapLocToFreq.put(key, numCurr);
                }

                numVariants = numVariants + numCurr;
                ((Node)node.getUserObject()).setTotalDescription(" [" + numVariants + " records]");
//                System.out.println(numVariants);
                tree.repaint();
            }
           
            return numVariants;
        }
        
        @Override
        protected void done(){
            try {
                String additionalDesc = " [" + get() + " records in all]";
                ((Node)node.getUserObject()).setTotalDescription(additionalDesc);
                tree.repaint();
            } catch (InterruptedException ex) {
                Logger.getLogger(RegionStatsPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(RegionStatsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
//            tree.repaint();
        }
          
      }
}
