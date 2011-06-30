///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.ut.biolab.medsavant.view.filter.hpontology;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URI;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import org.ut.biolab.medsavant.view.filter.ontology.Node;
//import org.ut.biolab.medsavant.view.filter.ontology.Tree;
//
///**
// *
// * @author Nirvana Nursimulu
// */
//public class HPOParser {
//    
//    /**
//     * The location of the OBO file.
//     */
//    public static final String LOCATION_FILE = 
//            "http://compbio.charite.de/svn/hpo/trunk/src/ontology/"
//            + "human-phenotype-ontology.obo";
//    
//    /**
//     * The identifier of the root node.
//     */
//    public static final String rootId = "HP:0000001";
//    
//    /**
//     * Makes and returns the human phenotype tree.
//     * Need to implement this.
//     * @return 
//     */
//    public static Tree getHPOTree() throws Exception{
//        
//        Tree tree = new HPTree();
//        
//        URL url = new URL(LOCATION_FILE);
//        BufferedReader reader = new BufferedReader
//                (new InputStreamReader(url.openStream()));
////        System.out.println("Was able to open the file");
//        
//        String line;
//        String accumulatedString = "";
//        Node currNode;
//        boolean startSeeingTerm = false;
//
//        while ((line = reader.readLine()) != null){
//
//            // If we have reached a new term, collect the info we have so far,
//            // and put into the tree.
//            if (line.trim().equals("[Term]")){
//                startSeeingTerm = true;
//
//                if ((currNode = getNodeFromString(accumulatedString)) != null){
//                    if (isRoot(currNode)){
//                        tree.addRoot(currNode);
//                    }
//                    else{
//                        List<String> parentIDs = getParentIDs(accumulatedString);
//                        tree.addNode(currNode, parentIDs);
//                    }
//                     // Time to accumulate a new string.
//                    accumulatedString = "";
////                    System.out.println(currNode.getIdentifier() + "\t" + currNode.getDescription());
//                }
//            }
//            if (startSeeingTerm){
//                accumulatedString = accumulatedString + "\n" + line;
//            }
//        } 
//        
//        // We have missed the last term but we now add it.
//        if ((currNode = getNodeFromString(accumulatedString)) != null){
//            if (isRoot(currNode)){
//                tree.addRoot(currNode);
//            }
//            else{
//                List<String> parentIDs = getParentIDs(accumulatedString);
//                tree.addNode(currNode, parentIDs);
//            }
////            System.out.println(currNode.getIdentifier() + "\t" + currNode.getDescription());
//        }
//        
////        System.out.println(tree.getSize());
//        return tree;
//    }
//    
//    private static List<String> getParentIDs(String accumulatedString){
//        
//        List<String> parentIDs = new ArrayList<String>();
//        
//        String[] split = accumulatedString.split("\n");
//        for (String part: split){
//            String[] insideSplit = part.split(":");
//            if (insideSplit.length < 2){
//                continue;
//            }
//            if (insideSplit[0].trim().equals("is_a")){
//                String insideinside = part.substring(5);
//                String parentID = insideinside.split("!")[0].trim();
//                parentIDs.add(parentID);
//            }
//        }
//        
//        return parentIDs;
//    }
//    
//    /**
//     * Get the node object from this string.
//     * @param accumulatedString the string from which we will be making this node
//     * @return the node object made from this string.
//     */
//    private static Node getNodeFromString(String accumulatedString){
//        
//        if (!isValidTerm(accumulatedString)){
//            return null;
//        }
//        else{
//            String[] split = accumulatedString.split("\n");
////            System.out.println("\nNEW--");
//            String identifier = "";
//            String description = "";
//            
//            for (String part: split){
////                System.out.println(part);
//                String[] insideSplit = part.split(":");
//                if (insideSplit.length < 2){
//                    continue;
//                }
//                // Get the ID of this node.
//                if (insideSplit[0].trim().equals("id")){
//                    identifier = insideSplit[1].trim() + ":" + insideSplit[2].trim();
//                }
//                // Get the name/description of this node.
//                else if (insideSplit[0].trim().equals("name")){
//                    description = "";
//                    for (int i = 1; i < insideSplit.length; i++){
//                        description = description + ":" + insideSplit[i].trim();
//                    }
//                    description = description.substring(1);
//                }
//            }
//            Node node = new Node(identifier);
//            node.setDescription(description);
//            return node;
//        }
//
//    }
//    
//    /**
//     * Says if this is a term object.
//     * @param accumulatedString
//     * @return 
//     */
//    private static boolean isValidTerm(String accumulatedString){
//
//        return accumulatedString.trim().startsWith("[Term]");
//    }    
//    
//    /**
//     * Returns true iff this node is a root node.
//     * @param node
//     * @return 
//     */
//    private static boolean isRoot(Node node){
//        
//        return node.getIdentifier().equals(rootId);
//    }
//    
//}
