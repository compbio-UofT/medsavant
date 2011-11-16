/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.hpontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;

/**
 *
 * @author Nirvana Nursimulu
 */
public class HPTree extends Tree{
    
//     /**
//     * Map from GO ID to locations
//     */
//    HashMap<String, HashSet<String>> idToLocs;
        
        public HPTree() throws Exception{
            super();
            super.idToLocs = CreateMappingsFile.getMappings();
//            this.idToLocs = super.idToLocs;
        }
    
}
