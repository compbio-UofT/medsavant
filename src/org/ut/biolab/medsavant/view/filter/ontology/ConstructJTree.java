/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nirvana Nursimulu
 */
public class ConstructJTree {
    
    /**
     * Obtain the jTree object to be made from this ontology.
     * @param tree the Tree object containing ontology information.
     * @param isForest true iff there is more than one root, in which case, a
     * default root is created.
     * @return the jTree object made.
     */
    public static JTree getTree(Tree tree, boolean isForest){

        DefaultMutableTreeNode actualRoot;
        if (isForest){
            // "dummy" root of the tree.
            Node root = new Node("...");
            root.setDescription("...");
            actualRoot = new DefaultMutableTreeNode(root);
        }
        else{
            actualRoot = 
                    new DefaultMutableTreeNode(tree.getRootNodes().toArray(new Node[1])[0]);
        }
        // Add the nodes beneath the root node to this tree.
        addNodes(actualRoot, tree, isForest);
        JTree jtree = new JTree(actualRoot);
        
        return jtree;
    }
    
    /**
     * Add the nodes to form part of the jTree.
     * @param actualRoot the actual root of the tree.
     * @param tree the tree containing all ontology information.
     * @param isForest true iff the tree has multiple roots.
     */
    private static void addNodes(DefaultMutableTreeNode actualRoot, Tree tree, boolean isForest){
        
        // To contain the roots of the tree.
        Set<Node> roots = tree.getRootNodes();
        
        // Get the name of the children while going down the tree.
        TreeSet<Node> children;
        
        // The child in consideration in context.
        DefaultMutableTreeNode child;
        
        // To contain the parent nodes (to be used when displaying) in question.
        List<DefaultMutableTreeNode> parentNodes = 
                new ArrayList<DefaultMutableTreeNode>();
        
        
        // To contain the children nodes in question.
        List<DefaultMutableTreeNode> childrenNodes = 
                new ArrayList<DefaultMutableTreeNode>();
        
        
        // Add all roots to the tree.
        for (Node root: roots){
        
            // Connect the root to its children.
            child = new DefaultMutableTreeNode(root);
            if (isForest){
                
                actualRoot.add(child);
            }
            
            // The future parents to be considered.
            parentNodes.add(child);
        }
        
        // While we still have children nodes...
        while(!parentNodes.isEmpty()){
            
            // Go through the tree in a breadth-first manner.
            for (DefaultMutableTreeNode parent: parentNodes){

                // Get the set of children, and have the parents accept their
                // children.
                children = tree.getChildrenNodes
                        (((Node)parent.getUserObject()).getIdentifier());

                for (Node child2: children){

                    child = new DefaultMutableTreeNode(child2);
                    childrenNodes.add(child);
                    parent.add(child);
                }
            }

            // Now have the children become parents.
            parentNodes.clear();
            parentNodes.addAll(childrenNodes);
            childrenNodes.clear();           
        }

    }
    
}
