package org.ut.biolab.medsavant.view.genetics.filter.geneontology;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.ut.biolab.medsavant.view.genetics.filter.geneontology;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.URL;
//
///**
// *
// * @author Nirvana Nursimulu
// */
//public class RearrangeMappingFile {
//    
//    /**
//     * Location from which we are getting the mapping file.
//     */
//    public static final String LOCATION_FROM = 
//            "http://savantbrowser.com/nirvana/maps/refseqgeneshg19.bed-17_May_2011";
//    
//    /**
//     * Location to which we are moving the mapping file.
//     */
//    public static final String LOCATION_TO = "";
//    
//    public static void main(String[] args) throws Exception{
//        reduceRepetition();
//    }
//
//    /**
//     * RUN THIS FUNCTION ONLY ONCE.
//     * Reduces repetition in the mapping file by merging gene locations if 
//     * possible.
//     */
//    private static void reduceRepetition() throws Exception{
//        
//        URL url = new URL(LOCATION_FROM);
//        BufferedReader reader = 
//                new BufferedReader(new InputStreamReader(url.openStream()));
//        
//        String line;
//        while ((line = reader.readLine()) != null){
//            
//        }
//        reader.close();
//    }
//    
//}