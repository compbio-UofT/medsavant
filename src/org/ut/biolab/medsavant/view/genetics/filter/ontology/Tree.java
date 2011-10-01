/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Nirvana Nursimulu
 */
public class Tree {
    
    
    /**
     * Fake root of the tree.  Useful since this is actually a forest of trees.
     */
    protected Node fakeRoot; 
    
    /**
     * Dictionary from an identifier to a node.
     */
    protected HashMap<String, Node> identifierToNode;
    
    /**
     * Map from ID to locations.  This is customizable, therefore, has 
     * protected access.
     */
    protected HashMap<String, HashSet<String>> idToLocs;
    
    public static final String NAME_OF_ROOT = "ROOT";
    
    /**
     * Constructor
     * @param fileGenLocation the location of the file mapping GO IDs to 
     * genome locations.
     */
    public Tree(){
        
        // no special node for the fake root.
        fakeRoot = new Node(NAME_OF_ROOT, null);
        fakeRoot.setLocs(new HashSet<String>(), false);
        identifierToNode = new HashMap<String, Node>();
        identifierToNode.put(NAME_OF_ROOT, fakeRoot);  
        idToLocs = new HashMap<String, HashSet<String>>();
    }
    
    /**
     * Returns a copy of this tree. Caution: a copy of this tree does not 
     * necessarily respond in the desired manner.  USE WITH CARE.
     * @return 
     */
    public Tree getCopyTree(){
        
       
//        Tree copyTree = new Tree();
//        copyTree.fakeRoot = this.fakeRoot.getCopy();
//        copyTree.idToLocs = this.idToLocs;
//        copyTree.identifierToNode = new HashMap<String, Node>();
//        copyTree.identifierToNode.put(NAME_OF_ROOT, copyTree.fakeRoot);
//        
//        List<Node> parentNodes = new ArrayList<Node>();
//        parentNodes.add(this.fakeRoot);
//        List<Node> parallelParentNodes = new ArrayList<Node>();
//        parallelParentNodes.add(copyTree.fakeRoot);
//        
//        List<Node> childrenNodes = new ArrayList<Node>();
//        List<Node> parallelChildrenNodes = new ArrayList<Node>();
//        
//        while (!parentNodes.isEmpty()){
//            int i = 0;
//            
//            childrenNodes.clear();
//            parallelChildrenNodes.clear();
//            
//            for (Node parentNode: parentNodes){
//
//                Node parallelParentNode = parallelParentNodes.get(i);
//                TreeSet<Node> currChildren = parentNode.getChildren();
//                
//                for (Node currChild: currChildren){
//
//                    if (currChild.isSpecialNode()){
//                        continue;
//                    }
//                    Node currParallelChild = copyTree.identifierToNode.get(currChild.getIdentifier());
//
//                    if (currParallelChild == null){
//                        
//                        currParallelChild = currChild.getCopy();
//                        copyTree.identifierToNode.put(currParallelChild.getIdentifier(), currParallelChild);
//                        childrenNodes.add(currChild);
//                        parallelChildrenNodes.add(currParallelChild);              
//                    }
//                    parallelParentNode.addChild(currParallelChild);
//                }
//                i++;
//            }
//            parentNodes.clear();
//            parallelParentNodes.clear();
//            
//            for (Node n: childrenNodes){
//                parentNodes.add(n);
//            }
//            for (Node n: parallelChildrenNodes){
//                parallelParentNodes.add(n);
//            }
//
//        }
        return this;
    }
    
    
    public int getSize(){
        return identifierToNode.keySet().size();
    }
    
    /**
     * Get the root nodes of this tree.
     * @return the actual roots of this forest (XNode objects).
     */
    public Set<Node> getRootNodes(){
        
        return fakeRoot.getChildren();
    }
    
    public HashSet<String> getLocsOfFakeRoot(){
        return fakeRoot.getLocs();
    }
    
    /**
     * Get the children of the node with the provided identifier.
     * @param identifier the identifier of the parent.
     * @return the TreeSet of children nodes of this parent.
     */
    public TreeSet<Node> getChildrenNodes(String identifier){
        
        Node node = identifierToNode.get(identifier);
        if (node != null){
            return node.getChildren();
        }
        // If this node is special, it is not fated to have children at all, and
        // is not even registered to belong to the tree..
        else{
            return new TreeSet<Node>();
        }
        
    }
    
