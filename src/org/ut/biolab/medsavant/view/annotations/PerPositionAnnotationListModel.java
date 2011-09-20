/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.view.patients.DetailedListModel;

/**
 *
 * @author mfiume
 */
class PerPositionAnnotationListModel implements DetailedListModel {

    public static String ANNOTATION_GATK = "Genome Analysis Tool Kit (GATK)";
    public static String ANNOTATION_SIFT = "SIFT";
    public static String ANNOTATION_POLYPHEN = "Polymorphism Phenotyping v2 (Polyphen-2)";
    
    public PerPositionAnnotationListModel() {
    }

    public List<Vector> getList(int limit) throws Exception {
        
         List<Vector> list = new ArrayList<Vector>();
         list.add(new Vector(Arrays.asList(new Object[] {ANNOTATION_GATK})));
         list.add(new Vector(Arrays.asList(new Object[] {ANNOTATION_POLYPHEN})));
         list.add(new Vector(Arrays.asList(new Object[] {ANNOTATION_SIFT})));
         return list;
    }

    public List<String> getColumnNames() {
        List<String> list = new ArrayList<String>();
        list.add("Name");
        return list;
    }

    public List<Class> getColumnClasses() {
        List<Class> list = new ArrayList<Class>();
        list.add(String.class);
        return list;
    }

    public List<Integer> getHiddenColumns() {
        List<Integer> list = new ArrayList<Integer>();
        return list;
    }
    
}
