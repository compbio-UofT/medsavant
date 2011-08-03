/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Nirvana Nursimulu
 */
public class TreeUtils {
    
    /**
     * Get the nodes that are visible in this tree.
     * @param tree
     * @return a listPaths containing all those nodes that are visible.
     */
    public static List<DefaultMutableTreeNode> getVisibleNodes(JTree tree){
        
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
      public static void getPaths
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
       * Get the height of this jTree.
       * @param jtree
       * @return 
       */
      public static int getHeight(JTree jtree){
        int height = 0;
        DefaultMutableTreeNode root = 
                (DefaultMutableTreeNode)jtree.getModel().getRoot();
        List<DefaultMutableTreeNode> listParents = new ArrayList<DefaultMutableTreeNode>();
        listParents.add(root);
        
        List<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>();
        
        while (!listParents.isEmpty()){
            
            children.clear();
            for (DefaultMutableTreeNode parent: listParents){
                for (Enumeration e = parent.children(); e.hasMoreElements();){
                    children.add((DefaultMutableTreeNode)e.nextElement());
                }
            }
            listParents.clear();
            
            for (DefaultMutableTreeNode child: children){
                listParents.add(child);
            }
            
            height = height + 1;
        }
        
        return height;
    }
    
}