    /**
     * Mark the locations of this node.
     * @param node the node whose locations are to be marked.
     */
    private void markLocations(Node node){
        
        // If the locations have already been marked, do not do anything.
        if (node.getLocs() != null){
            
            return;
        }
//        HashSet<String> locs = 
//                idToLocs.get(node.getIdentifier().replace(':', '_'));
        HashSet<String> locs = 
            idToLocs.get(node.getIdentifier());
        if (locs == null){
            
            locs = new HashSet<String>();
        }
        node.setLocs(locs, true);
    }
    
    /**
     * Add this "node" to the tree.  Assumes that this method is called only 
     * once for each child node.
     * @param child the child in this relationship.
     * @param parentID the parent's identifier in this relationship.
     * @param descriptionSpecialNode the description of the special node.
     */
    public void addNode(Node child, List<String> parentIDs, String descriptionSpecialNode){
        
        // First of all, get the child node if it already exists from the 
        // dictionary.
        // If the child node does not already exist, add to the list of nodes
        // to watch out for.  Note that the information in this node should
        // be complete since this node is being encountered as a child.
        Node childref = identifierToNode.get(child.getIdentifier());
        if (childref != null){
            
            child.copyInfoExceptChildrenTo(childref);
        }
        else{
            
            // Put into dictionary if node not seen yet.
            childref = child;
            identifierToNode.put(childref.getIdentifier(), childref);            
        }
        
        markLocations(childref);
        
        // For each parent, mark this as their child.
        for (String parentID: parentIDs){
        
            // If the parent has already been encountered, retrieve the record.
            // Otherwise, create a record in the dictionary.
            Node parentRef = identifierToNode.get(parentID);
            if (parentRef == null){
                
                parentRef = new Node(parentID, descriptionSpecialNode);
                identifierToNode.put(parentID, parentRef);
            }
            
            // Mark the child.  This is the easy part.
            parentRef.addChild(childref);        
        }

    }
    
    /**
     * Add a (true) root to this tree.
     * @param root 
     */
    public void addRoot(Node root, String descriptionSpecialNode){
        
        // See if we already have this root in a map.  If so, use it; otherwise,
        // put the root into the dictionary.
        Node rootRef = identifierToNode.get(root.getIdentifier());
        if (rootRef == null){
            
            rootRef = root;
            identifierToNode.put(rootRef.getIdentifier(), rootRef);
        }
        else{
            root.copyInfoExceptChildrenTo(rootRef);
            root = rootRef;
        }
        
        // Mark the location of this node.
        markLocations(root);
        
        root.setSpecialNode(descriptionSpecialNode);
        // Add this node as a child to the fake root.
        fakeRoot.addChild(root);
        
    }
    
    /**
     * Provides the option of propagating up the genome locations.  This is an
     * optional operation.  
     */
    public void propagateUp(){
        // send to recursive function.  
        setLocations(fakeRoot);
    }
    
    /**
     * Set the locations of this node. This is done recursively.
     * @param curr the present node to consider.
     * @return all locations associated with this node.
     */
    private void setLocations(Node curr){
        
        // get all the children of the present node.
        TreeSet<Node> children = curr.getChildren();
//        TreeSet<Node> children = this.getChildrenNodes(curr.getIdentifier()); 
        
        // locations for this node.
        HashSet<String> currLocs = curr.getLocs();

        // for each child:
        for (Node child: children){

            // If this node is special, there is no need to do anything (as
            // the gene locations of this child are exactly the same as those of
            // the parent at this point in time.
            if (child.isSpecialNode()){
                continue;
            }
            
            // if this node has not already been discovered.
            if (!child.isDiscovered()){
                // apply this function to this node.
                setLocations(child);
            }
            
            // add all locations of this child to this parent node
            HashSet<String> childLocs =  child.getLocs();
            currLocs.addAll(childLocs);  
        }
        
        curr.setLocs(currLocs, false);
        
        // say that this node has been discovered.
        curr.discover();
    }

}
